package com.trenska.longwang.controller;

import com.trenska.longwang.config.DbRetrieveThreadPool;
import com.trenska.longwang.model.sys.CommonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DbReviewThreadPoolController {
    @Autowired
    private DbRetrieveThreadPool dbRetrieveThreadPool;

    @GetMapping("/test")
    public CommonResponse<Boolean> test() {
        dbRetrieveThreadPool.runAsync(() -> Thread.currentThread().setName("First springboot test case>>>"));
        return CommonResponse.getInstance().data(true);
    }
}
