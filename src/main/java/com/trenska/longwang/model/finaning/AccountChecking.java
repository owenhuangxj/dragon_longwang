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
@ApiModel("对账单模型")
public class AccountChecking {
	@ApiModelProperty("客户ID")
	private Integer custId;
	@ApiModelProperty("客户编号")
	private String custNo;
	@ApiModelProperty("客户名称")
	private String  custName;
	@ApiModelProperty("客户期初欠款")
	private String initDebt;
	@ApiModelProperty("销售应收")
	private String salesAmount;
	@ApiModelProperty("已收")
	private String receivedAmount;
	@ApiModelProperty("已付")
	private String payedAmount;
	@ApiModelProperty("待收欠款")
	private String debtAmount;
}
