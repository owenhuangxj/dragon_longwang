package com.trenska.longwang.entity.goods;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Owen
 * @since 2019-04-07
 */
@Data
@TableName("t_brand")
@ApiModel("商品品牌实体类")
public class Brand extends Model<Brand> {

	@TableId(value = "brand_id", type = IdType.AUTO)
	@ApiModelProperty("商品品牌id")
	private Integer brandId;

	@ApiModelProperty("商品品牌名称")
	private String brandName;

	@ApiModelProperty("商品品牌状态 : 1 有效 0 无效")
	private Boolean stat;

	@ApiModelProperty("商品品牌是否可以删除标志位，true 可以删除,false 不可以删除")
	private Boolean deletable;

}
