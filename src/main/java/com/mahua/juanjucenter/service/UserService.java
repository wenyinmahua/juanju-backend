package com.mahua.juanjucenter.service;

import com.mahua.juanjucenter.model.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户服务
 *
* @author mahua
*
*/
public interface UserService extends IService<User> {

	/**
	 * 用户注册
	 * @param userAccount 用户账号
	 * @param userPassword 用户密码
	 * @param checkPassword 用户校验密码
	 * @return 新用户id
	 */
	long userRegister(String userAccount, String userPassword, String checkPassword);

	/**
	 *  用户登录
	 * @param userAccount 登录账号
	 * @param userPassword  登录密码
	 * @return 脱敏后的用户信息
	 */
	User doLogin(String userAccount, String userPassword, HttpServletRequest request);
}
