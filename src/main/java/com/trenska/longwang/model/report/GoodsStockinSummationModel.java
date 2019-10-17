package com.trenska.longwang.model.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 2019/8/6
 * 创建人:Owen
 */
@Data
@ApiModel("商品入库报表合计模型")
public class GoodsStockinSummationModel {
	@ApiModelProperty("入库数量汇总")
	private String stockinNumSum;
	@ApiModelProperty("入库金额汇总")
	private String stockinAmntSum;
}
