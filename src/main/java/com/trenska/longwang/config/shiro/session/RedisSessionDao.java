package com.trenska.longwang.config.shiro.session;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Shiro的Session存入Redis中避免分布式集群场景下服务器之间Session不共享的问题
 */
@Slf4j
@Primary
@Component
public class RedisSessionDao extends AbstractSessionDAO {
    private static final String SHIRO_SESSION_PREFIX = "TrenskaShiroSession:";
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    protected Serializable doCreate(Session session) {
        if (session == null) {
            return null;
        }
        log.info("Creat shiro session and save to redis>>>");
        String sessionId = SHIRO_SESSION_PREFIX + generateSessionId(session);

        // 将Session和sessionId绑定到一起（可以基于Session拿到sessionId）
        assignSessionId(session, sessionId);
        redisTemplate.opsForValue().set(sessionId, session, 1, TimeUnit.HOURS);

        // 返回的sessionId已经包含ShiroSession:前缀，所以doReadSession、update、delete方法中不需要再拼接ShiroSession:前缀
        return sessionId;
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        if (sessionId == null) {
            return null;
        }
        log.info("Read shiro session from redis,sessionId is {}>>>", sessionId);
        Session session = (Session) redisTemplate.opsForValue().get(sessionId);
        if (session != null) {
            // 更新session的缓存时间
            redisTemplate.opsForValue().set(sessionId, session, 1, TimeUnit.HOURS);
        }
        return session;
    }

    @Override
    public void update(Session session) throws UnknownSessionException {
        if (session == null) {
            return;
        }
        log.info("Update shiro session in redis,sessionId is {}>>>", session.getId());
        redisTemplate.opsForValue().set(session.getId(), session, 1, TimeUnit.HOURS);
    }

    @Override
    public void delete(Session session) {
        if (session == null) {
            return;
        }
        log.info("Delete shiro session from redis,sessionId is {}>>>", session.getId());
        redisTemplate.delete(session.getId());
    }

    @Override
    public Collection<Session> getActiveSessions() {
        log.info("Get active shiro sessions from redis>>>");
        Set keys = redisTemplate.keys(SHIRO_SESSION_PREFIX + "*");
        return redisTemplate.opsForValue().multiGet(keys);
    }
}