-- auto-generated definition
create table user
(
    id            bigint auto_increment comment '用户id'
        primary key,
    username      varchar(255)                       null comment '用户昵称',
    user_account  varchar(255)                       null comment '账号',
    avatar_url    varchar(1024)                      null comment '用户头像',
    gender        tinyint  default 0                 null comment '性别',
    user_password varchar(255)                       not null comment '密码',
    phone         varchar(128)                       null comment '电话',
    email         varchar(255)                       null comment '邮箱',
    major         varchar(255)                       null comment '专业',
    user_status   int      default 0                 null comment '状态：0-正常，1-禁用',
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     tinyint  default 0                 null comment '是否删除',
    user_role     tinyint  default 0                 null comment '用户角色：user/admin',
    stu_id        varchar(255)                       not null comment '学号',
    tages         varchar(1024)                      null comment '标签列表',
    constraint stu_id
        unique (stu_id),
    constraint stu_id_2
        unique (stu_id)
)
    comment '用户表';

