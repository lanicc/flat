create table flat_service_group
(
    id                   int auto_increment comment '主键',
    name                 varchar(30) not null comment '群名称',
    open_conversation_id varchar(64) default '' comment '钉钉群id',
    config               json comment '配置',
    primary key (id)
) comment '服务群';

create table flat_sub_service_group
(
    id                   int auto_increment comment '主键',
    name                 varchar(30) not null comment '群名称',
    open_conversation_id varchar(64) default '' comment '钉钉群id',
    service_group_id     int         not null comment '用户id',
    robot_code           varchar(64) default '' comment '钉钉群机器人id',
    template_id          varchar(64) default '' comment '钉钉群模板id',
    service              varchar(30) not null comment '服务项',
    ticket_id            int         not null comment '工单id',
    user_id              varchar(64) not null comment '用户id',
    user_nick            varchar(64) not null comment '用户昵称',
    created_at           timestamp   default current_timestamp comment '创建时间',
    primary key (id)
) comment '专项服务群';

create table flat_ticket
(
    id               int auto_increment comment '主键',
    service_group_id int           not null comment '服务群id',
    user_id          varchar(64)   not null comment '用户id',
    user_nick        varchar(64)   not null comment '用户昵称',
    user_desc        varchar(1024) not null comment '用户描述',
    worker_desc      varchar(1024) default '' comment '维护者描述',
    worker_id        varchar(64)   default '' comment '维护者id',
    worker_nick      varchar(64)   default '' comment '维护者昵称',
    result_type      int           default 0 comment '类型，0机器人解答，1人工解答',
    result           varchar(1024) default '' comment '处理结果',
    created_at       timestamp     default current_timestamp comment '创建时间',
    updated_at       timestamp     default current_timestamp on update current_timestamp comment '更新时间',
    completed_at     timestamp     default null comment '完成时间',
    primary key (id)
)
    comment '工单表'
;
