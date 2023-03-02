package com.trenska.longwang.controller.indent;

import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.annotation.CheckDuplicateSubmit;
import com.trenska.longwang.constant.DragonConstant;
import com.trenska.longwang.dao.customer.CustomerMapper;
import com.trenska.longwang.dao.financing.ReceiptMapper;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.financing.Receipt;
import com.trenska.longwang.entity.indent.Indent;
import com.trenska.longwang.entity.indent.IndentDetail;
import com.trenska.longwang.enums.IndentStat;
import com.trenska.longwang.model.indent.IndentInfoModel;
import com.trenska.longwang.model.indent.IndentNoCustIdNameModel;
import com.trenska.longwang.model.prints.WebPrintModel;
import com.trenska.longwang.model.sys.CommonResponse;
import com.trenska.longwang.service.customer.ICustomerService;
import com.trenska.longwang.service.financing.IReceiptService;
import com.trenska.longwang.service.impl.financing.ReceiptService;
import com.trenska.longwang.service.indent.IIndentService;
import com.trenska.longwang.service.stock.IStockDetailService;
import com.trenska.longwang.service.stock.IStockService;
import com.trenska.longwang.util.*;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * @author Owen
 * @since 2019-04-22
 */
@RestController
@RequestMapping("/indent")
@Api(description = "订货单接口")
@CrossOrigin
@Slf4j
@SuppressWarnings("all")
public class IndentController {
	@Value("${template.path}")
	private String templatePath;

	@Autowired
	private IIndentService indentService;

	@Autowired
	private ICustomerService customerService;

	@Autowired
	private IReceiptService iReceiptService;

	@Autowired
	private ReceiptMapper receiptMapper;

	@Autowired
	private IStockService stockService;

	@Autowired
	private IStockDetailService stockDetailService;

	@Autowired
	private CustomerMapper customerMapper;

	@Autowired
	private ReceiptService receiptService;

	@CheckDuplicateSubmit
	@ApiImplicitParams({
			@ApiImplicitParam(name = "empId", value = "制单人id", paramType = "body", dataType = "string", required = true),
			@ApiImplicitParam(name = "salesmanId", value = "业务员id", paramType = "body", dataType = "string", required = true),
			@ApiImplicitParam(name = "shipway", value = "送货方式", paramType = "body", dataType = "string", required = true),
			@ApiImplicitParam(name = "payway", value = "支付方式", paramType = "body", dataType = "string", required = true),
			@ApiImplicitParam(name = "custId", value = "客户id", paramType = "body", dataType = "string", required = true),
			@ApiImplicitParam(name = "indentRemarks", value = "订货单备注", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "indentDetails", value = "订货单详情", paramType = "body", dataType = "list")
	})
	@PostMapping("/add")
	@ApiOperation("添加订货单")
	public CommonResponse addIndent(@RequestBody Indent indent) {
		return indentService.saveIndent(indent);
	}

