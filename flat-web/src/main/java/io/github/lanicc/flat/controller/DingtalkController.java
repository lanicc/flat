package io.github.lanicc.flat.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dingtalk.api.request.OapiImChatScencegroupMessageSendV2Request;
import com.dingtalk.api.request.OapiImChatScenegroupCreateRequest;
import com.taobao.api.ApiException;
import io.github.lanicc.flat.dingtalk.DingtalkOpenApi;
import io.github.lanicc.flat.dingtalk.DingtalkOpenApiManager;
import io.github.lanicc.flat.elastic.ElasticsearchService;
import io.github.lanicc.flat.mapper.ServiceGroupMapper;
import io.github.lanicc.flat.mapper.SubServiceGroupMapper;
import io.github.lanicc.flat.mapper.TicketMapper;
import io.github.lanicc.flat.model.ServiceGroup;
import io.github.lanicc.flat.model.SubServiceGroup;
import io.github.lanicc.flat.model.Ticket;
import io.github.lanicc.flat.model.dingtalk.RobotCallbackRequest;
import io.github.lanicc.flat.util.DingtalkMsgUtil;
import io.github.lanicc.flat.yuque.YuqueOpenApi;
import io.github.lanicc.flat.yuque.YuqueOpenApiManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created on 2022/10/20.
 *
 * @author lan
 */
@RestController
// @RequestMapping("/")
public class DingtalkController {

    ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

    @Autowired
    private ElasticsearchService elasticsearchService;
    List<String> items;

    @PostConstruct
    public void init() {
        items = elasticsearchService.listItems();
    }

    @Autowired
    private ServiceGroupMapper serviceGroupMapper;

    @Autowired
    private SubServiceGroupMapper subServiceGroupMapper;

    @Autowired
    private TicketMapper ticketMapper;

    @Autowired
    private DingtalkOpenApiManager dingtalkOpenApiManager;

    @Autowired
    private YuqueOpenApiManager yuqueOpenApiManager;

    private static final ThreadLocal<DingtalkOpenApi> DINGTALK_OPEN_API_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<YuqueOpenApi> YUQUE_OPEN_API_THREAD_LOCAL = new ThreadLocal<>();

    @PostMapping(value = {"/dingtalk/sub_service_group_robot"})
    public void specialGroupRobotCallback(@RequestBody RobotCallbackRequest req) throws ApiException {
        String content = req.getText().getContent();
        if (StringUtils.isNotBlank(content)) {
            String conversationId = req.getConversationId();
            SubServiceGroup subServiceGroup =
                    subServiceGroupMapper.selectOne(
                            Wrappers.<SubServiceGroup>lambdaQuery()
                                    .eq(SubServiceGroup::getOpenConversationId, conversationId)
                    );
            assert subServiceGroup != null;
            Integer ticketId = subServiceGroup.getTicketId();
            Ticket ticket = ticketMapper.selectById(ticketId);
            assert ticket != null;
            if (StringUtils.isBlank(ticket.getUserDesc())) {
                ticket.setUserDesc(content);
            } else {
                ticket.setUserDesc(StringUtils.joinWith("\n", ticket.getUserDesc(), content));
            }
            ticket.setUpdatedAt(new Date());
            ticketMapper.updateById(ticket);
        }

    }

    @PostMapping(value = {"/dingtalk/service_group_robot"})
    public void serviceGroupRobotCallback(@RequestBody RobotCallbackRequest req) throws ApiException {

        ServiceGroup serviceGroup =
                serviceGroupMapper.selectOne(
                        Wrappers.<ServiceGroup>lambdaQuery()
                                .eq(ServiceGroup::getOpenConversationId, req.getConversationId())
                );
        assert serviceGroup != null;

        DINGTALK_OPEN_API_THREAD_LOCAL.set(dingtalkOpenApiManager.getOrMake(serviceGroup.getConfig()));
        YUQUE_OPEN_API_THREAD_LOCAL.set(yuqueOpenApiManager.getOrMake(serviceGroup));

        List<ServiceGroup.Service> services = serviceGroup.getConfig().getServices();
        Map<String, ServiceGroup.Service> serviceMap = services.stream()
                .collect(Collectors.toMap(ServiceGroup.Service::getName, e -> e));
        String content = StringUtils.trim(req.getText().getContent());

        if (StringUtils.contains(content, "专项处理群")) {
            String item = content.replace("专项处理群", "");
            ServiceGroup.Service service = serviceMap.get(item);
            if (Objects.nonNull(service)) {
                createNewSceneGroup(serviceGroup, service, req);
            }
        } else {
            searchQuestionAnswer(content, serviceGroup, services, req);
        }
    }

    private void replyCommonOptions(List<ServiceGroup.Service> services, RobotCallbackRequest req) {
        robotSendMsg(
                req.getRobotCode(),
                req.getConversationId(),
                req.getSenderStaffId(),
                "常见问题可直接@我找到答案哦"
        );

        // robotSendMsg(
        //         req.getRobotCode(),
        //         req.getConversationId(),
        //         req.getSenderStaffId(),
        //         "常见问题可直接@我找到答案哦请选择问题分类，进入专项处理群: \n" +
        //                 DingtalkMsgUtil.constructDtmdMsg(
        //                         services,
        //                         ServiceGroup.Service::getName,
        //                         s -> s.getName() + "专项处理群"
        //                 )
        // );
    }

