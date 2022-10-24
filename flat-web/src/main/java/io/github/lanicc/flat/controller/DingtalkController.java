package io.github.lanicc.flat.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dingtalk.api.request.OapiImChatScencegroupMessageSendV2Request;
import com.dingtalk.api.request.OapiImChatScenegroupCreateRequest;
import com.taobao.api.ApiException;
import io.github.lanicc.flat.dingtalk.DingtalkConfig;
import io.github.lanicc.flat.dingtalk.DingtalkOpenApi;
import io.github.lanicc.flat.elastic.ElasticsearchService;
import io.github.lanicc.flat.util.DingtalkMsgUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2022/10/20.
 *
 * @author lan
 */
@RestController
// @RequestMapping("/")
public class DingtalkController {

    RestTemplate restTemplate = new RestTemplate();

    static String actionUrlFormat = "http://101.69.230.114:12350/dingtalk/robot/action";
    static String actionUrlFormat2 = "http://101.69.230.114:12350/dingtalk/robot/ques";

    ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

    @Autowired
    private DingtalkOpenApi dingtalkOpenApi;

    @Autowired
    private ElasticsearchService elasticsearchService;
    List<String> items;

    @PostConstruct
    public void init() {
        items = elasticsearchService.listItems();
    }


    @PostMapping(value = {"/dingtalk/robot/ques"})
    public void s() {
        return;
    }

    @PostMapping(value = {"/", "/dingtalk/robot"})
    public void test(HttpServletRequest request, @RequestBody JSONObject body) throws ApiException {
        System.out.println(body);

        String conversationId = body.getString("conversationId");
        String robotCode = body.getString("robotCode");
        String senderStaffId = body.getString("senderStaffId");
        String senderNick = body.getString("senderNick");

        String content = StringUtils.trim(body.getJSONObject("text").getString("content"));
        if (StringUtils.isNotBlank(content)) {
            if (content.contains("专项处理群")) {
                String item = content.replace("专项处理群", "");
                ElasticsearchService.Maintainer maintainer = elasticsearchService.getByItem(item);
                if (Objects.nonNull(maintainer)) {
                    String newConversationId = createSceneGroup(item + "-问题群-" + senderNick, senderStaffId, maintainer.getMaintainerDingId());
                    executor.schedule(() ->{
                                try {
                                    robotSendMsg(
                                            robotCode,
                                            conversationId,
                                            senderStaffId,
                                            String.format("@%s\n" +
                                                            "- 已为您创建新任务单 \n" +
                                                            "- 任务标题: %s \n" +
                                                            "- 任务单状态: 处理中 \n" +
                                                            "- 当前小二: %s \n" +
                                                            "- 请[点击此处](%s)进入任务单专项群",
                                                    senderNick,
                                                    item,
                                                    maintainer.getMaintainer(),
                                                    dingtalkOpenApi.groupUrl(newConversationId)
                                            )
                                    );
                                } catch (ApiException e) {
                                    e.printStackTrace();
                                }
                                robotSendMsg(
                                        DingtalkConfig.quesScenegroupRobotCode(),
                                        newConversationId,
                                        senderStaffId,
                                        "请@我描述您的问题，提供环境、链路、等必要的信息"
                                );
                            }
                            , 2, TimeUnit.SECONDS);

                }
                return;
            } else {
                List<ElasticsearchService.YuqueDocSearchResult> searchResults = elasticsearchService.search(content);
                if (CollectionUtils.isNotEmpty(searchResults)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("为您找到如下问题的解决方法: \n");
                    for (ElasticsearchService.YuqueDocSearchResult result : searchResults) {
                        sb.append(String.format("- [%s](https://souche.yuque.com/%s/%s)\n", result.getTitle(), result.getNamespace(), result.getSlug()));
                    }
                    sb.append("\n");
                    sb.append("如上述方案无法解决，可选择问题分类，进入专项处理群: \n")
                            .append(DingtalkMsgUtil.constructDtmdMsg(
                                    items,
                                    item -> item,
                                    item -> item + "专项处理群"
                            ));
                    robotSendMsg(
                            robotCode,
                            conversationId,
                            senderStaffId,
                            sb.toString()
                    );
                }
                return;
            }

        }
        robotSendMsg(
                robotCode,
                conversationId,
                senderStaffId,
                "请选择问题分类，进入专项处理群: \n" +
                        DingtalkMsgUtil.constructDtmdMsg(
                                items,
                                item -> item,
                                item -> item + "专项处理群"
                        )
        );
    }


