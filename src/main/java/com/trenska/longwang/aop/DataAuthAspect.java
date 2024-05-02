package com.trenska.longwang.aop;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trenska.longwang.annotation.DataAuthVerification;
import com.trenska.longwang.constant.DragonConstant;
import com.trenska.longwang.entity.customer.Customer;
import com.trenska.longwang.entity.sys.EmpAreaGrp;
import com.trenska.longwang.util.NumberUtil;
import com.trenska.longwang.util.ResponseUtil;
import com.trenska.longwang.util.SysUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.trenska.longwang.constant.DragonConstant.CUST_IDS_LABEL;

/**
 * 2019/6/27
 * 创建人:Owen
 * 处理数据权限
 * 首先判断登陆是否超时
 * 如果没有超时就获取数据权限(当前账号可以访问的客户)注入到类型为Map<String,Set<Integer>>,名为custIds的Map里面以便dao层使用
 */
@Slf4j
@Aspect
@Component
public class DataAuthAspect {
    @Before("@annotation(dataAuthority)")
    public void before(JoinPoint joinPoint, DataAuthVerification dataAuthority) throws IOException {
        Integer empIdInToken = SysUtil.getEmpIdInToken();
        if (NumberUtil.isIntegerNotUsable(empIdInToken)) {
            ResponseUtil.accessDenied(DragonConstant.ACCESS_TIMEOUT, DragonConstant.ACCESS_TIMEOUT_MSG,
                    DragonConstant.ACCESS_TIMEOUT_MSG);
            return;
        }
        long start = System.currentTimeMillis();
        /* 查询员工负责的客户数量 */
        Integer empAreaGrpCount = new EmpAreaGrp().selectCount(new LambdaQueryWrapper<>());
        log.info("查询员工负责的客户数量消耗:", (System.currentTimeMillis() - start) / 1000);
        // 如果员工负责的客户数量为0，注入空集合到Map的custIds属性中去
        if (empAreaGrpCount == 0) {
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                /* Controller层查询参数为Map，传递给Service层 */
                if (arg instanceof Map) {
                    Map<String, Object> params = (Map<String, Object>) arg;
                    params.put(CUST_IDS_LABEL, new HashSet<Integer>(0));
                }
            }
            return;
        }
        start = System.currentTimeMillis();
        /* 获取当前账号负责的区域 */
        List<EmpAreaGrp> empAreaGrps = new EmpAreaGrp().selectList(
                new LambdaQueryWrapper<EmpAreaGrp>()
                        .eq(EmpAreaGrp::getEmpId, empIdInToken)
                        .select(EmpAreaGrp::getAreaGrpId)
        );
        log.info("获取当前账号负责的区域消耗:{}", (System.currentTimeMillis() - start) / 1000);
        if (CollectionUtils.isEmpty(empAreaGrps)) {
            ResponseUtil.accessDenied(DragonConstant.NO_AREA_AUTHORITY, DragonConstant.NO_ACCESS_PERMISSION_MSG,
                    DragonConstant.NO_ACCESS_PERMISSION_MSG);
            return;
        }
        Set<Integer> areaGrpIds = empAreaGrps.stream().map(EmpAreaGrp::getAreaGrpId).collect(Collectors.toSet());
        start = System.currentTimeMillis();
        /* 获取当前账号负责的客户 */
        List<Customer> customers = new Customer().selectList(
                new LambdaQueryWrapper<Customer>()
                        .in(Customer::getAreaGrpId, areaGrpIds)
                        .select(Customer::getCustId)
        );
        log.info("获取当前账号负责的客户消耗:", (System.currentTimeMillis() - start) / 1000);
        Set<Integer> custIds;
//		Integer customerCount = new Customer().selectCount(new LambdaQueryWrapper<>());
        if (CollectionUtils.isEmpty(customers)) {
            custIds = new HashSet<>(0);
        } else {
            custIds = customers.stream().map(Customer::getCustId).collect(Collectors.toSet());
        }
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof Map) {
                Map<String, Object> params = (Map<String, Object>) arg;
                params.put(CUST_IDS_LABEL, custIds);
            }
        }
    }
}