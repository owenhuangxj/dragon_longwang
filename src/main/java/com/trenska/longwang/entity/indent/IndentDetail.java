package com.trenska.longwang.entity.indent;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.trenska.longwang.entity.goods.Goods;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Owen
 * @since 2019-04-23
 */
@Data
@TableName("t_indent_detail")
@ApiModel("订货单详情实体类")
@NoArgsConstructor
public class IndentDetail extends Model<IndentDetail> {

	@ApiModelProperty("商品id，新建订货单时传递")
	private Integer goodsId;

	@ApiModelProperty("单位id，新建订货单时传递")
	private Integer unitId;

	@ApiModelProperty("赠品标志位")
	private Boolean isGift;

	@NotNull
	@ApiModelProperty("乘积因子，新建订货单时传递")
	private Integer multi;

//	@ApiModelProperty("库存，同步商品总库存，前端不传递")
//	private Integer stock;

	@ApiModelProperty("价格，新建订货单时传递")
	private String price;

	@ApiModelProperty("订货数量，新建订货单时传递")
	private Integer num;

	@ApiModelProperty("活动id")
	private Integer activeId;

	@ApiModelProperty("扣点，新建时前端传递(扣点活动)")
	private String discount;

	@ApiModelProperty("赠品,创建订货单时参加满赠活动才传递")
	@TableField(exist = false)
	private List<Gift> gifts = new ArrayList<>();

	@ApiModelProperty("订货单详情id,新建时前端不传递")
	@TableId(value = "detail_id", type = IdType.AUTO)
	private Long detailId;

	@ApiModelProperty("订货单编号，前端不操作")
	private String indentNo;

	@TableField(exist = false)
	@ApiModelProperty("出库单的商品信息，新建时不传递")
	private Goods goods;

	/**
	 * 冗余字段，根据业务需求增加的字段，减少多表查询
	 */
	@ApiModelProperty("业务员id，前端不操作")
	private Integer empId;
	/**
	 * 冗余字段，根据业务需求增加的字段，减少多表查询
	 */
	@TableField(exist = false)
	@ApiModelProperty("业务员id，前端不操作")
	private String empName;

//	@ApiModelProperty("业务员id，新建订货单时前端传递")
//	private Integer salesmanId;
//
//	@TableField(exist = false)
//	@ApiModelProperty("业务员姓名，新建订货单时前端不传递")
//	private String salesmanName;

	@TableField(exist = false)
	@ApiModelProperty("单位,新建订货单时不传递")
	private String unitName;

//	@ApiModelProperty("订货单生成时间，前端不传递")
//	private String indentTime;

	@ApiModelProperty("已出库数量,新建订货单时不传递，订货单出货时传递")
	private Integer stockout;

	@ApiModelProperty("出库生产批次，新建订货单时不传递,订货单出货时传递")
	@TableField(exist = false)
	private List<StockMadedate> stockoutMadedates = new ArrayList<>();

	@ApiModelProperty("新建退货单时传递，生产日期")
	private String madeDate;

//	@ApiModelProperty("订货单状态：待审核、待出库、待发货、已发货、已完成、已取消、已作废，新建订货单时不传递")
//	private String stat;

//	@ApiModelProperty("订货单收款状态：待收款、已收款，前端不传递")
//	private String receiptStat;

//	@ApiModelProperty("订单来源，新建时不传递")
//	private String odrSrc;

	/**
	 * price * history
	 */
	@ApiModelProperty("订单详情的实际金额 :history*price(1-discount/100)，前端不传递")
	private String amount;

	/**
	 * 金额-金额*扣点/100 （history（1-discount/100））
	 */
	@ApiModelProperty("扣点优惠金额，前端不传递")
	private String discountAmount;

	@ApiModelProperty("单一商品总额，前端传递 = amount + discountAmount")
	@TableField(exist = false)
	private String total;

	@ApiModelProperty("备注")
	private String remarks;

//	@TableLogic
	@ApiModelProperty("逻辑删除位，前端不操作")
	private Boolean deleted;

	public IndentDetail(Boolean deleted) {
		this.deleted = deleted;
	}

//	public IndentDetail(String stat) {
//		this.stat = stat;
//	}

	public IndentDetail(Long detailId, Boolean deleted) {
		this.detailId = detailId;
		this.deleted = deleted;
	}

	public IndentDetail(Long detailId,Integer num) {
		this.detailId = detailId;
		this.num = num;
	}

//	public IndentDetail(String indentNo, String stat) {
//		this.indentNo = indentNo;
//		this.stat = stat;
//	}

	public IndentDetail(Long detailId, String indentNo, Boolean deleted) {
		this.detailId = detailId;
		this.indentNo = indentNo;
		this.deleted = deleted;
	}

//	public IndentDetail(String indentNo, String stat, String receiptStat) {
//		this.indentNo = indentNo;
//		this.stat = stat;
//		this.receiptStat = receiptStat;
//	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		IndentDetail that = (IndentDetail) o;
		return detailId.equals(that.detailId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(detailId);
	}
}