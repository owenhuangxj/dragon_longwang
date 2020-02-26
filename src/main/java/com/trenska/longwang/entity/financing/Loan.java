package com.trenska.longwang.entity.financing;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 调账单
 * </p>
 *
 * @author Owen
 * @since 2019-07-30
 */
@Data
@TableName("t_loan")
@ApiModel("调帐单")
public class Loan extends Model<Loan> {

    @TableId(value = "loan_id", type = IdType.AUTO)
    private Long loanId;

    @ApiModelProperty("调帐单no")
    private String loanNo;

    @ApiModelProperty("借方")
    private Integer borrowCustId;

    @ApiModelProperty("借方名称")
    @TableField(exist = false)
    private String borrowCustName;

    @ApiModelProperty("贷方")
    private Integer lendCustId;

    @ApiModelProperty("贷方名称")
    @TableField(exist = false)
    private String lendCustName;

    @ApiModelProperty("金额")
    private String amount;

    @ApiModelProperty("制单人id")
    private int empId;
    @ApiModelProperty("时间")
    private String loanTime;

    @ApiModelProperty("状态")
    private boolean stat;

    @ApiModelProperty("说明")
    private String remarks;

}