package io.github.lanicc.flat.dingtalk;

import com.dingtalk.api.request.OapiImChatScenegroupCreateRequest;
import com.taobao.api.ApiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created on 2022/10/24.
 *
 * @author lan
 */
class DingtalkOpenApiTest {

    DingtalkOpenApi dingtalkOpenApi = new DingtalkOpenApi();

    @Test
    void reqForAccessToken() {
        String accessToken = dingtalkOpenApi.reqForAccessToken();
        assertNotNull(accessToken);
        assertEquals(accessToken, dingtalkOpenApi.reqForAccessToken());
    }

    @Test
    void createSceneGroup() {
        OapiImChatScenegroupCreateRequest request = new OapiImChatScenegroupCreateRequest();
        // openApi.createSceneGroup()
    }

    @Test
    void addMember() {
    }

    @Test
    void sendMsg() {
    }

    @Test
    void groupUrl() throws ApiException {
        System.out.println(dingtalkOpenApi.groupUrl("cidFbIzvlkb07o0Rz/g8b/QBw=="));
    }
}
