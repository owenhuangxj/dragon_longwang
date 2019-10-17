package com.trenska.longwang.entity.stock;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Owen
 * @since 2019-06-15
 */
@Data
@TableName("t_goods_stock")
@ApiModel("商品库存")
@NoArgsConstructor
public class GoodsStock extends Model<GoodsStock> {
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;
	@ApiModelProperty("商品id")
	private Integer goodsId;
	/**
	 * 生产批次
	 */
	@ApiModelProperty("生成批次")
	private String madeDate;
	/**
	 * 存量
	 */
	@ApiModelProperty("数量")
	private Integer num;

	@ApiModelProperty("单价")
	private String stockPrice;

	public GoodsStock(Long id, Integer num) {
		this.id = id;
		this.num = num;
	}



	public GoodsStock(Long id, Integer goodsId, String madeDate, Integer num, String stockPrice) {
		this.id = id;
		this.goodsId = goodsId;
		this.madeDate = madeDate;
		this.num = num;
		this.stockPrice = stockPrice;
	}

	public GoodsStock(Integer goodsId, String madeDate, Integer num, String stockPrice) {
		this.goodsId = goodsId;
		this.madeDate = madeDate;
		this.num = num;
		this.stockPrice = stockPrice;

	}

}
