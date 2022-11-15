package io.github.lanicc.flat.yuque;

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
public class YuqueOpenApiManager {

    private final Map<Integer, YuqueOpenApi> yuqueOpenApiMap = new ConcurrentHashMap<>();

    public YuqueOpenApi get(int sgId) {
        return yuqueOpenApiMap.get(sgId);
    }

    public YuqueOpenApi make(ServiceGroup serviceGroup) {
        ServiceGroup.Config config = serviceGroup.getConfig();
        ServiceGroup.Yuque yuque = config.getYuque();
        YuqueOpenApi yuqueOpenApi = new YuqueOpenApi(yuque.getUrl(), yuque.getAccessToken());
        yuqueOpenApiMap.put(serviceGroup.getId(), yuqueOpenApi);
        return yuqueOpenApi;
    }

    public YuqueOpenApi getOrMake(ServiceGroup serviceGroup) {
        synchronized (this) {
            YuqueOpenApi yuqueOpenApi = get(serviceGroup.getId());
            if (Objects.isNull(yuqueOpenApi)) {
                return make(serviceGroup);
            }
            return yuqueOpenApi;
        }
    }
}
