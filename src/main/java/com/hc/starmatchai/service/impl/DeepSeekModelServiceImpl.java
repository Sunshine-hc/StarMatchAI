package com.hc.starmatchai.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hc.starmatchai.common.constant.AiAnalysisConstants;
import com.hc.starmatchai.common.dto.model.MatchRequest;
import com.hc.starmatchai.common.exception.BusinessException;
import com.hc.starmatchai.common.dto.model.ZodiacSign;
import com.hc.starmatchai.common.util.RedisUtil;
import com.hc.starmatchai.service.AIModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import cn.hutool.core.util.StrUtil;

import javax.annotation.Resource;
import com.hc.starmatchai.common.util.AiAnalysisUtil;
import com.hc.starmatchai.common.enums.LanguageEnum;
import com.hc.starmatchai.common.util.PromptTemplateUtil;
import com.hc.starmatchai.common.util.PromptTemplateUtil.SectionMarkers;

@Slf4j
@Service("deepseek-chatModelService")
public class DeepSeekModelServiceImpl implements AIModelService {

    @Value("${ai.deepseek.api-key}")
    private String apiKey;

    @Value("${ai.deepseek.api-url}")
    private String apiUrl;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private AiAnalysisUtil aiAnalysisUtil;

    @Resource
    private PromptTemplateUtil promptTemplateUtil;

    private final ConcurrentHashMap<SseEmitter, AtomicBoolean> emitterStatusMap = new ConcurrentHashMap<>();

