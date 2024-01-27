package com.mahua.juanju.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mahua.juanju.Exception.BusinessException;
import com.mahua.juanju.common.BaseResponse;
import com.mahua.juanju.common.ErrorCode;
import com.mahua.juanju.common.ResultUtils;
import com.mahua.juanju.model.User;
import com.mahua.juanju.model.request.UserLoginRequest;
import com.mahua.juanju.model.request.UserRegisterRequest;
import com.mahua.juanju.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.mahua.juanju.constant.UserConstant.ADMIN_ROLE;
import static com.mahua.juanju.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author mahua
 */
@CrossOrigin(origins = {"http://localhost:5173"})
@RestController
@RequestMapping("/user")
@Tag(name = "用户接口")
public class UserController {

	@Resource
	private UserService userService;

	@PostMapping("/register")
	@Operation(summary = "用户注册")
	public BaseResponse<Long> Register(@RequestBody UserRegisterRequest userRegisterRequest){
		//@RequestBody 注解：将前端传来的JSON参数和UserRegisterRequest参数进行绑定，并自动将参数注入到UserRegisterRequest对象中
		if(userRegisterRequest == null){
			throw new BusinessException(ErrorCode.NULL_PARAMS);
		}

		String userAccount = userRegisterRequest.getUserAccount();
		String userPassword = userRegisterRequest.getUserPassword();
		String checkPassword = userRegisterRequest.getCheckPassword();
		String stuId = userRegisterRequest.getStuId();
		if(StringUtils.isAnyBlank( userAccount,userPassword,checkPassword,stuId)){
			throw new BusinessException(ErrorCode.NULL_PARAMS);
		}
		long result = userService.userRegister(userAccount, userPassword, checkPassword,stuId);
		return ResultUtils.success(result);
	}


	@PostMapping("/login")
	@Operation(summary = "用户登录")
	public BaseResponse<User> Login(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
		//@RequestBody 注解：将前端传来的JSON参数和UserRegisterRequest参数进行绑定，并自动将参数注入到UserRegisterRequest对象中
		if(userLoginRequest == null){
			throw new BusinessException(ErrorCode.NULL_PARAMS);
		}
		String userAccount = userLoginRequest.getUserAccount();
		String userPassword = userLoginRequest.getUserPassword();
		if(StringUtils.isAnyBlank( userAccount,userPassword)){
			throw new BusinessException(ErrorCode.NULL_PARAMS);
		}
		User uer = userService.doLogin(userAccount, userPassword,request);
		User safetyUser = userService.getSafetyUser(uer);
		return ResultUtils.success(safetyUser);

	}

	@PostMapping("/logout")
	@Operation(summary = "用户退出登录")
	public BaseResponse<Integer> userLogout( HttpServletRequest request){
		//@RequestBody 注解：将前端传来的JSON参数和UserRegisterRequest参数进行绑定，并自动将参数注入到UserRegisterRequest对象中
		if(request == null){
			throw new BusinessException(ErrorCode.NULL_PARAMS);
		}
		 int result =  userService.logout(request);
		return ResultUtils.success(result);
	}


	@GetMapping("/search")
	@Operation(summary = "用户搜索")
	public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request){
		if(!isAdmin(request)){
			throw new BusinessException(ErrorCode.NO_AUTHORIZED);
		}
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		if(StringUtils.isNotBlank(username)){
			queryWrapper.like("username",username);
		}
		List<User> userList = userService.list(queryWrapper);
		List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
		return ResultUtils.success(list);
//		return userList.stream().map(user -> {
////		拉姆达表达式
//			user.setUserPassword(null);
//			return userService.getSafetyUser(user);
//		}).collect(Collectors.toList());
	}

	@GetMapping("/search/tags")
	@Operation(summary = "根据标签搜索用户")
	public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList){
		if(CollectionUtils.isEmpty(tagNameList)){
			throw new BusinessException(ErrorCode.NULL_PARAMS);
		}
		List<User> userList = userService.searchUsersByTags(tagNameList);
		return ResultUtils.success(userList);
	}

	@PostMapping("/delete")
	@Operation(summary = "用户删除")
	public BaseResponse<Boolean> delete(@RequestBody long id,HttpServletRequest request){
		if(!isAdmin(request)){
			throw new BusinessException(ErrorCode.NO_AUTHORIZED);
		}
		if(id <= 0){
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		boolean result = userService.removeById(id);
		return ResultUtils.success(result);
	}

	@GetMapping("/current")
	@Operation(summary = "获取当前用户信息")
	public BaseResponse<User> getCurrentUser(HttpServletRequest request){
		Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
		User currentUser = (User) userObj;
		if(currentUser == null){
			throw new BusinessException(ErrorCode.NO_LOGIN);
		}
		long userId = currentUser.getId();
		User user = userService.getById(userId);
		User safetyUser = userService.getSafetyUser(user);
		return ResultUtils.success(safetyUser);
	}

	/**
	 * 是否为管理员
	 * @param request
	 * @return
	 */
	public boolean isAdmin(HttpServletRequest request){
		Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
		User user = (User) userObj;
		Integer userRole = user.getUserRole();
		if(user == null || userRole != ADMIN_ROLE){
			return false;
		}
		return true;
	}



}
