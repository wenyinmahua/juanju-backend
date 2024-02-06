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
import com.mahua.juanju.model.request.TeamRequest;
import com.mahua.juanju.service.TeamService;
import com.mahua.juanju.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

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

//	@Operation(summary = "创建队伍")
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
	@Operation(summary = "更新队伍")
	@PutMapping("/update")
	public BaseResponse<Boolean> updateTeam(@RequestBody Team team){
		if (team == null){
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		boolean result = teamService.updateById(team);
		if (!result){
			throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新失败");
		}
		return ResultUtils.success(true);
	}
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
	public BaseResponse<Page<Team>> teamList(@RequestBody TeamQuery teamQuery){
		if (teamQuery == null ){
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		long pageSize = teamQuery.getPageSize();
		long pageNum = teamQuery.getPageNum();
		Team team = new Team();
		BeanUtils.copyProperties(teamQuery,team);
		QueryWrapper<Team> queryWrapper = new QueryWrapper(team);
		Page<Team> teamList = teamService.page(new Page(pageNum,pageSize),queryWrapper);
		if (teamList == null){
			throw new BusinessException(ErrorCode.SYSTEM_ERROR,"获取队伍列表错误");
		}
		return ResultUtils.success(teamList);
	}


}
