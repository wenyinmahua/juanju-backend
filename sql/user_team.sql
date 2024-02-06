create table user_team
(
    id    bigint  auto_increment  primary key  comment '用户_队伍id',
    user_id    bigint   comment '用户id',
    team_id    bigint   comment '队伍id',
    join_time  datetime     default CURRENT_TIMESTAMP null comment '加入时间',
    create_time   datetime     default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     tinyint      default 0                 null comment '是否删除'
)
    comment '用户_队伍表';