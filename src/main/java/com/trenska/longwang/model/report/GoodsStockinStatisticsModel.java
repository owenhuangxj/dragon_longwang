package com.trenska.longwang.model.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * 2019/6/3
 * 创建人:Owen
 */
@Data
@NoArgsConstructor
@ApiModel("商品入库报表")
public class GoodsStockinStatisticsModel {

	@ApiModelProperty("商品id")
	private String goodsId;

	@ApiModelProperty("商品编号")
	private String goodsNo;

	@ApiModelProperty("商品名称")
	private String goodsName;

	@ApiModelProperty("规格")
	private Set<String> propNames = new HashSet<>();

	@ApiModelProperty("单位")
	private String unitName;

	@ApiModelProperty("入库数量")
	private String stockinNum;

	@ApiModelProperty("入库平均单价")
	private String avgPrice;

	@ApiModelProperty("入库金额")
	private String stockinAmnt;

}
