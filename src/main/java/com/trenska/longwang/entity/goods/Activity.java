/*
package com.trenska.longwang.entity.goods;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.trenska.longwang.entity.customer.AreaGrp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

*/
/**
 * @author Owen
 * @since 2020-03-29
 *//*

@Data
@Document(collection = "activity")
@ApiModel("商品活动实体类")
public class Activity extends Model<Activity> {
	@Id
	@ApiModelProperty("商品活动id")
	private Integer activeId;

	@ApiModelProperty(value = "商品活动名称", required = true)
	private String activeName;

	@ApiModelProperty("活动类型，暂定两种 满赠 扣点 默认:满赠")
	private String activeType;

	@ApiModelProperty("活动开始时间，格式为YYYY-MM-DD HH:mm:SS 即年-月-日 时:分:秒")
	private String beginTime;

	@ApiModelProperty("活动结束时间")
	private String endTime;

	@ApiModelProperty("促销活动状态 true 进行中 false 关闭,默认 true")
	private boolean stat;

	@ApiModelProperty("是否所有客户参加活动 true : 是 false : 否")
	private boolean isAllJoin;

	@ApiModelProperty("最小订购数量，最小为1")
	private Integer minOdrNum;

	@ApiModelProperty("活动商品赠送数量，默认1")
	private Integer giftNum;

	@ApiModelProperty(value = "折扣点数", required = true)
	private Integer discount;

	@ApiModelProperty(value = "参加活动的商品集合")
	private Set<Goods> goods = new HashSet<>();

	@ApiModelProperty(value = "商品活动对应的区域分组集合")
	private Set<AreaGrp> areaGrps;

	*/
/**
	 * 与活动是1:1关系
	 *//*

	@ApiModelProperty("满赠活动的赠品信息")
	private Goods gift;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Activity)) return false;
		Activity active = (Activity) o;
		return Objects.equals(activeId, active.activeId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(activeId);
	}
}*/
