package com.hc.starmatchai.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hc.starmatchai.common.BusinessException;
import com.hc.starmatchai.common.PageResult;
import com.hc.starmatchai.common.ValidateUtils;
import com.hc.starmatchai.entity.MatchRecord;
import com.hc.starmatchai.mapper.MatchRecordMapper;
import com.hc.starmatchai.model.MatchRequest;
import com.hc.starmatchai.model.MatchResult;
import com.hc.starmatchai.model.ZodiacSign;
import com.hc.starmatchai.model.dto.MatchHistoryQueryDTO;
import com.hc.starmatchai.service.AIModelService;
import com.hc.starmatchai.service.MatchService;
import com.hc.starmatchai.factory.AIModelFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.core.task.AsyncTaskExecutor;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class MatchServiceImpl implements MatchService {

    @Resource
    private MatchRecordMapper matchRecordMapper;

    @Resource
    private AIModelFactory aiModelFactory;

    @Resource
    private AsyncTaskExecutor taskExecutor; // 注入线程池

    private final Map<SseEmitter, Boolean> emitterStatus = new ConcurrentHashMap<>();

    public MatchServiceImpl(MatchRecordMapper matchRecordMapper) {
        this.matchRecordMapper = matchRecordMapper;
    }

    @Override
    public MatchResult calculateMatch(MatchRequest request) {
        log.info("开始处理星座匹配请求, request={}", JSONUtil.toJsonStr(request));

        // 计算星座
        ZodiacSign sign1 = calculateZodiacSign(request.getPerson1Birthday());
        ZodiacSign sign2 = calculateZodiacSign(request.getPerson2Birthday());
        log.info("计算得到星座: person1Sign={}, person2Sign={}", sign1.getChineseName(), sign2.getChineseName());

        // 调用AI获取分析结果和评分
        log.info("开始调用AI模型进行分析");
        Map<String, String> aiAnalysis = getAIAnalysis(sign1, sign2, request.getAiModel());
        log.info("AI分析完成, 匹配得分: {}", aiAnalysis.get("score"));

        MatchResult result = MatchResult.builder()
                .person1Sign(sign1)
                .person2Sign(sign2)
                .matchScore(Integer.parseInt(aiAnalysis.get("score")))
                .analysis(aiAnalysis.get("analysis"))
                .advantages(aiAnalysis.get("advantages"))
                .disadvantages(aiAnalysis.get("disadvantages"))
                .suggestions(aiAnalysis.get("suggestions"))
                .build();

        log.info("星座匹配分析完成, result={}", JSONUtil.toJsonStr(result));
        return result;
    }

    @Override
    public void saveMatchRecord(MatchResult result, MatchRequest request) {
        log.info("开始保存匹配记录, result={}, request={}",
                JSONUtil.toJsonStr(result), JSONUtil.toJsonStr(request));
        try {
            // 使用 Lambda 方式构建实体
            MatchRecord record = new MatchRecord();
            record.setPerson1Birthday(request.getPerson1Birthday());
            record.setPerson2Birthday(request.getPerson2Birthday());
            record.setPerson1Sign(result.getPerson1Sign().getChineseName());
            record.setPerson2Sign(result.getPerson2Sign().getChineseName());
            record.setMatchScore(result.getMatchScore());
            record.setAnalysis(result.getAnalysis());
            record.setAdvantages(result.getAdvantages());
            record.setDisadvantages(result.getDisadvantages());
            record.setSuggestions(result.getSuggestions());
            record.setAiModel(request.getAiModel());
            record.setCreatedAt(new Date());
            record.setUpdatedAt(new Date());

            matchRecordMapper.insert(record);
            log.info("匹配记录保存成功, record={}", JSONUtil.toJsonStr(record));
        } catch (Exception e) {
            log.error("保存匹配记录失败: {}", e.getMessage(), e);
            throw new BusinessException("保存匹配记录失败");
        }
    }

    private ZodiacSign calculateZodiacSign(Date birthday) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(birthday);
        int month = cal.get(Calendar.MONTH) + 1; // Calendar月份从0开始
        int day = cal.get(Calendar.DAY_OF_MONTH);

        if ((month == 3 && day >= 21) || (month == 4 && day <= 19))
            return ZodiacSign.ARIES;
        if ((month == 4 && day >= 20) || (month == 5 && day <= 20))
            return ZodiacSign.TAURUS;
        if ((month == 5 && day >= 21) || (month == 6 && day <= 21))
            return ZodiacSign.GEMINI;
        if ((month == 6 && day >= 22) || (month == 7 && day <= 22))
            return ZodiacSign.CANCER;
        if ((month == 7 && day >= 23) || (month == 8 && day <= 22))
            return ZodiacSign.LEO;
        if ((month == 8 && day >= 23) || (month == 9 && day <= 22))
            return ZodiacSign.VIRGO;
        if ((month == 9 && day >= 23) || (month == 10 && day <= 23))
            return ZodiacSign.LIBRA;
        if ((month == 10 && day >= 24) || (month == 11 && day <= 22))
            return ZodiacSign.SCORPIO;
        if ((month == 11 && day >= 23) || (month == 12 && day <= 21))
            return ZodiacSign.SAGITTARIUS;
        if ((month == 12 && day >= 22) || (month == 1 && day <= 19))
            return ZodiacSign.CAPRICORN;
        if ((month == 1 && day >= 20) || (month == 2 && day <= 18))
            return ZodiacSign.AQUARIUS;
        return ZodiacSign.PISCES;
    }

    private Map<String, String> getAIAnalysis(ZodiacSign sign1, ZodiacSign sign2, String modelCode) {
        AIModelService aiModelService = aiModelFactory.getAIModelService(modelCode);
        return aiModelService.getMatchAnalysis(sign1, sign2);
    }

    @Override
    public PageResult<MatchRecord> getMatchHistory(MatchHistoryQueryDTO queryDTO) {
        log.info("查询匹配历史记录, queryDTO={}", JSONUtil.toJsonStr(queryDTO));
        try {
            Page<MatchRecord> pageParam = new Page<>(queryDTO.getPage(), queryDTO.getSize());

            // 使用 Lambda 方式构建查询条件
            LambdaQueryWrapper<MatchRecord> queryWrapper = new LambdaQueryWrapper<MatchRecord>()
                    // 添加时间范围条件
                    .ge(queryDTO.getStartTime() != null, MatchRecord::getCreatedAt, queryDTO.getStartTime())
                    .le(queryDTO.getEndTime() != null, MatchRecord::getCreatedAt, queryDTO.getEndTime())
                    // 添加星座条件
                    .eq(StrUtil.isNotBlank(queryDTO.getPerson1Sign()),
                            MatchRecord::getPerson1Sign, queryDTO.getPerson1Sign())
                    .eq(StrUtil.isNotBlank(queryDTO.getPerson2Sign()),
                            MatchRecord::getPerson2Sign, queryDTO.getPerson2Sign())
                    // 添加分数范围条件
                    .ge(queryDTO.getMinScore() != null,
                            MatchRecord::getMatchScore, queryDTO.getMinScore())
                    .le(queryDTO.getMaxScore() != null,
                            MatchRecord::getMatchScore, queryDTO.getMaxScore())
                    // 添加AI模型条件
                    .eq(StrUtil.isNotBlank(queryDTO.getAiModel()),
                            MatchRecord::getAiModel, queryDTO.getAiModel());

            // 处理排序
            if (StrUtil.isNotBlank(queryDTO.getOrderBy())) {
                // 根据字段名获取对应的 Lambda 方法引用
                SFunction<MatchRecord, ?> orderByColumn = getOrderByColumn(queryDTO.getOrderBy());
                if (orderByColumn != null) {
                    queryWrapper.orderBy(true,
                            StrUtil.equalsIgnoreCase("asc", queryDTO.getOrderDirection()),
                            orderByColumn);
                }
            } else {
                // 默认按创建时间倒序
                queryWrapper.orderByDesc(MatchRecord::getCreatedAt);
            }

            // 执行查询
            Page<MatchRecord> pageResult = matchRecordMapper.selectPage(pageParam, queryWrapper);

            PageResult<MatchRecord> result = PageResult.build(
                    pageResult.getRecords(),
                    pageResult.getTotal(),
                    pageResult.getSize(),
                    pageResult.getCurrent());

            log.info("查询匹配历史记录成功, 总记录数={}",
                    pageResult.getTotal());
            return result;
        } catch (Exception e) {
            log.error("查询匹配历史记录失败: {}", e.getMessage(), e);
            throw new BusinessException("查询历史记录失败");
        }
    }

    /**
     * 获取排序字段对应的 Lambda 方法引用
     */
    private SFunction<MatchRecord, ?> getOrderByColumn(String orderBy) {
        switch (orderBy) {
            case "matchScore":
                return MatchRecord::getMatchScore;
            case "createdAt":
                return MatchRecord::getCreatedAt;
            case "person1Sign":
                return MatchRecord::getPerson1Sign;
            case "person2Sign":
                return MatchRecord::getPerson2Sign;
            default:
                return null;
        }
    }

    @Override
    public SseEmitter createMatchStreamEmitter(MatchRequest request) {
        // 参数验证
        ValidateUtils.validateMatchRequest(request);

        // 创建发射器，设置更长的超时时间
        SseEmitter emitter = new SseEmitter(60000L); // 1分钟

        // 记录emitter状态
        emitterStatus.put(emitter, true);

        // 设置超时回调
        emitter.onTimeout(() -> {
            log.info("SSE连接已结束");
            emitterStatus.put(emitter, false);
            cleanupEmitter(emitter);
        });

        // 设置完成回调
        emitter.onCompletion(() -> {
            log.info("星座匹配流式请求完成");
            emitterStatus.remove(emitter);
        });

        // 设置错误回调
        emitter.onError(ex -> {
            log.error("星座匹配流式请求异常", ex);
            emitterStatus.put(emitter, false);
            emitterStatus.remove(emitter);
            try {
                Map<String, Object> errorEvent = new HashMap<>();
                errorEvent.put("event", "error");
                errorEvent.put("data", "分析过程发生错误，请重试");
                emitter.send(SseEmitter.event().data(JSONUtil.toJsonStr(errorEvent)));
                emitter.complete();
            } catch (IOException e) {
                log.error("发送错误消息失败", e);
            }
        });

        // 使用线程池执行异步任务
        taskExecutor.execute(() -> {
            try {
                log.info("开始执行星座匹配流式分析");
                if (emitterStatus.get(emitter)) {
                    calculateMatchStream(request, emitter);
                }
            } catch (Exception e) {
                log.error("流式分析异常, request={}, error={}",
                        JSONUtil.toJsonStr(request), e.getMessage(), e);
                handleStreamError(emitter, e);
            }
        });

        return emitter;
    }

    private void sendEvent(SseEmitter emitter, String eventName, Object data) {
        try {
            if (emitterStatus.get(emitter)) {
                Map<String, Object> eventData = new HashMap<>();
                eventData.put("event", eventName);
                eventData.put("data", data);
                emitter.send(SseEmitter.event().data(JSONUtil.toJsonStr(eventData)));
            }
        } catch (Exception e) {
            log.error("发送事件失败: {}", eventName, e);
        }
    }

    private void handleStreamError(SseEmitter emitter, Exception e) {
        try {
            if (emitterStatus.get(emitter)) {
                Map<String, Object> errorEvent = new HashMap<>();
                errorEvent.put("event", "error");
                errorEvent.put("data", e.getMessage());
                emitter.send(SseEmitter.event().data(JSONUtil.toJsonStr(errorEvent)));
            }
        } catch (IOException ex) {
            log.error("发送错误消息失败", ex);
        } finally {
            try {
                emitter.complete();
            } catch (Exception ex) {
                log.error("关闭emitter失败", ex);
            }
            emitterStatus.remove(emitter);
        }
    }

    @Override
    public void calculateMatchStream(MatchRequest request, SseEmitter emitter) {
        try {
            if (!emitterStatus.get(emitter)) {
                return;
            }

            // 计算星座
            ZodiacSign sign1 = calculateZodiacSign(request.getPerson1Birthday());
            ZodiacSign sign2 = calculateZodiacSign(request.getPerson2Birthday());

            // 发送初始信息
            sendSignsEvent(emitter, sign1, sign2);

            // 获取对应的AI模型服务
            AIModelService aiModelService = aiModelFactory.getAIModelService(request.getAiModel());

            log.info("开始调用AI模型进行分析");

            // 调用AI流式分析并获取完整结果
            aiModelService.getMatchAnalysisStream(sign1, sign2, emitter);

            log.info("AI模型分析完成，准备保存结果");

            // todo 准备保存结果

        } catch (Exception e) {
            log.error("流式分析异常", e);
            handleStreamError(emitter, e);
        }
    }

    private void sendSignsEvent(SseEmitter emitter, ZodiacSign sign1, ZodiacSign sign2) {
        Map<String, String> signsData = new HashMap<>();
        signsData.put("person1Sign", sign1.getChineseName());
        signsData.put("person2Sign", sign2.getChineseName());
        sendEvent(emitter, "signs", signsData);
    }

    private void cleanupEmitter(SseEmitter emitter) {
        try {
            emitterStatus.remove(emitter);
            emitter.complete();
        } catch (Exception ex) {
            log.debug("清理SSE资源时发生异常，连接可能已关闭", ex);
        }
    }
}