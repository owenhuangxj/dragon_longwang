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
@ApiModel("客户对账合计部分模型")
@NoArgsConstructor
public class AccountCheckingSummationModel {
	@ApiModelProperty("期初欠款合计")
	private String initDebtTotal = "0";
	@ApiModelProperty("销售应收合计/增加欠款合计")
	private String salesAmountTotal = "0";
	@ApiModelProperty("已收合计")
	private String receivedAmountTotal = "0";
	@ApiModelProperty("已付合计")
	private String payedAmountTotal = "0";
	/**
	 * 待收金额合计 = 期初欠款合计+销售应收合计-已收合计-已付合计
	 */
	@ApiModelProperty("待收金额合计")
	private String debtAmountTotal = "0";

	public AccountCheckingSummationModel(String initDebtTotal, String salesAmountTotal, String receivedAmountTotal, String payedAmountTotal, String debtAmountTotal) {
		this.initDebtTotal = initDebtTotal;
		this.salesAmountTotal = salesAmountTotal;
		this.receivedAmountTotal = receivedAmountTotal;
		this.payedAmountTotal = payedAmountTotal;
		this.debtAmountTotal = debtAmountTotal;
	}
}