	/**
	 * 审核订货单
	 *
	 * @param indentNo
	 * @return
	 */
	@CheckDuplicateSubmit
	@PostMapping("/confirm/{indentNo}/{custId}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indentNo", value = "订货单No", paramType = "path", dataType = "string"),
			@ApiImplicitParam(name = "custId", value = "客户id", paramType = "path", dataType = "int")
	})
	@ApiOperation("审核订货单")
	public CommonResponse confirmIndent(@PathVariable("indentNo") String indentNo, @PathVariable("custId") Integer custId) {
		/**
		 * 增加客户额度判断，超过额度将不能审核
		 */
		CommonResponse commonResponse = CustomerUtil.checkDebtLimit(indentNo, custId);
		if (commonResponse.getSucc() == false) {
			return commonResponse;
		}
		return indentService.confirmIndent(indentNo);
	}

	/**
	 * 财务审核订货单
	 *
	 * @param indentId
	 * @return
	 */
	@CheckDuplicateSubmit
	@PostMapping("/audit/{indentId}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indentId", value = "订货单Id", paramType = "path", dataType = "long"),
			@ApiImplicitParam(name = "auditRemarks", value = "财审备注", paramType = "query", dataType = "string")
	})
	@ApiOperation("财务审核订货单")
	public CommonResponse auditIndent(@PathVariable("indentId") Long indentId, String auditRemarks) {
		return indentService.auditIndentById(indentId, auditRemarks);
	}

	/**
	 * 修改订货单的财审备注
	 *
	 * @param indentNo
	 * @return
	 */
	@CheckDuplicateSubmit
	@PostMapping("/auditRemarks/update/{indentNo}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indentNo", value = "订货单编号", paramType = "path", dataType = "string"),
			@ApiImplicitParam(name = "auditRemarks", value = "财审备注", paramType = "query", dataType = "string")
	})
	@ApiOperation("修改订货单的财审备注")
	public CommonResponse updateAuditRemarks(@PathVariable("indentNo") String indentNo, String auditRemarks) {
		return indentService.updateAuditRemarks(indentNo, auditRemarks);
	}

	/**
	 * 取消订货单 待审核的订货单才可取消
	 *
	 * @param indentNo 订货单编号
	 * @return
	 */
	@CheckDuplicateSubmit
	@PostMapping("/cancel/{indentNo}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indentNo", value = "订货单编号", paramType = "path", dataType = "string")
	})
	@ApiOperation("取消订货单")
	public CommonResponse cancelIndentByNo(@PathVariable("indentNo") String indentNo) {

		if (StringUtils.isEmpty(indentNo)) {
			return CommonResponse.getInstance().succ(false).msg(DragonConstant.INVALID_INDENT);
		}
		return indentService.cancelIndentByNo(indentNo);
	}

	/**
	 * 获取订货单商品扣点信息
	 * @param size
	 * @param current
	 * @param endTime
	 * @param beginTime
	 * @return
	 */
	@GetMapping("/discount/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "beginTime", value = "开始时间", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "结束时间", paramType = "query", dataType = "string")
	})
	@ApiOperation("扣点信息")
	public CommonResponse getDiscounts(
			@PathVariable(value = "size") Integer size,
			@PathVariable(value = "current") Integer current,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "beginTime") String beginTime
	) {
		Map<String, Object> params = new HashMap<>();
		params.put("endTime", endTime);
		params.put("beginTime", beginTime);
		Page page = new Page(current, size);
		Page<String> pageInfo = indentService.getDiscounts(page, params);
		return CommonResponse.getInstance().succ(true).page(pageInfo);
	}

	/**
	 * 撤销审核订货单 ->"已作废"、"已取消"状态的订单不能“撤销审核”
	 *
	 * @param indentId 订货单id
	 * @return
	 */
	@PostMapping("/repeal/{indentId}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indentId", value = "订货单id", paramType = "path", dataType = "long"),
	})
	@ApiOperation("撤销审核订货单")
	public CommonResponse repealIndentByNo(@PathVariable("indentId") Long indentId) {
		if (NumberUtil.isLongNotUsable(indentId)) {
			return CommonResponse.getInstance().succ(false).msg("无此订货单信息");
		}
		return indentService.repealIndent(indentId);
	}

	/**
	 * 订货单出库
	 * 	1.增加客户欠款
	 * 	2.减少库存
	 *
	 * @return
	 */
	@PostMapping("/stockout")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indent", value = "订货单", paramType = "body", dataType = "Indent")
	})
	@ApiOperation("订货单出库")
	@CheckDuplicateSubmit
	public CommonResponse stockoutIndent(@RequestBody Indent indent) {
		if (indent == null) {
			return CommonResponse.getInstance().succ(false).msg("不存在该订货单信息，不能出库！");
		}

		List<IndentDetail> indentDetails = indent.getIndentDetails();
		if (CollectionUtils.isEmpty(indentDetails)) {
			return CommonResponse.getInstance().succ(false).msg("请输入订货单的商品信息！");
		}

		if (!NumberUtil.isIntegerUsable(indent.getCustId())) {
			return CommonResponse.getInstance().succ(false).msg("出库失败:请输入有效的客户信息！");
		}
		Long indentId = indent.getIndentId();
		if (NumberUtil.isLongNotUsable(indentId) || StringUtils.isEmpty(indent.getIndentNo())) {
			return CommonResponse.getInstance().succ(false).msg("出库失败:订单关键信息不能为空！");
		}
		return indentService.stockoutIndent(indent);
	}

	/**
	 * 作废订货单的出库单
	 *
	 * @return
	 */
	@PostMapping("/stockout/invalid/{indentNo}/{stockNo}")
	@ApiOperation("作废订货单的出库单")
	public CommonResponse validStockout(@PathVariable("indentNo") String indentNo, @PathVariable("stockNo") String stockNo) {
		return null;
	}

	@PutMapping("/invalid/receipt/{receiptId}")
	@ApiOperation("作废订货单的收款单")
	public CommonResponse invalidReceiptById(@PathVariable("receiptId") Long receiptId) {
		if (NumberUtil.isLongNotUsable(receiptId)) {
			return CommonResponse.getInstance().succ(false).msg(DragonConstant.INVALID_RECEIPT);
		}
		Receipt receipt = iReceiptService.getById(receiptId);

		if (Objects.isNull(receipt)) {
			return CommonResponse.getInstance().succ(false).msg(DragonConstant.INVALID_RECEIPT);
		}
		String busiNo = receipt.getBusiNo();
		if (StringUtils.isEmpty(busiNo)) {
			return CommonResponse.getInstance().succ(false).msg(DragonConstant.INVALID_INDENT);
		}
		return indentService.cancelReceipt(receipt);
	}

	@PutMapping("/invalid/pay/{receiptId}")
	@ApiOperation("作废订货单的付款单")
	public CommonResponse cancelPayReceiptById(@PathVariable("receiptId") Long receiptId) {
		boolean notUsable = NumberUtil.isLongNotUsable(receiptId);
		if (notUsable) {
			return CommonResponse.getInstance().succ(false).msg(DragonConstant.INVALID_PAY_RECEIPT);
		}
		Receipt pay = iReceiptService.getById(receiptId);

		boolean isPayNull = Objects.isNull(pay);
		if (isPayNull) {
			return CommonResponse.getInstance().succ(false).msg(DragonConstant.INVALID_PAY_RECEIPT);
		}
		String busiNo = pay.getBusiNo();
		if (StringUtils.isEmpty(busiNo)) {
			return CommonResponse.getInstance().succ(false).msg(DragonConstant.INVALID_INDENT);
		}
		return indentService.cancelPayReceipt(pay);
	}

	/**
	 * 订货单收款，一次多个收款
	 *
	 * @param receipt 收款单
	 * @return
	 */
	@PostMapping("/charge")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "receipt", value = "收款单", paramType = "body", dataType = "Receipt")
	})
	@ApiOperation("订货单收款")
	@CheckDuplicateSubmit
	public CommonResponse chargeIndent(@RequestBody @ApiParam Receipt receipt) {
		if (receipt == null) {
			return CommonResponse.getInstance().succ(false).msg("无效的收款单！");
		}
		if (StringUtils.isEmpty(receipt.getBusiNo())) {
			return CommonResponse.getInstance().succ(false).msg("无效的订货单！");
		}
		return receiptService.saveReceipt(receipt, DragonConstant.DHD_CHINESE, DragonConstant.SK_CHINESE);
	}

	/********************************************** 订单付款一次多个付款 **********************************************/
	@PostMapping("/pay")
	@ApiOperation("订货单付款")
	@CheckDuplicateSubmit
	public CommonResponse payIndent(@ApiParam(name = "pay", value = "付款单", type = "Receipt") @RequestBody Receipt pay) {
		if (pay == null) {
			return CommonResponse.getInstance().succ(false).msg("无效的付款单！");
		}
		if (StringUtils.isEmpty(pay.getBusiNo())) {
			return CommonResponse.getInstance().succ(false).msg("无效的订货单！");
		}
		return receiptService.saveReceipt(pay, DragonConstant.DHD_CHINESE, DragonConstant.FK_CHINESE);
	}

	/**
	 * 作废订货单 已收款并且已出库的订单才可以作废
	 *
	 * @param indentId 订货单ID
	 * @return
	 */
	@PostMapping("/invalid/{indentId}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indentId", value = "订货单编号", paramType = "body", dataType = "string")
	})
	@ApiOperation("作废订货单")
	@CheckDuplicateSubmit
	public CommonResponse invalid(@PathVariable("indentId") Long indentId) {
		if (!NumberUtil.isLongUsable(indentId)) {
			return CommonResponse.getInstance().succ(false).msg("不存在该订货单信息");
		}
		Indent indent = indentService.getById(indentId);
		if (indent == null) {
			return CommonResponse.getInstance().succ(false).msg("不存在该订货单信息");
		}
		if (!IndentStat.FINISHED.getName().equals(indent.getStat())) {
			return CommonResponse.getInstance().succ(false).msg("未完成的订货单不能作废");
		}

		/**
		 * 	 1.作废收款单
		 * 	 	1.1 客户欠款增加
		 * 	 	1.2 记录交易明细
		 * 	 2. 作废出库单
		 * 	 	2.1 作废出库单，增加作废记录
		 * 	 	2.2 还回库存
		 * 	 3. 作废订货单
		 * 	 	3.1 增加一条欠款明细，金额是订单金额
		 * 	 	3.2 客户欠款减少，金额是订单金额
		 */
		return indentService.invalidIndent(indent);
	}

	@PutMapping("/update")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indentId", value = "订货单id", paramType = "body", dataType = "string", required = true),
			@ApiImplicitParam(name = "salesmanId", value = "业务员id", paramType = "body", dataType = "string", required = true),
			@ApiImplicitParam(name = "shipway", value = "送货方式", paramType = "body", dataType = "string", required = true),
			@ApiImplicitParam(name = "payway", value = "支付方式", paramType = "body", dataType = "string", required = true),
			@ApiImplicitParam(name = "orderSrc", value = "订单来源", paramType = "body", dataType = "string", required = true),
			@ApiImplicitParam(name = "indentRemarks", value = "订货单备注", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "indentDetails", value = "订货单详情", paramType = "body", dataType = "list")
	})
	@ApiOperation("修改订货单信息")
	public CommonResponse updateIndent(@RequestBody Indent indent) {
		if (Objects.isNull(indent) || NumberUtil.isLongNotUsable(indent.getIndentId())) {
			return CommonResponse.getInstance().succ(false).msg("无效的订货单信息!");
		}
		if (indent.getIndentDetails().isEmpty()) {
			return CommonResponse.getInstance().succ(false).msg("无效的商品信息!");
		}
		return indentService.updateIndent(indent);
	}

	@PutMapping("/change")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indentId", value = "订货单id", paramType = "body", dataType = "string", required = true),
			@ApiImplicitParam(name = "salesmanId", value = "业务员id", paramType = "body", dataType = "string", required = true),
			@ApiImplicitParam(name = "shipway", value = "送货方式", paramType = "body", dataType = "string", required = true),
			@ApiImplicitParam(name = "payway", value = "支付方式", paramType = "body", dataType = "string", required = true),
			@ApiImplicitParam(name = "orderSrc", value = "订单来源", paramType = "body", dataType = "string", required = true),
			@ApiImplicitParam(name = "indentRemarks", value = "订货单备注", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "indentDetails", value = "订货单详情", paramType = "body", dataType = "list")
	})
	@ApiOperation("核定修改订货单")
	@CheckDuplicateSubmit
	public CommonResponse changeIndent(@RequestBody Indent indent) {
		if (indent == null) {
			return CommonResponse.getInstance().succ(false).msg("无效的订单信息!");
		}
		if (indent.getIndentDetails().isEmpty()) {
			return CommonResponse.getInstance().succ(false).msg("无效的商品信息!");
		}
		return indentService.changeIndent(indent);
	}

	@GetMapping("/list/page/search/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "custId", value = "客户id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "endTime", value = "结束时间", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "custName", value = "客户名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "areaGrpId", value = "区域分组id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "empName", value = "销售员姓名", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "beginTime", value = "开始时间", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "indentType", value = "订货/退货", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "indentNo", value = "订货/退货单号", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "receiptStat", value = "订货单状态：待收款、已收款", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "iouStatus", value = "交账状态->true : 已交账 ; false : 未交账", paramType = "query", dataType = "boolean"),
			@ApiImplicitParam(name = "auditStat", value = "财审状态->true : 已财审 ; false : 未财审", paramType = "query", dataType = "boolean"),
			@ApiImplicitParam(name = "stat", value = "订货单状态：待审核、待出库、待出库、已完成、已取消、已作废", paramType = "query",	dataType = "string"),
	})
	@ApiOperation("订货/退货单通用分页")
	public CommonResponse<Indent> listIndentPage(
			@PathVariable(value = "size") Integer size,
			@PathVariable(value = "current") Integer current,
			@RequestParam(value = "indentType") String indentType,
			@RequestParam(required = false, name = "stat") String stat,
			@RequestParam(required = false, name = "custId") Integer custId,
			@RequestParam(required = false, name = "empName") String empName,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "shipway") String shipway,
			@RequestParam(required = false, name = "indentNo") String indentNo,
			@RequestParam(required = false, name = "custName") String custName,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@RequestParam(required = false, name = "areaGrpId") Integer areaGrpId,
			@RequestParam(required = false, name = "auditStat") Boolean auditStat,
			@RequestParam(required = false, name = "iouStatus") Boolean iouStatus,
			@RequestParam(required = false, name = "receiptStat") String receiptStat
	) {
		Page page = new Page(current, size);
		Map<String, Object> params = new HashMap<>();
		params.put("stat", stat);
		params.put("custId", custId);
		params.put("shipway", shipway);
		params.put("endTime", endTime);
		params.put("empName", empName);
		params.put("custName", custName);
		params.put("indentNo", indentNo);
		params.put("areaGrpId", areaGrpId);
		params.put("iouStatus", iouStatus);
		params.put("auditStat", auditStat);
		params.put("beginTime", beginTime);
		params.put("indentType", indentType);
		params.put("receiptStat", receiptStat);

		Page<Indent> pageInfo = indentService.getIndentPageSelective(params, page);
		return CommonResponse.getInstance().page(pageInfo);
	}

	@GetMapping("/info/{indentNo}")
	@ApiOperation("获取订货单信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indentNo", value = "订货单编号", paramType = "path", dataType = "string")
	})
	public Indent getIndentInfo(@PathVariable("indentNo") String indentNo) {
		return indentService.getIndentInfo(indentNo);
	}

	@DeleteMapping("/delete/{indentId}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indentId", value = "订货单id", paramType = "path", dataType = "long")
	})
	@ApiOperation("删除订货单")
	@CheckDuplicateSubmit
	public CommonResponse deleteIndentById(@ApiParam(name = "indentId", required = true) @PathVariable("indentId") Long indentId) {
		if (!NumberUtil.isLongUsable(indentId)) {
			return CommonResponse.getInstance().succ(false).msg("删除订货单失败 : 不存在此订货单");
		}
		return indentService.removeIndentById(indentId, DragonConstant.DHD_CHINESE);
	}

	@DeleteMapping("/delete/batch")
	@ApiOperation("批量删除订货单")
	@CheckDuplicateSubmit
	public CommonResponse batchDeleteIndentByIds(
			@ApiParam(name = "indentIds", value = "需要批量删除的订货单编号集合/数组", required = true)
			@RequestParam(value = "indentIds") Collection<Long> indentIds
	) {
		if (indentIds == null || indentIds.isEmpty()) {
			return CommonResponse.getInstance().succ(false).msg("删除订货单失败 : 不存在此订货单");
		}
		return indentService.removeIndentByIds(indentIds, DragonConstant.DHD_CHINESE);
	}

	@GetMapping("/goods/{custId}/{goodsId}")
	@ApiOperation("通过用户id、商品id获取商品的订货单详情")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "custId", value = "客户id", paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "goodsId", value = "商品id", paramType = "path", dataType = "int")
	})
	public IndentInfoModel getIndentInfoModelInfo(
			@PathVariable("custId") Integer custId,
			@PathVariable("goodsId") Integer goodsId
	) {
		return indentService.getIndentInfoModel(custId, goodsId);
	}

	@GetMapping("/iou/{indentId}")
	@ApiOperation("增加或更新欠条")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indentId", value = "订货单id", paramType = "path", dataType = "long"),
			@ApiImplicitParam(name = "iouRemarks", value = "备注", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "iouAmnt", value = "欠条金额", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "iouTime", value = "回款时间", paramType = "query", dataType = "string")
	})
	@CheckDuplicateSubmit
	public CommonResponse addOrUpdateIou(@PathVariable("indentId") Long indentId, String iouAmnt, String iouTime, String iouRemarks) {
		if (NumberUtil.isLongNotUsable(indentId)) {
			return CommonResponse.getInstance().succ(false).msg(DragonConstant.INVALID_INDENT).data(null);
		}
		return indentService.addOrUpdateIou(indentId, iouAmnt, iouTime, iouRemarks);
	}

	@GetMapping("/iou/{indentId}/{iouAmnt}")
	@ApiOperation("增加欠条")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indentId", value = "订货单id", paramType = "path", dataType = "long"),
			@ApiImplicitParam(name = "iouAmnt", value = "欠条金额", paramType = "path", dataType = "string")
	})
	@CheckDuplicateSubmit
	public CommonResponse addIou(@PathVariable("indentId") Long indentId, @PathVariable("iouAmnt") String iouAmnt) {
		return indentService.addIou(indentId, iouAmnt);
	}

	@GetMapping("/list/indentNos/{current}/{size}")
	public PageHelper<String> findIndentNo(
			@PathVariable("current") Integer current,
			@PathVariable("size") Integer size,
			String str
	) {
		Page page = new Page(current, size);
		Page<String> pageInfo = indentService.getIndentNosSelective(page, str);
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/list/indentNoCustName/{current}/{size}")
	public PageHelper<IndentNoCustIdNameModel> findIndentNoCustIdName(
			@PathVariable("current") Integer current,
			@PathVariable("size") Integer size,
			String str
	) {
		Page page = new Page(current, size);
		Page<IndentNoCustIdNameModel> pageInfo = indentService.getIndentNoCustNameSelective(page, str);
		return PageHelper.getInstance().pageData(pageInfo);
	}

	/**
	 * http://localhost:80/indent/exportDhdPDF/2
	 *
	 * @param
	 * @param response
	 * @return
	 */
	@ApiOperation(value = "导出订货单为PDF")
	@RequestMapping(value = "/exportDhdPDF/{indentId}", method = RequestMethod.GET, produces = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> exportDhdPDF(@PathVariable Long indentId, HttpServletResponse response) {

		HttpHeaders headers = new HttpHeaders();

		/**
		 * 数据导出(PDF 格式)
		 */

		Map<String, Object> params = indentService.pdfAndPrint(indentId);
		if (null == params || params.isEmpty()) {
			return new ResponseEntity<String>("{ \"succ\" : \"false\", \"msg\" : \"获取数据失败\" }",
					headers, HttpStatus.NOT_FOUND);
		}

		String htmlStr = "";
		//通过是否有送货人判断是否出库   未出库
		if (params.containsKey("shipMan")) {
			htmlStr = PdfUtil.freemarkerRender(params, templatePath + File.separator + "dhdpdftpl/dhd.ftl");
		} else {
			htmlStr = PdfUtil.freemarkerRender(params, templatePath + File.separator + "xsdd/xsd.ftl");
		}
		log.info("dhd = " + htmlStr);

		byte[] pdfBytes = PdfUtil.createPDF(htmlStr, templatePath + File.separator + "simsun.ttc");
		if (pdfBytes != null && pdfBytes.length > 0) {
			String fileName = null;
			try {
				fileName = new String(("订货单-" + params.get("dhdNo")).getBytes("gb2312"), "ISO8859-1") + ".pdf";
			} catch (UnsupportedEncodingException e) {
				return new ResponseEntity<String>("{ \"succ\" : \"false\", \"msg\" : \"导出失败\" }",
						headers, HttpStatus.NOT_FOUND);
			}
			headers.setContentDispositionFormData("attachment", fileName);
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			return new ResponseEntity<byte[]>(pdfBytes, headers, HttpStatus.OK);
		}

		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		return new ResponseEntity<String>("{ \"succ\" : \"false\", \"msg\" : \"导出失败\" }",
				headers, HttpStatus.NOT_FOUND);

	}

	/**
	 * http://localhost:80/indent/printDhd/12
	 *
	 * @param indentId
	 * @return
	 */
	@ApiOperation(value = "打印订货单")
	@RequestMapping(value = "/printDhd/{indentId}", method = RequestMethod.GET)
	public CommonResponse printDhd(@PathVariable Long indentId) {
		try {

			Map<String, Object> params = indentService.pdfAndPrint(indentId);
			if (null == params || params.isEmpty()) {
				return CommonResponse.getInstance().succ(false).msg("获取数据失败");
			}

			String htmlStr = "";
			//通过是否有送货人判断是否出库   未出库
			if (params.containsKey("shipMan")) {
				htmlStr = PdfUtil.freemarkerRender(params, templatePath + File.separator + "dhdpdftpl/dhd.ftl");
			} else {
				htmlStr = PdfUtil.freemarkerRender(params, templatePath + File.separator + "xsdd/xsd.ftl");
			}

			WebPrintModel wm = PrintSingleton.INSTNACE.getInstance().retOk(htmlStr, "24.1", "13");

			return CommonResponse.getInstance().succ(true).data(wm);
		} catch (Exception e) {
			return CommonResponse.getInstance().succ(false).msg(e.getMessage());
		}
	}

	/**
	 * http://localhost:80/indent/exportThdPDF/11
	 *
	 * @param indentId 退回单ID
	 * @return
	 */
	@ApiOperation(value = "导出退货单为PDF")
	@RequestMapping(value = "/exportThdPDF/{indentId}", method = RequestMethod.GET)
	public ResponseEntity<?> exportThdPDF(@PathVariable Long indentId) {

		Map<String, Object> params = indentService.pdfAndPrintThd(indentId);
		HttpHeaders headers = new HttpHeaders();

		if (null == params || params.isEmpty()) {
			return new ResponseEntity<String>("{ \"succ\" : \"false\", \"msg\" : \"获取数据失败\" }",
					headers, HttpStatus.NOT_FOUND);
		}
		String htmlStr = PdfUtil.freemarkerRender(params, templatePath + File.separator + "xsthd/tpl.ftl");
		log.info("thd = " + htmlStr);
		byte[] pdfBytes = PdfUtil.createPDF(htmlStr, templatePath + File.separator + "simsun.ttc");
		if (pdfBytes != null && pdfBytes.length > 0) {
			String fileName = null;
			try {
				fileName = new String(("退货单-" + params.get("thdNo")).getBytes("gb2312"), "ISO8859-1") + ".pdf";
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			headers.setContentDispositionFormData("attachment", fileName);
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			return new ResponseEntity<byte[]>(pdfBytes, headers, HttpStatus.OK);
		}

		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		return new ResponseEntity<String>("{ \"code\" : \"404\", \"message\" : \"not found\" }",
				headers, HttpStatus.NOT_FOUND);
	}

	@ApiOperation(value = "打印退货单")
	@RequestMapping(value = "/printThd/{indentId}", method = RequestMethod.GET)
	public CommonResponse printThd(@PathVariable Long indentId) {

		try {
			Map<String, Object> params = indentService.pdfAndPrintThd(indentId);
			if (null == params || params.isEmpty()) {
				return CommonResponse.getInstance().succ(false).msg("获取数据失败");
			}
			String htmlStr = PdfUtil.freemarkerRender(params, templatePath + File.separator + "xsthd/tpl.ftl");
			WebPrintModel wm = PrintSingleton.INSTNACE.getInstance().retOk(htmlStr, "21", "29.7");
			return CommonResponse.getInstance().succ(true).data(wm);
		} catch (Exception e) {
			return CommonResponse.getInstance().succ(false).msg(e.getMessage());
		}
	}
}