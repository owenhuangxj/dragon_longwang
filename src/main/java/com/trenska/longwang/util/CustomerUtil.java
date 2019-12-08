package com.trenska.longwang.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trenska.longwang.constant.Constant;
import com.trenska.longwang.dao.customer.AreaGrpMapper;
import com.trenska.longwang.entity.customer.Customer;
import com.trenska.longwang.entity.sys.EmpAreaGrp;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.customer.IAreaGrpService;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 2019/5/15
 * 创建人:Owen
 * 操作t_customer表的工具类
 */
public class CustomerUtil {


	/**
	 * 增加客户欠款
	 * @param custId
	 * @param amount
	 * @return
	 */
	public static boolean addCustomerDebt(Integer custId, BigDecimal amount){
		Customer customer = new Customer(custId).selectById();
		String oldDebt = customer.getDebt();
		String newDebt = new BigDecimal(oldDebt).add(amount).toString();
		return new Customer(custId,newDebt).updateById();
	}
	/**
	 * 增加客户欠款
	 * @param custId
	 * @param amount
	 * @return
	 */
	public static boolean addCustomerDebt(Integer custId, String oldDebt , BigDecimal amount){
		String newDebt = new BigDecimal(oldDebt).add(amount).toString();
		return new Customer(custId,newDebt).updateById();

	}
	/**
	 * 减少客户欠款
	 * @param custId 客户id
	 * @param oldDebt 客户欠款
	 * @param amount 减少量
	 * @return
	 */
	public static boolean subtractCustomerDebt(Integer custId, String oldDebt , BigDecimal amount){
		String newDebt = new BigDecimal(oldDebt).subtract(amount).toString();
		return new Customer(custId,newDebt).updateById();

	}

	/**
	 *
	 * @param custId 客户id
	 * @param amount 减少量
	 * @return
	 */
	public static boolean subtractCustomerDebt(Integer custId, String amount){
		Customer customer = new Customer().selectById(custId);
		return new Customer(custId,
				new BigDecimal(customer.getDebt())
						.subtract(new BigDecimal(amount)).toString()
		).updateById();

	}

	/**
	 * 根据账号id获取账号的数据权限=>能访问的客户数据=>关联客户id
	 * @param empId
	 * @param areaGrpMapper
	 * @return
	 */
	public static Set<Integer> getCustIdsByEmpId(Integer empId , AreaGrpMapper areaGrpMapper){

		Set<Integer> areaGrpIds = new EmpAreaGrp().selectList(
				new LambdaQueryWrapper<EmpAreaGrp>()
						.eq(EmpAreaGrp::getEmpId,empId)
		).stream().map(EmpAreaGrp::getAreaGrpId).collect(Collectors.toSet());

		Set<Integer> custAreaGrpIds = new HashSet<>();

		// 获取所有区域分组id
		areaGrpIds.forEach(areaGrpId-> custAreaGrpIds.addAll(areaGrpMapper.selectSubAreaGrpIds(areaGrpId)));

		List<Customer> customers = new ArrayList<>();
		if(!custAreaGrpIds.isEmpty()){
			customers.addAll(
					new Customer().selectList(
							new LambdaQueryWrapper<Customer>()
									.in(Customer::getAreaGrpId, custAreaGrpIds)
					)
			);
		}

		Set<Integer> custIds = new HashSet<>();

		if (!customers.isEmpty()){
			custIds.addAll(customers.stream().map(Customer::getCustId).collect(Collectors.toList()));
		}

		return custIds;
	}

	/**
	 * 根据账号id获取账号的数据权限=>能访问的客户数据=>关联客户id
	 * @param empId
	 * @return
	 */
	public static Set<Integer> getCustIdsByEmpId(Integer empId , IAreaGrpService areaGrpService){

		Set<Integer> areaGrpIds = new EmpAreaGrp().selectList(
				new LambdaQueryWrapper<EmpAreaGrp>()
						.eq(EmpAreaGrp::getEmpId,empId)
		).stream().map(EmpAreaGrp::getAreaGrpId).collect(Collectors.toSet());

		Set<Integer> custAreaGrpIds = new HashSet<>();

		// 获取所有区域分组id
		areaGrpIds.forEach(areaGrpId->{
			custAreaGrpIds.addAll(areaGrpService.getSubAreaGrpIds(areaGrpId));
		});

		List<Customer> customers = new ArrayList<>();
		if(!custAreaGrpIds.isEmpty()){
			customers.addAll(
					new Customer().selectList(
							new LambdaQueryWrapper<Customer>()
									.in(Customer::getAreaGrpId, custAreaGrpIds)
					)
			);
		}

		Set<Integer> custIds = new HashSet<>();

		if (!customers.isEmpty()){
			custIds.addAll(customers.stream().map(Customer::getCustId).collect(Collectors.toList()));
		}

		return custIds;
	}




	/**
	 * 获取账户的数据权限 -> 账号所能访问的客户信息，以客户id集合的方式返回
	 * @param request
	 * @param areaGrpService
	 * @return
	 */
	public static Set<Integer> getCurrentUserDataAuth(HttpServletRequest request , IAreaGrpService areaGrpService){
		Integer empIdInRedis = SysUtil.getEmpIdInRedis(request);
		return CustomerUtil.getCustIdsByEmpId(empIdInRedis, areaGrpService);
	}

	/**
	 * 获取账户的数据权限 -> 账号所能访问的客户信息，以客户id集合的方式返回
	 * @param request
	 * @param areaGrpMapper
	 * @return
	 */
	public static Set<Integer> getCurrentUserDataAuth(HttpServletRequest request , AreaGrpMapper areaGrpMapper){

		Integer empIdInRedis = SysUtil.getEmpIdInRedis(request);

		if(NumberUtil.isIntegerNotUsable(empIdInRedis)) {
			ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			HttpServletResponse response = requestAttributes.getResponse();
			try {
				ResponseUtil.accessDenied(response,Constant.ACCESS_TIMEOUT,Constant.ACCESS_TIMEOUT_MSG);
				return new HashSet<>();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return getCustIdsByEmpId(empIdInRedis, areaGrpMapper);
	}

	/**
	 * 判断客户欠款额度,超过额度将不能出库
	 */
	public static ResponseModel checkDebtLimit(int custId){

		Customer customer = new Customer(custId).selectById();

		if (Objects.isNull(customer)) {
			return ResponseModel.getInstance().succ(false).msg(Constant.CUSTOMER_NOT_EXISTS_MSG);
		}

		/**如果创建客户时没有设置debtLimit，客户就没有欠款额度的限制*/
		if(Constant.NO_DEBT_LIMIT_LABEL.equals(customer.getDebtLimit())){
			return ResponseModel.getInstance().succ(true);
		}
		if (new BigDecimal(customer.getDebt()).compareTo(new BigDecimal(customer.getDebtLimit())) >= 0) {
			return ResponseModel.getInstance().succ(false).msg(Constant.CUSTOMER_OUT_OF_DEBT_MSG);
		}

		return ResponseModel.getInstance().succ(true);

	}
}
