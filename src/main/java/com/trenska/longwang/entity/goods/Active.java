package com.trenska.longwang.entity.goods;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.trenska.longwang.entity.customer.AreaGrp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Owen
 * @since 2019-04-12
 */
@Data
@TableName("t_active")
@ApiModel("商品活动实体类")
public class Active extends Model<Active> {
	@ApiModelProperty("商品活动id")
	@TableId(value = "active_id", type = IdType.AUTO)
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
	private Boolean stat;

	@ApiModelProperty("是否所有客户参加活动 true : 是 false : 否")
	private Boolean isAllJoin;

	@ApiModelProperty("最小订购数量，最小为1")
	private Integer minOdrNum;

	@ApiModelProperty("活动商品赠送数量，默认1")
	private Integer giftNum;

	@ApiModelProperty(value = "折扣点数", required = true)
	private Integer discount;
	@TableLogic
	@ApiModelProperty(value = "逻辑删除 1 删除 0 未删除", readOnly = true, required = true)
	private Boolean deleted;

	@ApiModelProperty(value = "参加活动的商品", required = true)
	@TableField(exist = false)
	private Set<ActiveGoods> activeGoods = new HashSet<>();

	@ApiModelProperty(value = "参加活动的商品id的集合", required = true)
	@TableField(exist = false)
	private Set<Integer> goodsIds = new HashSet<>();

	@ApiModelProperty(value = "参加活动的商品集合，用于查询时向前端返回数据",readOnly = true,hidden = true)
	@TableField(exist = false)
	private Set<Goods> goods = new HashSet<>();

	@ApiModelProperty(value = "参加活动的赠品集合，用于查询时向前端返回数据",readOnly = true,hidden = true)
	@TableField(exist = false)
	private Set<Goods> gifts = new HashSet<>();

	@TableField(exist = false)
	@ApiModelProperty("商品活动对应的区域分组的id集合，用于创建促销活动时接收数据，参加商品活动的区域分组id的集合,创建活动时，该参数的传递方式为 : \" +\n" +
			"\t\t\t\t\t\"\\\"areaGrpIds\\\": [\\n\" +\n" +
			"\t\t\t\t\t\"    1,2,...n\\n\" +\n" +
			"\t\t\t\t\t\"  ] 当isAllJoin为true时表示所有区域分组的客户都参加活动，即所有客户都参加该活动，此参数不要传递")
	private Set<Integer> areaGrpIds = new HashSet<>();

	@TableField(exist = false)
	@ApiModelProperty(value = "商品活动对应的区域分组集合，用于查询时向前端返回数据",readOnly = true,hidden = true)
	private Set<AreaGrp> areaGrps = new HashSet<>();

	/**
	 * 与活动是1:1关系
	 */
	@TableField(exist = false)
	@ApiModelProperty("用于新建订货单时向前端返回满赠活动的赠品信息")
	private Goods gift;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Active)) return false;
		Active active = (Active) o;
		return Objects.equals(activeId, active.activeId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(activeId);
	}
}