    private String createSceneGroup(String title, String userIds, String ownerId) throws ApiException {
        OapiImChatScenegroupCreateRequest req = new OapiImChatScenegroupCreateRequest();
        req.setValidationType(0L);
        req.setChatBannedType(0L);
        req.setMentionAllAuthority(1L);
        req.setOwnerUserId(ownerId);
        req.setUserIds(userIds);
        // 问题群模板
        req.setTemplateId(DingtalkConfig.quesScenegroupTemplateId());
        req.setManagementType(0L);
        req.setTitle(title);
        req.setShowHistoryType(1L);
        req.setSearchable(0L);

        return dingtalkOpenApi.createSceneGroup(req);
    }
    // private String createSceneGroup(String title, String userIds) throws URISyntaxException {
    //     HttpHeaders headers = new HttpHeaders();
    //
    //     // headers.add("x-acs-dingtalk-access-token", accessToken());
    //     headers.add("Content-Type", "application/json");
    //     HttpEntity<String> httpEntity =
    //             new HttpEntity<>("{\n" +
    //                     "        \"validation_type\":0,\n" +
    //                     "        \"chat_banned_type\":0,\n" +
    //                     "        \"mention_all_authority\":1,\n" +
    //                     "        \"owner_user_id\":\"070515353326085011\",\n" +
    //                     "        \"user_ids\":\"" + userIds + "\",\n" +
    //                     "        \"icon\":\"@asdf12zcv\",\n" +
    //                     "        \"template_id\":\"adade71d-b3f1-479e-a985-0c4dd95479df\",\n" +
    //                     "        \"management_type\":\"0\",\n" +
    //                     "        \"title\":\"" + title + "\",\n" +
    //                     "        \"show_history_type\":\"0\",\n" +
    //                     "        \"searchable\":\"0\"\n" +
    //                     "}\n", headers);
    //
    //     JSONObject res =
    //             restTemplate.postForObject(
    //                     of2("https://oapi.dingtalk.com/topapi/im/chat/scenegroup/create", "access_token", accessToken()), httpEntity, JSONObject.class);
    //     System.out.println(res);
    //     JSONObject result = res.getJSONObject("result");
    //     String openConversationId = result.getString("open_conversation_id");
    //     String chatId = result.getString("chat_id");
    //     return openConversationId;
    // }

    public static void main(String[] args) throws URISyntaxException, ApiException {
        DingtalkController dingtalkController = new DingtalkController();
        dingtalkController.robotSendMsg("EZkXcLtMPzpkoX216664474884781286", "cidNkt/vsM0Ru/GPJZ4B55+BA==", "$:LWCP_v1:$kn5DdRoLXjKUSPxysmYXTigSAtY6dgpS", "你好");
    }

    private void robotSendMsg(String robotCode, String openConversationId, String atUsers, String msg) {
        OapiImChatScencegroupMessageSendV2Request req = new OapiImChatScencegroupMessageSendV2Request();
        req.setRobotCode(robotCode);
        req.setIsAtAll(false);
        req.setAtUsers(atUsers);
        req.setTargetOpenConversationId(openConversationId);
        Map<String, String> msgParamMap = new HashMap<>();
        msgParamMap.put("title", "消息标题");
        msgParamMap.put("markdown_content", msg);
        req.setMsgParamMap(JSON.toJSONString(msgParamMap));
        req.setMsgTemplateId("inner_app_template_markdown");
        dingtalkOpenApi.sendMsg(req);
    }
    // private void robotSendMsg(String robotCode, String openConversationId, String chatbotUserId, String msg) throws URISyntaxException {
    //
    //     HttpHeaders headers = new HttpHeaders();
    //
    //     // headers.add("x-acs-dingtalk-access-token", accessToken());
    //     headers.add("Content-Type", "application/json");
    //     HttpEntity<String> httpEntity =
    //             new HttpEntity<>("{\n" +
    //                     "    \"robot_code\": \"" + robotCode + "\",\n" +
    //                     // "    \"receiver_union_ids\": \"" + senderId + "\",\n" +
    //                     "    \"is_at_all\": \"false\",\n" +
    //                     // "    \"receiver_user_ids\": \"\",\n" +
    //                     // "    \"at_mobiles\": \"\",\n" +
    //                     "    \"at_users\":\"" + chatbotUserId + "\",\n" +
    //                     "    \"target_open_conversation_id\": \"" + openConversationId + "\",\n" +
    //                     "    \"msg_param_map\": \"{\\\"title\\\":\\\"测s试\\\",\\\"markdown_content\\\":\\\"" + msg + "\\\"}\",\n" +
    //                     "    \"msg_template_id\": \"inner_app_template_markdown\"\n" +
    //                     "}", headers);
    //
    //     HashMap hashMap =
    //             restTemplate.postForObject(
    //                     of2("https://oapi.dingtalk.com/topapi/im/chat/scencegroup/message/send_v2", "access_token", accessToken()), httpEntity, HashMap.class);
    //     System.out.println(hashMap);
    // }

    private void addSceneGroupUser(String openConversationId, String userIds) throws URISyntaxException {
        HttpHeaders headers = new HttpHeaders();

        // headers.add("x-acs-dingtalk-access-token", accessToken());
        headers.add("Content-Type", "application/json");
        HttpEntity<String> httpEntity =
                new HttpEntity<>("{\n" +
                        "        \"user_ids\":\"" + userIds + "\",\n" +
                        "        \"open_conversation_id\":\"" + openConversationId + "\"\n" +
                        "}", headers);

        HashMap hashMap =
                restTemplate.postForObject(
                        of2("https://oapi.dingtalk.com/topapi/im/chat/scencegroup/message/send_v2", "access_token", accessToken()), httpEntity, HashMap.class);
        System.out.println(hashMap);
    }


    private String accessToken() throws URISyntaxException {
        // URI uri = of("/gettoken", "appkey", appKey, "appsecret", appSecret);
        // HashMap result = restTemplate.getForObject(uri, HashMap.class);
        // System.out.println(result);
        // return result.get("access_token").toString();
        return dingtalkOpenApi.reqForAccessToken();
    }

    private URI of2(String url, String... uriParams) throws URISyntaxException {
        return new URI(appendParam(url, uriParams));
    }

    private String appendParam(String url, String... uriParams) {
        if (uriParams == null || uriParams.length == 0) {
            return url;
        }
        StringJoiner stringJoiner = new StringJoiner("&", "?", "");
        for (int i = 0; i < uriParams.length; i += 2) {
            stringJoiner.add(uriParams[i] + "=" + uriParams[i + 1]);
        }
        return url + stringJoiner;
    }
}
