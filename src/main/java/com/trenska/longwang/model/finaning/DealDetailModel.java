package com.trenska.longwang.model.finaning;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Owen
 * @since 2019-06-10
 */
@Data
@ApiModel("交易明细模型")
@NoArgsConstructor
public class DealDetailModel {

    /**
     * 交易/账目发生时间-操作时间
     */
    @ApiModelProperty("交易/账目发生时间-操作时间")
    private String time;
    /**
     * 借->相当于客户来说就是买
     */
    @ApiModelProperty("借")
    private String buy;

    @ApiModelProperty("账户类型")
    private String payway;
    /**
     * 贷->相当于客户来说就是付钱
     */
    @ApiModelProperty("交易后客户欠款")
    private String pay;

    @ApiModelProperty("欠款余额")
    private String debt;

    @ApiModelProperty("业务类型")
    private String oper;
    /**
     * 单据名称-单号
     */
    @ApiModelProperty("单据名称-单号")
    private String nameNo;

    @ApiModelProperty("业务类型")
    private String remarks;

    @ApiModelProperty("财审备注")
    private String auditRemarks;

    public DealDetailModel(String time, String buy, String pay, String debt, String oper ,String nameNo,String payway,String remarks) {
        this.time = time;
        this.buy = buy;
        this.pay = pay;
        this.debt = debt;
        this.oper = oper;
        this.nameNo = nameNo;
        this.payway = payway;
        this.remarks = remarks;
    }

    public DealDetailModel(String time, String buy, String pay, String debt, String oper, String nameNo,String payway, String remarks, String auditRemarks) {
        this.time = time;
        this.buy = buy;
        this.payway = payway;
        this.pay = pay;
        this.debt = debt;
        this.oper = oper;
        this.nameNo = nameNo;
        this.remarks = remarks;
        this.auditRemarks = auditRemarks;
    }
}