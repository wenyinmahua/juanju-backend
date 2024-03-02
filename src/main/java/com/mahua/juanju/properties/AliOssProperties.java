package com.mahua.juanju.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 该类文件存放的是配置信息的对象
 */
@Component
@ConfigurationProperties(prefix = "juanju.alioss")
@Data
public class AliOssProperties {

	private String endpoint;
	private String accessKeyId;
	private String accessKeySecret;
	private String bucketName;

}
