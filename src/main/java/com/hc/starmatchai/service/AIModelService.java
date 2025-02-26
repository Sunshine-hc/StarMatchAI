package com.hc.starmatchai.service;

import cn.hutool.json.JSONUtil;
import com.hc.starmatchai.common.dto.model.ZodiacSign;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.Map;
import java.io.IOException;
import java.util.HashMap;

public interface AIModelService {
    /**
     * 获取星座匹配分析和评分（同步方式）
     * 
     * @param sign1 第一个星座
     * @param sign2 第二个星座
     * @return 分析结果（包含score、analysis、advantages、disadvantages、suggestions）
     */
    Map<String, String> getMatchAnalysis(ZodiacSign sign1, ZodiacSign sign2);

    /**
     * 获取星座匹配分析和评分（流式输出）
     * 
     * @return 完整的匹配结果
     */
    void getMatchAnalysisStream(ZodiacSign sign1, ZodiacSign sign2, SseEmitter emitter);

    /**
     * 辅助方法：发送带延迟的事件
     */
    default void sendEventWithDelay(SseEmitter emitter, String eventName, String data, long delayMillis)
            throws InterruptedException, IOException {
        Thread.sleep(delayMillis);
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("event", eventName);
        eventData.put("data", data);
        emitter.send(SseEmitter.event().data(JSONUtil.toJsonStr(eventData)));
    }
}