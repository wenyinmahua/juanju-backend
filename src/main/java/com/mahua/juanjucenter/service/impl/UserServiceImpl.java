package com.mahua.juanjucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mahua.juanjucenter.model.User;
import com.mahua.juanjucenter.service.UserService;
import com.mahua.juanjucenter.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mahua.juanjucenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
 * @author mahua
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{
	@Resource
	private UserMapper userMapper;
	/**
	 * 盐值，混淆密码
	 */
	private static final String SALT = "mahua";


	@Override
	public long userRegister(String userAccount, String userPassword, String checkPassword) {
		//1.校验
		if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
			//TODO 修改为自定义异常
			return -1;
		}
		if (userAccount.length() < 4){
			return -1;
		}
		if (userPassword.length() < 8 || !userPassword.equals(checkPassword)){
			return -1;
		}
		//账号不能含有特殊字符
		String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
		Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
		if(matcher.find()){
			return -1;
		}

		//账号不能重复
		//创建一个QueryWrapper对象，该对象用于封装查询条件，用于查询User实体类的相关信息。
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		//判断插入的userAccount在数据库中是否已有
		queryWrapper.eq("user_account",userAccount);
		long count = userMapper.selectCount(queryWrapper);
		if(count > 0){
			return -1;
		}
		//2.加密
		String encryptPassword = DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());
		//3.插入数据
		User user = new User();
		user.setUserAccount(userAccount);
		user.setUserPassword(encryptPassword);
		userMapper.insert(user);
		log.info("用户注册成功"+user.getId());
		return user.getId() ;
	}

	@Override
	public User doLogin(String userAccount, String userPassword, HttpServletRequest request) {
		//1.校验
		if(StringUtils.isAnyBlank(userAccount,userPassword)){
			return null;
		}
		if (userAccount.length() < 4){
			return null;
		}
		if (userPassword.length() < 8){
			return null;
		}
		//账号不能含有特殊字符
		String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
		Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
		if(matcher.find()){
			return null;
		}

		//2.加密
		userPassword = DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());
		//账号不能重复
		//创建一个QueryWrapper对象，该对象用于封装查询条件，用于查询User实体类的相关信息。
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		//判断插入的userAccount在数据库中是否已有
		queryWrapper.eq("user_account",userAccount);
		queryWrapper.eq("user_password",userPassword);
		User user = userMapper.selectOne(queryWrapper);
		//用户不存在
		if(user == null){
			log.info("user login failed，userAccount cannot match userPassword");
			return null;
		}

		//3.用户脱敏
		User safetyUser = getSafetyUser(user);
		//4.记录用户的登录态
		request.getSession().setAttribute(USER_LOGIN_STATE,user);

		return safetyUser;
	}


	@Override
	public User getSafetyUser(User user){
		if (user == null){
			return null;
		}
		User safetyUser = new User();
		safetyUser.setId(user.getId());
		safetyUser.setUsername(user.getUsername());
		safetyUser.setUserAccount(user.getUserAccount());
		safetyUser.setAvatarUrl(user.getAvatarUrl());
		safetyUser.setGender(user.getGender());
		safetyUser.setPhone(user.getPhone());
		safetyUser.setEmail(user.getEmail());
		safetyUser.setMajor(user.getMajor());
		safetyUser.setUserStatus(user.getUserStatus());
		safetyUser.setCreateTime(user.getCreateTime());
		safetyUser.setUserRole(user.getUserRole());
		safetyUser.setStuId(user.getStuId());

		return safetyUser;
	}

	@Override
	public int logout(HttpServletRequest request) {
		request.getSession().removeAttribute(USER_LOGIN_STATE);
		return 1;
	}
}




