package com.trenska.longwang.controller.financing;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.financing.Payway;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.financing.IPaywayService;
import com.trenska.longwang.util.NumberUtil;
import com.trenska.longwang.util.PageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 2019/5/18
 * 创建人:Owen
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/payway")
@Api(description = "账户、收/付款方式接口 : 现金、转账、返利、运费")
public class PaywayController {

	@Autowired
	private IPaywayService paywayService;

	@GetMapping(value = "/list/all")
	@ApiOperation("获取所有收/付款方式")
	public List<Payway> listAllPayways() {
		return paywayService.list();
	}

	@GetMapping(value = "/list/page/{current}/{size}")
	@ApiOperation("分页收/付款方式")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int")
	})
	public PageHelper<Payway> listPaywaysPage(
			@PathVariable("current") Integer current,
			@PathVariable("size") Integer size
	) {
		return PageHelper.getInstance().pageData(paywayService.getPaywayPage(PageUtils.getPageParam(new PageHelper(current, size))));
	}

	@PostMapping(value = "/add")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "payway", value = "名称", required = true, paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "type", value = "收款/付款", required = true, paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "pdesc", value = "收/付款描述", paramType = "query", dataType = "string")
	})
	@ApiOperation("增加收/付款方式")
	public ResponseModel addPayway(
			@RequestParam("payway") String payway,
			@RequestParam("type") String type,
			@RequestParam(value = "pdesc" ,defaultValue = "") String pdesc
	) {
		if (StringUtils.isEmpty(payway)) {
			return ResponseModel.getInstance().succ(false).msg("请输入" + type.substring(0,2) + "方式");
		}
		// 验证收/付款方式是否已经存在
		boolean exists = checkPayway(payway, type);

		if (exists) {
			return ResponseModel.getInstance().succ(true).msg( type.substring(0,2) + "方式已经存在，不需要创建");
		}
		paywayService.save(new Payway(payway, type, pdesc));

		return ResponseModel.getInstance().succ(true).msg("增加" +  type.substring(0,2) + "方式成功");
	}

	@PostMapping(value = "/update/{paywayId}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "paywayId", value = "id", paramType = "path", dataType = "string"),
			@ApiImplicitParam(name = "payway", value = "名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "type", value = "收/付款方式", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "pdesc", value = "描述", paramType = "query", dataType = "string")
	})
	@ApiOperation("更新收/付款方式")
	public ResponseModel updatePayway(
			@PathVariable("paywayId") Integer paywayId,
			@RequestParam(value = "payway") String payway,
			@RequestParam(value = "type") String type,
			@RequestParam(value = "pdesc") String pdesc) {
		if (checkPayway(payway, type)) {
			return ResponseModel.getInstance().succ(false).msg( type.substring(0,2) + "方式重名");
		}
		paywayService.saveOrUpdate(new Payway(paywayId, payway, type, pdesc));
		return ResponseModel.getInstance().succ(true).msg("修改" +  type.substring(0,2) + "方式成功");

	}

	@DeleteMapping(value = "/delete/{id}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "收/付款方式id", required = true, paramType = "path", dataType = "int")
	})
	@ApiOperation("删除收/付款方式")
	public ResponseModel deletePayway(@PathVariable Integer id) {
		if (!NumberUtil.isIntegerUsable(id)) {
			return ResponseModel.getInstance().succ(false).msg("无此收/付款方式");
		}
		Payway payway = paywayService.getById(id);
		if (null == payway) {
			return ResponseModel.getInstance().succ(false).msg("无此收/付款方式");
		}
		if(!payway.getDeletable()){
			return ResponseModel.getInstance().succ(false).msg("系统预留方式，不可删除");
		}
		return ResponseModel.getInstance().succ(paywayService.removeById(id))
				.msg("删除" + payway.getType().substring(0,2) + "方式成功.");
	}

	@GetMapping(value = "/check/payway")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "payway", value = "名称", paramType = "query", required = true, dataType = "string"),
			@ApiImplicitParam(name = "type", value = "收/付款", paramType = "query", required = true, dataType = "string")
	})
	@ApiOperation("查询收/付款方式名称是否存在")
	public ResponseModel checkPaywayExists(
			@RequestParam("payway") String payway,
			@RequestParam("type") String type
	) {
		if (StringUtils.isEmpty(payway)) {
			return ResponseModel.getInstance().succ(false).msg("收/付款名称不能为空");
		}
		if (StringUtils.isEmpty(type)) {
			return ResponseModel.getInstance().succ(false).msg("收/付款方式不能为空");
		}
		boolean eixts = this.checkPayway(payway, type);

		return ResponseModel.getInstance().succ(!eixts).msg(eixts ? "已经存在" : "可以创建");
	}


	/**
	 * 检查收款/付款单是否已经存在
	 *
	 * @param payway
	 * @param type
	 * @return true:存在 false:不存在
	 */
	private boolean checkPayway(String payway, String type) {
		Payway pay = paywayService.getOne(
				new LambdaQueryWrapper<Payway>()
						.eq(Payway::getPayway, payway)
						.eq(Payway::getType, type)
		);
		return null != pay;
	}
}