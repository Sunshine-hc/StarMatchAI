package com.hc.starmatchai.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hc.starmatchai.common.constant.AiAnalysisConstants;
import com.hc.starmatchai.common.dto.model.MatchRequest;
import com.hc.starmatchai.common.dto.model.ZodiacSign;
import com.hc.starmatchai.common.enums.LanguageEnum;
import com.hc.starmatchai.common.exception.BusinessException;
import com.hc.starmatchai.common.util.AiAnalysisUtil;
import com.hc.starmatchai.common.util.PromptTemplateUtil;
import com.hc.starmatchai.common.util.PromptTemplateUtil.SectionMarkers;
import com.hc.starmatchai.service.AIModelService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service("qwen-turboModelService")
public class QwenTurboModelServiceImpl implements AIModelService {
    @Value("${ai.qwen-turbo.api-key}")
    private String apiKey;

    @Value("${ai.qwen-turbo.api-url}")
    private String apiUrl;

    @Resource
    private AiAnalysisUtil aiAnalysisUtil;

    @Resource
    private PromptTemplateUtil promptTemplateUtil;

    private final ConcurrentHashMap<SseEmitter, AtomicBoolean> emitterStatusMap = new ConcurrentHashMap<>();

    @Override
    public Map<String, String> getMatchAnalysis(MatchRequest req) {
        return Collections.emptyMap();
    }

