package com.trenska.longwang.model.finaning;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 2019/5/16
 * 创建人:Owen
 */
@Data
@NoArgsConstructor
@ApiModel("客户交易明细模型")
public class CustomerDealDetailModel {
	@ApiModelProperty("单据名称-单号")
	private String nameNo;
	@ApiModelProperty("时间")
	private String time;
	@ApiModelProperty("金额")
	private String amount;
	@ApiModelProperty("欠款余额")
	private String debt;
}
