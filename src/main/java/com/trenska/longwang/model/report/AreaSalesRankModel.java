package com.trenska.longwang.model.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 2019/5/31
 * 创建人:Owen
 */
@Data
@NoArgsConstructor
@ApiModel("区域销售排名")
public class AreaSalesRankModel {

	@ApiModelProperty("排名")
	private String rankNum;

	@ApiModelProperty("销售金额")
	private String salesAmnt;

	@ApiModelProperty("实收金额")
	private String indentTotal;

	@ApiModelProperty("优惠金额")
	private String discountTotal;

	@ApiModelProperty("销售数量")
	private Integer salesNum;

	@ApiModelProperty("区域分组id")
	private Integer areaGrpId;

	@ApiModelProperty("区域分组名称")
	private String areaGrpName;

}
