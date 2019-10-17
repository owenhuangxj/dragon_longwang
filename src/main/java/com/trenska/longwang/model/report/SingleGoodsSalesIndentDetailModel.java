package com.trenska.longwang.model.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Owen
 * @since 2019-06-01
 */
@Data
@ApiModel("商品销售订货单明细")
@NoArgsConstructor
public class SingleGoodsSalesIndentDetailModel implements Serializable {

	@ApiModelProperty("商品id")
	private Integer goodsId;

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

	@ApiModelProperty("订货单号")
	private String indentNo;

	@ApiModelProperty("销售日期")
	private String indentTime;

	@ApiModelProperty("客户名称")
	private String custName;

	@ApiModelProperty("销售数量")
	private String num;

	@ApiModelProperty("销售单价")
	private String price;

	@ApiModelProperty("扣点")
	private String discount;

	@ApiModelProperty("优惠金额")
	private String discountAmount;

	@ApiModelProperty("销售金额")
	private String salesAmnt;

	@ApiModelProperty("扣除扣点的金额")
	private String indentTotal;

	@ApiModelProperty("备注")
	private String remarks;

	@ApiModelProperty("出库批次")
	private Set<SingleGoodsSalesIndentStockoutDetailModel> madeDates = new HashSet<>();

}
