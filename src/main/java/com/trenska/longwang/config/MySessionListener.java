package com.trenska.longwang.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
/**
 * 会话监听器
 */
@Slf4j
@Component
public class MySessionListener implements SessionListener {

    private final AtomicInteger sessionCount = new AtomicInteger(0);

    @Override
    public void onStart(Session session) {
        sessionCount.incrementAndGet();
        log.debug("login + 1 == {}",sessionCount.get());
    }
    @Override
    public void onStop(Session session) {
        sessionCount.decrementAndGet();
        log.debug("logout - 1 == {}",sessionCount.get());
    }
    @Override
    public void onExpiration(Session session) {
        sessionCount.decrementAndGet();
        log.debug("timeout + 1 == {}",sessionCount.get());
    }
}