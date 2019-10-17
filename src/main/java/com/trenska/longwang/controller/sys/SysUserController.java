package com.trenska.longwang.controller.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.annotation.ActionLog;
import com.trenska.longwang.annotation.DuplicateSubmitToken;
import com.trenska.longwang.constant.Constant;
import com.trenska.longwang.constant.LogType;
import com.trenska.longwang.context.ApplicationContextHolder;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.sys.SysConfig;
import com.trenska.longwang.entity.sys.SysEmp;
import com.trenska.longwang.exception.AccountDuplicatedException;
import com.trenska.longwang.model.sys.LoginResultModel;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.sys.ISysEmpRoleService;
import com.trenska.longwang.service.sys.ISysEmpService;
import com.trenska.longwang.util.ObjectCopier;
import com.trenska.longwang.util.PageUtils;
import com.trenska.longwang.util.TimeUtil;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 登录相关接口
 */

@RestController
@CrossOrigin
@Slf4j
@RequestMapping("/user")
@Api(value = "UserController", description = "账号接口")
public class SysUserController{

	@Value("${spring.redis.token-timeout}")
	private Integer tokenTimeout;

	@Resource(name = Constant.REDIS_TEMPLATE_NAME)
	private RedisTemplate redisTemplate;

	@Resource(name = Constant.REDIS_JSON_TEMPLATE_NAME)
	private RedisTemplate<String, Object> jsonRedisTemplate;

	@Autowired
	private ISysEmpService userService;

	@Autowired
	private ISysEmpRoleService empRoleService;

