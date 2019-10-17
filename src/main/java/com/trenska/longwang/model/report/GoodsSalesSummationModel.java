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
@ApiModel("商品销售汇总合计模型")
@NoArgsConstructor
public class GoodsSalesSummationModel {
	@ApiModelProperty("销售金额合计")
	private String salesAmntSum;
	@ApiModelProperty("销售数量合计")
	private String salesNumSum;

	public GoodsSalesSummationModel(String salesAmntSum, String salesNumSum) {
		this.salesAmntSum = salesAmntSum;
		this.salesNumSum = salesNumSum;
	}
}
