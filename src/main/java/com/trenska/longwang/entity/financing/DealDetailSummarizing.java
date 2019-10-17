package com.trenska.longwang.entity.financing;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 2019/6/10
 * 创建人:Owen
 */
@Data
@ApiModel("交易明细汇总")
@NoArgsConstructor
public class DealDetailSummarizing{
	// 应收欠款 = 上期结余+增加应收欠款-收回欠款-抵消金额
	@ApiModelProperty("应收欠款")
	private String needCollect = "0.00";
	// 上期结余欠款 : 截止到统计时间段之前的欠款金额合计
	@ApiModelProperty("上期欠款金额")
	private String lastSurplusDebt = "0.00";
	// 增加应收欠款 : 该时间段增加的欠款金额合计（统计订货单(已出库)的金额）
	@ApiModelProperty("增加欠款")
	private String plusDebt = "0.00";
	// 收回欠款 : 该时间段的收款单金额合计（统计收款单的金额）
	@ApiModelProperty("减少欠款-收款")
	private String receiptedDetb = "0.00";
	// 抵消金额 : 该时间段的付款金额合计（统计付款单和退货单的金额）
	@ApiModelProperty("减少欠款-付款")
	private String cutDebt = "0.00";
}
