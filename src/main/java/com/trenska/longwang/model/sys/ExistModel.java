package com.trenska.longwang.model.sys;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 2019/4/18
 * 创建人:Owen
 */
@Data
@ApiModel("返回对象是否可以创建的响应模型")
public class ExistModel {
	@ApiModelProperty("返回的消息")
	private String msg;
	@ApiModelProperty("是否存在的标志 true 已经存在，false 不存在")
	private boolean exists = true;

}
