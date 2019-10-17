package com.trenska.longwang.model.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Owen
 * @since 2019-06-01
 */
@Data
@ApiModel("商品销售明细")
@NoArgsConstructor
public class SingleGoodsSalesDetailModel implements Serializable {

	@ApiModelProperty("商品id")
	private Integer goodsId;

	@ApiModelProperty("商品编号")
	private String goodsNo;

	@ApiModelProperty("商品名称")
	private String goodsName;

	@ApiModelProperty("商品单位")
	private String unit;

	@ApiModelProperty("销售金额合计")
	private String salesAmntSum;

	@ApiModelProperty("销售数量合计")
	private String salesNumSum;

	@ApiModelProperty("规格值")
	private Set<String> propNames = new HashSet<>();

	@ApiModelProperty("商品销售订货单明细")
	private List<SingleGoodsSalesIndentDetailModel> indentDetails = new ArrayList<>();

}
