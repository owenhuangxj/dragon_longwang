package com.trenska.longwang.model.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 2019/6/3
 * 创建人:Owen
 */
@Data
@ApiModel("订单统计合计")
public class IndentStatisticsSummationModel {

	@ApiModelProperty("订货单数合计")
	private String orderNumSum ="0";

	@ApiModelProperty("退货单数合计")
	private String returnNumSum = "0";

	@ApiModelProperty("订货客户数合计")
	private String orderCustNumSum = "0";

	@ApiModelProperty("退货客户数合计")
	private String returnCustNumSum = "0";

	@ApiModelProperty("订货金额合计")
	private String orderAmntSum = "0";

	@ApiModelProperty("退货金额合计")
	private String returnAmntSum = "0";

	@ApiModelProperty("待收金额")
	private String owedAmnt = "0";

	@ApiModelProperty("金额合计=定货金额-退货金额")
	private String total = "0";
}
