package com.trenska.longwang.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trenska.longwang.entity.sys.SysEmp;
import com.trenska.longwang.entity.sys.SysPerm;
import com.trenska.longwang.entity.sys.SysRole;
import com.trenska.longwang.service.sys.ISysEmpService;
import com.trenska.longwang.service.sys.ISysPermService;
import com.trenska.longwang.service.sys.ISysRoleService;
import org.apache.shiro.SecurityUtils;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
	 * 权限控制/鉴权的时候调用
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		SysEmp sysEmp = (SysEmp) getAvailablePrincipal(principals);
		SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
		authorizationInfo.setRoles(sysEmp.getRoles().stream().map(SysRole::getRname).collect(Collectors.toSet()));
		authorizationInfo.setStringPermissions(sysEmp.getPerms().stream().map(SysPerm::getPval).collect(Collectors.toSet()));
		return authorizationInfo;
	}

	/**
	 * Shiro在做认证->登陆 的时候会调用doGetAuthenticationInfo 进行认证
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
		/**
		 * 获取Subject.login(token);方法传递过来的token
		 * 在SysUserController中创建的是UsernamePasswordToken，所以强转为UsernamePasswordToken
		 */
		UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
		SysEmp sysEmp = empService.getOne(
				new LambdaQueryWrapper<SysEmp>()
						.eq(SysEmp::getEmpAcct, token.getUsername())
		);

		Set<SysRole> sysRoles = new HashSet<>();
		Set<SysPerm> sysPerms = new HashSet<>();
		SimpleAuthenticationInfo authenticationInfo = null;
		if (null != sysEmp) {
			sysRoles.addAll(roleService.getRolesByEmpId(sysEmp.getEmpId()));
			sysPerms.addAll(permService.getPermsByEmpId(sysEmp.getEmpId()));
			sysEmp.setRoles(sysRoles);
			sysEmp.setPerms(sysPerms);

			/**
			 * principal : 主体 这里的SysEmp;credentials : 凭据 这里的密码 ，realmName 这里的realm的名字
			 * public SimpleAuthenticationInfo(Object principal, Object credentials, String realmName) {
			 *  this.principals = new SimplePrincipalCollection(principal, realmName);
			 *  this.credentials = credentials;
			 * }
			 * 生成AuthenticationInfo对象并返回给Shiro，shiro会保存该对象，并且可以通过SecurityUtils.getSubject().getPrincipal()
			 * 方法在应用的任何位置获取到principal
			 */

			List<SysEmp> principals = new ArrayList<>();
			principals.add(sysEmp);
			authenticationInfo = new SimpleAuthenticationInfo(principals, sysEmp.getEmpPwd(), getName());
			// 将用户信息存入Session中
			SecurityUtils.getSubject().getSession(true).setAttribute("sysEmp", sysEmp);
//			ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
//			HttpServletRequest request = requestAttributes.getRequest();
//			HttpSession session = request.getSession(true);
//			session.setAttribute("sysEmp", sysEmp);
			if (sysEmp.getSalt() != null) {
				authenticationInfo.setCredentialsSalt(ByteSource.Util.bytes(sysEmp.getSalt()));
			}
		}
		return authenticationInfo;
	}
}