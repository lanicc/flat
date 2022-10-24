package io.github.lanicc.flat.dingtalk;

import com.alibaba.fastjson.JSON;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiImChatScencegroupMessageSendV2Request;
import com.dingtalk.api.request.OapiImChatScenegroupCreateRequest;
import com.dingtalk.api.request.OapiImChatScenegroupGetRequest;
import com.dingtalk.api.request.OapiImChatScenegroupMemberAddRequest;
import com.dingtalk.api.response.OapiImChatScencegroupMessageSendV2Response;
import com.dingtalk.api.response.OapiImChatScenegroupCreateResponse;
import com.dingtalk.api.response.OapiImChatScenegroupGetResponse;
import com.dingtalk.api.response.OapiImChatScenegroupMemberAddResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.taobao.api.ApiException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Created on 2022/10/23.
 *
 * @author lan
 */
@Slf4j
@Component
public class DingtalkOpenApi {

    static RestTemplate restTemplate;

    static {
        Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder = new Jackson2ObjectMapperBuilder();
        jackson2ObjectMapperBuilder.serializationInclusion(JsonInclude.Include.NON_NULL);
        jackson2ObjectMapperBuilder.propertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        jackson2ObjectMapperBuilder.failOnUnknownProperties(false);
        ObjectMapper mapper = jackson2ObjectMapperBuilder.build();
        HttpMessageConverter<Object> jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter(mapper);
        restTemplate = new RestTemplate(Collections.singletonList(jackson2HttpMessageConverter));
    }

    String appKey = "dinge9sdxrh8jywuvmaq";
    String appSecret = "JjEUZCvB9HdgPLYYBoV9m5aRO50X8fRYYMJBjFn-aeHKUs-EFZhOqOmJWO0sAT7O";


    /**
     * 创建场景群
     *
     * @return 场景群OpenConversationId
     */
    public String createSceneGroup(OapiImChatScenegroupCreateRequest req) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient(UrlConstant.SCENE_GROUP_CREATE);
        OapiImChatScenegroupCreateResponse rsp = client.execute(req, reqForAccessToken());

        if (rsp.getSuccess()) {
            return rsp.getResult().getOpenConversationId();
        }
        throw new ApiException("unknown error");
    }

    /**
     * 获取加群链接
     */
    public String groupUrl(String openConversationId) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient(UrlConstant.SCENEGROUP_INFO);
        OapiImChatScenegroupGetRequest req = new OapiImChatScenegroupGetRequest();
        req.setOpenConversationId(openConversationId);
        OapiImChatScenegroupGetResponse rsp = client.execute(req, reqForAccessToken());
        assertRsp(rsp, OapiImChatScenegroupGetResponse::isSuccess);
        return rsp.getResult().getGroupUrl();
    }

    /**
     * 场景群拉人
     */
    public void addMember(OapiImChatScenegroupMemberAddRequest req) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient(UrlConstant.SCENEGROUP_MEMBER_ADD);
        OapiImChatScenegroupMemberAddResponse rsp = client.execute(req, reqForAccessToken());
        assertRsp(rsp, OapiImChatScenegroupMemberAddResponse::isSuccess);
    }

    /**
     * 群助手消息
     */
    public void sendMsg(OapiImChatScencegroupMessageSendV2Request req) {
        DingTalkClient client = new DefaultDingTalkClient(UrlConstant.SCENEGROUP_MESSAGE_SEND_V2);
        OapiImChatScencegroupMessageSendV2Response rsp;
        try {
            rsp = client.execute(req, reqForAccessToken());
            assertRsp(rsp, OapiImChatScencegroupMessageSendV2Response::isSuccess);
        } catch (ApiException e) {
            log.error("", e);
        }
    }

    public String reqForAccessToken() {
        GetTokenResult cache = readCache();
        if (cache.getExpiresIn() > TimeUnit.SECONDS.toMillis(10)) {
            return cache.getAccessToken();
        }
        try {
            URI uri = of("/gettoken", "appkey", appKey, "appsecret", appSecret);
            GetTokenResult result = restTemplate.getForObject(uri, GetTokenResult.class);
            assert result != null;
            cacheAccessToken(result);
            return result.getAccessToken();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void cacheAccessToken(GetTokenResult result) throws IOException {
        FileOutputStream out = new FileOutputStream(System.getProperty("user.home") + "/dingtalk_openapi_accesstoken.txt");
        DataOutputStream dataOutputStream = new DataOutputStream(out);
        dataOutputStream.writeLong(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + result.getExpiresIn());
        dataOutputStream.writeUTF(result.getAccessToken());
        dataOutputStream.flush();
        dataOutputStream.close();
    }

    private GetTokenResult readCache() {
        try {
            FileInputStream in = new FileInputStream(System.getProperty("user.home") + "/dingtalk_openapi_accesstoken.txt");
            DataInputStream dataInputStream = new DataInputStream(in);
            long expireAt = dataInputStream.readLong() - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
            String accessToken = dataInputStream.readUTF();
            return new GetTokenResult(accessToken, expireAt);
        } catch (IOException e) {
            log.warn("read cache error", e);
            return new GetTokenResult();
        }
    }


    private URI of(String uri, String... uriParams) throws URISyntaxException {
        if (uriParams == null || uriParams.length == 0) {
            return new URI("https://api.dingtalk.com" + uri);
        } else {
            StringJoiner stringJoiner = new StringJoiner("&", "?", "");
            for (int i = 0; i < uriParams.length; i += 2) {
                stringJoiner.add(uriParams[i] + "=" + uriParams[i + 1]);
            }
            return new URI("https://api.dingtalk.com" + uri + stringJoiner);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @AllArgsConstructor
    @NoArgsConstructor
    private static class GetTokenResult extends DingtalkResult {
        private String accessToken;
        private long expiresIn;
    }

    @Data
    private static class DingtalkResult {

        private Integer errcode;
        private String errmsg;
    }

    private <T> void assertRsp(T t, Predicate<T> predicate) throws ApiException {
        if (Objects.isNull(t)) {
            throw new ApiException("reponse is null");
        }
        if (!predicate.test(t)) {
            throw new ApiException("unexpected response: " + JSON.toJSONString(t));
        }
    }
}
