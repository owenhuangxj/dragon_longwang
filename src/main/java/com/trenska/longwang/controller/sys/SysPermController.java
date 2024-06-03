package com.trenska.longwang.controller.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trenska.longwang.entity.sys.SysPerm;
import com.trenska.longwang.service.sys.ISysPermService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@Slf4j
@RequestMapping("/user")
@Api(value = "PermController", description = "权限接口")
public class SysPermController {

	@Autowired
	private ISysPermService permService;

	@GetMapping("/pname/{pname}")
	public List<SysPerm> listByPname(@PathVariable("pname") String pname) {
		LambdaQueryWrapper<SysPerm> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(SysPerm::getPname, pname);
		return permService.list(queryWrapper);
	}
}
