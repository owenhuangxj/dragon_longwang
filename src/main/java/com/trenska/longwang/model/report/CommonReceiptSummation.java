package com.trenska.longwang.model.report;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 2019/8/13
 * 创建人:Owen
 */
@Data
public class CommonReceiptSummation {
	@ApiModelProperty("收/付款合计")
	private String receiptSum = "0.00";
}
