package com.trenska.longwang.controller.stock;

import com.trenska.longwang.annotation.CheckDuplicateSubmit;
import com.trenska.longwang.entity.stock.Stock;
import com.trenska.longwang.model.sys.CommonResponse;
import com.trenska.longwang.service.stock.IStockDetailService;
import com.trenska.longwang.util.ResponseUtil;
import com.trenska.longwang.validate.ValidateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

/**
 * 入库前端控制器
 *
 * @author Owen
 * @since 2019-04-15
 */
@RestController
@RequestMapping("/stock/in")
@Api(description = "入库接口")
@CrossOrigin
public class StockinController {
	@Autowired
	private IStockDetailService stockinService;
	@Value("${template.path}")
	private String templatePath;

	@PostMapping("/add")
	@CheckDuplicateSubmit
	@ApiOperation("商品入库")
	public CommonResponse stockin(@Valid @RequestBody @ApiParam Stock stock) {
		// 入库
		return stockinService.stockin(stock);
	}

	@CheckDuplicateSubmit
	@PutMapping("/change")
	@ApiOperation("修改入库单")
	public CommonResponse changeStockin(@Valid @RequestBody @ApiParam Stock stock) throws IOException {
		validStock(stock);
		return stockinService.changeStockin(stock);
	}

	@CheckDuplicateSubmit
	@PutMapping("/cancel/{stockNo}")
	@ApiOperation("作废入库单")
	public CommonResponse cancelStockin(@PathVariable("stockNo") String stockNo) {
		if (stockNo == null) {
			return CommonResponse.getInstance().succ(false).msg("无效的入库单");
		}
		return stockinService.cancelStockin(stockNo);
	}

	public void validStock(Stock stock) throws IOException {
		if (stock == null || CollectionUtils.isEmpty(stock.getStockins())) {
			ResponseUtil.accessDenied(HttpServletResponse.SC_BAD_REQUEST,"无效的库存信息.","信息为空.");
			throw new RuntimeException("无效的库存信息.");
		}
		ValidateUtil.validateString(stock.getStockNo(), "stockNo", false, 15);
	}
}