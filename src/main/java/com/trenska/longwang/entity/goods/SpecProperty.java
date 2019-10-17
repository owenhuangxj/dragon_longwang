package com.trenska.longwang.entity.goods;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * 商品规格属性表，与商品规格表是 1：n 的关系
 * </p>
 *
 * @author Owen
 * @since 2019-04-09
 */
@Data
@TableName("t_spec_property")
@ApiModel("商品规格属性实体类")
@NoArgsConstructor
public class SpecProperty extends Model<SpecProperty> {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty("商品规格属性id")
	private Integer specPropId;

	@ApiModelProperty("商品规格id,关联t_spec数据库表中的spec_id字段")
	private Integer specId;

	@ApiModelProperty("商品规格属性名称")
	@NotNull
	private String propName;

	@ApiModelProperty("商品规格属性的状态，1 有效 0 无效，默认为 1")
	private Boolean stat;

	@ApiModelProperty("商品规格属性是否可以删除标志位，tue 可以删除,false 不可以删除")
	private Boolean deletable;

	private Boolean deleted;

	public SpecProperty(Integer specPropId, Boolean deletable) {
		this.specPropId = specPropId;
		this.deletable = deletable;
	}
}
