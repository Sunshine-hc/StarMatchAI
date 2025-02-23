package com.hc.starmatchai.factory;

import com.hc.starmatchai.common.BusinessException;
import com.hc.starmatchai.service.AIModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AIModelFactory {

    private final Map<String, AIModelService> aiModelServiceMap = new ConcurrentHashMap<>();

    @Autowired
    public AIModelFactory(Map<String, AIModelService> serviceMap) {
        serviceMap.forEach((name, service) -> {
            if (name.endsWith("ModelService")) {
                String modelName = name.replace("ModelService", "");
                aiModelServiceMap.put(modelName, service);
            }
        });
    }

    public AIModelService getAIModelService(String modelCode) {
        AIModelService service = aiModelServiceMap.get(modelCode);
        if (service == null) {
            throw new BusinessException("不支持的AI模型类型：" + modelCode);
        }
        return service;
    }
}