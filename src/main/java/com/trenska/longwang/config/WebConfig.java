package com.trenska.longwang.config;
import com.trenska.longwang.converters.StringToBrandConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.filter.CorsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.context.annotation.Configuration;
import com.trenska.longwang.interceptor.UserTokenInvalidCheckInterceptor;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

/**
 * 2019/4/1
 * 创建人:Owen
 * WebMvcConfigurer的继承类要生效需要@EnableWebMvc注解或者EnableWebMvcConfiguration类被加载
 * 	①、EnableWebMvcConfiguration被WebMvcAutoConfigurationAdapter通过@Import注解引入
 *	②、WebMvcAutoConfigurationAdapter是由@Configuration注解的方式被引入
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
	@Value("${check.login.close:true}")
	private boolean closeLoginCheck;

	/**
	 * 处理无法访问swagger-ui.html
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("swagger-ui.html")
				.addResourceLocations("classpath:/META-INF/resources/");
		registry.addResourceHandler("/webjars/**")
				.addResourceLocations("classpath:/META-INF/resources/webjars/");
	}
	//解决跨域(vue)
	// CORS:Cross-Origin Resource Sharing 跨站点资源共享
	@Bean
	public CorsFilter corsFilter() {
		CorsConfiguration conf = new CorsConfiguration();
		conf.addAllowedHeader("*");
		conf.addAllowedMethod("*");
		conf.addAllowedOrigin("*");
		conf.setAllowCredentials(true);
		conf.setMaxAge(3600L); // 一个小时
		conf.addExposedHeader("set-cookie");
		conf.addExposedHeader("access-control-allow-headers");
		conf.addExposedHeader("access-control-allow-methods");
		conf.addExposedHeader("access-control-allow-origin");
		conf.addExposedHeader("access-control-max-age");
		conf.addExposedHeader("X-Frame-Options");
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", conf); // 对接口配置跨域设置
		return new CorsFilter(source);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new UserTokenInvalidCheckInterceptor(closeLoginCheck)).addPathPatterns("/**").excludePathPatterns(
				"/*/login", "/*/logout", "/error");
	}

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new StringToBrandConverter());
	}
}