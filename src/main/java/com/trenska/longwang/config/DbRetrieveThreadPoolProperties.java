package com.trenska.longwang.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "thread.pool.db.retrieve")
public class DbRetrieveThreadPoolProperties {
    @Value("${corePoolSize:3}")
    private int corePoolSize;
    @Value("${maximumPoolSize:5}")
    private int maximumPoolSize;
    @Value("${keepAliveTime:1}")
    private long keepAliveTime;
    @Value("${blockingQueueCapacity:3}")
    private int blockingQueueCapacity;
    @Value("${threadNamePrefix:DB-RETRIEVE-}")
    private String threadNamePrefix;
}