package com.hc.starmatchai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hc.starmatchai.common.BusinessException;
import com.hc.starmatchai.dto.MatchRecordDTO;
import com.hc.starmatchai.entity.MatchRecord;
import com.hc.starmatchai.mapper.MatchRecordMapper;
import com.hc.starmatchai.service.MatchRecordService;
import com.hc.starmatchai.util.UserContext;
import com.hc.starmatchai.vo.MatchRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchRecordServiceImpl implements MatchRecordService {

    private final MatchRecordMapper matchRecordMapper;

    @Override
    @Transactional
    public MatchRecordVO createMatchRecord(MatchRecordDTO matchRecordDTO) {
        // 获取当前用户ID
        Long userId = UserContext.getCurrentUserId();

        // 创建匹配记录实体
        MatchRecord matchRecord = new MatchRecord();
        BeanUtils.copyProperties(matchRecordDTO, matchRecord);

        // 设置用户ID和创建时间
        matchRecord.setUserId(userId);
        matchRecord.setCreatedAt(new Date());

        // 根据生日计算星座
        matchRecord.setPerson1Sign(calculateZodiacSign(matchRecordDTO.getPerson1Birthday()));
        matchRecord.setPerson2Sign(calculateZodiacSign(matchRecordDTO.getPerson2Birthday()));

        // TODO: 调用AI服务进行匹配分析
        // 这里应该调用您的AI服务，获取匹配分数、分析、优势、劣势和建议
        // 暂时使用模拟数据
        matchRecord.setMatchScore(85);
        matchRecord.setAnalysis("这是一个很好的匹配...");
        matchRecord.setAdvantages("双方都很有耐心...");
        matchRecord.setDisadvantages("可能会在决策方面有分歧...");
        matchRecord.setSuggestions("建议多沟通，相互理解...");
        matchRecord.setAiModel("gpt-4");

        // 保存匹配记录
        matchRecordMapper.insert(matchRecord);

        // 转换为VO并返回
        MatchRecordVO vo = new MatchRecordVO();
        BeanUtils.copyProperties(matchRecord, vo);
        return vo;
    }

    @Override
    public MatchRecordVO getMatchRecordById(Long id) {
        Long userId = UserContext.getCurrentUserId();

        MatchRecord matchRecord = matchRecordMapper.selectById(id);
        if (matchRecord == null) {
            throw new BusinessException("匹配记录不存在");
        }

        // 检查记录是否属于当前用户
        if (!matchRecord.getUserId().equals(userId)) {
            throw new BusinessException("无权访问该匹配记录");
        }

        MatchRecordVO vo = new MatchRecordVO();
        BeanUtils.copyProperties(matchRecord, vo);
        return vo;
    }

    @Override
    public List<MatchRecordVO> getCurrentUserMatchRecords(int pageNum, int pageSize) {
        Long userId = UserContext.getCurrentUserId();

        // 创建分页对象
        Page<MatchRecord> page = new Page<>(pageNum, pageSize);

        // 计算30天前的时间
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        Date thirtyDaysAgoDate = Date.from(thirtyDaysAgo.atZone(ZoneId.systemDefault()).toInstant());

        // 使用Lambda表达式创建查询条件
        LambdaQueryWrapper<MatchRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MatchRecord::getUserId, userId)
                .ge(MatchRecord::getCreatedAt, thirtyDaysAgoDate) // 只查询30天内的数据
                .orderByDesc(MatchRecord::getCreatedAt);

        // 执行分页查询
        Page<MatchRecord> resultPage = matchRecordMapper.selectPage(page, queryWrapper);

        // 转换为VO列表
        return resultPage.getRecords().stream()
                .map(record -> {
                    MatchRecordVO vo = new MatchRecordVO();
                    BeanUtils.copyProperties(record, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteMatchRecord(Long id) {
        Long userId = UserContext.getCurrentUserId();

        MatchRecord matchRecord = matchRecordMapper.selectById(id);
        if (matchRecord == null) {
            throw new BusinessException("匹配记录不存在");
        }

        // 检查记录是否属于当前用户
        if (!matchRecord.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该匹配记录");
        }

        matchRecordMapper.deleteById(id);
    }

    @Override
    public void associateAnonymousRecords(String deviceId, Long userId) {

    }

    /**
     * 根据生日计算星座
     * 
     * @param birthday 生日
     * @return 星座名称
     */
    private String calculateZodiacSign(Date birthday) {
        // 实现星座计算逻辑
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(birthday);
        int month = cal.get(java.util.Calendar.MONTH) + 1; // 月份从0开始
        int day = cal.get(java.util.Calendar.DAY_OF_MONTH);

        String[] signs = { "摩羯座", "水瓶座", "双鱼座", "白羊座", "金牛座", "双子座",
                "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座" };
        int[] days = { 20, 19, 21, 20, 21, 22, 23, 23, 23, 24, 23, 22 };

        int index = month - 1;
        if (day < days[index]) {
            index = index - 1;
            if (index < 0) {
                index = 11;
            }
        }
        return signs[index];
    }
}