    @Override
    public void getMatchAnalysisStream(MatchRequest request, SseEmitter emitter, Long userId, String matchNo) {
        ZodiacSign sign1 = request.getSign1();
        ZodiacSign sign2 = request.getSign2();
        Date person1Birthday = request.getPerson1Birthday();
        Date person2Birthday = request.getPerson2Birthday();
        String aiModel = request.getAiModel();
        String language = request.getLanguage();

        // 初始化emitter状态为活跃
        AtomicBoolean isActive = new AtomicBoolean(true);
        emitterStatusMap.put(emitter, isActive);

        // 添加完成、超时和错误处理器
        emitter.onCompletion(() -> {
            log.info("SSE连接已完成");
            isActive.set(false);
            emitterStatusMap.remove(emitter);

            // 将分析结果同步至数据库
            aiAnalysisUtil.syncAnalysisToDatabase(userId, matchNo, AiAnalysisConstants.AnalysisType.MATCH, request);
        });

        emitter.onTimeout(() -> {
            log.info("SSE连接超时");
            isActive.set(false);
            emitterStatusMap.remove(emitter);
        });

        emitter.onError(ex -> {
            log.error("SSE连接发生错误: {}", ex.getMessage());
            isActive.set(false);
            emitterStatusMap.remove(emitter);
        });

        try {
            // 发送开始事件
            Map<String, Object> startEvent = new HashMap<>();
            startEvent.put("event", "start");
            startEvent.put("data", "开始分析");
            emitter.send(SseEmitter.event().data(JSONUtil.toJsonStr(startEvent)));

            log.info("开始进行星座匹配分析, sign1={}, sign2={}", sign1.getChineseName(), sign2.getChineseName());
            String prompt = buildPrompt(sign1, person1Birthday, sign2, person2Birthday, language);
            log.info("请求AI模型参数：{}", JSONUtil.toJsonStr(prompt));

            // 获取对应语言的标记
            final Map<String, SectionMarkers> sectionMarkersMap = promptTemplateUtil.getSectionMarkersMap();
            final SectionMarkers markers = sectionMarkersMap.getOrDefault(language,
                    sectionMarkersMap.get(LanguageEnum.ZH_CN.getCode()));

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", aiModel);
            requestBody.put("stream", true);
            requestBody.put("top_p", 0.8);
            requestBody.put("temperature", 1.0);
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(message);
            requestBody.put("messages", messages);
            String requestJson = JSONUtil.toJsonStr(requestBody);

            // 创建OkHttp客户端
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            Request aiApiRequest = new Request.Builder()
                    .url(apiUrl)
                    .post(RequestBody.create(MediaType.parse("application/json"), requestJson))
                    .header("Authorization", "Bearer " + apiKey)
                    .build();

            // 创建用于累积内容的StringBuilder
            final StringBuilder fullContent = new StringBuilder(2048);
            // 用于标记各部分是否已发送
            final boolean[] sectionsSent = new boolean[5];

            client.newCall(aiApiRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    log.error("通义千问-Turbo API请求失败", e);
                    safelySendEvent(emitter, "error", "AI服务调用失败，请稍后重试");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    log.info("开始处理流式响应");
                    if (!response.isSuccessful()) {
                        throw new BusinessException("AI服务调用失败: " + response.code());
                    }

                    try (ResponseBody responseBody = response.body()) {
                        if (responseBody == null) {
                            throw new BusinessException("AI响应为空");
                        }

                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(responseBody.byteStream(), StandardCharsets.UTF_8), 8192);

                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (!line.startsWith("data: ")) {
                                continue; // 快速跳过非数据行
                            }

                            String data = line.substring(6).trim();

                            // 快速检查是否完成
                            if ("[DONE]".equals(data)) {
                                // 处理最后一部分内容
                                processFullContent(fullContent.toString(), emitter, sectionsSent, userId, matchNo,
                                        markers);
                                break;
                            }

                            // 快速检查是否包含content字段，避免不必要的JSON解析
                            if (data.indexOf("\"content\"") == -1) {
                                continue;
                            }

                            try {
                                // 使用JSONUtil解析，保持与原代码兼容
                                JSONObject chunk = JSONUtil.parseObj(data);
                                String content = chunk.getJSONArray("choices")
                                        .getJSONObject(0)
                                        .getJSONObject("delta")
                                        .getStr("content", "");

                                if (content != null && !content.isEmpty()) {
                                    fullContent.append(content);

                                    // 只有在内容中包含关键标记时才处理
                                    boolean shouldProcess = content.indexOf("\n\n") != -1 ||
                                            content.indexOf(markers.getScoreMarker()) != -1 ||
                                            content.indexOf(markers.getAnalysisMarker()) != -1 ||
                                            content.indexOf(markers.getAdvantagesMarker()) != -1 ||
                                            content.indexOf(markers.getDisadvantagesMarker()) != -1 ||
                                            content.indexOf(markers.getSuggestionsMarker()) != -1;

                                    if (shouldProcess) {
                                        // 检查是否所有部分都已处理，如果是则跳过
                                        boolean allProcessed = true;
                                        for (boolean sent : sectionsSent) {
                                            if (!sent) {
                                                allProcessed = false;
                                                break;
                                            }
                                        }

                                        if (!allProcessed) {
                                            processFullContent(fullContent.toString(), emitter, sectionsSent, userId,
                                                    matchNo, markers);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // 简化错误日志，减少开销
                                log.error("解析错误: {}", e.getMessage());
                            }
                        }
                        log.info("结束处理流式响应");
                    }

                    // 在处理完成后，安全地发送完成事件
                    safelySendEvent(emitter, "complete", "分析完成");
                }

                // 优化processFullContent方法
                void processFullContent(String content, SseEmitter emitter, boolean[] sectionsSent, Long userId,
                        String matchNo, SectionMarkers markers) throws IOException {

                    // 处理匹配得分
                    if (!sectionsSent[0]) {
                        int scoreIndex = content.indexOf(markers.getScoreMarker());
                        if (scoreIndex >= 0) {
                            int scoreStart = scoreIndex + markers.getScoreMarker().length();
                            int scoreEnd = content.indexOf('\n', scoreStart);
                            if (scoreEnd > scoreStart) {
                                String scoreStr = content.substring(scoreStart, scoreEnd).trim();
                                // 使用更高效的方式提取数字
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < scoreStr.length(); i++) {
                                    char c = scoreStr.charAt(i);
                                    if (c >= '0' && c <= '9') {
                                        sb.append(c);
                                    }
                                }
                                scoreStr = sb.toString();

                                if (!scoreStr.isEmpty()) {
                                    sendAnalysisSection(emitter, AiAnalysisConstants.AnalysisField.MATCH_SCORE,
                                            scoreStr, userId, matchNo);
                                    sectionsSent[0] = true;
                                }
                            }
                        }
                    }

                    // 处理整体分析
                    if (!sectionsSent[1]) {
                        sectionsSent[1] = processSection(content, markers.getAnalysisMarker(),
                                AiAnalysisConstants.AnalysisField.ANALYSIS, emitter, userId, matchNo);
                    }

                    // 处理优势特点
                    if (!sectionsSent[2]) {
                        sectionsSent[2] = processSection(content, markers.getAdvantagesMarker(),
                                AiAnalysisConstants.AnalysisField.ADVANTAGES, emitter, userId, matchNo);
                    }

                    // 处理潜在问题
                    if (!sectionsSent[3]) {
                        sectionsSent[3] = processSection(content, markers.getDisadvantagesMarker(),
                                AiAnalysisConstants.AnalysisField.DISADVANTAGES, emitter, userId, matchNo);
                    }

                    // 处理相处建议
                    if (!sectionsSent[4]) {
                        sectionsSent[4] = processSection(content, markers.getSuggestionsMarker(),
                                AiAnalysisConstants.AnalysisField.SUGGESTIONS, emitter, userId, matchNo);
                    }
                }

                // 提取公共处理逻辑，减少代码重复
                private boolean processSection(String content, String marker, String eventType,
                        SseEmitter emitter, Long userId, String matchNo) throws IOException {
                    int sectionIndex = content.indexOf(marker);
                    if (sectionIndex >= 0) {
                        int sectionStart = sectionIndex + marker.length();
                        int sectionEnd = content.indexOf("\n\n", sectionStart);

                        String sectionText;
                        if (sectionEnd > sectionStart) {
                            sectionText = content.substring(sectionStart, sectionEnd).trim();
                        } else {
                            // 对于最后一部分，可能没有结束标记，需要判断是否为完整部分
                            sectionText = content.substring(sectionStart).trim();
                            if (!sectionText.endsWith("。") && !sectionText.endsWith("！")
                                    && !sectionText.endsWith("？") && !sectionText.endsWith(".")
                                    && !sectionText.endsWith("!") && !sectionText.endsWith("?")) {
                                // 如果不是以句号、感叹号或问号结尾（中英文），认为不是完整部分，不处理
                                return false;
                            }
                        }

                        if (!sectionText.isEmpty()) {
                            sectionText = cleanContent(sectionText);
                            sendAnalysisSection(emitter, eventType, sectionText, userId, matchNo);
                            return true;
                        }
                    }
                    return false;
                }
            });

        } catch (Exception e) {
            log.error("通义千问-Turbo API流式请求异常: {}", e.getMessage(), e);
            safelySendEvent(emitter, "error", "AI服务调用失败，请稍后重试");
        }
    }

    private String buildPrompt(ZodiacSign sign1, Date person1Birthday, ZodiacSign sign2, Date person2Birthday) {
        return buildPrompt(sign1, person1Birthday, sign2, person2Birthday, LanguageEnum.ZH_CN.getCode());
    }

    private String buildPrompt(ZodiacSign sign1, Date person1Birthday, ZodiacSign sign2, Date person2Birthday,
            String language) {
        return promptTemplateUtil.getMatchAnalysisPrompt(sign1, person1Birthday, sign2, person2Birthday, language);
    }

    private Map<String, String> parseResponse(String response) {
        log.info("开始解析DeepSeek响应");
        Map<String, String> result = new HashMap<>();
        try {
            Map<String, Object> responseMap = JSONUtil.parseObj(response);
            Map<String, Object> choices = (Map<String, Object>) ((List<?>) responseMap.get("choices")).get(0);
            String content = ((Map<String, String>) choices.get("message")).get("content");

            log.info("解析到的content内容: {}", content);

            String[] sections = content.split("\n\n");
            for (String section : sections) {
                if (section.startsWith("匹配得分：")) {
                    String scoreStr = section.substring(5).trim().replaceAll("[^0-9]", "");
                    result.put("score", scoreStr);
                    log.info("解析到匹配得分: {}", scoreStr);
                } else if (section.startsWith("整体分析：")) {
                    result.put("analysis", section.substring(5).trim());
                } else if (section.startsWith("优势特点：")) {
                    result.put("advantages", section.substring(5).trim());
                } else if (section.startsWith("潜在问题：")) {
                    result.put("disadvantages", section.substring(5).trim());
                } else if (section.startsWith("相处建议：")) {
                    result.put("suggestions", section.substring(5).trim());
                }
            }

            // 检查并设置默认值
            checkAndSetDefaultValues(result);

            log.info("响应解析完成, 包含字段: {}", result.keySet());
            return result;
        } catch (Exception e) {
            log.error("解析AI响应失败: {}", e.getMessage(), e);
            throw new BusinessException("AI响应解析失败");
        }
    }

    private void checkAndSetDefaultValues(Map<String, String> result) {
        if (!result.containsKey("score")) {
            log.warn("未解析到匹配得分，使用默认值: 60");
            result.put("score", "60");
        }
        if (!result.containsKey("analysis")) {
            log.warn("未解析到整体分析，使用默认值");
            result.put("analysis", "暂无分析");
        }
        if (!result.containsKey("advantages")) {
            log.warn("未解析到优势特点，使用默认值");
            result.put("advantages", "暂无优势分析");
        }
        if (!result.containsKey("disadvantages")) {
            log.warn("未解析到潜在问题，使用默认值");
            result.put("disadvantages", "暂无问题分析");
        }
        if (!result.containsKey("suggestions")) {
            log.warn("未解析到相处建议，使用默认值");
            result.put("suggestions", "暂无建议");
        }
    }

    /**
     * 安全地发送SSE事件
     *
     * @param emitter   SSE发射器
     * @param eventType 事件类型
     * @param data      事件数据
     */
    private void safelySendEvent(SseEmitter emitter, String eventType, String data) {
        AtomicBoolean isActive = emitterStatusMap.getOrDefault(emitter, new AtomicBoolean(false));

        if (isActive.get()) {
            try {
                Map<String, Object> event = new HashMap<>();
                event.put("event", eventType);
                event.put("data", data);

                emitter.send(SseEmitter.event().data(JSONUtil.toJsonStr(event)));

                // 如果是完成事件，标记emitter为非活跃并完成
                if ("complete".equals(eventType)) {
                    isActive.set(false);
                    emitter.complete();
                    emitterStatusMap.remove(emitter);
                }
            } catch (Exception e) {
                log.warn("发送{}事件失败: {}", eventType, e.getMessage());
                try {
                    // 尝试完成emitter
                    isActive.set(false);
                    emitter.complete();
                } catch (Exception ex) {
                    // 忽略完成时的异常
                } finally {
                    emitterStatusMap.remove(emitter);
                }
            }
        } else {
            log.debug("跳过向非活跃emitter发送{}事件", eventType);
        }
    }

    /**
     * 安全地发送分析部分
     */
    private void sendAnalysisSection(SseEmitter emitter, String key, String content, Long userId, String matchNo) {
        if (StrUtil.isBlank(content)) {
            return;
        }

        AtomicBoolean isActive = emitterStatusMap.getOrDefault(emitter, new AtomicBoolean(false));
        if (isActive.get()) {
            try {
                log.info("发送分析部分: key={}, content={}", key, content);
                // 发送SSE事件
                Map<String, Object> eventData = new HashMap<>();
                eventData.put("event", key);
                eventData.put("data", content);
                emitter.send(SseEmitter.event().data(JSONUtil.toJsonStr(eventData)));

                // 保存到Redis
                aiAnalysisUtil.saveAnalysisToRedis(userId, matchNo, key, content);
            } catch (Exception e) {
                log.warn("发送分析部分失败: {}", e.getMessage());
                isActive.set(false);
            }
        }
    }

    /**
     * 清理内容中的特殊符号
     */
    private String cleanContent(String content) {
        if (StrUtil.isBlank(content)) {
            return content;
        }
        return content.replaceAll("\\*\\*(.*?)\\*\\*", "$1") // 移除成对的星号，保留中间的内容
                .replaceAll("\\s+", " ") // 将多个空格替换为单个空格
                .trim();
    }
}
