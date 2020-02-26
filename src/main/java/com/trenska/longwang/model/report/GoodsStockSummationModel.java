package com.trenska.longwang.model.report;
import com.trenska.longwang.constant.Constant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 2019/8/6
 * 创建人:Owen
 */
@Data
@ApiModel("进出库数量汇总合计模型")
public class GoodsStockSummationModel {
	@ApiModelProperty("期初库存合计")
	private String initStockSum = "0";
	@ApiModelProperty("生产入库合计")
	private String makeInSum = "0";
	@ApiModelProperty("采购入库合计")
	private String purchaseInSum = "0";
	@ApiModelProperty("退货出库合计")
	private String returnsOutSum = "0";
	@ApiModelProperty("退货入库合计")
	private String returnsInSum = "0";
	@ApiModelProperty("销售出库合计")
	private String salesOutSum = "0";
	@ApiModelProperty("其他入库合计")
	private String otherInSum = "0";
	@ApiModelProperty("其他出库合计")
	private String otherOutSum = "0";
	@ApiModelProperty("报溢入库合计")
	private String overflowSum = "0";
	@ApiModelProperty("报损出库合计")
	private String breakageSum = "0";
	@ApiModelProperty("期末库存合计")
	private String overStockSum = "0";
}
