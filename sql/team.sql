create table team
(
    id    bigint  auto_increment  primary key  comment '队伍id',
    name         varchar(255)                        not null comment '队伍名称',
    description  varchar(1024)      default '暂无描述'    null comment '队伍描述',
    avatar_url    varchar(1024)                          null comment '队伍头像',
    max_num        tinyint      default 5                null comment '队伍最大人数',
    team_password varchar(255)                           null comment '加入队伍密码',
    expire_time   datatime                         null comment '队伍过期时间',
    user_id       bigint                                 not null comment '创始人id',
    category  varchar(255)                               not null comment '队伍分类',
    status     tinyint      default 0    not null comment '队伍状态：0-公开 1-私有 2-加密',
    create_time   datetime     default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     tinyint      default 0                 null comment '是否删除'
)
    comment '队伍表';