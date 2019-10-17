package com.trenska.longwang.model.sys;

import com.trenska.longwang.entity.sys.SysConfig;
import com.trenska.longwang.entity.sys.SysPerm;
import com.trenska.longwang.entity.sys.SysRole;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Set;

/**
 * 2019/4/3
 * 创建人:Owen
 * 登陆后返回的结果模型
 */
@Data
@ApiModel
public class LoginResultModel {

	@ApiModelProperty("登陆成功与否 true:登陆成功")
	private Boolean success ;

	@ApiModelProperty("登陆成功返回的令牌")
	private String token;

	@ApiModelProperty("登陆成功返回的头像路径")
	private String avatarPath;

	@ApiModelProperty("登陆成功返回的账号id")
	private Integer empId;

	@ApiModelProperty("登陆成功返回的用户昵称")
	private String empName;

	@ApiModelProperty("登陆成功后返回的用户角色")
	private Set<SysRole> roles;

	@ApiModelProperty("登陆成功后返回的用户权限")
	private Set<SysPerm> perms;

	@ApiModelProperty("登陆返回的消息，默认为操作成功")
	private String msg = "操作成功";

	@ApiModelProperty("sessionId")
	private String sessionId;

	@ApiModelProperty("系统配置")
	private SysConfig sysConfig;
}
