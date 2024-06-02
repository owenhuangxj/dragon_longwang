package com.trenska.longwang.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trenska.longwang.constant.DragonConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
@SuppressWarnings("all")
@Slf4j
@Configuration
//@ConfigurationProperties(prefix = "spring.redis")
public class RedisConfig extends CachingConfigurerSupport {

	private final static String REGISTRY_KEY = "com.trenska.dragon";
//	@Value("${spring.redis.host}")
	private String host;
//	@Value("${spring.redis.port}")
	private int port;
	@Value("${spring.redis.timeout}")
	private int timeout;

	/**
	 * 配置RedisTemplate:
	 * 	将对象的key和value都是以字符串的方式存储在redis中
	 * 主要是缓存简单数据类型的时候使用
	 * 	@Bean的autowireCandidate属性设置是否将该Bean作为其它Bean依赖注入的对象，默认为true,
	 * 	如果有其它与此类型相同的Bean在容器中时，为了不影响该类型Bean的自动注入，设置此属性为false
	 */
	@ConditionalOnMissingBean(name = DragonConstant.REDIS_TEMPLATE_NAME)
	@Bean(name = DragonConstant.REDIS_TEMPLATE_NAME, autowireCandidate = false)
	public RedisTemplate<String, String> redisTemplateIfMiss(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
		redisTemplate.setKeySerializer(stringRedisSerializer);
		redisTemplate.setValueSerializer(stringRedisSerializer);
		return redisTemplate;
}

	/**
	 * 配置操作JSON格式数据的RedisTemplate:
	 * 	将对象转成json字符串存储在redis中时使用此Redis模板
	 */
	@ConditionalOnMissingBean(name = DragonConstant.REDIS_JSON_TEMPLATE_NAME)
	@Bean(name = DragonConstant.REDIS_JSON_TEMPLATE_NAME)
	public RedisTemplate<String, Object> redisJsonTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
		@SuppressWarnings("all")
		Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
		// key采用String的序列化方式
		redisTemplate.setKeySerializer(stringRedisSerializer);
		// hash的key也采用String的序列化方式
		redisTemplate.setHashKeySerializer(stringRedisSerializer);
		// value序列化方式采用jackson
		redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
		// hash的value序列化方式也采用jackson
		redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}

	@Bean("redisCacheManager")
	public CacheManager cacheManager(RedisTemplate<String,String> redisTemplate) {
		RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMillis(timeout));
		return RedisCacheManager.builder(RedisCacheWriter.nonLockingRedisCacheWriter(redisTemplate.getConnectionFactory())).cacheDefaults(redisCacheConfiguration).build();
	}

	/**
	 * 该方法只是声明了key的生成策略,还未被使用,需在@Cacheable注解中指定keyGenerator
	 * 如: @Cacheable(value = "key", keyGenerator = "redisCacheKeyGenerator")
	 */
	@Bean("redisCacheKeyGenerator")
	public KeyGenerator redisCacheKeyGenerator() {
		return (proxy, method, params) -> {
			StringBuilder sb = new StringBuilder();
			Class<?> clazz = proxy.getClass();
			String proxyName = clazz.getSuperclass().getName();
			String className = method.getDeclaringClass().getName();
			sb.append(className);
			sb.append(DragonConstant.SPLITTER);
			String methodName = method.getName();
			sb.append(methodName);
			for (Object param : params) {
				// 由于参数可能不同, hashCode肯定不一样, 缓存的key也需要不一样
				sb.append(DragonConstant.SPLITTER);
				sb.append(param);
			}
			String key = sb.toString();
			return key;
		};
	}
}