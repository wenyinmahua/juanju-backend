package com.mahua.juanju.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mahua.juanju.model.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mahua.juanju.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户服务
 *
* @author mahua
*
*/
public interface  UserService extends IService<User> {

	/**
	 * 用户注册
	 * @param userAccount 用户账号
	 * @param userPassword 用户密码
	 * @param checkPassword 用户校验密码
	 * @return 新用户id
	 */
	long userRegister(String userAccount, String userPassword, String checkPassword,String sutId);

	/**
	 *  用户登录
	 * @param userAccount 登录账号
	 * @param userPassword  登录密码
	 * @return 脱敏后的用户信息
	 */
	User doLogin(String userAccount, String userPassword, HttpServletRequest request);

	/**
	 *  用户脱敏
	 * @param user 需要脱敏的用户
	 * @return 脱敏后的用户
	 */
	User getSafetyUser(User user);

	/**
	 * 用户注销
	 * @param request
	 */
	int logout(HttpServletRequest request);

	IPage<User> searchUsersByTags(Long pageSize, Long pageNum, List<String> tagNameList);

	int updateUser(User user,User loginUser);

	User getLoginUser(HttpServletRequest request);

	boolean isAdmin(HttpServletRequest request);

	boolean isAdmin(User loginUser);

	/**
	 * 匹配用户
	 * @param num
	 * @param loginUser
	 * @return
	 */
	List<User> matchUsers(long num, User loginUser);

	Page<UserVO> recommend(long pageNum);
}
