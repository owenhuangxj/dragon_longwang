package com.trenska.longwang.model.goods;
import com.trenska.longwang.entity.goods.GoodsSpec;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Owen
 * @since 2019-08-01
 */
@Data
@ApiModel("商品导出模型")
@NoArgsConstructor
public class GoodsExportModel {

	@ApiModelProperty("商品编号")
	private String goodsNo;

	@ApiModelProperty("商品名称")
	private String goodsName;

	@ApiModelProperty("商品主单位")
	private String mainUnit;

	@ApiModelProperty("品牌名称")
	private String brandName;

	@ApiModelProperty("商品分类一级名称")
	private String frtCatName;

	@ApiModelProperty("状态")
	private Boolean stat;

	@ApiModelProperty("状态,stat == true -> 上架;stat == false --> 下架")
	private String status;

	@ApiModelProperty("系统库存")
	private String stock;

	@ApiModelProperty("可用库存 stock - stockout")
	private String avbStock;

	@ApiModelProperty("价格")
	private String price;

	@ApiModelProperty("规格")
	private String propName;

	@ApiModelProperty("商品对应的规格(参数)集合")
	private Collection<GoodsSpec> goodsSpecs = new ArrayList<>();

}