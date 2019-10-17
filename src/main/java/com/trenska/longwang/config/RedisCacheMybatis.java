package com.trenska.longwang.config;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.trenska.longwang.constant.Constant;
import com.trenska.longwang.context.ApplicationContextHolder;
import org.apache.ibatis.cache.Cache;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RedisCacheMybatis  implements Cache, Serializable {
    /**
     * 读写锁：与synchronized的区别：
     *      synchronized : 读写竞争==>读的时候不能写，反之亦然
     *      ReentrantReadWriteLock ： 读-写不竞争，读-读不竞争，写-写竞争
     */
    private ReadWriteLock lock = new ReentrantReadWriteLock(true);
    // 缓存的id
    private String id;

    @Resource(name = Constant.REDIS_JSON_TEMPLATE_NAME)
    private RedisTemplate<String,Object> redisJsonTemplate;

    @Override
    public String getId() {
        return this.id;
    }

    public RedisCacheMybatis(String id) {
        this.id = id;
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return this.lock;
    }

    @Override
    public void putObject(Object key, Object value) {
        ensureTemplateAvailable();
        redisJsonTemplate.opsForValue().set(key.toString(),value);
    }

    @Override
    public Object getObject(Object key) {
        ensureTemplateAvailable();
        return redisJsonTemplate.opsForValue().get(key.toString());
    }

    @Override
    public Object removeObject(Object key) {
        ensureTemplateAvailable();
        return redisJsonTemplate.delete(key.toString());
    }

    @Override
    public void clear() {
        ensureTemplateAvailable();
        Set<String> keys = redisJsonTemplate.keys("*".concat( this.id).concat("*")); //包含改id的所有key
        if(!CollectionUtils.isEmpty(keys)) redisJsonTemplate.delete(keys);
    }

    @Override
    public int getSize() {
        ensureTemplateAvailable();
        return redisJsonTemplate.execute((RedisServerCommands::dbSize)).intValue();
    }

    /**
     * 确保RedisTemplate有效的方法
     */
    private void ensureTemplateAvailable(){
        if(null == redisJsonTemplate) this.redisJsonTemplate = ApplicationContextHolder.getBean("redisJsonTemplate");
    }
}
