package com.trenska.longwang.model.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * 2019/5/30
 * 创建人:Owen
 */
@Data
@ApiModel("客户销售汇总")
@NoArgsConstructor
public class CustSalesSummarizingModel {
	private Integer custId;
	private String custName;
	private String goodsName;
	private String unitName;
	private String salesNum;
	private String avgPrice;

	@ApiModelProperty("实收金额")
	private String amount;

	@ApiModelProperty("销售金额")
	private String salesAmnt;

	@ApiModelProperty("优惠金额")
	private String discountAmnt;

	private Set<String> propNames = new HashSet<>();
}
