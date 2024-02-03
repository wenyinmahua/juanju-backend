package com.mahua.juanju.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mahua.juanju.model.User;
import com.mahua.juanju.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 缓存预热任务
 */
@Component
@Slf4j
public class preCacheJob {

	@Resource
	private UserService userService;

	@Resource
	private RedisTemplate<String,Object> redisTemplate;


	private List<Long> mainUserList = Arrays.asList(1L);
	//每天00 : 00点执行，预热推荐缓存
	@Scheduled(cron = "0 0 0 * * ?")
	public void doCacheRecommendUser(){
		for (Long id : mainUserList) {
			//预热推荐用户,只加载该用户的前64条数据
			for(int pageNum = 1;pageNum <= 8; pageNum++){
				String redisKey = String.format("juanju:user:recommend:%s:%s",id,pageNum);
				ValueOperations<String,Object> valueOperations = redisTemplate.opsForValue();
				Page<User> userPage = (Page<User>) valueOperations.get(redisKey);

				//无缓存，查数据库
				QueryWrapper<User> queryWrapper = new QueryWrapper<>();
				Page<User> userPageList = userService.page(new Page<>(pageNum,8 ),queryWrapper);
				List<User> userList = userPageList.getRecords();
				userList = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
				userPageList.setRecords(userList);
				int total = Math.min((int) userPageList.getTotal(), 64);
				userPageList.setTotal(total);
				try {
					valueOperations.set(redisKey,userPageList,300000, TimeUnit.MILLISECONDS);
				} catch (Exception e) {
					log.error("redis set key error",e);
				}
			}

		}

	}

}
