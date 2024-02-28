package com.mahua.juanju.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mahua.juanju.Exception.BusinessException;
import com.mahua.juanju.common.ErrorCode;
import com.mahua.juanju.constant.TeamStatusEnum;
import com.mahua.juanju.model.User;
import com.mahua.juanju.model.domain.Team;
import com.mahua.juanju.model.domain.UserTeam;
import com.mahua.juanju.model.dto.TeamQuery;
import com.mahua.juanju.model.request.TeamJoinRequest;
import com.mahua.juanju.model.request.TeamUpdateRequest;
import com.mahua.juanju.model.vo.TeamUserVO;
import com.mahua.juanju.model.vo.UserVO;
import com.mahua.juanju.service.TeamService;
import com.mahua.juanju.mapper.TeamMapper;
import com.mahua.juanju.service.UserService;
import com.mahua.juanju.service.UserTeamService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
* @author mahua
* @description 针对表【team(队伍表)】的数据库操作Service实现
* @createDate 2024-02-06 17:56:53
*/
@Slf4j
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

	@Resource
	private UserTeamService userTeamService;

	@Resource
	private UserService userService;

	@Resource
	private TeamMapper teamMapper;

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
		if (expireTime != null){
			if (new Date().after(expireTime)){
				throw new BusinessException(ErrorCode.PARAMS_ERROR);
			}
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

	@Override
	public Page<TeamUserVO> listTeams(TeamQuery teamQuery,boolean isAdmin) {
		QueryWrapper<Team> queryWrapper = new QueryWrapper<>();

		if (teamQuery != null){

			Long userId = teamQuery.getUserId();
			if(userId != null && userId > 0){
				queryWrapper.eq("user_id",userId);
			}

			String searchText = teamQuery.getSearchText();
			if (StringUtils.isNotBlank(searchText)){
				queryWrapper.and(qw -> qw.like("name",searchText).or().like("description",searchText));
			}

			String name = teamQuery.getName();
			if(StringUtils.isNotBlank(name)){
				queryWrapper.eq("name",name);
			}

			String description = teamQuery.getDescription();
			if (StringUtils.isNotBlank(description)){
				queryWrapper.like("description",description);
			}
			Integer status = teamQuery.getStatus();
			TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
			if (statusEnum == null){
				statusEnum = TeamStatusEnum.PUBLIC;
			}
			if(!isAdmin && !statusEnum.equals(TeamStatusEnum.PUBLIC)){
				throw new BusinessException(ErrorCode.NO_AUTHORIZED);
			}
			queryWrapper.eq("status",statusEnum.getValue());

			Integer maxNum = teamQuery.getMaxNum();;
			if (maxNum != null && maxNum > 1){
				queryWrapper.eq("max_num",maxNum);
			}

		}
		queryWrapper.and(qw -> qw.gt("expire_time", new Date()).or().isNull("expire_time"));

		List<Team> teamList = this.list(queryWrapper);
		if (CollectionUtils.isEmpty(teamList)){
			return new Page<>();
		}
		List<TeamUserVO> teamUserVOList = new ArrayList<>();
		// 关联查询创建人的用户信息

		for(Team team : teamList){
			Long userId = team.getUserId();
			if (userId == null){
				continue;
			}
			User user = userService.getById(userId);
			TeamUserVO teamUserVO = new TeamUserVO();
			BeanUtils.copyProperties(team,teamUserVO);
			// 用户脱敏
			if(user != null){
				UserVO userVO = new UserVO();
				BeanUtils.copyProperties(user,userVO);
				teamUserVO.setCreateUser(userVO);
			}

			teamUserVOList.add(teamUserVO);
		}
		Page<TeamUserVO> teamUserVOPage = new Page<>();
		teamUserVOPage.setRecords(teamUserVOList);
		teamUserVOPage.setTotal(teamUserVOList.size());

		return teamUserVOPage;
	}

	@Override
	public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
		if (teamUpdateRequest == null){
			throw new BusinessException(ErrorCode.NULL_PARAMS);
		}
		Long teamId = teamUpdateRequest.getId();
		if (teamId == null || teamId <= 0){
			throw new BusinessException(ErrorCode.NULL_PARAMS);
		}
		Team olderTeam = this.getById(teamId);
		if (olderTeam == null){
			throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在");
		}
		if (olderTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)){
			throw new BusinessException(ErrorCode.NO_AUTHORIZED);
		}

		if (teamNotUpdate(olderTeam,teamUpdateRequest)){
			throw new BusinessException(ErrorCode.PARAMS_ERROR,"未更新数据");
		}

		TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
		if (statusEnum.equals(TeamStatusEnum.SECRET)){
			if (StringUtils.isBlank(teamUpdateRequest.getTeamPassword())){
				throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码不能为空");
			}
		}

		Team updateTeam = new Team();
		BeanUtils.copyProperties(teamUpdateRequest,updateTeam);
		return this.updateById(updateTeam);
	}

	@Override
	public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
		if (teamJoinRequest == null){
			throw new BusinessException(ErrorCode.NULL_PARAMS);
		}

		Long teamId = teamJoinRequest.getTeamId();
		if (teamId <= 0 || teamId == null){
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Team team = this.getById(teamId);
		if (team == null){
			throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在");
		}
		if (team.getExpireTime() != null && team.getExpireTime().before(new Date())){
			throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍已过期");
		}
		Integer status = team.getStatus();
		TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
		if (statusEnum.equals(TeamStatusEnum.PRIVATE)){
			throw new BusinessException(ErrorCode.PARAMS_ERROR,"禁止加入私有队伍");
		}
		String password = teamJoinRequest.getTeamPassword();
		if (statusEnum.equals(TeamStatusEnum.SECRET)){
			if (StringUtils.isBlank(password) || !password.equals(team.getTeamPassword())){
				throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");
			}
		}
		// 用户已加入队伍数量
		long userId = loginUser.getId();
		QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("user_id",userId);
		long hasUserJoinNum = userTeamService.count(queryWrapper);
		if (hasUserJoinNum > 5){
			throw new BusinessException(ErrorCode.PARAMS_ERROR,"最多只能创建和加入五个队伍");
		}
		// 不能重复加入队伍
		queryWrapper.eq("team_id",teamId);
		long userJoinTeam = userTeamService.count(queryWrapper);
		if (userJoinTeam > 0){
			throw new BusinessException(ErrorCode.PARAMS_ERROR,"已加入该队伍");
		}
		// 已加入队伍的人数
		queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("team_id",teamId);
		long numberNum = userTeamService.count(queryWrapper);
		if (numberNum >= team.getMaxNum()){
			throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数已达到上限");
		}
		UserTeam userTeam = new UserTeam();
		userTeam.setUserId(userId);
		userTeam.setTeamId(teamId);
		userTeam.setJoinTime(new Date());
		userTeamService.save(userTeam);
		return true;
	}


	private boolean teamNotUpdate(Team olderTeam, TeamUpdateRequest teamUpdateRequest){
		return olderTeam.getName().equals(teamUpdateRequest.getName()) &&
				olderTeam.getTeamPassword().equals(teamUpdateRequest.getTeamPassword()) &&
				olderTeam.getAvatarUrl().equals(teamUpdateRequest.getAvatarUrl()) &&
				olderTeam.getDescription().equals(teamUpdateRequest.getDescription()) &&
				olderTeam.getCategory().equals(teamUpdateRequest.getCategory()) &&
//				olderTeam.getMaxNum().equals(teamUpdateRequest.getMaxNum()) &&
				olderTeam.getStatus().equals(teamUpdateRequest.getStatus()) &&
				olderTeam.getExpireTime().equals(teamUpdateRequest.getExpireTime())
				;
	}


}




