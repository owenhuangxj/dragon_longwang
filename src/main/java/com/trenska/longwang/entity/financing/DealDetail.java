package com.trenska.longwang.entity.financing;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @author Owen
 * @since 2019-05-20
 */
@Data
@TableName("t_deal_detail")
@ApiModel("交易明细")
@NoArgsConstructor
public class DealDetail extends Model<DealDetail> {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /*** 客户id ***/
    @ApiModelProperty("客户id")
    private Integer custId;
    /*** 单据名称-单号 ***/
    @ApiModelProperty("单据名称-单号")
    private String nameNo;
    /**
     * 交易/账目发生时间-操作时间
     */
    @TableField("deal_time")
    @ApiModelProperty("交易/账目发生时间-操作时间")
    private String time;
    /**
     * 账目操作发生时间
     */
    @ApiModelProperty("账目变更数目+/-")
    private String amount;
    /**
     * 交易后客户欠款
     */
    @ApiModelProperty("交易后客户欠款")
    @TableField(value = "debt")
    private String newDebt;
    /**
     * 操作
     */
    @ApiModelProperty("操作类型")
    private String oper;

    @ApiModelProperty("账户类型")
    private String payway = "";

    @ApiModelProperty("备注")
    private String remarks = "";

    public String auditRemarks = "";

    public DealDetail(Long id, String auditRemarks) {
        this.id = id;
        this.auditRemarks = auditRemarks;
    }

    public DealDetail(Integer custId , String nameNo, String time, String amount, String newDebt, String oper, String payway, String remarks) {
        this.oper = oper;
        this.time = time;
        this.custId = custId;
        this.nameNo = nameNo;
        this.amount = amount;
        this.newDebt = newDebt;
        this.payway = payway;
        this.remarks = remarks;
    }

    public DealDetail(Integer custId, String nameNo, String time, String amount, String newDebt, String oper, String payway, String remarks, String auditRemarks) {
        this.custId = custId;
        this.nameNo = nameNo;
        this.time = time;
        this.amount = amount;
        this.newDebt = newDebt;
        this.oper = oper;
        this.payway = payway;
        this.remarks = remarks;
        this.auditRemarks = auditRemarks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DealDetail that = (DealDetail) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}