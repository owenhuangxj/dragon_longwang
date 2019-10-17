package com.trenska.longwang.entity.goods;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * 商品-规格实体类，商品和规格是多对多的关系
 * </p>
 *
 * @author Owen
 * @since 2019-04-11
 */
@Data
@TableName("t_goods_spec")
@ApiModel("商品-规格实体类")
public class GoodsSpec extends Model<GoodsSpec> {

	@TableId(value = "gds_spec_id", type = IdType.AUTO)
	@ApiModelProperty("商品-规格id")
	private Integer gdsSpecId;

	@NotNull
	@ApiModelProperty("商品id")
	private Integer goodsId;

	@NotNull
	@ApiModelProperty("商品规格id")
	private Integer specId;

	@NotNull
	@ApiModelProperty("商品规格属性id")
	private Integer specPropId;

	@ApiModelProperty("商品规格名称")
	private String specName;

	@NotNull
	@ApiModelProperty("商品规格属性名称")
	private String propName;

}
