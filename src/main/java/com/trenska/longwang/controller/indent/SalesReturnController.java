package com.trenska.longwang.controller.indent;

import com.trenska.longwang.annotation.CheckDuplicateSubmit;
import com.trenska.longwang.constant.Constant;
import com.trenska.longwang.entity.indent.Indent;
import com.trenska.longwang.enums.IndentStat;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.indent.IIndentService;
import com.trenska.longwang.util.NumberUtil;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * @author Owen
 * @since 2019-05-15
 */
@RestController
@RequestMapping("/sales/return")
@Api(description = "退货单接口")
@CrossOrigin
@Slf4j
public class SalesReturnController {

	@Autowired
	private IIndentService indentService;

	@ApiImplicitParams({
			@ApiImplicitParam(name = "empId",value = "制单人id",paramType = "body",dataType = "string",required = true),
			@ApiImplicitParam(name = "salesmanId",value = "业务员id",paramType = "body",dataType = "string",required = true),
			@ApiImplicitParam(name = "custId",value = "客户id",paramType = "body",dataType = "string",required = true),
			@ApiImplicitParam(name = "indentRemarks",value = "退货单备注",paramType = "body",dataType = "string"),
			@ApiImplicitParam(name = "indentDetails",value = "退货单详情",paramType = "body",dataType = "list")
	})
	@PostMapping("/add")
	@CheckDuplicateSubmit
	@ApiOperation("添加退货单")
	public ResponseModel addSalesReturn(@RequestBody Indent indent){
		if(null == indent){
			return ResponseModel.getInstance().succ(false).msg("无此退货单.");
		}
		return indentService.saveSalesReturn(indent);
	}

	/**
	 * 作废退货单
	 * @param indentId 退货单ID
	 * @return
	 */
	@CheckDuplicateSubmit
	@PostMapping("/invalid/{indentId}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indentId", value = "退货单id", paramType = "body", dataType = "string")
	})
	@ApiOperation("作废退货单")
	public ResponseModel invalid(@PathVariable("indentId") Integer indentId) {
		Indent indent = indentService.getById(indentId);
		if(indent == null ) {
			return ResponseModel.getInstance().succ(false).msg("不存在该退货单信息");
		}else if(!IndentStat.FINISHED.getName().equals(indent.getStat())){
			return ResponseModel.getInstance().succ(false).msg("已完成的退货单才能作废");
		}
		return indentService.invalidSalseReturn(indent);
	}

	@CheckDuplicateSubmit
	@DeleteMapping("/delete/{indentId}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indentId", value = "退货单id", paramType = "path", dataType = "int")
	})
	@ApiOperation("删除退货单")
	public ResponseModel deleteIndentById(@ApiParam(name = "indentId", required = true) @PathVariable("indentId") Long indentId) {
		if(!NumberUtil.isLongUsable(indentId)){
			return ResponseModel.getInstance().succ(false).msg("删除退货单失败 : 不存在此退货单");
		}
		return indentService.removeIndentById(indentId,Constant.THD_CHINESE);
	}

	@CheckDuplicateSubmit
	@DeleteMapping("/delete/batch")
	@ApiOperation("批量删除退货单")
	public ResponseModel batchDeleteReturnSales(@ApiParam(name = "indentIds", value = "需要批量删除的退货单编号集合/数组", required = true) @RequestParam(value = "indentIds") Collection<Long> indentIds) {
		if(indentIds == null || indentIds.isEmpty()){
			return ResponseModel.getInstance().succ(false).msg("删除退货单失败 : 包含不存在的退货单");
		}
		return indentService.removeIndentByIds(indentIds, Constant.THD_CHINESE);
	}
}