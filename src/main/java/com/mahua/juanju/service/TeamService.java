package com.mahua.juanju.service;

import com.mahua.juanju.model.User;
import com.mahua.juanju.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author mahua
* @description 针对表【team(队伍表)】的数据库操作Service
* @createDate 2024-02-06 17:56:53
*/
public interface TeamService extends IService<Team> {

	long addTeam(Team team, User loginUser);
}
