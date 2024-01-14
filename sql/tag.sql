create table tag
(
    id          bigint auto_increment comment 'id'
        primary key,
    tag_name    varchar(255)                       null comment '标签名称',
    user_id     bigint                             null comment '用户id',
    parent_id   bigint                             null comment '父级id',
    is_parent   tinyint                            null comment '是否为父级:0不是父标签，1是父标签',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint  default 0                 not null comment '是否删除',
    constraint idx_userId
        unique (user_id),
    constraint uniIdx_tageName
        unique (tag_name)
)
    comment '标签表';

