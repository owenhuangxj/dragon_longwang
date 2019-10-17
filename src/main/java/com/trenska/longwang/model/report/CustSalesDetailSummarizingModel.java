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
@NoArgsConstructor
@ApiModel("客户销售明细统计模型")
public class CustSalesDetailSummarizingModel {

	@ApiModelProperty("销售数量合计")
	private String salesNumSum = "0";

	@ApiModelProperty("销售金额合计")
	private String salesAmntSum = "0";

	@ApiModelProperty("实收金额合计")
	private String receivableAmntSum = "0";

	@ApiModelProperty("优惠金额合计")
	private String salesDiscountSum = "0";

	public CustSalesDetailSummarizingModel(String salesNumSum, String salesAmntSum, String receivableAmntSum) {
		this.salesNumSum = salesNumSum;
		this.salesAmntSum = salesAmntSum;
		this.receivableAmntSum = receivableAmntSum;
	}
}
