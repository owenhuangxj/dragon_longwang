package com.trenska.longwang.entity.sys;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@TableName("sys_perm")
@ApiModel
public class SysPerm extends Model<SysPerm>{

    @TableId(type = IdType.INPUT)
    @ApiModelProperty("权限值")
    @NotNull
    private String pval;

    @ApiModelProperty("权限名")
    private String pname;

    @NotNull
    @ApiModelProperty("权限类型")
    private Integer ptype;

    @ApiModelProperty("权限父节点的值")
    private String parent;

    @ApiModelProperty("权限是否是叶子节点,true : 子节点")
    private Boolean isLeaf;

    @ApiModelProperty("权限更新时间")
    private String permUpdated;

    @TableField(update = "now()")
    @ApiModelProperty("权限创建时间")
    private String permCreated;
}
