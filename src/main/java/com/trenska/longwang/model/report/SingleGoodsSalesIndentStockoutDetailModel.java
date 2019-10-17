package com.trenska.longwang.model.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Owen
 * @since 2019-06-01
 */
@Data
@ApiModel("商品销售明细订货单出库详情")
@NoArgsConstructor
public class SingleGoodsSalesIndentStockoutDetailModel implements Serializable {

	@ApiModelProperty("出库数量")
	private String stockoutNum;
	@ApiModelProperty("出库批次")
	private String madeDate;

}
