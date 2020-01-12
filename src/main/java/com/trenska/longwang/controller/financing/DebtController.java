package com.trenska.longwang.controller.financing;


import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.constant.Constant;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.financing.DealDetail;
import com.trenska.longwang.entity.financing.DealDetailSummarizing;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.financing.IDealDetailService;
import com.trenska.longwang.util.NumberUtil;
import com.trenska.longwang.util.PageUtils;
import com.trenska.longwang.util.StringUtil;
import com.trenska.longwang.util.TimeUtil;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * 交易明细 前端控制器
 * </p>
 *
 * @author Owen
 * @since 2019-05-20
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/debt")
@Api(description = "欠款单接口")
public class DebtController {

	@Autowired
	private IDealDetailService dealDetailService;

	@PostMapping(value = "/item")
	@ApiOperation("新建欠款单")
	public ResponseModel add(@RequestBody @ApiParam DealDetail dealDetail) {
		if (dealDetail == null) {
			return ResponseModel.getInstance().succ(false).msg("无效的欠款单！");
		}
		if (NumberUtil.isIntegerNotUsable(dealDetail.getCustId())) {
			return ResponseModel.getInstance().succ(false).msg("无效的客户信息！");
		}
		if (!StringUtil.isNumeric(dealDetail.getAmount(), false)) {
			return ResponseModel.getInstance().succ(false).msg("无效的欠款金额！");
		}
		if (StringUtils.isEmpty(dealDetail.getOper())) {
			return ResponseModel.getInstance().succ(false).msg("操作类型不能为空！");
		}
		dealDetailService.addDebt(dealDetail);
		return ResponseModel.getInstance().succ(true).msg("客户欠款增加成功！");
	}

	@RequestMapping(value = "/page/{current}/{size}", method = RequestMethod.GET)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "custId", value = "客户id", paramType = "query", required = true, dataType = "int"),
			@ApiImplicitParam(name = "beginTime", value = "开始日期", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "结束日期", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "oper", value = "业务类型", paramType = "query", dataType = "string")
	})
	@ApiOperation("客户欠款明细")
	public PageHelper<DealDetail> listCustomerTradeDetail(
			@RequestParam(name = "oper") String oper,
			@RequestParam(required = false, name = "custId") Integer custId,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@PathVariable("current") Integer current, @PathVariable("size") Integer size
	) {
		Map<String, Object> params = new HashMap<>();
		params.put("oper", oper);
		params.put("custId", custId);
		params.put("endTime", endTime);
		params.put("beginTime", beginTime);
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Page<DealDetail> pageInfo = dealDetailService.page(page,params);
		return PageHelper.getInstance().pageData(pageInfo);
	}
}

