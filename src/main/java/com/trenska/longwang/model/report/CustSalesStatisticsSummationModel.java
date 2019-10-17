package com.trenska.longwang.model.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 2019/6/11
 * 创建人:Owen
 */
@Data
@ApiModel("客户销售统计合计部分模型")
@NoArgsConstructor
public class CustSalesStatisticsSummationModel {

	@ApiModelProperty("销售数量合计")
	private String salesNumSum;

	@ApiModelProperty("销售金额合计")
	private String salesAmntSum;

	@ApiModelProperty("实收金额合计")
	private String indentTotalSum;

	@ApiModelProperty("优惠金额合计")
	private String discountAmntSum;

}
