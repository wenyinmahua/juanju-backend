package com.mahua.juanju.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mahua.juanju.Exception.BusinessException;
import com.mahua.juanju.common.ErrorCode;
import com.mahua.juanju.model.User;
import com.mahua.juanju.service.UserService;
import com.mahua.juanju.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.mahua.juanju.constant.UserConstant.USER_LOGIN_STATE;

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
	public long userRegister(String userAccount, String userPassword, String checkPassword,String stuId) {
		//1.校验
		if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,stuId)){
			throw new BusinessException(ErrorCode.NULL_PARAMS,"参数为空");
		}
		if (userAccount.length() < 4){
			throw new BusinessException(ErrorCode.USER_ACCOUNT_ERROR,"账号长度不能小于4");
		}
		if (userPassword.length() < 8 || !userPassword.equals(checkPassword)){
			throw new BusinessException(ErrorCode.USER_PASSWORD_ERROR,"密码长度不能小于8");
		}
		if (stuId.length() != 10){
			throw new BusinessException(ErrorCode.PARAMS_ERROR,"学号长度必须为10");
		}
		//账号不能含有特殊字符
		String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
		Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
		if(matcher.find()){
			throw new BusinessException(ErrorCode.USER_ACCOUNT_ERROR,"账号不能含有特殊字符");
		}

		//账号不能重复
		//创建一个QueryWrapper对象，该对象用于封装查询条件，用于查询User实体类的相关信息。
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		//判断插入的userAccount在数据库中是否已有
		queryWrapper.eq("user_account",userAccount);
		long count = userMapper.selectCount(queryWrapper);
		if(count > 0){
			throw new BusinessException(ErrorCode.USER_ACCOUNT_EXIST,"账号已存在");
		}
		//学号不能重复
		queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("stu_id",stuId);
		count = userMapper.selectCount(queryWrapper);
		if(count > 0){
			throw new BusinessException(ErrorCode.STU_ID_EXIST,"学号已存在");
		}
		//2.加密
		String encryptPassword = DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());
		//3.插入数据
		User user = new User();
		user.setUserAccount(userAccount);
		user.setUserPassword(encryptPassword);
		user.setStuId(stuId);
		userMapper.insert(user);
		log.info("用户注册成功"+user.getId());
		return user.getId() ;
	}

	@Override
	public User doLogin(String userAccount, String userPassword, HttpServletRequest request) {
		//1.校验
		if(StringUtils.isAnyBlank(userAccount,userPassword)){
			throw new BusinessException(ErrorCode.NULL_PARAMS, "参数为空");
		}
		if (userAccount.length() < 4){
			throw new BusinessException(ErrorCode.USER_ACCOUNT_ERROR,"账号长度不能小于4");
		}
		if (userPassword.length() < 8){
			throw new BusinessException(ErrorCode.USER_PASSWORD_ERROR,"密码长度不能小于8");
		}
		//账号不能含有特殊字符
		String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
		Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
		if(matcher.find()){
			throw new BusinessException(ErrorCode.USER_ACCOUNT_ERROR,"账号不能含有特殊字符");
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
			throw new BusinessException(ErrorCode.USER_ACCOUNT_ERROR,"账号或密码错误");
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
			throw new BusinessException(ErrorCode.USER_NOT_EXIST,"用户不存在");
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
		safetyUser.setTags(user.getTags());

		return safetyUser;
	}

	/**
	 * 退出登录
	 *
	 * @param request 请求
	 * @return
	 */
	@Override
	public int logout(HttpServletRequest request) {
		request.getSession().removeAttribute(USER_LOGIN_STATE);
		return 1;
	}

	/**
	 * 在内存中查询用户
	 *
	 * @param tagNameList 查询的用户需要具有的标签
	 * @return
	 */

	@Override
	public List<User> searchUsersByTags(List<String> tagNameList){
		if (CollectionUtils.isEmpty(tagNameList)){
			throw new BusinessException(ErrorCode.NULL_PARAMS);
		}
		//SQL查询
		/*QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		for (String tageName : tagNameList) {
			queryWrapper = queryWrapper.like("tags",tageName);
		}
		List<User> userList = userMapper.selectList(queryWrapper);
		userList.forEach(user ->{
			getSafetyUser(user);
		});
		userList.forEach(this::getSafetyUser);
		return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
*/
		//内存查询
		//1.查询所有的用户
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		for (String tageName : tagNameList) {
			queryWrapper = queryWrapper.like("tags",tageName);
		}
		List<User> userList = userMapper.selectList(queryWrapper);
		//2.在内存中判断是否包含要求的标签，需要将Json转换为java对象（String）格式,反序列化
		Gson gson = new Gson();
		/*for (User user : userList) {
			String tagStr = user.getTags();
			Set<String> tempTagNameSet = gson.fromJson(tagStr,new TypeToken<Set<String>>(){}.getType());
			for (String tagName : tempTagNameSet) {
				if (!tempTagNameSet.contains(tagName)){
					return false;
				}
			}
			return true;
		}*/
		return userList.stream().filter(user ->{
			String tagStr = user.getTags();
			if (StringUtils.isEmpty(tagStr)){
				return false;
			}
			//Json 转换为String类型
			Set<String> tempTagNameSet = gson.fromJson(tagStr,new TypeToken<Set<String>>(){}.getType());
			//如果取得的结果为空，那么就将其变为一个HashSet对象，类似于if
			tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
			for (String tagName : tempTagNameSet) {
				if (!tempTagNameSet.contains(tagName)){
					return false;
				}
			}
			return true;
		}).map(this::getSafetyUser).collect(Collectors.toList());


	}

	/**
	 * 根据用户搜索标签（SQL查询版）
	 *
	 * @param tagNameList 用户要拥有的标签
	 * @return
	 */
	@Deprecated//方法已过期的注解
	private List<User> searchUsersByTagsSQL(List<String> tagNameList) {
		if (CollectionUtils.isEmpty(tagNameList)) {
			throw new BusinessException(ErrorCode.NULL_PARAMS);
		}
		//SQL查询
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		for (String tageName : tagNameList) {
			queryWrapper = queryWrapper.like("tags",tageName);
		}
		List<User> userList = userMapper.selectList(queryWrapper);
//		userList.forEach(user ->{
//			getSafetyUser(user);
//		});
//		===>
//		userList.forEach(this::getSafetyUser);
		return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
	}

}




