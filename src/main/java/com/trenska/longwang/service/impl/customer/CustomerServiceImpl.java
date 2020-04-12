package com.trenska.longwang.service.impl.customer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.annotation.DataAuthVerification;
import com.trenska.longwang.constant.DragonConstant;
import com.trenska.longwang.dao.customer.AreaGrpMapper;
import com.trenska.longwang.dao.customer.CustomerMapper;
import com.trenska.longwang.dao.financing.DealDetailMapper;
import com.trenska.longwang.dao.financing.ReceiptMapper;
import com.trenska.longwang.dao.goods.GoodsCustSpecialMapper;
import com.trenska.longwang.dao.indent.IndentMapper;
import com.trenska.longwang.entity.customer.Customer;
import com.trenska.longwang.entity.financing.Receipt;
import com.trenska.longwang.entity.goods.GoodsCustSpecify;
import com.trenska.longwang.entity.indent.Indent;
import com.trenska.longwang.enums.IndentStat;
import com.trenska.longwang.model.report.CustomerInfoModel;
import com.trenska.longwang.model.customer.GoodsActiveInfoModel;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.customer.ICustomerService;
import com.trenska.longwang.util.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 2019/4/3
 * 创建人:Owen
 */
@Service
@SuppressWarnings("all")
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements ICustomerService {

	@Autowired
	private AreaGrpMapper areaGrpMapper;

	@Autowired
	private GoodsCustSpecialMapper goodsCustSpecialMapper;

	@Autowired
	private DealDetailMapper dealDetailMapper;

	@Autowired
	private IndentMapper indentMapper;

	@Autowired
	private ReceiptMapper receiptMapper;

	/**
	 * 在服务层注入数据权限控制，通过empId查找对应的数据权限-> 可以产看的所有的用户id
	 *
	 * @param page
	 * @param request
	 * @return
	 */
	@Override
	public Page<Customer> getCustomerPage(Page page) {

		int customerCount = count();
		if (customerCount == 0) {
			return new Page<Customer>(1, 0);
		}
		// 在服务层注入数据权限控制，通过empId查找对应的数据权限 : 可以查看的用户
		Set<Integer> custIds = CustomerUtil.getCurrentUserDataAuth(areaGrpMapper);

		if (CollectionUtils.isEmpty(custIds)) {
			try {
				ResponseUtil.accessDenied(DragonConstant.ACCESS_TIMEOUT, DragonConstant.ACCESS_TIMEOUT_MSG, "");
				page.setTotal(0);
				page.setRecords(new ArrayList(0));
				return page;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Map<String, Object> params = new HashMap<>();

		params.put(DragonConstant.CUST_IDS_LABEL, custIds);

		List<Customer> customers = baseMapper.selectCustomerPage(page, params);

		int retain = SysUtil.getSysConfigRetain();

		customers.stream().forEach(customer -> {
			BigDecimal debt = new BigDecimal(customer.getDebt());
			debt = debt.setScale(retain, RoundingMode.HALF_UP);
			customer.setDebt(debt.toString());
		});

		Integer total = super.baseMapper.selectCount(new QueryWrapper());

		page.setTotal(total);

		page.setRecords(customers);

		return page;
	}

	@Override
	@DataAuthVerification
	public Page<Customer> getCustomerPageSelective(Map<String, Object> params, Page page) {
		Integer total = super.baseMapper.selectCustomerCountSelective(params);
		List<Customer> records = super.baseMapper.selectCustomerWithDataPermPageSelective(params, page);
		page.setTotal(total);
		page.setRecords(records);
		return page;
	}

	@Override
	@Transactional
	public ResponseModel addCustomer(Customer customer) {
		List<Customer> dbCustomers = this.list(
				new LambdaQueryWrapper<Customer>()
						.eq(Customer::getCustName, customer.getCustName())
						.or()
						.eq(Customer::getCustNo, customer.getCustNo())
		);

		if (CollectionUtils.isNotEmpty(dbCustomers)) {
			Set<Customer> sameCustNameCustomers =
					dbCustomers.stream().filter(cust -> customer.getCustName().equals(cust.getCustName())).collect(Collectors.toSet());
			if (!sameCustNameCustomers.isEmpty()) {
				return ResponseModel.getInstance().succ(false).msg("客户名称已经存在，不能创建");
			} else {
				Set<Customer> sameCustNoCustomers =
						dbCustomers.stream().filter(cust -> customer.getCustNo().equals(cust.getCustNo())).collect(Collectors.toSet());
				if (!sameCustNoCustomers.isEmpty()) {
					return ResponseModel.getInstance().succ(false).msg("客户编号已经存在，不能创建");
				}
			}
		}

		if (StringUtils.isEmpty(customer.getDebtLimit())) {
			customer.setDebtLimit(DragonConstant.NO_DEBT_LIMIT_LABEL);
		}
		String time = TimeUtil.getCurrentTime(DragonConstant.TIME_FORMAT);
		customer.setCreatedTime(time);
		String initDebt = customer.getInitDebt();
		customer.setDebt(initDebt);
		String amount = "0.00";
		if (new BigDecimal(initDebt).compareTo(BigDecimal.ZERO) > 0) {
			if (!initDebt.startsWith(DragonConstant.PLUS)) {
				amount = DragonConstant.PLUS.concat(initDebt);
			}else{
				amount = initDebt;
			}
		} else if (new BigDecimal(initDebt).compareTo(BigDecimal.ZERO) < 0) {
			if (!initDebt.startsWith(DragonConstant.MINUS)) {
				amount = DragonConstant.MINUS.concat(initDebt);
			}else {
				amount = initDebt;
			}
		}
		// mybatis的insert方法会返回自增的id给对应的主键，比如此时会将t_customer表的cust_id返回给custId属性
		super.baseMapper.insert(customer);

		/********************* 插入一条期初欠款交易明细 , 无期初欠款欠款默认为0 *********************/
		Integer custId = customer.getCustId();
		DealDetailUtil.saveDealDetail(custId, "", time, amount, initDebt, DragonConstant.QCQK_CHINESE, "", "");

		// 处理拥有所有数据权限的账号不能看到新建的客户信息的bug

		return ResponseModel.getInstance().succ(true).msg("添加客户成功");
	}

	@Override
	public String getCustomerSpecialPrice(Map<String, Object> params) {
		Integer retain = SysUtil.getSysConfigRetain();
		return NumberFormatter.format(BigDecimal.valueOf(Double.valueOf(super.baseMapper.selectCustomerSpecialPrice(params))), retain);

	}

	@Override
	public List<GoodsActiveInfoModel> getCustomerActiveInfo(Map<String, Object> params) {
		return super.baseMapper.selectCustomerActiveInfo(params);
	}

	/**
	 * 无条件分页
	 *
	 * @param page
	 * @return
	 */
	@Override
	public Page<Customer> getCustomerPageNoParams(Page page) {
		page.setRecords(super.baseMapper.selectCustomerPageSelective(page));
		page.setTotal(count());
		return page;
	}

	/**
	 * 删除客户，需要处理客户特价
	 *
	 * @param custId
	 * @return
	 */
	@Override
	@Transactional
	public ResponseModel deleteCustomerById(Integer custId) {
		Customer customer = this.getById(custId);
		if (null == customer) {
			return ResponseModel.getInstance().succ(false).msg("无此客户");
		}

		Integer validIndentCount = indentMapper.selectCount(
				new LambdaQueryWrapper<Indent>()
						.eq(Indent::getCustId, custId)
						.ne(Indent::getStat, IndentStat.INVALID.getName())
		);

		if (validIndentCount > 0) {
			return ResponseModel.getInstance().succ(false).msg("客户关联了未作废的订单，不能删除");
		}

		Integer invalidReceiptCount = receiptMapper.selectCount(
				new LambdaQueryWrapper<Receipt>()
						.eq(Receipt::getCustId, custId)
						.ne(Receipt::getStat, false)
		);

		if (invalidReceiptCount > 0) {
			return ResponseModel.getInstance().succ(false).msg("客户关联了未作废的收款或付款单，不能删除");
		}


		// 删除客户特价信息
		goodsCustSpecialMapper.delete(
				new LambdaQueryWrapper<GoodsCustSpecify>()
						.eq(GoodsCustSpecify::getCustId, custId)
		);
		this.removeById(custId);
		return ResponseModel.getInstance().succ(true).msg("客户删除成功");
	}

	@Override
	@Transactional
	public ResponseModel deleteCustomerByIds(Collection<Integer> custIds) {
		// 删除客户特价信息
		goodsCustSpecialMapper.delete(
				new LambdaQueryWrapper<GoodsCustSpecify>()
						.in(GoodsCustSpecify::getCustId, custIds)
		);

		this.removeByIds(custIds);
		return ResponseModel.getInstance().succ(true).msg("客户删除成功");
	}

	/**
	 * 获取客户excel导出信息
	 * 注意: 添加了数据权限
	 *
	 * @param params
	 * @return
	 */
	@Override
	@DataAuthVerification
	public List<CustomerInfoModel> getCustomerInfoSelective(Map<String, Object> params) {
		Set<Integer> custIds = new HashSet<>();
		List<CustomerInfoModel> customerInfos = super.baseMapper.selectExportingCustomerInfoSelective(params);
		return customerInfos;
	}

	@Override
	public Set<Integer> getCustIdsOfSalesman(Integer salesmanId) {
		return super.baseMapper.selectCustIdsOfSalesman(salesmanId);
	}
}