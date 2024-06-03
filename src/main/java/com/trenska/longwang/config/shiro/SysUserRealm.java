package com.trenska.longwang.config.shiro;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trenska.longwang.entity.sys.SysEmp;
import com.trenska.longwang.entity.sys.SysPerm;
import com.trenska.longwang.entity.sys.SysRole;
import com.trenska.longwang.service.sys.ISysEmpService;
import com.trenska.longwang.service.sys.ISysPermService;
import com.trenska.longwang.service.sys.ISysRoleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.stream.Collectors;
@Slf4j
public class SysUserRealm extends AuthorizingRealm {
	@Autowired
	private ISysEmpService empService;
	@Autowired
	private ISysRoleService roleService;
	@Autowired
	private ISysPermService permService;

	@Override
	public void setCredentialsMatcher(CredentialsMatcher credentialsMatcher) {
		HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
		hashedCredentialsMatcher.setHashAlgorithmName(Sha256Hash.ALGORITHM_NAME);
		hashedCredentialsMatcher.setStoredCredentialsHexEncoded(false);
		hashedCredentialsMatcher.setHashIterations(1024);
		super.setCredentialsMatcher(hashedCredentialsMatcher);
	}

	/**
	 * Shiro在做认证->登陆 的时候会调用doGetAuthenticationInfo 进行认证
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
		log.info("Do authentication>>>");
		// 获取Subject.login(token);方法传递过来的token
		// 在SysUserController中创建的是UsernamePasswordToken，所以强转为UsernamePasswordToken
		UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
		SysEmp sysEmp = empService.getOne(
				new LambdaQueryWrapper<SysEmp>()
						.eq(SysEmp::getEmpAcct, token.getUsername())
		);
		if (sysEmp == null) {
			log.error("User retrieved from database is null>>>");
			return null;
		}
		SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(sysEmp, sysEmp.getEmpPwd(),
				"LongWangShiroRealm");
		authenticationInfo.setCredentialsSalt(ByteSource.Util.bytes(sysEmp.getSalt()));
		return authenticationInfo;
	}

	/**
	 * 权限控制/鉴权的时候调用
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		log.info("Do authorization>>>");
		SysEmp sysEmp = (SysEmp) principals.getPrimaryPrincipal();
		if (sysEmp == null) {
			log.error("User info saved in shiro is null>>>");
			return null;
		}
		Set<SysRole> sysRoles = roleService.getRolesByEmpId(sysEmp.getEmpId());
		Set<SysPerm> sysPerms = permService.getPermsByEmpId(sysEmp.getEmpId());
		SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
		authorizationInfo.setRoles(sysRoles.stream().map(SysRole::getRname).collect(Collectors.toSet()));
		authorizationInfo.setStringPermissions(sysPerms.stream().map(SysPerm::getPval).collect(Collectors.toSet()));
		return authorizationInfo;
	}
}