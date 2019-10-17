package com.trenska.longwang.entity.sys;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author Owen
 * @since 2019-05-17
 */
@Data
@ApiModel("账号-角色")
@TableName("sys_emp_role")
@NoArgsConstructor
public class SysEmpRole extends Model<SysEmpRole> {

    @NotNull
    @ApiModelProperty("账号id")
    @TableId(value = "emp_id", type = IdType.INPUT)
    private Integer empId;

    @NotNull
    @ApiModelProperty("角色id")
    private Integer rid;

    public SysEmpRole(@NotNull Integer empId, @NotNull Integer rid) {
        this.empId = empId;
        this.rid = rid;
    }
}
