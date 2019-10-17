package com.trenska.longwang.model.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 2019/6/3
 * 创建人:Owen
 */
@Data
@ApiModel("订单统计")
public class IndentStatisticsModel {
	@ApiModelProperty("时间")
	private String statisticsTime;
	@ApiModelProperty("订货单数")
	private String orderNum ="0";
	@ApiModelProperty("退货单数")
	private String returnNum = "0";
	@ApiModelProperty("订货金额")
	private String orderAmnt = "0";
	@ApiModelProperty("退货金额")
	private String returnAmnt = "0";
	@ApiModelProperty("订货客户数")
	private String orderCustNum = "0";
	@ApiModelProperty("退货客户数")
	private String returnCustNum = "0";
	@ApiModelProperty("金额合计=定货金额-退货金额")
	private String total = "0";
}
