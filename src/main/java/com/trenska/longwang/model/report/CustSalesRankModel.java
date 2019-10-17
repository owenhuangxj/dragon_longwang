package com.trenska.longwang.model.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 2019/6/1
 * 创建人:Owen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("客户销售排名")
public class CustSalesRankModel {

	@ApiModelProperty("排名")
	private String rankNum;

	@ApiModelProperty("销售金额")
	private String salesAmnt;

	@ApiModelProperty("实收金额")
	private String indentTotal;

	@ApiModelProperty("优惠金额")
	private String discountTotal;

	@ApiModelProperty("销售数量")
	private String salesNum;

	@ApiModelProperty("客户id")
	private Integer custId;

	@ApiModelProperty("客户名称")
	private String custName;

}
