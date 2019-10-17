package com.trenska.longwang.model.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 2019/6/17
 * 创建人:Owen
 */
@Data
@NoArgsConstructor
@ApiModel("合计通用模型")
public class CommonSummation {

	@ApiModelProperty("数量合计")
	private String salesNumSum;

	@ApiModelProperty("销售金额合计")
	private String salesAmntSum;

	@ApiModelProperty("优惠金额合计")
	private String discountAmntSum;

	@ApiModelProperty("实收合计")
	private String indentTotalSum;
}
