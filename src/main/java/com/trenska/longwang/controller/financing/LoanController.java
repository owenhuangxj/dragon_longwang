package com.trenska.longwang.controller.financing;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.financing.Loan;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.financing.ILoanService;
import com.trenska.longwang.util.PageUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *  调账单前端控制器
 * </p>
 *
 * @author Owen
 * @since 2019-07-30
 */
@RestController
@RequestMapping("/loan")
public class LoanController {

	@Autowired
	private ILoanService loanService;

	@PostMapping(value = "/add")
	@ApiOperation("新建调账单")
	public ResponseModel addLoan(@RequestBody @ApiParam Loan loan, HttpServletRequest request) {
		if(ObjectUtils.isEmpty(loan)){
			return ResponseModel.getInstance().succ(false).msg("无效的调帐单");
		}
		return loanService.addLoan(loan ,request);
	}

	@PutMapping(value = "/invalid/{loanId}")
	@ApiOperation("作废调账单")
	public ResponseModel invalidLoan(@PathVariable("loanId") Long loanId) {
		if(ObjectUtils.isEmpty(loanId)){
			return ResponseModel.getInstance().succ(false).msg("无效的调帐单");
		}
		return loanService.invalidLoanById(loanId);
	}

	@GetMapping(value = "/list/page/{current}/{size}")
	@ApiOperation("分页调账单")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "stat", value = "状态", paramType = "query", dataType = "boolean"),
			@ApiImplicitParam(name = "custName", value = "客户", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "结束日期", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "beginTime", value = "开始日期", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int")
	})
	public PageHelper<Loan> listPaywaysPage(
			@RequestParam(required = false, name = "stat") Boolean stat,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "custName") String custName,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@PathVariable("current") Integer current, @PathVariable("size") Integer size
	) {
		Map<String,Object> params = new HashMap<>();
		params.put("stat",stat);
		params.put("endTime",endTime);
		params.put("custName",custName);
		params.put("beginTime",beginTime);
		PageHelper<Object> pageHelper = new PageHelper<>(current, size);
		Page page = PageUtils.getPageParam(pageHelper);
		Page<Loan> pageInfo = loanService.getLoanPageSelective(page,params);
		return PageHelper.getInstance().pageData(pageInfo);
	}

}