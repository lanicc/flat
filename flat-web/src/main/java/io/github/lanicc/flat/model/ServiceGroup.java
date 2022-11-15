package io.github.lanicc.flat.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import lombok.Data;

import java.util.List;

/**
 * Created on 2022/10/27.
 *
 * @author lan
 */
@Data
@TableName(value = "flat_service_group", autoResultMap = true)
public class ServiceGroup {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private String openConversationId;

    @TableField(typeHandler = FastjsonTypeHandler.class)
    private Config config;

    @Data
    public static class Config {
        private String ownerDingId;
        private String appKey;
        private String appSecret;
        private String groupTemplateId;
        private String groupTemplateRobotCode;
        private String subGroupTemplateId;
        private String subGroupTemplateRobotCode;

        private Yuque yuque;

        private List<Service> services;
    }

    @Data
    public static class Service {
        private String name;
        private String maintainerDingId;
        private String maintainer;
    }

    @Data
    public static class Yuque {
        private String accessToken;
        private String accessTokenOwner;
        private String url;
    }

}
