package io.github.lanicc.flat.service;

import com.dingtalk.api.request.OapiImChatScenegroupCreateRequest;
import com.taobao.api.ApiException;
import io.github.lanicc.flat.dingtalk.DingtalkOpenApiManager;
import io.github.lanicc.flat.mapper.ServiceGroupMapper;
import io.github.lanicc.flat.model.ServiceGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created on 2022/10/27.
 *
 * @author lan
 */
@Component
public class ServiceGroupService {

    @Autowired
    private ServiceGroupMapper serviceGroupMapper;

    @Autowired
    private DingtalkOpenApiManager dingtalkOpenApiManager;

    public ServiceGroup createServiceGroup(ServiceGroup serviceGroup) {
        assert serviceGroupMapper.insert(serviceGroup) > 0;
        String openConversationId = createDingGroup(serviceGroup);
        serviceGroup.setOpenConversationId(openConversationId);
        serviceGroupMapper.updateById(serviceGroup);
        return serviceGroup;
    }

    public ServiceGroup recreateDingServiceGroup(int id) {
        ServiceGroup serviceGroup = serviceGroupMapper.selectById(id);
        assert serviceGroup != null;
        String openConversationId = createDingGroup(serviceGroup);
        serviceGroup.setOpenConversationId(openConversationId);
        serviceGroupMapper.updateById(serviceGroup);
        return serviceGroup;
    }

    private String createDingGroup(ServiceGroup serviceGroup) {
        OapiImChatScenegroupCreateRequest request = new OapiImChatScenegroupCreateRequest();
        request.setTitle(serviceGroup.getName());
        request.setTemplateId(serviceGroup.getConfig().getGroupTemplateId());
        request.setOwnerUserId(serviceGroup.getConfig().getOwnerDingId());
        request.setShowHistoryType(1L);
        request.setSearchable(1L);

        try {
            return dingtalkOpenApiManager.getOrMake(serviceGroup.getConfig()).createSceneGroup(request);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }
}
