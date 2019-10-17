package com.trenska.longwang.entity.indent;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.trenska.longwang.entity.goods.Goods;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Owen
 * @since 2019-05-07
 */
@Data
@TableName("t_gift")
@ApiModel("赠品实体类")
public class Gift extends Model<Gift> {

	@TableId(value = "id", type = IdType.AUTO)
	@ApiModelProperty("id,前端不传递")
	private Integer id;

	/**
	 * t_indent_detail表的主键
	 */
	@ApiModelProperty("订货单详情id,前端不传递")
	private Integer detailId;
	@ApiModelProperty("活动id,前端不传递")
	private Integer activeId;

	/**
	 * 本品id
	 */
	@ApiModelProperty("本品id,前端不传递")
	private Integer goodsId;

	/**
	 * 赠品id
	 */
	@ApiModelProperty("赠品id,新建时前端传递")
	private Integer giftId;

	@TableField(exist = false)
	@ApiModelProperty("赠品信息")
	private Goods gift;

	/**
	 * 赠品数量
	 */
	@ApiModelProperty("赠品数量，新建时前端传递")
	private Integer num;


}
