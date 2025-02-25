package com.hc.starmatchai.controller;

import com.hc.starmatchai.common.Result;
import com.hc.starmatchai.dto.MatchRecordDTO;
import com.hc.starmatchai.service.MatchRecordService;
import com.hc.starmatchai.vo.MatchRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "星座匹配记录", description = "星座匹配记录的创建、查询和管理")
@RestController
@RequestMapping("/api/match-record")
@RequiredArgsConstructor
public class MatchRecordController {

    private final MatchRecordService matchRecordService;

    @Operation(summary = "创建匹配记录", description = "根据两个人的生日创建星座匹配记录")
    @PostMapping
    public Result<MatchRecordVO> create(
            @Parameter(description = "匹配信息") @RequestBody @Validated MatchRecordDTO matchRecordDTO) {
        MatchRecordVO matchRecordVO = matchRecordService.createMatchRecord(matchRecordDTO);
        return Result.success(matchRecordVO);
    }

    @Operation(summary = "获取匹配记录详情", description = "根据ID获取匹配记录的详细信息")
    @GetMapping("/{id}")
    public Result<MatchRecordVO> getById(
            @Parameter(description = "记录ID") @PathVariable Long id) {
        MatchRecordVO matchRecordVO = matchRecordService.getMatchRecordById(id);
        return Result.success(matchRecordVO);
    }

    @Operation(summary = "获取当前用户的匹配记录列表", description = "分页获取当前登录用户的所有匹配记录")
    @GetMapping("/list")
    public Result<List<MatchRecordVO>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer size) {
        List<MatchRecordVO> list = matchRecordService.getCurrentUserMatchRecords(page, size);
        return Result.success(list);
    }

    @Operation(summary = "删除匹配记录", description = "根据ID删除匹配记录")
    @DeleteMapping("/{id}")
    public Result<String> delete(
            @Parameter(description = "记录ID") @PathVariable Long id) {
        matchRecordService.deleteMatchRecord(id);
        return Result.success("");
    }
}