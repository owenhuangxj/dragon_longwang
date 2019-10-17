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
@ApiModel("进出库数量汇总")
@NoArgsConstructor
public class GoodsStockSummarizingModel {
	private Integer goodsId;
	@ApiModelProperty("商品编号")
	private String goodsNo = "";
	@ApiModelProperty("商品名称")
	private String goodsName = "";
	@ApiModelProperty("品牌")
	private String brandName = "";
	@ApiModelProperty("规格")
	private Set<String> propNames = new HashSet<>();
	@ApiModelProperty("单位")
	private String unitName;
	@ApiModelProperty("期初库存")
	private String initStock;
	@ApiModelProperty("生产入库")
	private String makeIn = "0";
	@ApiModelProperty("采购入库")
	private String purchaseIn = "0";
	@ApiModelProperty("退货出库")
	private String returnsOut = "0";
	@ApiModelProperty("退货入库")
	private String returnsIn = "0";
	@ApiModelProperty("销售出库")
	private String salesOut = "0";
	@ApiModelProperty("其他入库")
	private String otherIn = "0";
	@ApiModelProperty("其他出库")
	private String otherOut = "0";
	@ApiModelProperty("报溢入库")
	private String overflow = "0";
	@ApiModelProperty("报损出库")
	private String breakage = "0";
	@ApiModelProperty("期末库存")
	private String overStock;
	@ApiModelProperty(hidden = true)
	private String beginTime;
	@ApiModelProperty(hidden = true)
	private String endTime;
}