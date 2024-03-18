package com.mahua.juanju.constant;

public interface UserConstant {
	/**
	 * 用户登录态键
	 *
	 * @author mahua
	 */
	String USER_LOGIN_STATUS = "userLoginStatus";

	//-------权限

	String USER_LOGIN_KEY = "juanju:user:login:";
	int USER_LOGIN_TIME = 60*60*24*30;

	/**
	 * 默认权限
	 */
	int DEFAULT_ROLE = 0;

	/**
	 * 管理员权限
	 */
	int ADMIN_ROLE = 1;
}
