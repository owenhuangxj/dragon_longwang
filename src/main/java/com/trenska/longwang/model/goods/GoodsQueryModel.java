package com.trenska.longwang.model.goods;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 2019/4/12
 * 创建人:Owen
 */
@Data
@ApiModel("商品多条件查询时接收查询条件的模型，方便前端传参，实际可以在Goods实体类进行操作")
@NoArgsConstructor
public class GoodsQueryModel {

	/**
	 * 商品编号,相同商品名称对应多个规格，每个规格就是一个商品
	 */

	@ApiModelProperty("商品名称/编号/条码三合一")
	private String combine;

	/**
	 * 冗余的产品品牌名称，减少多表查询和方便实体类的设计，但是在品牌名称修改时需要级联操作
	 */
	@ApiModelProperty("产品品牌名称")
	private String brandName;

	@ApiModelProperty("商品分类一级名称")
	private String frtCatName;

	@ApiModelProperty("商品分类二级名称")
	private String scdCatName;

	@ApiModelProperty("商品状态，true 上架，false 下架")
	private Boolean stat;

	@ApiModelProperty("商品对应的单规格值")
	private String propName;

	@ApiModelProperty("最小起订量")
	private Integer minOdrQtt;

	public GoodsQueryModel(String combine, String brandName, String frtCatName, String scdCatName, Boolean stat, String propName, Integer minOdrQtt) {
		this.combine = combine;
		this.brandName = brandName;
		this.frtCatName = frtCatName;
		this.scdCatName = scdCatName;
		this.stat = stat;
		this.propName = propName;
		this.minOdrQtt = minOdrQtt;
	}
}
