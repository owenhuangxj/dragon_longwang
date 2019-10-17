package com.trenska.longwang.model.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 2019/6/24
 * 创建人:Owen
 */
@Data
@ApiModel("客户销售总账合计模型")
@NoArgsConstructor
public class CustSalesSummationModel {

	@ApiModelProperty("销售数量合计")
	private String salesNumSum = "0";
	@ApiModelProperty("销售合计")
	private String salesAmntSum = "0";
	@ApiModelProperty( "销售实收合计")
	private String receivedAmntSum = "0";
	@ApiModelProperty( "销售应收合计")
	private String receivableAmntSum = "0";
	@ApiModelProperty( "销售优惠合计")
	private String salesDiscountSum = "0";

	public CustSalesSummationModel(String salesNumSum, String salesAmntSum, String salesDiscountSum) {
		this.salesNumSum = salesNumSum;
		this.salesAmntSum = salesAmntSum;
		this.salesDiscountSum = salesDiscountSum;
	}
}
