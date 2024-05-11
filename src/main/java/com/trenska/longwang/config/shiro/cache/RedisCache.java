package com.trenska.longwang.config.shiro.cache;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.util.CollectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisCache<K, V> implements Cache<K, V> {
    private final String SHIRO_CACHE_PREFIX = "ShiroCache:";

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public V get(K k) throws CacheException {
        log.info("Retrieve shiro data from redis!!!");
        if (k == null) {
            return null;
        }
        V v = (V) redisTemplate.opsForValue().get(SHIRO_CACHE_PREFIX + k);

        // 延长活跃数据缓存时间
        if (v != null) {
            redisTemplate.opsForValue().set(SHIRO_CACHE_PREFIX + k, v, 1, TimeUnit.HOURS);
        }
        return v;
    }

    @Override
    public V put(K k, V v) throws CacheException {
        redisTemplate.opsForValue().set(SHIRO_CACHE_PREFIX + k, v, 1, TimeUnit.HOURS);
        return v;
    }

    @Override
    public V remove(K k) throws CacheException {
        V v = (V) redisTemplate.opsForValue().get(k);
        if (v != null) {
            redisTemplate.delete(SHIRO_CACHE_PREFIX + k);
        }
        return v;
    }

    @Override
    public void clear() throws CacheException {
        Set keys = redisTemplate.keys(SHIRO_CACHE_PREFIX + "*");
        redisTemplate.delete(keys);
    }

    @Override
    public int size() {
        return CollectionUtils.size(keys());
    }

    @Override
    public Set<K> keys() {
        return redisTemplate.keys(SHIRO_CACHE_PREFIX + "*");
    }

    @Override
    public Collection<V> values() {
        Set keys = redisTemplate.keys(SHIRO_CACHE_PREFIX + "*");

        // 通过pipeline减少redis IO(即RTT,往返时间)
        return redisTemplate.opsForValue().multiGet(keys);
    }
}
