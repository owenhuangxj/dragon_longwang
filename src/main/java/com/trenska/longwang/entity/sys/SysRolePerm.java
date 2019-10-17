package com.trenska.longwang.entity.sys;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @author Owen
 * @since 2019-05-17
 */
@Data
@TableName("sys_role_perm")
@ApiModel("角色-权限映射关系")
@NoArgsConstructor
public class SysRolePerm extends Model<SysRolePerm>{

    @ApiModelProperty(value = "角色id",required = true)
    private Integer rid;

    @ApiModelProperty(value = "权限值",required = true)
    private String pval;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SysRolePerm that = (SysRolePerm) o;
        return rid.equals(that.rid) &&
                pval.equals(that.pval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rid, pval);
    }

    public SysRolePerm(Integer rid, String pval) {
        this.rid = rid;
        this.pval = pval;
    }
}
