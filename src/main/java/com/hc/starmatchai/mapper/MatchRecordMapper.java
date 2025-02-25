package com.hc.starmatchai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hc.starmatchai.entity.MatchRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 匹配记录Mapper接口
 */
@Mapper
public interface MatchRecordMapper extends BaseMapper<MatchRecord> {
    // 使用MyBatis Plus提供的BaseMapper方法即可
    // 不需要额外定义selectPageByUserId方法
}