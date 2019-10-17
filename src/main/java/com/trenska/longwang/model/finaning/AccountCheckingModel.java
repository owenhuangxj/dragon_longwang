package com.trenska.longwang.model.finaning;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ApiModel("对账单数据模型")
@NoArgsConstructor
public class AccountCheckingModel {

    @ApiModelProperty("客户ID")
    private Integer custId;
    @ApiModelProperty("客户编号")
    private String custNo;
    @ApiModelProperty("客户名称")
    private String  custName;
    @ApiModelProperty("上期结欠")
    private String initDebt = "0";
    @ApiModelProperty("销售应收")
    private String salesAmount = "0";
    @ApiModelProperty("已收")
    private String receivedAmount = "0";
    @ApiModelProperty("已付")
    private String payedAmount = "0";
    /**
     * 起初欠款+销售应收-已收-已付
     */
    @ApiModelProperty("待收欠款")
    private String debtAmount = "0";

    @ApiModelProperty("调账减少/借入")
    private String borrow = "0";

    @ApiModelProperty("调账增加/借出")
    private String lend = "0";

}
