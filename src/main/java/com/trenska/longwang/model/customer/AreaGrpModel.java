package com.trenska.longwang.model.customer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Collection;
import java.util.HashSet;

/**
 * 2019/5/27
 * 创建人:Owen
 */
@Data
@ApiModel("区域分组模型")
public class AreaGrpModel {

	@ApiModelProperty("区域分组id")
	private Integer areaGrpId;

	@ApiModelProperty("区域分组名称")
	private String areaGrpName;

	@ApiModelProperty("区域分组的父节点id，即父节点的areaGrpId")
	private Integer pid;

	@Min(1)
	@Max(3)
	@ApiModelProperty("区域深度，一共三级，分别对应1、2、3. \n1：一级区域；2：二级区域；3：三级区域")
	private Integer areaGrpDeep;

	private Collection<AreaGrpModel> subAreaGrps = new HashSet<>();
}
