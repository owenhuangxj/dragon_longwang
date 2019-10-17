package com.trenska.longwang.entity.financing;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.trenska.longwang.model.finaning.ReceiptModel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@TableName("t_receipt")
@Data
@ApiModel("收款/付款实体类")
@NoArgsConstructor
public class Receipt extends Model<Receipt> {

    @TableId(value = "receipt_id",type =  IdType.AUTO)
    @ApiModelProperty("id，新建时前端不传递")
    private Long receiptId;

    @ApiModelProperty("收款/付款编号，新建时前端不传递")
    private String receiptNo;

    @ApiModelProperty("客户id:收款单时为付款方，付款单时为收款放，新建时前端传递")
    private Integer  custId;

    @TableField(exist = false)
    @ApiModelProperty("客户名，新建时前端不传递")
    private String custName;

    @ApiModelProperty("制单人id，新建时前端传递")
    private Integer empId;

    @TableField(exist = false)
    @ApiModelProperty("制单人姓名，新建时前端不传递")
    private String empName;

    @ApiModelProperty("收款人id，新建收款单时前端传递，新建付款单不传递")
    private Integer chargemanId;

    @TableField(exist = false)
    @ApiModelProperty("收款人姓名，新建时前端不传递")
    private String chargemanName;

    @ApiModelProperty("单据类型：收款单/付款单")
    private String type;

    @ApiModelProperty("账目类型，比如: 退货支出、其它支出、电费、租金、销售收入、其它收入等")
    private String accountType;

    @ApiModelProperty("收/付款方式名称，新建收款单时传递")
    private String payway;

    @ApiModelProperty("收款/付款时间，新建时前端传递")
    private String receiptTime;

    @ApiModelProperty("收款/付款额，新建时前端传递")
    private String receiptAmount;

    @ApiModelProperty("欠条金额，用于订货单收款时设置欠条金额")
    @TableField(exist = false)
    private Integer iouAmnt;

    @ApiModelProperty("收款/付款 状态 :true 已完成，false 已作废")
    private Boolean stat;

    @ApiModelProperty("备注，新建时前端传递")
    private String receiptRemarks;

//    @TableLogic
    @ApiModelProperty("逻辑删除，前端不处理")
    private boolean deleted;

    @ApiModelProperty("关联业务单号，比如订货单收款时前端传递indentNo")
    private String busiNo;

    @ApiModelProperty("收款/付款单创建时间，新建时前端不传递")
    private String createTime;

    @TableField(exist = false)
    @ApiModelProperty("收款/付款模型，收款时传递")
    Set<ReceiptModel> receiptSet = new HashSet<>();

    public Receipt(Long receiptId,Boolean stat){
        this.receiptId = receiptId;
        this.stat = stat;
    }

}