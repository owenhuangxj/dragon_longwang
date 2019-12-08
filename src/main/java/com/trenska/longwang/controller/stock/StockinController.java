package com.trenska.longwang.controller.stock;

import com.trenska.longwang.annotation.DuplicateSubmitToken;
import com.trenska.longwang.entity.stock.Stock;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.stock.IStockDetailService;
import com.trenska.longwang.service.stock.IStockService;
import com.trenska.longwang.util.StockUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Objects;

/**
 * 入库前端控制器
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
	@DuplicateSubmitToken
	@ApiOperation("商品入库")
	public ResponseModel stockin(@Valid @RequestBody @ApiParam Stock stock, HttpServletRequest request) {
		// 入库
		if (Objects.isNull(stock)){
			return ResponseModel.getInstance().succ(false).msg("无效的库存信息.");
		}

		if(Objects.isNull(stock.getStockins())){
			return ResponseModel.getInstance().succ(false).msg("请输入商品信息.");
		}

		if (stock.getStockins().isEmpty()){
			return ResponseModel.getInstance().succ(false).msg("请输入商品信息.");
		}
		return stockinService.stockin(stock,request);
	}

	@DuplicateSubmitToken
	@PutMapping("/cancel/{stockNo}")
	@ApiOperation("作废入库单")
	public ResponseModel cancelStockin(@PathVariable("stockNo") String stockNo, HttpServletRequest request) {
		if(stockNo == null){
			return ResponseModel.getInstance().succ(false).msg("无效的入库单");
		}
		return stockinService.cancelStockin(stockNo,request);
	}
}