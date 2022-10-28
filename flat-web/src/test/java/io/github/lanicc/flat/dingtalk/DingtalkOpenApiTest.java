package io.github.lanicc.flat.dingtalk;

import com.alibaba.fastjson.JSON;
import com.dingtalk.api.request.OapiImChatScencegroupMessageSendV2Request;
import com.dingtalk.api.request.OapiImChatScenegroupCreateRequest;
import com.taobao.api.ApiException;
import io.github.lanicc.flat.model.ServiceGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created on 2022/10/24.
 *
 * @author lan
 */
class DingtalkOpenApiTest {
    DingtalkOpenApi dingtalkOpenApi;
    ServiceGroup serviceGroup;

    @BeforeEach
    void setUp() throws IOException {
        ClassPathResource resource = new ClassPathResource("setup/config-example.json");
        ServiceGroup.Config serviceGroupConfig = JSON.parseObject(resource.getInputStream(), ServiceGroup.Config.class);
        assertNotNull(serviceGroupConfig);
        serviceGroup = new ServiceGroup();
        serviceGroup.setName("测试服务群1");
        serviceGroup.setConfig(serviceGroupConfig);

        dingtalkOpenApi = new DingtalkOpenApi(serviceGroupConfig.getAppKey(), serviceGroupConfig.getAppSecret());
    }

    @Test
    void reqForAccessToken() {
        String accessToken = dingtalkOpenApi.reqForAccessToken();
        assertNotNull(accessToken);
        System.out.println(accessToken);
    }

    @Test
    void createSceneGroup() throws ApiException {
        OapiImChatScenegroupCreateRequest request = new OapiImChatScenegroupCreateRequest();
        request.setTitle(serviceGroup.getName());
        request.setTemplateId(serviceGroup.getConfig().getGroupTemplateId());
        request.setOwnerUserId(serviceGroup.getConfig().getOwnerDingId());
        request.setShowHistoryType(1L);
        request.setSearchable(1L);
        String openConversationId = dingtalkOpenApi.createSceneGroup(request);
        assertNotNull(openConversationId);
        System.out.println(openConversationId);
    }

    @Test
    void addMember() {
    }

    @Test
    void sendMsg() {
        OapiImChatScencegroupMessageSendV2Request req = new OapiImChatScencegroupMessageSendV2Request();
        req.setRobotCode(serviceGroup.getConfig().getGroupTemplateRobotCode());
        req.setIsAtAll(false);
        req.setTargetOpenConversationId("cidmv8JOYdiwOuGsHRgu11RPA==");
        Map<String, String> msgParamMap = new HashMap<>();
        msgParamMap.put("title", "消息标题");
        msgParamMap.put("markdown_content", "hello world");
        req.setMsgParamMap(JSON.toJSONString(msgParamMap));
        req.setMsgTemplateId("inner_app_template_markdown");
        dingtalkOpenApi.sendMsg(req);
    }

    @Test
    void groupUrl() throws ApiException {
        System.out.println(dingtalkOpenApi.groupUrl("cidmv8JOYdiwOuGsHRgu11RPA=="));
    }


}
