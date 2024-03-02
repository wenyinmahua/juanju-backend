package com.mahua.juanju.config;


import com.mahua.juanju.properties.AliOssProperties;
import com.mahua.juanju.utils.AliOSSUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration//声明是配置文件
@Slf4j//日志
public class OssConfiguration {
	@Bean//创建一个Bean
	@ConditionalOnMissingBean//容器里面要是有Bean，那么就不需要创建一个新的bean
	public AliOSSUtil aliOssUtil(AliOssProperties aliOssProperties){
		log.info("开始创建阿里云文件上传工具{}",aliOssProperties);
		return new AliOSSUtil(aliOssProperties.getEndpoint(),
				aliOssProperties.getAccessKeyId(),
				aliOssProperties.getAccessKeySecret(),
				aliOssProperties.getBucketName());
	}
}