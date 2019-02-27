
package com.tmall.config;

import com.tmall.interceptor.LoginInterceptor;
import com.tmall.interceptor.OtherInterceptor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.servlet.config.annotation.*;


//必须在这里进行配置
@Configuration
class WebMvcConfigurerTmp implements WebMvcConfigurer {

	@Bean
	public OtherInterceptor getOtherIntercepter() {
		return new OtherInterceptor();
	}
	@Bean
	public LoginInterceptor getLoginIntercepter() {
		return new LoginInterceptor();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry){
		registry.addInterceptor(getOtherIntercepter())
				.addPathPatterns("/**");
		registry.addInterceptor(getLoginIntercepter())
				.addPathPatterns("/**");
	}

	//这里只是将ajax的拿到了。
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		//所有请求都允许跨域
		registry.addMapping("/**")
				.allowedOrigins("*")
				.allowedMethods("*")
				.allowedHeaders("*");
	}

	//增加静态资源的映射，因为实现这个接口的原因，原先在配置文件写的静态资源无效
//	@Override
//	public void addResourceHandlers(ResourceHandlerRegistry registry) {
////		registry.addResourceHandler("/webapp/**").addResourceLocations("classpath:/webapp");
//		registry.addResourceHandler("/**").addResourceLocations("classpath:/");
//
//	}
}


