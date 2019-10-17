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
 * @author Owen
 * @since 2019-04-07
 */
@Data
@TableName("t_category")
@ApiModel("商品分类实体类")
@NoArgsConstructor
public class Category extends Model<Category> {
	@TableId(type = IdType.AUTO)
	private Integer catId;
	@ApiModelProperty("商品分类名称")
	@NotNull
	private String catName;
	@ApiModelProperty("商品分类父分类id，如果为主分类，该属性设置为 0")
	@NotNull
	private Integer pid;
	@ApiModelProperty("商品分类状态 0:无效;1:有效")
	private Boolean stat;
	@ApiModelProperty("商品分类是否可以删除标志位，true 可以删除,false 不可以删除")
	private Boolean deletable;

	public Category(Integer catId, Boolean deletable) {
		this.catId = catId;
		this.deletable = deletable;
	}
}
