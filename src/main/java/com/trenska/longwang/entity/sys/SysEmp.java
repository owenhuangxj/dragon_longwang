package com.trenska.longwang.entity.sys;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.trenska.longwang.entity.customer.AreaGrp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * 关联 表sys_emp
 */
@Data
@TableName("sys_emp")
@NoArgsConstructor
@ApiModel("员工账号")
public class SysEmp  extends Model<SysEmp> {
//    @ApiModelProperty("账号id，新建时前端不传递")
    @TableId(type= IdType.AUTO)
    private Integer empId;
    /**
     * 用户/员工账号，唯一，关联 emp_acct
     */
//    @ApiModelProperty("用户账号,唯一，手动输入")
    @NotNull
    private String empAcct;
    /**
     * 用户/员工类型，关联emp_type
     */
//    @ApiModelProperty("员工类型")
    private String empType;
    /**
     * 区域码，关联 ncode
     */
//    @ApiModelProperty("区域码")
    private Integer ncode;
    /**
     * 初始账号，关联 init_acct
     */
//    @ApiModelProperty("初始账号")
    private String initAcct;
    /**
     * 姓名，即实名，关联 emp_name
     */
//    @ApiModelProperty("姓名，即实名")
    private String empName;
    /**
     * 昵称，关联 nickname
     */
//    @ApiModelProperty("昵称")
    private String nickname;
    /**
     * 关联 emp_pwd
     */
    @NotNull
//    @ApiModelProperty("账号密码")
    private String empPwd;
    /**
     * 员工员工归属的分公司编号，关联branch_no
     */
//    @ApiModelProperty("员工归属的分公司编号")
    private Integer branchNo;
    /**
     * 密码加密的密盐，关联salt
     */
    private String salt;
    /**
     * 头像地址，关联 avatar_path
     */
    private String avatarPath;
    /**
     * 账号状态，关联 acct_status
     */
//    @ApiModelProperty("账号状态: 1 正常，0 冻结")
    private Integer acctStatus;
    /**
     * 账号创建时间，关联 created_time
     */
//    @ApiModelProperty("账号创建时间")
    private String createdTime;

    /**
     * 账号更新时间，关联updated_time
     */
//    @ApiModelProperty("账号更新时间")
    @TableField(update="now()")
    private String updatedTime;

    /**
     * 账号冻结时间，关联 deleted_time
     */
//    @ApiModelProperty("账号冻结时间")
    private String deletedTime;
    /**
     * 最后一次登陆时间，关联 last_login_time
     */
//    @ApiModelProperty("最后一次登陆时间")
    private String lastLoginTime;
    /**
     * 最近一次的登陆ip，关联 last_login_ip
     */
//    @ApiModelProperty("最近一次的登陆ip")
    private String lastLoginIp;
    /**
     * 登陆次数，关联 login_cts
     */
//    @ApiModelProperty("登陆次数")
    private Long loginCts;
    /**
     * 激活票，关联 ticket
     */
    private String ticket;
    /**
     * 最近一次登陆失败时间，关联 fail_time
     */
//    @ApiModelProperty("最近一次登陆失败时间")
    private String failTime;
    /**
     * 关联 fail_cts
     */
//    @ApiModelProperty("登陆失败次数")
    private Integer failCts;
    /**
     * 关联 email
     */
//    @ApiModelProperty("邮箱")
    private String email;

    @TableField(exist = false)
    @ApiModelProperty("账号所负责的区域分组id集合")
    private Collection<Integer> areaGrpIds = new HashSet<>();

    @TableField(exist = false)
    @ApiModelProperty("账号所负责的区域分组集合")
    private Collection<AreaGrp> areaGrps = new HashSet<>();

    @ApiModelProperty("是否拥有所有数据权限标志位")
    private Boolean allData;

//    @TableLogic
    @ApiModelProperty(hidden = true,value = "逻辑删除")
    private Boolean deleted;

    @TableField(exist = false)
    private List<Integer> rids = new ArrayList<>();

    @TableField(exist = false)
    private Set<SysRole> roles= new HashSet();

    @TableField(exist = false)
    private Set<SysPerm> perms = new HashSet();

    public SysEmp(Integer empId, @NotNull String empPwd, String salt, String updatedTime) {
        this.empId = empId;
        this.empPwd = empPwd;
        this.salt = salt;
        this.updatedTime = updatedTime;
    }
}