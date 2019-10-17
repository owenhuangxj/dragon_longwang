package com.trenska.longwang.entity.financing;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@TableName("t_account_type")
@Data
public class AccountType extends Model<AccountType> {

    @TableId(value = "account_type_id",type =  IdType.AUTO)
    private Integer typeId;

    @ApiModelProperty("账目类型名称")
    private String typeName;

    @ApiModelProperty("付款单/收款单")
    private String type;

    @ApiModelProperty("标记是否可以删除 true : 可以删除 false : 不可删除")
    private Boolean deletable;

    @ApiModelProperty("描述")
    private String adesc;

}