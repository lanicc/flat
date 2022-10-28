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
@TableName(value = "flat_ticket")
public class Ticket {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer serviceGroupId;

    private String userId;

    private String userNick;

    private String userDesc;

    private String workerDesc;

    private String workerId;

    private String workerNick;

    private int resultType;

    private String result;

    private Date createdAt;

    private Date updatedAt;

    private Date completedAt;
}
