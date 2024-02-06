package com.mahua.juanju.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mahua.juanju.Exception.BusinessException;
import com.mahua.juanju.common.ErrorCode;
import com.mahua.juanju.constant.TeamStatusEnum;
import com.mahua.juanju.model.User;
import com.mahua.juanju.model.domain.Team;
import com.mahua.juanju.model.domain.UserTeam;
import com.mahua.juanju.service.TeamService;
import com.mahua.juanju.mapper.TeamMapper;
import com.mahua.juanju.service.UserTeamService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

/**
* @author mahua
* @description 针对表【team(队伍表)】的数据库操作Service实现
* @createDate 2024-02-06 17:56:53
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

	@Resource
	private UserTeamService userTeamService;


	@Override
	@Transactional(rollbackFor = Exception.class)
	public long addTeam(Team team, User loginUser) {
		//1. 请求参数是否为空
		if(team == null){
			throw new BusinessException(ErrorCode.NULL_PARAMS);
		}
		//2. 是否登录，未登录不允许创建
		if(loginUser == null){
			throw new BusinessException(ErrorCode.NULL_PARAMS);
		}
		final long userId = loginUser.getId();
		//3. 校验信息
		//   1. 队伍人数 >=1 <=20
		int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
		if (maxNum <=1 ||maxNum >= 20 ){
			throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数不满足要求");
		}
		//   2. 队伍标题 <= 20
		String name = team.getName();
		if ( StringUtils.isBlank(name) || name.length() >= 20){
			throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍名称不满足要求");
		}
		//   3. 描述 <= 512
		String description = team.getDescription();
		if (StringUtils.isBlank(description)){
			team.setDescription("暂无描述");
		}
		if(description.length() > 512){
			throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍描述过长");
		}
		//   4. status 是否为公开、不传默认为公开（0）
		int status = Optional.ofNullable(team.getStatus()).orElse(0);
		TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
		if (statusEnum == null){
			team.setStatus(0);
		}
		//   5. 如果 status 是加密，一定要有密码，且密码 <= 32
		if (TeamStatusEnum.SECRET.equals(statusEnum)){
			String password = team.getTeamPassword();
			if (StringUtils.isBlank(password) || password.length() > 32){
				throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码设置不正确");
			}
		}
		//   6. 超过时间 > 当前时间
		Date expireTime = team.getExpireTime();
		if (new Date().after(expireTime)){
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}

		// 7. 校验用户最多创建 5 个队伍
		// todo 有bug 可能同时创建 100 个队伍
		QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("user_id",userId);
		long hasTeamNum = this.count(queryWrapper);
		if (hasTeamNum >= 5){
			throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户最多创建 5 个队伍");
		}
		//4. 插入队伍信息到队伍表
		team.setId(null);// 数据库设计时相关字段会自增
		team.setUserId(userId);
		boolean result = this.save(team);
		Long teamId = team.getId();
		if( !result || teamId == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR,"创建队伍失败");
		}
		//5. 插入用户 => 队伍关系到关系表
		UserTeam userTeam = new UserTeam();
		userTeam.setUserId(userId);
		userTeam.setTeamId(teamId);
		userTeam.setJoinTime(new Date());

		result = userTeamService.save(userTeam);
		if( !result) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR,"创建队伍失败");
		}
		return team.getId();
	}
}




