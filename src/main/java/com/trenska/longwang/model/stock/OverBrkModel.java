package com.trenska.longwang.model.stock;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 2019/4/17
 * 创建人:Owen
 */
@Data
@ApiModel("用于向前端传递报溢报损查询结果")
public class OverBrkModel {
	@ApiModelProperty("单号")
	private String stockNo;
	@ApiModelProperty("时间")
	private String stockTime;
	@ApiModelProperty("开单人")
	private String empName;
	@ApiModelProperty("品牌")
	private String brandName;
	@ApiModelProperty("单位")
	private String unitName;
	@ApiModelProperty("金额")
	private String total;
	@ApiModelProperty("状态")
	private Boolean stat;
}
