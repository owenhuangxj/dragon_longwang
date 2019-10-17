package com.trenska.longwang.model.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Owen
 * @since 2019-06-01
 */
@Data
@ApiModel("商品销售汇总")
@NoArgsConstructor
public class GoodsSalesSummarizingModel implements Serializable {

	@ApiModelProperty("商品id")
	private Integer goodsId;

	@ApiModelProperty("商品编号")
	private String goodsNo;

	@ApiModelProperty("商品名称")
	private String goodsName;

	@ApiModelProperty("单位")
	private String unit;

	@ApiModelProperty("销售数量")
	private String num;

	@ApiModelProperty("单价")
	private String price;

	@ApiModelProperty("销售金额")
	private String salesAmnt;

	@ApiModelProperty("扣点")
	private String discount;

	@ApiModelProperty("实收金额")
	private String indentTotal;

	@ApiModelProperty("优惠金额")
	private String discountTotal;

	@ApiModelProperty("规格")
	private Set<String> propNames = new HashSet<>();

}
