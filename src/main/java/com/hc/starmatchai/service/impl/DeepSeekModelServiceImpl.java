package com.hc.starmatchai.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hc.starmatchai.common.BusinessException;
import com.hc.starmatchai.model.ZodiacSign;
import com.hc.starmatchai.service.AIModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import cn.hutool.core.util.StrUtil;

@Slf4j
@Service("deepseekModelService")
public class DeepSeekModelServiceImpl implements AIModelService {

    @Value("${ai.deepseek.api-key}")
    private String apiKey;

    @Value("${ai.deepseek.api-url}")
    private String apiUrl;

    @Override
    public Map<String, String> getMatchAnalysis(ZodiacSign sign1, ZodiacSign sign2) {
        log.info("开始进行星座匹配分析, sign1={}, sign2={}", sign1.getChineseName(), sign2.getChineseName());
        try {
            String prompt = buildPrompt(sign1, sign2);
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
            log.error("DeepSeek API调用失败, sign1={}, sign2={}, error={}",
                    sign1.getChineseName(), sign2.getChineseName(), e.getMessage(), e);
            throw new BusinessException("AI分析失败，请稍后重试");
        }
    }

    @Override
    public void getMatchAnalysisStream(ZodiacSign sign1, ZodiacSign sign2, SseEmitter emitter) {
        try {
            String prompt = buildPrompt(sign1, sign2);
            log.info("开始调用DeepSeek API进行流式分析, prompt={}", prompt);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("stream", true);
            requestBody.put("temperature", 0.7);

            // 构建消息
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(message);
            requestBody.put("messages", messages);

            String requestJson = JSONUtil.toJsonStr(requestBody);
            log.info("DeepSeek API请求参数: {}", requestJson);

            // 使用OkHttp进行流式请求
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(RequestBody.create(MediaType.parse("application/json"), requestJson))
                    .header("Authorization", "Bearer " + apiKey)
                    .build();

            log.info("发送DeepSeek API请求");

            StringBuilder fullContent = new StringBuilder();
            boolean[] sectionsSent = new boolean[5]; // 用于标记各个部分是否已发送

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    log.error("DeepSeek API请求失败", e);
                    throw new BusinessException("AI服务调用失败");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        throw new BusinessException("AI服务调用失败: " + response.code());
                    }

                    try (ResponseBody responseBody = response.body()) {
                        if (responseBody == null) {
                            throw new BusinessException("AI响应为空");
                        }

                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(responseBody.byteStream()));

                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6).trim();

                                if ("[DONE]".equals(data)) {
                                    // 处理最后一部分内容
                                    processFullContent(fullContent.toString(), emitter, sectionsSent);
                                    break;
                                }

                                JSONObject chunk = JSONUtil.parseObj(data);
                                JSONObject choices = chunk.getJSONArray("choices").getJSONObject(0);
                                JSONObject delta = choices.getJSONObject("delta");

                                if (delta.containsKey("content")) {
                                    String content = delta.getStr("content");
                                    fullContent.append(content);

                                    // 当收集到完整的段落时进行处理
                                    if (content.contains("\n\n")) {
                                        processFullContent(fullContent.toString(), emitter, sectionsSent);
                                    }
                                }
                            }
                        }
                    }

                    // 发送完成事件
                    Map<String, Object> completeEvent = new HashMap<>();
                    completeEvent.put("event", "complete");
                    completeEvent.put("data", "分析完成");
                    emitter.send(SseEmitter.event().data(JSONUtil.toJsonStr(completeEvent)));
                }
            });

        } catch (Exception e) {
            log.error("DeepSeek API流式请求异常", e);
            throw new BusinessException("AI服务调用失败");
        }
    }

    private String buildPrompt(ZodiacSign sign1, ZodiacSign sign2) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("作为一位专业的占星师，请分析")
                .append(sign1.getChineseName())
                .append("和")
                .append(sign2.getChineseName())
                .append("的匹配关系。请严格按照以下格式输出：\n\n")
                .append("匹配得分：[请给出0-100的匹配度评分，只需要数字]\n\n")
                .append("整体分析：[从星座特质的角度分析两个星座的整体匹配程度]\n\n")
                .append("优势特点：[分析两个星座在感情中的互补优势]\n\n")
                .append("潜在问题：[指出可能存在的性格冲突或沟通障碍]\n\n")
                .append("相处建议：[给出具体的相处建议和改善方法]\n\n")
                .append("请确保严格按照以上格式回复，每个部分都要有实质性的内容。");
        return promptBuilder.toString();
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

    private void processFullContent(String content, SseEmitter emitter, boolean[] sectionsSent)
            throws IOException {

        // 处理匹配得分
        if (content.contains("匹配得分：") && !sectionsSent[0]) {
            String[] parts = content.split("匹配得分：", 2);
            if (parts.length > 1) {
                String scoreStr = parts[1].split("\n")[0].trim().replaceAll("[^0-9]", "");
                if (!StrUtil.isBlank(scoreStr)) {
                    // 清理内容中的星号
                    scoreStr = cleanContent(scoreStr);
                    sendAnalysisSection(emitter, "score", scoreStr);
                    sectionsSent[0] = true;
                }
            }
        }

        // 处理整体分析
        if (content.contains("整体分析：") && !sectionsSent[1]) {
            String[] parts = content.split("整体分析：", 2);
            if (parts.length > 1 && parts[1].contains("\n\n")) {
                String analysis = parts[1].substring(0, parts[1].indexOf("\n\n")).trim();
                // 清理内容中的星号
                analysis = cleanContent(analysis);
                sendAnalysisSection(emitter, "analysis", analysis);
                sectionsSent[1] = true;
            }
        }

        // 处理优势特点
        if (content.contains("优势特点：") && !sectionsSent[2]) {
            String[] parts = content.split("优势特点：", 2);
            if (parts.length > 1 && parts[1].contains("\n\n")) {
                String advantages = parts[1].substring(0, parts[1].indexOf("\n\n")).trim();
                // 清理内容中的星号
                advantages = cleanContent(advantages);
                sendAnalysisSection(emitter, "advantages", advantages);
                sectionsSent[2] = true;
            }
        }

        // 处理潜在问题
        if (content.contains("潜在问题：") && !sectionsSent[3]) {
            String[] parts = content.split("潜在问题：", 2);
            if (parts.length > 1 && parts[1].contains("\n\n")) {
                String disadvantages = parts[1].substring(0, parts[1].indexOf("\n\n")).trim();
                // 清理内容中的星号
                disadvantages = cleanContent(disadvantages);
                sendAnalysisSection(emitter, "disadvantages", disadvantages);
                sectionsSent[3] = true;
            }
        }

        // 处理相处建议
        if (content.contains("相处建议：") && !sectionsSent[4]) {
            String[] parts = content.split("相处建议：", 2);
            if (parts.length > 1) {
                String suggestions = parts[1].trim();
                if (suggestions.contains("\n\n")) {
                    suggestions = suggestions.substring(0, suggestions.indexOf("\n\n"));
                }
                // 清理内容中的星号
                suggestions = cleanContent(suggestions);
                sendAnalysisSection(emitter, "suggestions", suggestions);
                sectionsSent[4] = true;
            }
        }
    }

    private void sendAnalysisSection(SseEmitter emitter, String key, String content)
            throws IOException {
        if (!StrUtil.isBlank(content)) {
            log.info("发送分析部分: key={}, content={}", key, content);
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("event", key);
            eventData.put("data", content);
            emitter.send(SseEmitter.event().data(JSONUtil.toJsonStr(eventData)));
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