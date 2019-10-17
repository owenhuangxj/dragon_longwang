package com.trenska.longwang.aop;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trenska.longwang.annotation.DataAuthVerification;
import com.trenska.longwang.constant.Constant;
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
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 2019/6/27
 * 创建人:Owen
 * 处理数据权限
 * 	首先判断登陆是否超时
 * 	如果没有超时就获取数据权限(当前用户可以访问的客户)注入到类型为Map的参数里面以便dao层使用
 */
@Slf4j
@Aspect
@Component
public class DataAuthAop {
	@Pointcut("execution(public * com.trenska.longwang.service.impl..*.*(..))")
	public void dataPowerPointcut(){
	}
	@Before("dataPowerPointcut() && @annotation(dataAuthority)")
	public void before(JoinPoint joinPoint, DataAuthVerification dataAuthority) throws IOException {
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = requestAttributes.getRequest();
//		log.debug("request : {}",request );
		Integer empIdInRedis = SysUtil.getEmpIdInRedis(request);
		if(NumberUtil.isIntegerNotUsable(empIdInRedis)){
			HttpServletResponse response = requestAttributes.getResponse();
			ResponseUtil.accessDenied(response,Constant.ACCESS_TIMEOUT,Constant.ACCESS_TIMEOUT_MSG);
			return;
		}

		List<EmpAreaGrp> empAreaGrps = new EmpAreaGrp().selectList(
				new LambdaQueryWrapper<EmpAreaGrp>()
						.eq(EmpAreaGrp::getEmpId,empIdInRedis)
						.select(EmpAreaGrp::getAreaGrpId)
		);
		if(CollectionUtils.isEmpty(empAreaGrps)){
			HttpServletResponse response = requestAttributes.getResponse();
			ResponseUtil.accessDenied(response,Constant.ACCESS_FORBIDDEN,Constant.NO_ACCESS_PERMISSION_MSG);
			return;
		}

		Set<Integer> areaGrpIds = empAreaGrps.stream().map(EmpAreaGrp::getAreaGrpId).collect(Collectors.toSet());
		List<Customer> customers = new Customer().selectList(
				new LambdaQueryWrapper<Customer>()
						.in(Customer::getAreaGrpId,areaGrpIds)
						.select(Customer::getCustId)
		);

		Set<Integer> custIds = customers.stream().map(Customer::getCustId).collect(Collectors.toSet());
		Object[] args = joinPoint.getArgs();
		for (Object arg : args) {
			if(arg instanceof Map){
				Map<String,Object> params = (Map<String,Object>) arg;
				params.put("custIds",custIds);
			}
		}
	}
}