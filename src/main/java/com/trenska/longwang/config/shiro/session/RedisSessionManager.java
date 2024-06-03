package com.trenska.longwang.config.shiro.session;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.session.mgt.WebSessionKey;

import javax.servlet.ServletRequest;

/**
 * 解决一次WEB请求多次从SessionDAO(本工程具体实现是RedisSessionDao)中获取Session
 * extends的原因是因为本工程是WEB工程，重写的retrieveSession方法入参是WebSessionKey，
 * 从WebSessionKey中可以获取ServletRequest和ServletResponse
 */
@Slf4j
public class RedisSessionManager extends DefaultWebSessionManager {
    @Override
    protected Session retrieveSession(SessionKey sessionKey) throws UnknownSessionException {
        // 调用DefaultWebSessionManager的getSessionId方法而不是通过sessionKey.getSessionId获取
        String sessionId = (String) getSessionId(sessionKey);
        log.info("Param sessionId:{}", sessionId);
        if (sessionId == null) {
            return null;
        }
        WebSessionKey webSessionKey = (WebSessionKey) sessionKey;
        ServletRequest servletRequest = webSessionKey.getServletRequest();

        // 从ServletRequest中获取Session
        Session session = (Session) servletRequest.getAttribute(sessionId);

        // 如果session为null就
        if (session == null) {
            log.info("Retrieve session from datasource>>>");
            // retrieveSessionFromDataSource最终是通过RedisSessionDao获取的Session
            session = retrieveSessionFromDataSource(sessionId);
            if (session == null) {
                // 应该不需要做这个动作，因为父类AbstractSessionDAO.readSession方法已经抛出了此异常
                throw new UnknownSessionException("Could not find session with ID [" + sessionId + "]");
            }
            servletRequest.setAttribute(sessionId,session);
        }
        return session;
    }
}
