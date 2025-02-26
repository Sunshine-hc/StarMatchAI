package com.hc.starmatchai.service;

import com.hc.starmatchai.common.result.PageResult;
import com.hc.starmatchai.entity.MatchRecord;
import com.hc.starmatchai.common.dto.model.MatchRequest;
import com.hc.starmatchai.common.dto.model.MatchResult;
import com.hc.starmatchai.common.dto.MatchHistoryQueryDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface MatchService {
    MatchResult calculateMatch(MatchRequest request);

    void saveMatchRecord(MatchResult result, MatchRequest request);

    PageResult<MatchRecord> getMatchHistory(MatchHistoryQueryDTO queryDTO);

    /**
     * 创建匹配分析的流式响应发射器
     */
    SseEmitter createMatchStreamEmitter(MatchRequest request);

    /**
     * 计算匹配结果（流式输出）
     */
    void calculateMatchStream(MatchRequest request, SseEmitter emitter);
}