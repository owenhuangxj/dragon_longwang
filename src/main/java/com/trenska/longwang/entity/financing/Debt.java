package com.trenska.longwang.entity.financing;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Owen
 * @since 2019-05-20
 */
@Data
@ApiModel("欠款单明细")
@NoArgsConstructor
public class Debt{

    @ApiModelProperty("客户名称")
    private String custName;
    /**
     * 操作时间
     */
    @ApiModelProperty("操作时间")
    private String time;

    @ApiModelProperty("数目")
    private String amount;
    /**
     * 交易后客户欠款
     */
    @ApiModelProperty("交易后客户欠款")
    private String newDebt;
    /**
     * 操作
     */
    @ApiModelProperty("操作类型")
    private String oper;

    @ApiModelProperty("备注")
    private String remarks = "";
}