package io.github.lanicc.flat.dingtalk;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringJoiner;

/**
 * Created on 2022/10/20.
 *
 * @author lan
 */
public class DingtalkTests {

    String appKey = "dinge9sdxrh8jywuvmaq";
    String appSecret = "JjEUZCvB9HdgPLYYBoV9m5aRO50X8fRYYMJBjFn-aeHKUs-EFZhOqOmJWO0sAT7O";

    RestTemplate restTemplate = new RestTemplate();

    @Test
    void categoryRobotMessage() throws IOException {
        ClassPathResource resource = new ClassPathResource("category.properties");
        Properties category = new Properties();
        category.load(resource.getInputStream());

        String actionUrlFormat = "http://101.69.230.114:12350/dingtalk/robot?cate=";
        StringBuilder sb = new StringBuilder();
        category.forEach((cate, list) -> {
            sb.append(String.format("- [%s](%s): %s", cate, String.format(actionUrlFormat, cate), list));
        });
        System.out.println(sb);

        String dingtalkMsg =
                "{\n" +
                        "     \"msgtype\": \"markdown\",\n" +
                        "     \"markdown\": {\n" +
                        "         \"title\":\"杭州天气\",\n" +
                        "         \"text\": \"" + sb + "\"" +
                        "     }\n" +
                        " }";
        System.out.println(dingtalkMsg);
    }

    @Test
    void createServiceGroup() throws URISyntaxException {

        HttpHeaders headers = new HttpHeaders();

        // headers.add("x-acs-dingtalk-access-token", accessToken());
        headers.add("Content-Type", "application/json");
        HttpEntity<String> httpEntity =
                new HttpEntity<>("{\n" +
                        "        \"validation_type\":0,\n" +
                        "        \"chat_banned_type\":0,\n" +
                        "        \"mention_all_authority\":1,\n" +
                        "        \"owner_user_id\":\"070515353326085011\",\n" +
                        "        \"user_ids\":\"070515353326085011\",\n" +
                        "        \"icon\":\"@asdf12zcv\",\n" +
                        "        \"template_id\":\"adade71d-b3f1-479e-a985-0c4dd95479df\",\n" +
                        "        \"management_type\":\"0\",\n" +
                        "        \"title\":\"哈哈智能服务群\",\n" +
                        "        \"show_history_type\":\"0\",\n" +
                        "        \"searchable\":\"0\"\n" +
                        "}\n", headers);

        HashMap hashMap =
                restTemplate.postForObject(
                        of2("https://oapi.dingtalk.com/topapi/im/chat/scenegroup/create", "access_token", accessToken()), httpEntity, HashMap.class);
        System.out.println(hashMap);
        // {errcode=0, result={chat_id=chat2029b785b030eecbea3dc70fc5464dbb, open_conversation_id=cidFbIzvlkb07o0Rz/g8b/QBw==}, success=true, errmsg=ok, request_id=16kf0gd2t3dnp}
    }

    @Test
    void scencegroupSend() throws URISyntaxException {
        // https://oapi.dingtalk.com/topapi/im/chat/scencegroup/message/send_v2
        HttpHeaders headers = new HttpHeaders();

        // headers.add("x-acs-dingtalk-access-token", accessToken());
        headers.add("Content-Type", "application/json");
        HttpEntity<String> httpEntity =
                new HttpEntity<>("{\n" +
                        "    \"robot_code\": \"1ZMPuEzJsYPTx9216662530282211296\",\n" +
                        // "    \"receiver_union_ids\": \"\",\n" +
                        "    \"is_at_all\": \"false\",\n" +
                        // "    \"receiver_user_ids\": \"\",\n" +
                        // "    \"at_mobiles\": \"\",\n" +
                        // "    \"at_users\":\"\",\n" +
                        "    \"target_open_conversation_id\": \"cidCCnA82VI8vKihxVRn4dhvg==\",\n" +
                        "    \"msg_param_map\": \"{\\\"title\\\":\\\"测s试\\\",\\\"markdown_content\\\":\\\"- [搜索](http://101.69.230.114:12350/dingtalk/robot/action?cate=搜索&conversationId=cidCCnA82VI8vKihxVRn4dhvg==&robotCode=1ZMPuEzJsYPTx9216662530282211296): es,搜索,ng,ngsearcher,ng-searcher,searcher\n- [用户](http://101.69.230.114:12350/dingtalk/robot/action?cate=用户&conversationId=cidCCnA82VI8vKihxVRn4dhvg==&robotCode=1ZMPuEzJsYPTx9216662530282211296): 用户,user,gaea- [权限](http://101.69.230.114:12350/dingtalk/robot/action?cate=权限&conversationId=cidCCnA82VI8vKihxVRn4dhvg==&robotCode=1ZMPuEzJsYPTx9216662530282211296): 权限,shield,内网权限,统一工作台- [登录](http://101.69.230.114:12350/dingtalk/robot/action?cate=登录&conversationId=cidCCnA82VI8vKihxVRn4dhvg==&robotCode=1ZMPuEzJsYPTx9216662530282211296): sso,登录,账号,密码\\\"}\",\n" +
                        // "    \"msg_media_id_param_map\": \"{\\\"pic1\\\":\\\"@123\\\",\\\"pic2\\\":\\\"@456\\\"}\",\n" +
                        "    \"msg_template_id\": \"inner_app_template_markdown\"\n" +
                        "}", headers);

        HashMap hashMap =
                restTemplate.postForObject(
                        of2("https://oapi.dingtalk.com/topapi/im/chat/scencegroup/message/send_v2", "access_token", accessToken()), httpEntity, HashMap.class);
        System.out.println(hashMap);
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

    private URI of2(String url, String... uriParams) throws URISyntaxException {
        if (uriParams == null || uriParams.length == 0) {
            return new URI(url);
        } else {
            StringJoiner stringJoiner = new StringJoiner("&", "?", "");
            for (int i = 0; i < uriParams.length; i += 2) {
                stringJoiner.add(uriParams[i] + "=" + uriParams[i + 1]);
            }
            return new URI(url + stringJoiner);
        }
    }


    @Test
    void getAccessToken() throws URISyntaxException {
        System.out.println(accessToken());
    }
    private String accessToken() throws URISyntaxException {
        URI uri = of("/gettoken", "appkey", appKey, "appsecret", appSecret);
        HashMap result = restTemplate.getForObject(uri, HashMap.class);
        System.out.println(result);
        return result.get("access_token").toString();
        // return "a64f4a76245331c88773dfafd23fba20";
    }
}
