package com.trenska.longwang.model.finaning;

import com.trenska.longwang.constant.DragonConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ApiModel("对账单数据模型")
@NoArgsConstructor
public class AccountCheckingModel {

	@ApiModelProperty("客户ID")
	private Integer custId;
	@ApiModelProperty("客户编号")
	private String custNo;
	@ApiModelProperty("客户名称")
	private String custName;
	@ApiModelProperty("上期结欠")
	private String initDebt = DragonConstant.DFT_CURRENCY_PRECISION_STR;
	@ApiModelProperty("销售应收")
	private String salesAmount = DragonConstant.DFT_CURRENCY_PRECISION_STR;
	@ApiModelProperty("已收")
	private String receivedAmount = DragonConstant.DFT_CURRENCY_PRECISION_STR;
	@ApiModelProperty("已付")
	private String payedAmount = DragonConstant.DFT_CURRENCY_PRECISION_STR;
	/**
	 * 起初欠款+销售应收-已收-已付
	 */
	@ApiModelProperty("待收欠款")
	private String debtAmount = DragonConstant.DFT_CURRENCY_PRECISION_STR;

	@ApiModelProperty("调账减少/借入")
	private String borrow = DragonConstant.DFT_CURRENCY_PRECISION_STR;

	@ApiModelProperty("调账增加/借出")
	private String lend = DragonConstant.DFT_CURRENCY_PRECISION_STR;

}
