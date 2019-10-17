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
public class DeliveryDetailsStaticsModel {

	@ApiModelProperty("送货日期")
	private String deliveryDate;

	@ApiModelProperty("订单号")
	private String indentNo;

	@ApiModelProperty("客户名称")
	private String custName;

	@ApiModelProperty("订单数量")
	private String num;

	@ApiModelProperty("订单金额")
	private String salesAmnt;

	@ApiModelProperty("实收金额")
	private String indentTotal;

	@ApiModelProperty("优惠金额")
	private String discountTotal;
}
