package com.trenska.longwang.model.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Owen
 * @since 2019-06-11
 */
@Data
@ApiModel("商品销售订货单明细Excel模型")
@NoArgsConstructor
public class SingleGoodsSalesIndentDetailExcelModel implements Serializable {

	/**
	 * 冗余的字段，excel导出时需要显示
	 */
	@ApiModelProperty("产品编号")
	private String goodsNo;
	/**
	 * 冗余的字段，excel导出时需要显示
	 */
	@ApiModelProperty("产品名称")
	private String goodsName;

	@ApiModelProperty("客户名称")
	private String custName;

	@ApiModelProperty("订货单号")
	private String indentNo;

	@ApiModelProperty("销售日期")
	private String indentTime;

	@ApiModelProperty("销售数量")
	private String num;

	@ApiModelProperty("销售单价")
	private String price;

	@ApiModelProperty("销售金额")
	private String salesAmnt;

	@ApiModelProperty("扣点")
	private String discount;

	@ApiModelProperty("扣点金额")
	private String indentTotal;

	@ApiModelProperty("出库批次")
	private String madeDates;

	@ApiModelProperty("备注")
	private String remarks;

}
