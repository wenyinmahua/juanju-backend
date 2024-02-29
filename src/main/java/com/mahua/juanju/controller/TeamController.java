package com.mahua.juanju.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mahua.juanju.Exception.BusinessException;
import com.mahua.juanju.common.BaseResponse;
import com.mahua.juanju.common.ErrorCode;
import com.mahua.juanju.common.ResultUtils;
import com.mahua.juanju.model.User;
import com.mahua.juanju.model.domain.Team;
import com.mahua.juanju.model.dto.TeamQuery;
import com.mahua.juanju.model.request.TeamJoinRequest;
import com.mahua.juanju.model.request.TeamRequest;
import com.mahua.juanju.model.request.TeamUpdateRequest;
import com.mahua.juanju.model.vo.TeamUserVO;
import com.mahua.juanju.model.vo.UserVO;
import com.mahua.juanju.service.TeamService;
import com.mahua.juanju.service.UserService;
import com.mahua.juanju.service.UserTeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name="队伍接口")
@RestController
@RequestMapping("/team")
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:8000"},allowCredentials = "true")
public class TeamController {

	@Resource
	private UserService userService;

	@Resource
	private TeamService teamService;

	@Resource
	private UserTeamService userTeamService;

	@Operation(summary = "创建队伍")
	@PostMapping("/add")
	public BaseResponse<Long> createTeam (@RequestBody TeamRequest teamRequest, HttpServletRequest request){
		if(teamRequest == null){
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Team team = new Team();
		BeanUtils.copyProperties(teamRequest,team);
		User loginUser = userService.getLoginUser(request);
		long teamId = teamService.addTeam(team,loginUser);
		return ResultUtils.success(teamId);
	}

	@Operation(summary = "更新队伍")
	@PutMapping("/update")
	public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest,HttpServletRequest request){
		User loginUser = userService.getLoginUser(request);
		if (teamUpdateRequest == null){
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		boolean result = teamService.updateTeam(teamUpdateRequest,loginUser);
		if (!result){
			throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新失败");
		}
		return ResultUtils.success(true);
	}

	/**
	 * 获得队伍详细描述
	 * @param id
	 * @return
	 */
	@Operation(summary = "获得队伍")
	@GetMapping("/get")
	public BaseResponse<Team> getTeamListById(long id){
		if (id <= 0 ){
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Team team = teamService.getById(id);
		if (team == null){
			throw new BusinessException(ErrorCode.SYSTEM_ERROR,"获取队伍列表错误");
		}
		return ResultUtils.success(team);
	}

	@Operation(summary = "分页分类查询队伍")
	@GetMapping("/list")
	public BaseResponse<Page<TeamUserVO>> teamList(@ParameterObject TeamQuery teamQuery, HttpServletRequest request){
		if (teamQuery == null ){
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		boolean isAdmin = userService.isAdmin(request);
		Page<TeamUserVO> teamList = teamService.listTeams(teamQuery,isAdmin);
		if (teamList == null){
			throw new BusinessException(ErrorCode.SYSTEM_ERROR,"获取队伍列表错误");
		}
		return ResultUtils.success(teamList);
	}

	@Operation(summary = "加入队伍")
	@PostMapping("/join")
	public BaseResponse<Boolean> joinTeam (@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request){
		User loginUser = userService.getLoginUser(request);
		boolean result = teamService.joinTeam(teamJoinRequest,loginUser);
		return ResultUtils.success(result);
	}

	@Operation(summary = "退出队伍")
	@PostMapping("/quit")
	public BaseResponse quitTeam(Long teamId,HttpServletRequest request){
		teamService.quitTeam(teamId,request);
		return ResultUtils.success("退出队伍成功");
	}


	@Operation(summary = "删除队伍")
	@PostMapping("/delete")
	public BaseResponse<Boolean> deleteTeam(Long id){
		if(id == null || id <= 0){
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		boolean result = teamService.removeById(id);
		if (!result){
			throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
		}
		return ResultUtils.success(true);
	}

}
