package com.trenska.longwang.model.customer;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 2019/4/25
 * 创建人:Owen
 */
@Data
@ApiModel("商品活动详情模型类")
public class GoodsActiveInfoModel {
	private String name;
	private String activeType;
	private Integer giftId;
	private Integer giftNum;
	private Integer discount;
}
