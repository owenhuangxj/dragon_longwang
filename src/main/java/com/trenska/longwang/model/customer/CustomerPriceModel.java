package com.trenska.longwang.model.customer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 2019/4/24
 * 创建人:Owen
 */
@Data
@ApiModel("客户-价格对应关系模型数据类")
@NoArgsConstructor
public class CustomerPriceModel {
	@ApiModelProperty("价格名称/活动名称/分组名称")
	private String name;
	@ApiModelProperty("价格")
	private String price;

	public CustomerPriceModel(String name, String price) {
		this.name = name;
		this.price = price;
	}
}
