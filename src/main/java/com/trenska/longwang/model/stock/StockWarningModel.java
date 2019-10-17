package com.trenska.longwang.model.stock;

import com.trenska.longwang.entity.goods.GoodsSpec;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * 2019/4/26
 * 创建人:Owen
 */
@Data
@ApiModel("商品预警模型")
public class StockWarningModel {
	@ApiModelProperty("商品标签")
	private String barcode;
	@ApiModelProperty("商品编号")
	private String goodsNo;
	@ApiModelProperty("商品名称")
	private String goodsName;
	@ApiModelProperty("规格")
	private Set<GoodsSpec> goodsSpecs = new HashSet<>();
	@ApiModelProperty("单位")
	private String unitName;
	@ApiModelProperty("生产日期")
	private String madeDate;
	@ApiModelProperty("商品保质期(天)")
	private Integer expir;
	@ApiModelProperty("剩余保质期(天)")
	private Integer leftDays;
	@ApiModelProperty("过期时间(天)")
	private Integer passedDays;
	@ApiModelProperty("过期时间(月)")
	private Integer passedMonths;
	@ApiModelProperty("商品预警")
	private String warningLevel;
	@ApiModelProperty("库存数量")
	private Integer stock;
}
