package com.trenska.longwang.entity.customer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 2019/4/5
 * 创建人:Owen
 * 会员价格分组
 */
@Data
@TableName("t_price_grp")
@ApiModel("价格分组")
public class PriceGrp {

	@ApiModelProperty("价格分组id")
	@TableId(type = IdType.AUTO)
	private Integer priceGrpId;

	@NotNull(message = "价格分组名称不能为空")
	@ApiModelProperty("价格分组名")
	private String priceGrpName;

	@ApiModelProperty("价格分组备注")
	private String descr;
}
