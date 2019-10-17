package com.trenska.longwang.model.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 2019/8/28
 * 创建人:Owen
 * 商品送货统计模型
 */
@Data
@ApiModel("商品送货统计")
public class DeliveryStaticsModel {

	@ApiModelProperty("送货人id")
	private Integer shipmanId;

	@ApiModelProperty("送货人")
	private String shipman;

	@ApiModelProperty("送货数量")
	private String num;

	@ApiModelProperty("订单金额/销售金额")
	private String salesAmnt;

	@ApiModelProperty("优惠总额")
	private String discountTotal;

	@ApiModelProperty("实收金额: 销售-优惠")
	private String indentTotal;
}