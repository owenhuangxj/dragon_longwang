package com.trenska.longwang.model.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * 2019/5/31
 * 创建人:Owen
 */
@Data
@ApiModel("客户销售统计")
@NoArgsConstructor
public class CustSalesStatisticsModel {

	@ApiModelProperty("商品id")
	private String goodsId;

	@ApiModelProperty("客户id")
	private String custId;

	@ApiModelProperty("商品编号")
	private String goodsNo;

	@ApiModelProperty("商品名称")
	private String goodsName;

	@ApiModelProperty("规格")
	private Set<String> propNames = new HashSet<>();

	@ApiModelProperty("单位")
	private String unitName;

	@ApiModelProperty("销售数量")
	private String salesNum;

	@ApiModelProperty("平均单价")
	private String avgPrice;

	@ApiModelProperty("销售金额")
	private String salesAmnt;

	@ApiModelProperty("优惠金额")
	private String discountAmount;

	@ApiModelProperty("实收金额")
	private String indentTotal;

}
