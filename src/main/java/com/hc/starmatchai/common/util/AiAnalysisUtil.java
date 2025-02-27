package com.hc.starmatchai.common.util;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hc.starmatchai.common.constant.AiAnalysisConstants;
import com.hc.starmatchai.common.dto.model.MatchRequest;
import com.hc.starmatchai.common.exception.BusinessException;
import com.hc.starmatchai.entity.MatchRecord;
import com.hc.starmatchai.mapper.MatchRecordMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.HashMap;

@Component
@Slf4j
public class AiAnalysisUtil {

    @Autowired
    private RedisUtil redisUtil;

    @Resource
    private MatchRecordMapper matchRecordMapper;

    /**
     * 保存AI分析结果到Redis（增量更新Map形式）
     *
     * @param userId  用户ID
     * @param matchId 匹配ID
     * @param key     分析类型键（如：analysis, advantages等）
     * @param content 分析内容
     */
    public void saveAnalysisToRedis(Long userId, String matchId, String key, String content) {
        try {
            String redisKey = buildRedisKey(userId, matchId);

            // 获取现有的Map，如果不存在则创建新的
            Map<String, Object> analysisMap = getAnalysisMap(redisKey);

            // 更新或添加新的内容
            analysisMap.put(key, content);

            // 保存更新后的Map
            redisUtil.set(redisKey, JSONUtil.toJsonStr(analysisMap), AiAnalysisConstants.ExpireTime.DEFAULT_EXPIRE, TimeUnit.SECONDS);
            log.info("保存分析结果到Redis成功: userId={}, matchId={}, key={}", userId, matchId, key);
        } catch (Exception e) {
            log.error("保存分析结果到Redis失败: userId={}, matchId={}, key={}, error={}", userId, matchId, key, e.getMessage(), e);
        }
    }

    /**
     * 获取完整的分析结果Map
     *
     * @param userId  用户ID
     * @param matchId 匹配ID
     * @return 分析结果Map
     */
    public Map<String, Object> getAnalysisResult(Long userId, String matchId) {
        try {
            String redisKey = buildRedisKey(userId, matchId);
            return getAnalysisMap(redisKey);
        } catch (Exception e) {
            log.error("获取分析结果失败: userId={}, matchId={}, error={}", userId, matchId, e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 检查分析结果是否完整
     *
     * @param userId  用户ID
     * @param matchId 匹配ID
     * @return 是否完整
     */
    public boolean isAnalysisComplete(Long userId, String matchId) {
        Map<String, Object> analysisMap = getAnalysisResult(userId, matchId);
        return analysisMap.containsKey(AiAnalysisConstants.AnalysisField.ANALYSIS) &&
                analysisMap.containsKey(AiAnalysisConstants.AnalysisField.ADVANTAGES) &&
                analysisMap.containsKey(AiAnalysisConstants.AnalysisField.DISADVANTAGES) &&
                analysisMap.containsKey(AiAnalysisConstants.AnalysisField.SUGGESTIONS) &&
                analysisMap.containsKey(AiAnalysisConstants.AnalysisField.MATCH_SCORE);
    }

    /**
     * 从Redis获取分析Map
     */
    private Map<String, Object> getAnalysisMap(String redisKey) {
        Object jsonObj = redisUtil.get(redisKey);
        if (Objects.isNull(jsonObj)) {
            return new HashMap<>();
        }
        return JSONUtil.toBean(String.valueOf(jsonObj), new TypeReference<Map<String, Object>>() {}, false);
    }

    /**
     * 同步AI分析结果到数据库
     * @param analysisType @link{AiAnalysisConstants.AnalysisType.MATCH}
     */
    @Transactional
    public Long syncAnalysisToDatabase(Long userId, String matchNo, String analysisType, Object request) {
        String redisKey = buildRedisKey(userId, matchNo);
        try {
            Object analysisData = redisUtil.get(redisKey);
            if (Objects.isNull(analysisData)) {
                log.info(AiAnalysisConstants.ErrorMessage.DATA_NOT_FOUND + ", userId={}, type={}", userId, analysisType);
                return null;
            }

            JSONObject data = JSONUtil.parseObj(analysisData);

            if (StrUtil.equals(analysisType, AiAnalysisConstants.AnalysisType.MATCH)) {
                return saveMatchRecord(userId, (MatchRequest) request, data);
                // case AiAnalysisConstants.AnalysisType.FORTUNE:
                // return saveFortureRecord(userId, request, data);
            }

            log.info(AiAnalysisConstants.ErrorMessage.UNKNOWN_ANALYSIS_TYPE + ": {}", analysisType);
            return null;
        } catch (Exception e) {
            log.error("同步分析数据到数据库失败, userId={}, type={}, error={}", userId, analysisType, e.getMessage(), e);
            throw new BusinessException(AiAnalysisConstants.ErrorMessage.SYNC_FAILED);
        } finally {
            cleanupRedisData(redisKey);
        }
    }

    /**
     * 保存星座匹配记录
     */
    private Long saveMatchRecord(Long userId, MatchRequest request, JSONObject data) {
        MatchRecord matchRecord = new MatchRecord()
                .setUserId(userId)
                .setPerson1Birthday(request.getPerson1Birthday())
                 .setPerson1Sign(request.getSign1().getChineseName())
                .setPerson2Birthday(request.getPerson2Birthday())
                 .setPerson2Sign(request.getSign2().getChineseName())
                .setMatchScore(data.getInt(AiAnalysisConstants.AnalysisField.MATCH_SCORE))
                .setAnalysis(data.getStr(AiAnalysisConstants.AnalysisField.ANALYSIS))
                .setAdvantages(data.getStr(AiAnalysisConstants.AnalysisField.ADVANTAGES))
                .setDisadvantages(data.getStr(AiAnalysisConstants.AnalysisField.DISADVANTAGES))
                .setSuggestions(data.getStr(AiAnalysisConstants.AnalysisField.SUGGESTIONS))
                .setAiModel(request.getAiModel())
                .setCreatedAt(new Date())
                .setUpdatedAt(new Date());
                ;

        matchRecordMapper.insert(matchRecord);
        return matchRecord.getId();
    }

    /**
     * 清理Redis数据
     */
    private void cleanupRedisData(String redisKey) {
        try {
            redisUtil.delete(redisKey);
        } catch (Exception e) {
            log.error("清理Redis数据失败, key={}, error={}", redisKey, e.getMessage(), e);
        }
    }

    /**
     * 构建Redis键
     * @param userId 用户id
     * @param matchNo 匹配编号
     */
    private String buildRedisKey(Long userId, String matchNo) {
        return AiAnalysisConstants.RedisKey.MATCH_ANALYSIS + userId + ":" + matchNo;
    }
}