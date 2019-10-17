package com.trenska.longwang.model.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Owen
 * @since 2019-06-02
 */
@Data
@ApiModel("商品销售排名")
@NoArgsConstructor
public class GoodsSalesRankModel implements Serializable {

	@ApiModelProperty("排名")
	private String rankNum;

	@ApiModelProperty("商品编号")
	private String goodsNo;

	@ApiModelProperty("商品名称")
	private String goodsName;

	@ApiModelProperty("品牌")
	private String brandName;

	@ApiModelProperty("分类")
	private String catName;

	@ApiModelProperty("销售金额")
	private String salesAmnt;

	@ApiModelProperty("实收金额")
	private String indentTotal;

	@ApiModelProperty("优惠金额")
	private String discountAmount;

	@ApiModelProperty("销售数量")
	private String salesNum;

}
