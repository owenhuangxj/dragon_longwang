package com.trenska.longwang.controller.financing;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trenska.longwang.constant.DragonConstant;
import com.trenska.longwang.entity.financing.AccountType;
import com.trenska.longwang.model.sys.CommonResponse;
import com.trenska.longwang.service.financing.IAccountTypeService;
import com.trenska.longwang.util.NumberUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 2019/5/14
 * 创建人:Owen
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/account")
@Api(description = "账目类型接口")
public class AccountTypeController {

	@Autowired
	private IAccountTypeService accountTypeService;

	@GetMapping(value = "/list/all")
	@ApiOperation("获取所有业务类型")
	public List<AccountType> listAllAccountType() {
		return accountTypeService.list();
	}

	@RequestMapping(value = "/list/receipt/type",method =  RequestMethod.GET)
	@ApiOperation("查询收款类型")
	public CommonResponse listReceiptAccountType() {
		List<AccountType> list = accountTypeService.list(
				new LambdaQueryWrapper<AccountType>()
						.eq(AccountType::getType, DragonConstant.SK_CHINESE)
		);
		return CommonResponse.getInstance().succ(true).data(list);
	}

	@RequestMapping(value = "/list/pay/type",method =  RequestMethod.GET)
	@ApiOperation("查询付款类型")
	public CommonResponse listPayAccountType() {
		List<AccountType> list = accountTypeService.list(
				new LambdaQueryWrapper<AccountType>()
						.eq(AccountType::getType, DragonConstant.FK_CHINESE)
		);
		return CommonResponse.getInstance().succ(true).data(list);
	}

	@RequestMapping(value = "/add/receipt/type",method =  RequestMethod.POST)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "typeName", value = "类型名称", required = true, dataType = "string")
	})
	@ApiOperation("增加收款账目类型")
	public CommonResponse addReceiptAccountType(String typeName) {

		if(null == typeName) return CommonResponse.getInstance().succ(false).msg("账目类型不能为空");
		AccountType accountType = accountTypeService.getOne(
				new LambdaQueryWrapper<AccountType>()
						.eq(AccountType::getTypeName,typeName)
						.eq(AccountType::getType, DragonConstant.SK_CHINESE)
		);
		if(accountType != null){
			return CommonResponse.getInstance().succ(false).msg("收款类型已经存在不能创建");
		}

		AccountType rt = new AccountType();
		rt.setTypeName(typeName);
		rt.setType(DragonConstant.SK_CHINESE);
		boolean save = accountTypeService.save(rt);
		return CommonResponse.getInstance().succ(save).msg( save ? "增加收款账目类型成功" : "增加收款账目类型失败");
	}

	@RequestMapping(value = "/add/pay/type",method =  RequestMethod.POST)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "typeName", value = "类型名称", required = true, dataType = "string")
	})
	@ApiOperation("增加付款账目类型")
	public CommonResponse addPayAccountType(String typeName) {

		if(null == typeName) return CommonResponse.getInstance().succ(false).msg("账目类型不能为空");
		AccountType accountType = accountTypeService.getOne(
				new LambdaQueryWrapper<AccountType>()
						.eq(AccountType::getTypeName,typeName)
						.eq(AccountType::getType, DragonConstant.FK_CHINESE)
		);
		if(null != accountType){
			return CommonResponse.getInstance().succ(false).msg("付款类型已经存在不能创建");
		}
		AccountType rt = new AccountType();
		rt.setTypeName(typeName);
		rt.setType(DragonConstant.FK_CHINESE);
		boolean save = accountTypeService.save(rt);
		return CommonResponse.getInstance().succ(save).msg( save ? "增加付款账目类型成功" : "增加付款账目类型失败");
	}

	@DeleteMapping(value = "/delete/type/{id}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "收款/付款账目类型id", required = true, dataType = "int")
	})
	@ApiOperation("删除账目类型")
	public CommonResponse deleteAccountType(@PathVariable Integer id) {
		if(!NumberUtil.isIntegerUsable(id)){
			return CommonResponse.getInstance().succ(false).msg("无此账目类型");
		}
		AccountType accountType = accountTypeService.getById(id);
		if(null == accountType){
			return CommonResponse.getInstance().succ(false).msg("无此账目类型");
		}
		if(!accountType.getDeletable()){
			return CommonResponse.getInstance().succ(false).msg("系统预留，不可删除");
		}
		return CommonResponse.getInstance().succ(accountTypeService.removeById(id))
				.msg("删除".concat(accountType.getType()).concat("账目类型 : ").concat(accountType.getTypeName()).concat(" 成功."));
	}
}