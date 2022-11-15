package io.github.lanicc.flat.dingtalk;

import io.github.lanicc.flat.model.ServiceGroup;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 2022/10/27.
 *
 * @author lan
 */
@Component
public class DingtalkOpenApiManager {

    private final Map<String, DingtalkOpenApi> dingtalkOpenApiMap = new ConcurrentHashMap<>();

    public DingtalkOpenApi get(String appKey) {
        return dingtalkOpenApiMap.get(appKey);
    }

    public synchronized DingtalkOpenApi make(String appKey, String appSecret) {
        DingtalkOpenApi dingtalkOpenApi = new DingtalkOpenApi(appKey, appSecret);
        dingtalkOpenApiMap.put(appKey, dingtalkOpenApi);
        return dingtalkOpenApi;
    }

    public DingtalkOpenApi getOrMake(ServiceGroup.Config config) {
        synchronized (this) {
            DingtalkOpenApi dingtalkOpenApi = get(config.getAppKey());
            if (Objects.isNull(dingtalkOpenApi)) {
                return make(config.getAppKey(), config.getAppSecret());
            }
            return dingtalkOpenApi;
        }
    }
}
