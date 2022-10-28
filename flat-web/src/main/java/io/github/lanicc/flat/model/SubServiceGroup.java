package io.github.lanicc.flat.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * Created on 2022/10/27.
 *
 * @author lan
 */
@Data
@TableName(value = "flat_sub_service_group")
public class SubServiceGroup {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private String openConversationId;

    private Integer serviceGroupId;

    private String robotCode;

    private String templateId;

    private String service;

    private Integer ticketId;

    private String userId;

    private String userNick;

    private Date createdAt;
}
