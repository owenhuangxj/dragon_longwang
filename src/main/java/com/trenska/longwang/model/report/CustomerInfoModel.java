package com.trenska.longwang.model.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@ApiModel("客户-模型")
@NoArgsConstructor
public class CustomerInfoModel implements Serializable {

	@ApiModelProperty("客户编号")
	private String custNo;

	@ApiModelProperty("客户名称")
	private String custName;

	@ApiModelProperty("价格分组")
	private String priceGrp;

	@ApiModelProperty("归属区域")
	private String areaGrp;

	@ApiModelProperty("所属员工")
	private String empName;

	@ApiModelProperty("客户类型")
	private String custType;

	@ApiModelProperty("欠款额度")
	private String debtLimit;

	@ApiModelProperty("联系人")
	private String linkman;

	@ApiModelProperty("联系电话")
	private String linkPhone;

	@ApiModelProperty("地址")
	private String addr;

}