    private void searchQuestionAnswer(String content, ServiceGroup serviceGroup, List<ServiceGroup.Service> services, RobotCallbackRequest req) {
        if (StringUtils.isBlank(content)) {
            replyCommonOptions(services, req);
            return;
        }
        addTicket(serviceGroup, req);

        YuqueOpenApi yuqueOpenApi = YUQUE_OPEN_API_THREAD_LOCAL.get();
        List<YuqueOpenApi.SearchResultItem> searchResultItems = yuqueOpenApi.searchDoc(req.getText().getContent());
        StringBuilder sb = new StringBuilder();
        if (CollectionUtils.isNotEmpty(searchResultItems)) {
            sb.append("为您找到如下的解决办法: \n");
            int i = 0;
            for (YuqueOpenApi.SearchResultItem item : searchResultItems) {
                sb.append(String.format("- [%s](%s)\n", item.getInfo(), item.getUrl()));
                if (i++ >= 5) {
                    break;
                }
            }
            sb.append("\n");
            sb.append("如上述方案无法解决，可选择下面的问题分类，进入专项处理群: \n")
                    .append(DingtalkMsgUtil.constructDtmdMsg(
                            services,
                            ServiceGroup.Service::getName,
                            s -> s.getName() + "专项处理群"
                    ));

        } else {
            sb.append("很抱歉，没有找到合适的解决办法。 \n");
            sb.append("可选择下面的问题分类，进入专项处理群: \n")
                    .append(DingtalkMsgUtil.constructDtmdMsg(
                            services,
                            ServiceGroup.Service::getName,
                            s -> s.getName() + "专项处理群"
                    ));
        }
        robotSendMsg(
                req.getRobotCode(),
                req.getConversationId(),
                req.getSenderStaffId(),
                sb.toString()
        );
    }


    private void createNewSceneGroup(ServiceGroup serviceGroup, ServiceGroup.Service service, RobotCallbackRequest req) throws ApiException {
        Ticket ticket = addTicket(serviceGroup, req);
        String newConversationId = createSceneGroup(serviceGroup, service, req, ticket);
        DingtalkOpenApi dingtalkOpenApi = DINGTALK_OPEN_API_THREAD_LOCAL.get();
        executor.schedule(() -> {
                    DINGTALK_OPEN_API_THREAD_LOCAL.set(dingtalkOpenApi);
                    try {
                        robotSendMsg(
                                req.getRobotCode(),
                                req.getConversationId(),
                                req.getSenderStaffId(),
                                String.format("@%s\n" +
                                                "- 已为您创建新任务单 \n" +
                                                "- 任务标题: %s \n" +
                                                "- 任务单状态: 处理中 \n" +
                                                "- 当前小二: %s \n" +
                                                "- 请[点击此处](%s)进入任务单专项群",
                                        req.getSenderNick(),
                                        service.getName(),
                                        service.getMaintainer(),
                                        dingtalkOpenApi.groupUrl(newConversationId)
                                )
                        );
                        robotSendMsg(
                                serviceGroup.getConfig().getSubGroupTemplateRobotCode(),
                                newConversationId,
                                req.getSenderStaffId(),
                                "请@我描述您的问题，提供环境、链路、等必要的信息"
                        );
                    } catch (ApiException e) {
                        e.printStackTrace();
                    } finally {
                        DINGTALK_OPEN_API_THREAD_LOCAL.remove();
                    }
                }
                , 2, TimeUnit.SECONDS);
    }

    private String createSceneGroup(ServiceGroup serviceGroup, ServiceGroup.Service service, RobotCallbackRequest callbackRequest, Ticket ticket) throws ApiException {
        OapiImChatScenegroupCreateRequest req = new OapiImChatScenegroupCreateRequest();
        req.setValidationType(0L);
        req.setChatBannedType(0L);
        req.setMentionAllAuthority(1L);
        req.setOwnerUserId(service.getMaintainerDingId());
        req.setUserIds(callbackRequest.getSenderStaffId());
        // 问题群模板
        req.setTemplateId(serviceGroup.getConfig().getSubGroupTemplateId());
        req.setManagementType(0L);
        req.setTitle(callbackRequest.getText().getContent());
        req.setShowHistoryType(1L);
        req.setSearchable(0L);

        String openConversationId = DINGTALK_OPEN_API_THREAD_LOCAL.get().createSceneGroup(req);

        SubServiceGroup subServiceGroup = new SubServiceGroup();
        subServiceGroup.setName(callbackRequest.getText().getContent());
        subServiceGroup.setOpenConversationId(openConversationId);
        subServiceGroup.setServiceGroupId(serviceGroup.getId());
        subServiceGroup.setRobotCode(serviceGroup.getConfig().getSubGroupTemplateRobotCode());
        subServiceGroup.setTemplateId(serviceGroup.getConfig().getSubGroupTemplateId());
        subServiceGroup.setService(service.getName());
        subServiceGroup.setTicketId(ticket.getId());
        subServiceGroup.setUserId(ticket.getUserId());
        subServiceGroup.setUserNick(ticket.getUserNick());
        subServiceGroup.setCreatedAt(new Date());

        subServiceGroupMapper.insert(subServiceGroup);

        ticket.setWorkerId(service.getMaintainerDingId());
        ticket.setWorkerNick(service.getMaintainer());
        ticketMapper.updateById(ticket);

        return openConversationId;
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
        DINGTALK_OPEN_API_THREAD_LOCAL.get().sendMsg(req);
    }

    private Ticket addTicket(ServiceGroup serviceGroup, RobotCallbackRequest req) {
        Ticket ticket = new Ticket();
        ticket.setServiceGroupId(serviceGroup.getId());
        ticket.setUserId(req.getSenderId());
        ticket.setUserNick(req.getSenderNick());
        ticket.setUserDesc(req.getText().getContent());
        ticket.setCreatedAt(new Date());
        ticket.setUpdatedAt(new Date());
        ticketMapper.insert(ticket);
        return ticket;
    }

}
