package com.trenska.longwang.model.report;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 2019/5/30
 * 创建人:Owen
 */
@Data
@ApiModel("客户销售总账数据记录模型")
@NoArgsConstructor
public class CustSalesBillRecordsModel {
	private Integer custId;
	private String custNo;
	private String custName;
	private String brandName;
	private String odrAmnt;
	private String indentTotal;
	private String discountTotal;
}
