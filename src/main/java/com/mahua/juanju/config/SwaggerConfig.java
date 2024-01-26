package com.mahua.juanju.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/***
 * 创建Swagger配置
 */
@Configuration
public class SwaggerConfig {
	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("卷聚匹配系统用户系统API")
						.version("1.0")
						.contact(new Contact()
								.email("501847822@qq.com")
								.url("https://github.com/wenyinmahua")
								.name("闻音麻花"))
						.description( "系统简介")
						.termsOfService("https://github.com/wenyinmahua")
						.license(new License().name("Apache 2.0")
								.url("https://github.com/wenyinmahua")));
	}

}