    @Override
    public Map<String, String> getMatchAnalysis(MatchRequest req) {
        ZodiacSign sign1 = req.getSign1();
        ZodiacSign sign2 = req.getSign2();
        Date person1Birthday = req.getPerson1Birthday();
        Date person2Birthday = req.getPerson2Birthday();

        log.info("开始进行星座匹配分析, sign1={}, sign2={}", sign1.getChineseName(), sign2.getChineseName());
        try {
            String prompt = buildPrompt(sign1, person1Birthday, sign2, person2Birthday);
            log.info("生成的prompt内容: {}", prompt);

            log.info("开始调用DeepSeek API");
            long startTime = System.currentTimeMillis();
            String response = callDeepSeekAPI(prompt);
            long endTime = System.currentTimeMillis();
            log.info("DeepSeek API调用完成, 耗时: {}ms", endTime - startTime);
            log.info("API原始响应: {}", response);

            Map<String, String> result = parseResponse(response);
            log.info("星座匹配分析完成, 匹配得分: {}", result.get("score"));
            return result;
        } catch (Exception e) {
            log.error("DeepSeek API调用失败, sign1={}, sign2={}, error={}", sign1.getChineseName(), sign2.getChineseName(),
                    e.getMessage(), e);
            throw new BusinessException("AI分析失败，请稍后重试");
        }
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
            requestBody.put("messages", Collections.singletonList(
                    new HashMap<String, String>() {
                        {
                            put("role", "user");
                            put("content", prompt);
                        }
                    }));
            String requestJson = JSONUtil.toJsonStr(requestBody);

            // 创建OkHttp客户端
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            Request apiRequest = new Request.Builder()
                    .url(apiUrl)
                    .post(RequestBody.create(MediaType.parse("application/json"), requestJson))
                    .header("Authorization", "Bearer " + apiKey)
                    .build();

            // 创建用于累积内容的StringBuilder
            final StringBuilder fullContent = new StringBuilder(2048);
            // 用于标记各部分是否已发送
            final boolean[] sectionsSent = new boolean[5];

            client.newCall(apiRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    log.error("DeepSeek API请求失败", e);
                    safelySendEvent(emitter, "error", "AI服务调用失败，请稍后重试");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "无响应内容";
                        log.error("DeepSeek API调用失败: {}, 错误信息: {}", response.code(), errorBody);
                        safelySendEvent(emitter, "error", "AI服务调用失败，请稍后重试");
                        return;
                    }

                    try (ResponseBody responseBody = response.body()) {
                        if (responseBody == null) {
                            log.error("DeepSeek API响应为空");
                            safelySendEvent(emitter, "error", "AI服务调用失败，请稍后重试");
                            return;
                        }

                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(responseBody.byteStream(), StandardCharsets.UTF_8));

                        String line;
                        while ((line = reader.readLine()) != null && isActive.get()) {
                            if (!line.startsWith("data:")) {
                                continue;
                            }

                            // 如果流结束标记，处理完最后的内容
                            if (line.equals("data: [DONE]")) {
                                processFullContent(fullContent.toString(), emitter, sectionsSent, userId, matchNo,
                                        markers);
                                break;
                            }

                            String data = line.substring(5).trim();
                            try {
                                JSONObject jsonObject = JSONUtil.parseObj(data);
                                JSONObject choice = jsonObject.getJSONArray("choices").getJSONObject(0);
                                if (choice.containsKey("delta")) {
                                    JSONObject delta = choice.getJSONObject("delta");
                                    if (delta.containsKey("content")) {
                                        String content = delta.getStr("content");
                                        if (content != null && !content.isEmpty()) {
                                            fullContent.append(content);

                                            // 检查是否有新的部分需要处理
                                            boolean shouldProcess = content.contains("\n\n") ||
                                                    content.contains(markers.getScoreMarker()) ||
                                                    content.contains(markers.getAnalysisMarker()) ||
                                                    content.contains(markers.getAdvantagesMarker()) ||
                                                    content.contains(markers.getDisadvantagesMarker()) ||
                                                    content.contains(markers.getSuggestionsMarker());

                                            if (shouldProcess) {
                                                processFullContent(fullContent.toString(), emitter, sectionsSent,
                                                        userId, matchNo, markers);
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                log.error("解析DeepSeek响应失败: " + e.getMessage(), e);
                            }
                        }
                    }

                    safelySendEvent(emitter, "complete", "分析完成");
                }

                private void processFullContent(String content, SseEmitter emitter, boolean[] sectionsSent, Long userId,
                        String matchNo, SectionMarkers markers) throws IOException {
                    // 处理匹配得分
                    if (!sectionsSent[0]) {
                        int scoreIndex = content.indexOf(markers.getScoreMarker());
                        if (scoreIndex >= 0) {
                            int scoreStart = scoreIndex + markers.getScoreMarker().length();
                            int scoreEnd = content.indexOf('\n', scoreStart);
                            if (scoreEnd > scoreStart) {
                                String scoreStr = content.substring(scoreStart, scoreEnd).trim();
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

                private boolean processSection(String content, String marker, String eventType, SseEmitter emitter,
                        Long userId, String matchNo) throws IOException {
                    int sectionIndex = content.indexOf(marker);
                    if (sectionIndex >= 0) {
                        int sectionStart = sectionIndex + marker.length();
                        int sectionEnd = content.indexOf("\n\n", sectionStart);

                        String sectionText;
                        if (sectionEnd > sectionStart) {
                            sectionText = content.substring(sectionStart, sectionEnd).trim();
                        } else {
                            sectionText = content.substring(sectionStart).trim();
                            // 检查是否为完整的段落，通过检查结尾是否为中英文的句号、感叹号或问号
                            if (!sectionText.endsWith("。") && !sectionText.endsWith("！") && !sectionText.endsWith("？")
                                    && !sectionText.endsWith(".") && !sectionText.endsWith("!")
                                    && !sectionText.endsWith("?")) {
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
            log.error("DeepSeek API流式调用异常", e);
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

    private String callDeepSeekAPI(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");

            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            Object[] messages = new Object[] { message };
            requestBody.put("messages", messages);

            String requestJson = JSONUtil.toJsonStr(requestBody);
            log.info("DeepSeek API请求参数: {}", requestJson);

            long startTime = System.currentTimeMillis();
            HttpResponse response = HttpRequest.post(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestJson)
                    .execute();
            long endTime = System.currentTimeMillis();

            log.info("DeepSeek API响应状态码: {}, 耗时: {}ms", response.getStatus(), endTime - startTime);

            if (response.isOk()) {
                return response.body();
            } else {
                log.error("DeepSeek API调用失败, 状态码: {}, 响应内容: {}", response.getStatus(), response.body());
                throw new BusinessException("AI服务调用失败");
            }
        } catch (Exception e) {
            log.error("DeepSeek API请求异常: {}", e.getMessage(), e);
            throw new BusinessException("AI服务调用失败");
        }
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