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
@TableName("sys_role")
@ApiModel
public class SysRole extends Model<SysRole> {

    @TableId(type = IdType.INPUT)
    @ApiModelProperty("角色id")
    private Integer rid;

    @NotNull
    @ApiModelProperty(value = "角色名",required = true)
    private String rname;

    @ApiModelProperty("角色描述")
    private String rdesc;

    @ApiModelProperty("角色创建时间")
    private String roleCreated;

    @TableField(update = "now()")
    @ApiModelProperty("角色更新时间")
    private String roleUpdated;
}
