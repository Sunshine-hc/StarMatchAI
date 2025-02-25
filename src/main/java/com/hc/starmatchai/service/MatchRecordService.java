package com.hc.starmatchai.service;

import com.hc.starmatchai.dto.MatchRecordDTO;
import com.hc.starmatchai.vo.MatchRecordVO;

import java.util.List;

public interface MatchRecordService {
    /**
     * 创建匹配记录
     * @param matchRecordDTO 匹配记录DTO
     * @return 匹配记录VO
     */
    MatchRecordVO createMatchRecord(MatchRecordDTO matchRecordDTO);
    
    /**
     * 根据ID获取匹配记录
     * @param id 记录ID
     * @return 匹配记录VO
     */
    MatchRecordVO getMatchRecordById(Long id);
    
    /**
     * 获取当前用户的匹配记录列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 匹配记录VO列表
     */
    List<MatchRecordVO> getCurrentUserMatchRecords(int pageNum, int pageSize);
    
    /**
     * 删除匹配记录
     * @param id 记录ID
     */
    void deleteMatchRecord(Long id);

    /**
     * 关联匿名记录到用户
     * 用于用户登录后，将之前未登录状态下的记录关联到用户账号
     */
    void associateAnonymousRecords(String deviceId, Long userId);
}