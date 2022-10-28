package io.github.lanicc.flat.model.dingtalk;

import lombok.Data;

import java.util.List;

/**
 * Created on 2022/10/27.
 *
 * @author lan
 */
@Data
public class RobotCallbackRequest {

    private String conversationId;
    private String chatbotCorpId;
    private String chatbotUserId;
    private String msgId;
    private String senderNick;
    private boolean isAdmin;
    private String senderStaffId;
    private float sessionWebhookExpiredTime;
    private float createAt;
    private String senderCorpId;
    private String conversationType;
    private String senderId;
    private String conversationTitle;
    private boolean isInAtList;
    private String sessionWebhook;
    private String robotCode;
    private String msgtype;
    private Text text;

    private List<AtUsers> atUsers;

    @Data
    public static class AtUsers {
        private String dingtalkId;
    }

    @Data
    public static class Text {
        private String content;

    }
}
