package com.trenska.longwang.entity.indent;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.trenska.longwang.entity.customer.Customer;
import com.trenska.longwang.entity.financing.Receipt;
import com.trenska.longwang.entity.stock.Stock;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 订货单 实体类
 * @author Owen
 * @since 2019-04-22
 */
@TableName("t_indent")
@Data
@ApiModel("订货单实体类")
@NoArgsConstructor
public class Indent extends Model<Indent> {

	@TableId(value = "indent_id", type = IdType.AUTO)
	private Long indentId;

	@ApiModelProperty("订货单编号，由系统生成，前端不传递")
	private String indentNo;

	@ApiModelProperty("制单人id，新建订货单时前端传递")
	private Integer empId;

	@TableField(exist = false)
	@ApiModelProperty("制单人姓名，新建订货单时前端不传递")
	private String empName;

	@ApiModelProperty("业务员id，新建订货单时前端传递")
	private Integer salesmanId;

	@TableField(exist = false)
	@ApiModelProperty("业务员姓名，新建订货单时前端不传递")
	private String salesmanName;

	@TableField(exist = false)
	@ApiModelProperty("送货人id,新建订货单时不传递,订货单出库时传递")
	private Integer shipmanId;

	@ApiModelProperty("送货方式，预留，暂时不操作")
	private String shipway;

	@ApiModelProperty("支付方式，新建订货单时前端传递")
	private String payway;

	@ApiModelProperty("客户id，新建订货单时前端传递")
	private Integer custId;

	@TableField(exist = false)
	@ApiModelProperty("客户名称，新建订货单时前端不传递")
	private String custName;

	@ApiModelProperty("订单来源，新建订货单时前端传递")
	private String odrSrc;

	@ApiModelProperty("订货单备注，新建订货单时前端传递")
	private String indentRemarks;

	@ApiModelProperty("订货单生成时间，前端不传递")
	private String indentTime;
	@ApiModelProperty("销售时间(出库完成时间)，前端不传递")
	private String salesTime;

	@ApiModelProperty("本品数量合计")
	private Integer sum;

	@ApiModelProperty("赠品数量之和")
	private Integer giftSum;

	@ApiModelProperty("收款金额合计:扣点金额之和，即扣除扣点的金额合计，前端不传递")
	private String indentTotal;

	@ApiModelProperty("优惠金额合计:金额*数量-收款金额合计，前端不传递")
	private String discountTotal;

	@ApiModelProperty("欠条金额")
	private String iouAmnt;

	@ApiModelProperty("交账状态:只能标记打欠条的时候是否交账,true->打欠条的时候已交账")
	private Boolean iouStat;

	@ApiModelProperty("欠条回款时间:以便到最后流程提醒财务")
	private String iouTime;

	@ApiModelProperty("欠条备注，提醒财务")
	private String iouRemarks;

	@ApiModelProperty("是否可以财务审核 true:可以")
	private Boolean auditable;

	@ApiModelProperty("财务审核状态 true:通过")
	private Boolean auditStat;

	@ApiModelProperty("财务备注")
	private String auditRemarks;

	@ApiModelProperty("订货单状态：待审核、待发货、待收款、已发货、已收款、已完成、已取消、已作废，前端不传递")
	private String stat;

	@ApiModelProperty("订货单收款状态：待收款、已收款，前端不传递")
	private String receiptStat;

	@ApiModelProperty("订单总金额,前端不传递")
	private String odrAmnt;

	@ApiModelProperty("已付金额")
	private String payedAmnt;

	@ApiModelProperty("已收金额")
	private String receivedAmnt;

	@ApiModelProperty("待收金额")
	@TableField(exist = false)
	private String dueAmnt;

	@ApiModelProperty("订货单类型，默认为订货单")
	private String indentType;

//	@TableLogic
	@ApiModelProperty("逻辑删除位，前端不操作")
	private Boolean deleted;

	@TableField(exist = false)
	@ApiModelProperty("客户信息，前端不传递")
	private Customer customer;

	@TableField(exist = false)
	@ApiModelProperty("订货单详情,新建订货单时前端传递")
	private List<IndentDetail> indentDetails = new ArrayList<>();

	@TableField(exist = false)
	@ApiModelProperty("前端需要的indentDetails的copy")
	private List<IndentDetail> indentDetailsCopy = new ArrayList<>();

	@TableField(exist = false)
	@ApiModelProperty("订货单出库详情")
	private List<Stock> stocks = new ArrayList<>();

	@TableField(exist = false)
	@ApiModelProperty("订货单收款记录")
	private List<Receipt> receipts = new ArrayList<>();

	@TableField(exist = false)
	@ApiModelProperty("订货单付款记录")
	private List<Receipt> payReceipts = new ArrayList<>();

	public Indent(Long indentId , String stat){
		this.indentId = indentId;
		this.stat = stat;
	}

	public Indent(String indentNo, String stat) {
		this.indentNo = indentNo;
		this.stat = stat;
	}

	public Indent(Long indentId, String stat, String receiptStat) {
		this.indentId = indentId;
		this.stat = stat;
		this.receiptStat = receiptStat;
	}
}