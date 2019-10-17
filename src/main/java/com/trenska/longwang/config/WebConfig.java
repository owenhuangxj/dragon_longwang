package com.trenska.longwang.config;
import org.springframework.web.filter.CorsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.context.annotation.Configuration;
import com.trenska.longwang.interceptor.LoginTimeoutInterceptor;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

/**
 * 2019/4/1
 * 创建人:Owen
 *
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
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
	// CORS:Cross-Origin Resource Sharing 跨站点资源共享
	//解决跨域(vue)
	@Bean
	public CorsFilter corsFilter() {
		CorsConfiguration conf = new CorsConfiguration();
		conf.addAllowedHeader("*");
		conf.addAllowedMethod("*");
		conf.addAllowedOrigin("*");
		conf.setAllowCredentials(true);
		conf.setMaxAge(3600L);
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
		registry.addInterceptor(new LoginTimeoutInterceptor()).addPathPatterns("/**");
	}
}