	////////////////////////////////////////////////////////////登陆/注销\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	@PostMapping("/login")
	@DuplicateSubmitToken
	@ActionLog(type = LogType.RETRIEVE, content = "用戶登陸")
	@ApiOperation(value = "用户登陆,根据用户名和用户密码查询用户信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "empAcct", value = "用户账号", paramType = "body", required = true, dataType = "String"),
			@ApiImplicitParam(name = "empPwd", value = "用户密码", paramType = "body", required = true, dataType = "String")
	})
	public LoginResultModel login(@ApiParam(name = "emp", value = "登录参数", required = true) @Valid @RequestBody SysEmp emp, HttpServletRequest request) {

		UsernamePasswordToken token = new UsernamePasswordToken(emp.getEmpAcct(), emp.getEmpPwd());
		Subject subject = SecurityUtils.getSubject();
		try{
			subject.login(token); // 登陆后可以通过subject获取到Principal对象
		}catch (IncorrectCredentialsException ex){
			LoginResultModel loginResultModel = new LoginResultModel();
			loginResultModel.setSuccess(false);
			loginResultModel.setMsg(Constant.LOGIN_FAILURE_MSG);
			return loginResultModel;
		}catch (UnknownAccountException ex){
			LoginResultModel loginResultModel = new LoginResultModel();
			loginResultModel.setSuccess(false);
			loginResultModel.setMsg(Constant.LOGIN_FAILURE_MSG);
			return loginResultModel;
		}
		SysEmp dbSysEmp = (SysEmp) subject.getPrincipal();

		if (null == dbSysEmp){
			throw new AuthenticationException(Constant.LOGIN_FAILURE_MSG);
		}


		Integer empId = dbSysEmp.getEmpId();

		String accessToken = UUID.randomUUID().toString();
		// 将token存入redis中
		redisTemplate.opsForValue().set(Constant.ACCESS_TOKEN_IDENTIFIER.concat(accessToken), accessToken, tokenTimeout, TimeUnit.MILLISECONDS);

		jsonRedisTemplate.opsForValue().set(accessToken + Constant.EMP_ID_IDENTIFIER, empId, tokenTimeout, TimeUnit.MILLISECONDS);

//		jsonRedisTemplate.opsForValue().set(accessToken.concat("::emp"),sysEmp,tokenTimeout,TimeUnit.MILLISECONDS);

		LoginResultModel loginResultModel = new LoginResultModel();
		ObjectCopier.copyProperties(dbSysEmp, loginResultModel);

		// 获取登陆用户的系统配置,如果没有对应的记录则采用系统配置，系统配置的sysEmpId为10000
		SysConfig sysConfig = new SysConfig().selectById(empId);

		if (sysConfig == null) {
			sysConfig = ApplicationContextHolder.getBean(SysConfig.class);
		}
		loginResultModel.setSysConfig(sysConfig);

		SecurityUtils.getSubject().getSession().setAttribute("sysConfig",sysConfig);

		jsonRedisTemplate.opsForValue().set(Constant.SYS_CONFIG_IDENTIFIER + empId,sysConfig );

		/**
		 * 更新客户的登陆ip,最后一次登陆时间，总登陆次数
		 * 这里由于redis 缓存了SysEmp，在redis缓存失效内用户多次退出-进入系统，loginCts不会被更新
		 * 解决这个问题需要 将SysEmp的查询缓存取消，使用@Cacheable注解进行方法级别的缓存来控制其它查询的缓存
		 */
		SysEmp updatingSysEmp = new SysEmp();
		updatingSysEmp.setEmpId(dbSysEmp.getEmpId());
		updatingSysEmp.setLastLoginTime(TimeUtil.getCurrentTime(Constant.TIME_FORMAT));
		updatingSysEmp.setLoginCts(dbSysEmp.getLoginCts() + 1);
		updatingSysEmp.setLastLoginIp(request.getRemoteHost().concat(" ").concat(request.getRemoteAddr()).concat(String.valueOf(request.getRemotePort())));
		updatingSysEmp.updateById();

		loginResultModel.setSuccess(true);
		loginResultModel.setToken(accessToken);
		loginResultModel.setSessionId(UUID.randomUUID().toString());
		return loginResultModel;
	}

	@DuplicateSubmitToken
	@ApiOperation("退出登录")
	@PostMapping(value = "/logout")
	public ResponseModel loginOut() {
		log.debug("principal :    {}",SecurityUtils.getSubject().getPrincipal());
		SecurityUtils.getSubject().logout();
		return ResponseModel.getInstance().succ(true).msg("退出登录成功");
	}

	////////////////////////////////////////////////////////////增删改查\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	@PostMapping("/add")
	@DuplicateSubmitToken
	@ApiImplicitParams({
			@ApiImplicitParam(name = "empName", value = "员工姓名", paramType = "body", required = true, dataType = "String"),
			@ApiImplicitParam(name = "empAcct", value = "员工账号", paramType = "body", required = true, dataType = "String"),
			@ApiImplicitParam(name = "empPwd", value = "员工账号密码", paramType = "body", required = true, dataType = "String"),
			@ApiImplicitParam(name = "empType", value = "员工类型", paramType = "body", required = true, dataType = "String"),
			@ApiImplicitParam(name = "branchNo", value = "员工归属的分公司编号", paramType = "body", required = true, dataType = "int"),
			@ApiImplicitParam(name = "rids", value = "员工账号所拥有的权限id集合", paramType = "body", dataType = "list"),
			@ApiImplicitParam(name = "areaGrpIds", value = "区域分组id集合", paramType = "body", dataType = "list"),

	})
	@ApiOperation("添加账号")
	public ResponseModel add(@ApiParam(name = "emp", required = true) @RequestBody SysEmp emp) {

		if (Objects.isNull(emp)) {
			return ResponseModel.getInstance().succ(false).msg("请输入有效的账号信息");
		}

		if (StringUtils.isEmpty(emp.getEmpAcct())) {
			return ResponseModel.getInstance().succ(false).msg("帐号不能为空");
		}
		if (StringUtils.isEmpty(emp.getEmpPwd())) {
			return ResponseModel.getInstance().succ(false).msg("密码不能为空");
		}
		SysEmp sysEmp = userService.getOne(
				new LambdaQueryWrapper<SysEmp>()
						.eq(SysEmp::getEmpAcct,emp.getEmpAcct().trim())
		);
		if (!Objects.isNull(sysEmp)) {
			return ResponseModel.getInstance().succ(false).msg("用户已注册");
		}
		//密码加密
		RandomNumberGenerator saltGenerator = new SecureRandomNumberGenerator();
		String salt = saltGenerator.nextBytes().toBase64();
		String hashedPwd = new Sha256Hash(emp.getEmpPwd(), salt, 1024).toBase64();
		//保存新用户数据
		emp.setEmpPwd(hashedPwd);
		emp.setSalt(salt);
		emp.setCreatedTime(TimeUtil.getCurrentDate());

		return userService.saveEmp(emp);
	}

	@DuplicateSubmitToken
	@DeleteMapping("/delete/{empId}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "empId", value = "员工账号", paramType = "path", required = true, dataType = "int")
	})
	@ApiOperation("根据账号id删除账号")
	public ResponseModel delete(@ApiParam(name = "empId", required = true) @PathVariable Integer empId) {
		boolean success = userService.removeById(empId);
		return ResponseModel.getInstance().succ(success).msg(success ? "删除账号成功" : "删除账号失败");
	}

	@DuplicateSubmitToken
	@PutMapping("/update/pwd/{empId}")
	@ApiOperation("修改账号密码")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "empId", value = "员工id", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "empPwd", value = "员工账号密码", paramType = "query", required = true, dataType = "String")
	})
	public ResponseModel update(@PathVariable("empId") Integer empId , @RequestParam String empPwd) {

		if(empId < 0 ){
			return ResponseModel.getInstance().succ(false).msg("无效的用户id");
		}

		if (StringUtils.isEmpty(empPwd)) {
			return ResponseModel.getInstance().succ(false).msg("密码不能为空");
		}

		SysEmp oldUser = userService.getById(empId);

		if(null == oldUser) {
			return ResponseModel.getInstance().succ(false).msg("用户不存在");
		}

		if(empPwd.equals(oldUser.getEmpPwd())){
			return ResponseModel.getInstance().succ(false).msg("新密码与旧密码相同，不需要更新");
		}

		//密码加密
		RandomNumberGenerator saltGenerator = new SecureRandomNumberGenerator();
		String salt = saltGenerator.nextBytes().toBase64();
		String hashedPwd = new Sha256Hash(empPwd, salt, 1024).toBase64();

		SysEmp sysEmp = new SysEmp(empId, hashedPwd, salt, TimeUtil.getCurrentTime(Constant.TIME_FORMAT));

		userService.updateById(sysEmp);
		return ResponseModel.getInstance().succ(true).msg("密码更新成功");
	}

	@DuplicateSubmitToken
	@PutMapping("/update/info/{empId}")
	@ApiOperation("修改账号的名称、账号和密码")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "empAcct", value = "员工账号", paramType = "body", dataType = "String"),
			@ApiImplicitParam(name = "empName", value = "员工名称", paramType = "body", dataType = "String"),
			@ApiImplicitParam(name = "empPwd", value = "员工账号密码", paramType = "body", dataType = "String"),
			@ApiImplicitParam(name = "empId", value = "员工id", paramType = "path", required = true, dataType = "int")
	})
	@Transactional
	public ResponseModel updateEmpAcctNamePwd(@PathVariable("empId") Integer empId ,@RequestBody SysEmp data) throws AccountDuplicatedException {
		@NotNull String empAcct = data.getEmpAcct();
		String empName = data.getEmpName();
		@NotNull String empPwd = data.getEmpPwd();
		SysEmp updatingSysEmp = new SysEmp();

		if(empId < 0 ){
			return ResponseModel.getInstance().succ(false).msg("无效的用户id");
		}
		if(Objects.isNull(empPwd)&&Objects.isNull(empAcct)&&Objects.isNull(empName)){
			return ResponseModel.getInstance().succ(false).msg("无效的变更信息");
		}
		SysEmp dbUser = userService.getById(empId);

		if(null == dbUser) {
			return ResponseModel.getInstance().succ(false).msg("用户不存在");
		}

		String hashedPwd = null;
		String salt = null;
		if(StringUtils.isNotEmpty(empPwd)) {
			//密码加密
			RandomNumberGenerator saltGenerator = new SecureRandomNumberGenerator();
			salt = saltGenerator.nextBytes().toBase64();
			hashedPwd = new Sha256Hash(empPwd, salt, 1024).toBase64();
			updatingSysEmp.setSalt(salt);
		}

		if(hashedPwd.equals(dbUser.getEmpPwd())){
			return ResponseModel.getInstance().succ(false).msg("新密码与旧密码相同，不需要更新");
		}

		String currentTime = TimeUtil.getCurrentTime(Constant.TIME_FORMAT);
		updatingSysEmp.setEmpId(empId);
		if(!Objects.isNull(hashedPwd)){
			updatingSysEmp.setEmpPwd(hashedPwd);
		}
		if(!Objects.isNull(empName)){
			updatingSysEmp.setEmpName(empName);
		}
		if(!Objects.isNull(empAcct)){
			updatingSysEmp.setEmpAcct(empAcct);
		}
		updatingSysEmp.setUpdatedTime(currentTime);
		// 通过设置数据库字段的唯一性+ControllerAdvice来处理重名
		try {
			userService.updateById(updatingSysEmp);
		}catch (Exception ex){
			String exceptionName = ex.getClass().getSimpleName();
			if("DuplicateKeyException".equals(exceptionName)) {
				throw new AccountDuplicatedException("用户重名");
			}
		}
		return ResponseModel.getInstance().succ(true).msg("账号更新成功");
	}

	@GetMapping("/list/all")
	@ApiOperation("获取所有账号")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "empType", value = "员工类型", paramType = "query", dataType = "string")
	})
	public List<SysEmp> listAll(@RequestParam(name = "empType", required = false) String empType) {
		if (StringUtils.isEmpty(empType)) {
			return userService.list();
		} else {
			return userService.list(new QueryWrapper<SysEmp>().eq("emp_type", empType));
		}
	}

	/**
	 * 分页获取账号信息，包括角色和区域分组
	 * @param current
	 * @param size
	 * @param empType
	 * @return
	 */

	@GetMapping("/list/page/{current}/{size}")
	@ApiOperation("分页获取账号信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "empType", value = "员工类型", paramType = "query", required = true, dataType = "string")
	})
	public PageHelper<SysEmp> listSysEmpPage(
			@PathVariable("current") Integer current,
			@PathVariable("size") Integer size,
			@RequestParam(name = "empType", required = false) String empType
	) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("empType", empType);
		Page<SysEmp> pageInfo = userService.getSysEmpPage(params, page);
		return PageHelper.getInstance().pageData(pageInfo);
	}



//	@ApiOperation("注册账号")
//	@PostMapping("/register")
//	public ResponseModel register(@ApiParam("用户账号") @RequestParam("empAcct") String empAcct, @ApiParam("用户密码")@RequestParam("empPwd") String empPwd) {
//		SysEmp sysEmp = new SysEmp();
//		sysEmp.setEmpAcct(empAcct);
//
//		RandomNumberGenerator numberGenerator = new SecureRandomNumberGenerator();
//		String salt = numberGenerator.nextBytes().toBase64();
//		sysEmp.setSalt(salt);
//		Sha256Hash hashedPwd = new Sha256Hash(empPwd, salt, 1024);
//		sysEmp.setEmpPwd(hashedPwd.toBase64());
//		Boolean succ = userService.saveUser(sysEmp);
//		return ResponseModel.getInstance().succ(succ).msg(succ ? "注册账号成功":"注册账号失败");
//	}

}