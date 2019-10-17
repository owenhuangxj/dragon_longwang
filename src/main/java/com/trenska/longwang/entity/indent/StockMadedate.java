package com.trenska.longwang.entity.indent;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Owen
 * @since 2019-04-22
 * 记录批次库存
 */
@Data
@ApiModel("订货单的生产批次实体类")
@NoArgsConstructor
public class StockMadedate implements Serializable {

	@ApiModelProperty("生产批次")
	private String madeDate;

	@ApiModelProperty("出库的生产批次数量")
	private Integer num;

	@ApiModelProperty("出库单价，对应商品的入库单价")
	private String stockPrice;

	public StockMadedate(String madeDate, Integer num) {
		this.madeDate = madeDate;
		this.num = num;
	}
}