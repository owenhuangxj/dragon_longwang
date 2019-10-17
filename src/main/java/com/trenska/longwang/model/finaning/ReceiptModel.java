package com.trenska.longwang.model.finaning;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 2019/5/30
 * 创建人:Owen
 */
@Data
@ApiModel("收/付款模型")
@NoArgsConstructor
public class ReceiptModel {
	@ApiModelProperty("收/付款id")
	private Integer paywayId;
	@ApiModelProperty("金额")
	private String receiptAmount;
	@ApiModelProperty("方式")
	private String payway;

	public ReceiptModel(Integer paywayId, String receiptAmount) {
		this.paywayId = paywayId;
		this.receiptAmount = receiptAmount;
	}

	public ReceiptModel(Integer paywayId, String receiptAmount, String payway) {
		this.paywayId = paywayId;
		this.receiptAmount = receiptAmount;
		this.payway = payway;
	}
}
