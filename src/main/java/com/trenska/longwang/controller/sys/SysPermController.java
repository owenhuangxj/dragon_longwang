package com.trenska.longwang.controller.sys;

import com.trenska.longwang.service.sys.ISysPermService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@CrossOrigin
@Slf4j
@RequestMapping("/perm")
@Api(value = "PermController", description = "权限接口")
public class SysPermController {

	@Autowired
	private ISysPermService permService;


	public static void main(String[] args) {
		System.out.println(new BigDecimal("3755").setScale(2,BigDecimal.ROUND_HALF_UP) );

	}


}
