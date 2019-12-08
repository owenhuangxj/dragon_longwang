package com.trenska.longwang.cron.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xhtmlrenderer.util.LoggerUtil;

import java.util.Date;
import java.util.logging.Logger;

/**
 * 2019/11/23
 * 创建人:Owen
 */
@Async
@Component
@EnableScheduling
public class GoodsCopyTask {

	@Scheduled(cron = "0/10 * * * * ?")
	public void scheduledTaskByCron(){
		System.out.println("scheduled task : " + new Date());

	}


}
