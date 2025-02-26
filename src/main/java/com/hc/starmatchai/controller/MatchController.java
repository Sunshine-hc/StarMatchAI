package com.hc.starmatchai.controller;

import cn.hutool.json.JSONUtil;
import com.hc.starmatchai.common.result.PageResult;
import com.hc.starmatchai.common.result.Result;
import com.hc.starmatchai.common.util.ValidateUtils;
import com.hc.starmatchai.entity.MatchRecord;
import com.hc.starmatchai.common.dto.model.MatchRequest;
import com.hc.starmatchai.common.dto.model.MatchResult;
import com.hc.starmatchai.common.dto.MatchHistoryQueryDTO;
import com.hc.starmatchai.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "星座匹配", description = "星座匹配相关接口")
@RestController
@RequestMapping("/starMatchAI/match")
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @Operation(summary = "计算星座匹配度", description = "根据两人生日计算星座匹配度并生成分析报告")
    @PostMapping("/calculate")
    public Result<MatchResult> calculateMatch(@RequestBody @Validated MatchRequest request) {
        ValidateUtils.validateMatchRequest(request);
        MatchResult result = matchService.calculateMatch(request);
        matchService.saveMatchRecord(result, request);
        return Result.success(result);
    }

    @Operation(summary = "获取历史匹配记录", description = "分页获取历史匹配记录")
    @PostMapping("/history")
    public Result<PageResult<MatchRecord>> getHistory(@RequestBody @Validated MatchHistoryQueryDTO queryDTO) {
        return Result.success(matchService.getMatchHistory(queryDTO));
    }

    @Operation(summary = "计算星座匹配度(流式)", description = "流式返回匹配分析结果")
    @PostMapping(value = "/calculate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter calculateMatchStream(@RequestBody @Validated MatchRequest request) {
        log.info("收到星座匹配流式请求, request={}", JSONUtil.toJsonStr(request));
        return matchService.createMatchStreamEmitter(request);
    }
}