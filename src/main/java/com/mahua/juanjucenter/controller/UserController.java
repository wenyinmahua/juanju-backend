package com.mahua.juanjucenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mahua.juanjucenter.model.User;
import com.mahua.juanjucenter.model.request.UserLoginRequest;
import com.mahua.juanjucenter.model.request.UserRegisterRequest;
import com.mahua.juanjucenter.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.mahua.juanjucenter.constant.UserConstant.ADMIN_ROLE;
import static com.mahua.juanjucenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author mahua
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Resource
	private UserService userService;

	@PostMapping("/register")
	public Long Register(@RequestBody UserRegisterRequest userRegisterRequest){
		//@RequestBody 注解：将前端传来的JSON参数和UserRegisterRequest参数进行绑定，并自动将参数注入到UserRegisterRequest对象中
		if(userRegisterRequest == null){
			return null;
		}

		String userAccount = userRegisterRequest.getUserAccount();
		String userPassword = userRegisterRequest.getUserPassword();
		String checkPassword = userRegisterRequest.getCheckPassword();
		String stuId = userRegisterRequest.getStuId();
		if(StringUtils.isAnyBlank( userAccount,userPassword,checkPassword,stuId)){
			return null;
		}
		return userService.userRegister(userAccount, userPassword, checkPassword,stuId);

	}


	@PostMapping("/login")
	public User Login(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
		//@RequestBody 注解：将前端传来的JSON参数和UserRegisterRequest参数进行绑定，并自动将参数注入到UserRegisterRequest对象中
		if(userLoginRequest == null){
			return null;
		}
		String userAccount = userLoginRequest.getUserAccount();
		String userPassword = userLoginRequest.getUserPassword();
		if(StringUtils.isAnyBlank( userAccount,userPassword)){
			return null;
		}
		return userService.doLogin(userAccount, userPassword,request);
	}

	@PostMapping("/logout")
	public Integer userLogout( HttpServletRequest request){
		//@RequestBody 注解：将前端传来的JSON参数和UserRegisterRequest参数进行绑定，并自动将参数注入到UserRegisterRequest对象中
		if(request == null){
			return null;
		}
		 return userService.logout(request);
	}


	@GetMapping("/search")
	public List<User> searchUsers(String username, HttpServletRequest request){
		if(!isAdmin(request)){
			return new ArrayList<>();
		}
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		if(StringUtils.isNotBlank(username)){
			queryWrapper.like("username",username);
		}
		List<User> userList = userService.list(queryWrapper);
		return userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
//		return userList.stream().map(user -> {
////		拉姆达表达式
//			user.setUserPassword(null);
//			return userService.getSafetyUser(user);
//		}).collect(Collectors.toList());
	}

	@PostMapping("/delete")
	public boolean delete(@RequestBody long id,HttpServletRequest request){
		if(!isAdmin(request)){
		 	return false;
		}
		if(id <= 0){
			return false;
		}
		return userService.removeById(id);
	}

	@GetMapping("/current")
	public User getCurrentUser(HttpServletRequest request){
		Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
		User currentUser = (User) userObj;
		if(currentUser == null){
			return null;
		}
		long userId = currentUser.getId();
		//todo 校验用户是否合法
		User user = userService.getById(userId);
		return userService.getSafetyUser(user);
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
