package com.trenska.longwang.entity.stock;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.trenska.longwang.entity.goods.Goods;
import com.trenska.longwang.entity.indent.StockMadedate;
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
 * @since 2019-04-15
 */
@TableName("t_stock_detail")
@ApiModel("入库实体类")
@Data
@NoArgsConstructor
public class StockDetail extends Model<StockDetail> {
	@TableId(value = "detail_id", type = IdType.AUTO)
	private Long detailId;

	@ApiModelProperty("单号，由系统生成")
	private String stockNo;

	/**
	 * 冗余Stock类/t_stock表的相同字段
	 */
	@ApiModelProperty("业务单号，订货单出库时，该字段的值为indentNo")
	private String busiNo;

	@ApiModelProperty("操作者id")
	private Integer empId;

	@ApiModelProperty(value = "库存变更类 : 出库、入库，前端不传递")
	private String stockType;

	@ApiModelProperty(value = "操作类型，t_stock_detail表冗余字段，方便查询操作,入库时不传递")
	private String operType;

	//	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
//	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@ApiModelProperty("库存操作的具体时间,系统生成，前端不传递")
	private String stockTime;

	@ApiModelProperty(value = "商品id", required = true)
	private Integer goodsId;

	@TableField(exist = false)
	@ApiModelProperty("用于向前端传递查询的商品详情")
	private Goods goods;

	@ApiModelProperty(value = "库存操作数量", required = true)
	@NotNull
	private Integer num;

	@ApiModelProperty("库存操作数量,记录，前端不传递,显示变更量时选择此字段")
	private Integer history;

	@ApiModelProperty(value = "单位id，新建时传递", required = true)
	private Integer unitId;

	@TableField(exist = false)
	@ApiModelProperty(value = "单位名称，新建时前端不传递", required = true)
	private String unitName;

	@TableField(exist = false)
	@ApiModelProperty("金额，前端不传递")
	private String sum;

	@ApiModelProperty(value = "批次/生产日期，入库时传递", required = true)
//	@JsonDeserialize(using = LocalDateDeserializer.class)
//	@JsonSerialize(using = LocalDateSerializer.class)
	private String madeDate;

	@ApiModelProperty("出库商品的生产批次及数量,出库时传递，不按批次出库不传递")
	@TableField(exist = false)
	private List<StockMadedate> stockoutMadedates = new ArrayList<>();

	@ApiModelProperty(value = "入库单价/出库单价", required = true)
	private String stockPrice;

	@ApiModelProperty(value = "销售单价")
	private String salesPrice;

	@ApiModelProperty("入库商品的辅助单位和主单位的乘积因子，入库操作选择的单位是辅助单位时必须传递")
	private Integer multi;

	@ApiModelProperty("库存操作后的实际库存量，系统计算，前端不操作")
	private Integer stock;

	@ApiModelProperty("库存操作备注")
	private String detailRemarks;

	@ApiModelProperty("逻辑删除位")
//	@TableLogic
	private Boolean deleted;

	@ApiModelProperty("出库单状态 true 完成 false 作废")
	private Boolean stat;

	public StockDetail(Long detailId, Boolean stat,String stockType) {
		this.stockType = stockType;
		this.detailId = detailId;
		this.stat = stat;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof StockDetail)) return false;
		StockDetail that = (StockDetail) o;
		return Objects.equals(detailId, that.detailId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(detailId);
	}
}