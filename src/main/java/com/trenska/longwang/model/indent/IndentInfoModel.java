package com.trenska.longwang.model.indent;

import com.trenska.longwang.entity.goods.Active;
import com.trenska.longwang.entity.goods.Goods;
import com.trenska.longwang.entity.goods.GoodsCustSpecify;
import com.trenska.longwang.entity.goods.GoodsPriceGrp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Collection;
import java.util.HashSet;

/**
 * 2019/4/30
 * 创建人:Owen
 * 用于新建订货单时封装数据到前端
 */
@Data
@ApiModel
public class IndentInfoModel {
	@ApiModelProperty("商品")
	private Goods goods;
	@ApiModelProperty("客户特价")
	private GoodsCustSpecify goodsCustSpecify;
	@ApiModelProperty("活动,包括扣点")
	private Collection<Active> actives = new HashSet<>();
	@ApiModelProperty("价格分组")
	private GoodsPriceGrp goodsPriceGrp;
}
