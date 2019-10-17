package com.trenska.longwang.controller.customer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.annotation.DuplicateSubmitToken;
import com.trenska.longwang.constant.Constant;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.customer.Customer;
import com.trenska.longwang.entity.sys.SysEmp;
import com.trenska.longwang.model.report.CustomerInfoModel;
import com.trenska.longwang.model.customer.GoodsActiveInfoModel;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.customer.ICustomerService;
import com.trenska.longwang.util.ExcelUtil;
import com.trenska.longwang.util.NumberUtil;
import com.trenska.longwang.util.PageUtils;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.servlet.ShiroHttpSession;
import org.apache.shiro.web.session.HttpServletSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * 2019/4/3
 * 创建人:Owen
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/customer")
@Api(description = "客户接口")
public class CustomerController {
	@Autowired
	private ICustomerService customerService;

	@PostMapping("/add")
	@DuplicateSubmitToken
	@ApiOperation(value = "添加客户信息")
	public ResponseModel addCustomer(@Valid @RequestBody @ApiParam Customer customer) {
		if (null == customer) {
			return ResponseModel.getInstance().succ(false).msg("客户信息不能为空");
		}
		return customerService.addCustomer(customer);
	}

	/**
	 * 需要删除客户特价
	 * @param custId
	 * @return
	 */
	@DuplicateSubmitToken
	@DeleteMapping("/delete/{custId}")
	@ApiOperation(value = "根据客户id号即custId删除客户信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "custId", value = "客户id", paramType = "path", required = true, dataType = "int")
	})
	public ResponseModel deletePriceGrp(@PathVariable Integer custId) {
		if(!NumberUtil.isIntegerUsable(custId)){
			return ResponseModel.getInstance().succ(false).msg("无此客户");
		}
		return customerService.deleteCustomerById(custId);

	}
	@DuplicateSubmitToken
	@DeleteMapping("/delete/batch")
	@ApiOperation(value = "批量删除客户")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "custIds", value = "需要批量删除的客户id集合/数组", paramType = "query", required = true, dataType = "int")
	})
	public ResponseModel batchDeletePriceGrp(@RequestParam(value = "custIds") Collection<Integer> custIds) {
		if(null == custIds || (null != custIds && custIds.size() ==0)){
			return ResponseModel.getInstance().succ(false).msg("无效的客户信息");

		}
		return customerService.deleteCustomerByIds(custIds);
	}

	@PutMapping("/update")
	@DuplicateSubmitToken
	@ApiOperation(value = "修改客户信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "custId", value = "客户id", paramType = "body", required = true, dataType = "int"),
			@ApiImplicitParam(name = "custName", value = "客户名称", paramType = "body", required = true, dataType = "string"),
			@ApiImplicitParam(name = "custId", value = "客户区域分组id", paramType = "body", dataType = "int"),
			@ApiImplicitParam(name = "empId", value = "负责该客户的业务员id", paramType = "body", dataType = "int"),
			@ApiImplicitParam(name = "priceGrpId", value = "客户价格分组Id", paramType = "body", dataType = "int"),
			@ApiImplicitParam(name = "linkman", value = "联系人", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "linkPhone", value = "联系电话", paramType = "body", dataType = "int"),
			@ApiImplicitParam(name = "province", value = "地址省级部分", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "city", value = "地址市级部分", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "county", value = "地址县级部分", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "addr", value = "详细地址", paramType = "body", dataType = "string")
	})
	public ResponseModel updateCustomer(@RequestBody Customer customer) {
		Subject subject = SecurityUtils.getSubject();
		SysEmp sysEmp = (SysEmp) subject.getSession().getAttribute("sysEmp");
		log.debug("sysEmp : {}" , sysEmp);
		boolean succ = customerService.updateById(customer);
		return ResponseModel.getInstance().succ(succ).msg("客户信息更新成功");
	}

	@GetMapping("/list/page/{current}/{size}")
	@ApiOperation("分页获取客户信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int")
	})
	public PageHelper<Customer> listCustomerPage(@PathVariable("current") Integer current, @PathVariable("size") Integer size, HttpServletRequest request) {
		PageHelper page = PageHelper.getInstance();
		page.setCurrent(current);
		page.setSize(size);
		Page<Customer> pageInfo = customerService.getCustomerPage(PageUtils.getPageParam(page),request);
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/list/no-params/{current}/{size}")
	@ApiOperation("无条件分页")
	public PageHelper<Customer> listAllCustomers(
			@PathVariable("current") Integer current,
			@PathVariable("size") Integer size
	) {
		PageHelper pageHelper = PageHelper.getInstance();
		pageHelper.setSize(size);
		pageHelper.setCurrent(current);

		Page<Customer> pageInfo = customerService.getCustomerPageNoParams(PageUtils.getPageParam(pageHelper));
		return PageHelper.getInstance().pageData(pageInfo);
	}


	@GetMapping("/search/{current}/{size}")
	@ApiOperation("多条件查询客户信息并分页")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "empId", value = "业务员id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "areaGrpId", value = "区域分组id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "priceGrpId", value = "价格分组id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "custName", value = "客户名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "custTypeId", value = "客户类型id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int")
	})
	public PageHelper<Customer> listCustomerConditionPage(
			HttpServletRequest request,
			@RequestParam(value = "empId", required = false) Integer empId,
			@RequestParam(value = "custName", required = false) String custName,
			@RequestParam(value = "areaGrpId", required = false) Integer areaGrpId,
			@RequestParam(value = "custTypeId", required = false) Integer custTypeId,
			@RequestParam(value = "priceGrpId", required = false) Integer priceGrpId,
			@PathVariable("current") Integer current,@PathVariable("size") Integer size
	) {
		PageHelper pageHelper = PageHelper.getInstance();
		pageHelper.setSize(size);
		pageHelper.setCurrent(current);
		Map<String , Object> params = new HashMap<>();
		params.put("empId",empId);
		params.put("custName",custName);
		params.put("areaGrpId",areaGrpId);
		params.put("custTypeId",custTypeId);
		params.put("priceGrpId",priceGrpId);
		Page<Customer> pageInfo = customerService.getCustomerPageSelective(params, PageUtils.getPageParam(pageHelper),request);
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/price/special")
	@ApiOperation("获取客户特价")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "custId", value = "客户id", paramType = "param", dataType = "int"),
			@ApiImplicitParam(name = "goodsId", value = "商品id", paramType = "param", dataType = "int")
	})
	public String getCustomerSpecialPrice(
			@RequestParam("custId") Integer custId,
			@RequestParam("goodsId") Integer goodsId
	) {
		Map<String, Object> params = new HashMap<>();
		params.put("custId", custId);
		params.put("goodsId", goodsId);
		return customerService.getCustomerSpecialPrice(params);
	}

	@GetMapping("/active")
	@ApiOperation("获取活动信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "custId", value = "客户id", paramType = "param", dataType = "int"),
			@ApiImplicitParam(name = "goodsId", value = "商品id", paramType = "param", dataType = "int"),
			@ApiImplicitParam(name = "history", value = "定购量", paramType = "param", dataType = "int")
	})
	public List<GoodsActiveInfoModel> listCustomerActiveInfo(
			@RequestParam("custId") Integer custId,
			@RequestParam("goodsId") Integer goodsId,
			@RequestParam("history") Integer num
	) {
		Map<String, Object> params = new HashMap<>();
		params.put("custId", custId);
		params.put("goodsId", goodsId);
		params.put("num", num);
		return customerService.getCustomerActiveInfo(params);
	}

	@GetMapping("/check/exists/name/{custName}")
	@ApiOperation("查询客户名称是否已经存在，存在 succ = true ,不存在 succ = false")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "custName", value = "客户名称", paramType = "path", required = true, dataType = "string")
	})
	public ResponseModel checkCustNameExists(@PathVariable("custName") String custName) {

		if (StringUtils.isEmpty(custName)) {
			return ResponseModel.getInstance().succ(false).msg("客户名称不能为空");
		}

		int count = customerService.count(
				new LambdaQueryWrapper<Customer>()
						.eq(Customer::getCustName, custName)
		);
		return ResponseModel.getInstance().succ(count > 0).msg(count > 0 ? "客户名称已经存在" : "客户名称不存在，可以使用");

	}

	@GetMapping("/check/exists/no/{custNo}")
	@ApiOperation("查询客户编号是否已经存在，存在 succ = true ,不存在 succ = false")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "custNo", value = "客户编号", paramType = "path", required = true, dataType = "string")
	})
	public ResponseModel checkCustNoExists(@PathVariable("custNo") String custNo) {

		if (StringUtils.isEmpty(custNo)) {
			return ResponseModel.getInstance().succ(false).msg("客户编号不能为空");
		}

		int count = customerService.count(
				new LambdaQueryWrapper<Customer>()
						.eq(Customer::getCustNo, custNo)
		);
		return ResponseModel.getInstance().succ(count > 0).msg(count > 0 ? "客户编号已经存在" : "客户编号不存在，可以使用");

	}

	/**
	 *
	 * 切记不可在excel导出时设置 MAPPER XML 文件中映射的 fetchType="lazy"
	 */
	@GetMapping("/export/info")
	@ApiOperation("导出客户信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "empId", value = "业务员id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "empName", value = "业务员名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "areaGrpId", value = "区域分组id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "areaGrpName", value = "区域分组", paramType = "query", dataType="string"),
			@ApiImplicitParam(name = "custTypeId", value = "客户类型id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "custTypeName", value = "客户类型", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "priceGrpId", value = "价格分组id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "priceGrpName", value = "价格分组", paramType = "query", dataType = "int")
	})
	public ResponseEntity<byte[]> exportCustomerInfo(
			HttpServletRequest request,
			@RequestParam(value = "empId", required = false) Integer empId,
			@RequestParam(value = "empName", required = false) String empName,
			@RequestParam(value = "areaGrpId", required = false) Integer areaGrpId,
			@RequestParam(value = "areaGrpName", required = false) String areaGrpName,
			@RequestParam(value = "custTypeId", required = false) Integer custTypeId,
			@RequestParam(value = "custTypeName", required = false) String custTypeName,
			@RequestParam(value = "priceGrpId", required = false) Integer priceGrpId,
			@RequestParam(value = "priceGrpName", required = false) String priceGrpName
	) throws IOException, NoSuchFieldException, IllegalAccessException {
		Map<String , Object> params = new HashMap<>();
		params.put("empId",empId);
		params.put("areaGrpId",areaGrpId);
		params.put("priceGrpId",priceGrpId);
		params.put("custTypeId",custTypeId);
		List<CustomerInfoModel> records = customerService.getCustomerInfoSelective(params,request);
		////////////////////////////////////////////// 处理列标题 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Map<String, String> title = new LinkedHashMap<>();
		title.put("custNo", "客户编号");
		title.put("custName", "客户名称");
		title.put("priceGrp", "价格分组");
		title.put("areaGrp", "归属区域");
		title.put("empName", "所属员工");
		title.put("custType", "客户类型");
//		title.put("debtLimit", "欠款额度");
		title.put("linkman", "联系人");
		title.put("linkPhone", "联系电话");
//		title.put("addr", "客户地址");

		////////////////////////////////////////////// 处理查询条件 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Map<String, Object> query = new LinkedHashMap<>();
		if(NumberUtil.isIntegerUsable(empId) && !records.isEmpty()) {
			query.put("所属员工",empName);
		}
		if(NumberUtil.isIntegerUsable(areaGrpId) && StringUtils.isNotEmpty(areaGrpName)) {
			query.put("归属区域", areaGrpName);
		}
		if(NumberUtil.isIntegerUsable(priceGrpId) && StringUtils.isNotEmpty(priceGrpName)){
			query.put("价格分组",priceGrpName);
		}

		if(NumberUtil.isIntegerUsable(custTypeId) && StringUtils.isNotEmpty(custTypeName)){
			query.put("客户类型", custTypeName);
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook("客户信息",false,null, query, title, records,null);
		wb.write(baos);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentDispositionFormData("attachment",new String("客户信息.xls".getBytes(Constant.srcEncoding),Constant.destEncoding));
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		return new ResponseEntity<byte[]>(baos.toByteArray(),headers, HttpStatus.CREATED);
	}
}