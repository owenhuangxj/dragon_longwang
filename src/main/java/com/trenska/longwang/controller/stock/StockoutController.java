package com.trenska.longwang.controller.stock;

import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.annotation.CheckDuplicateSubmit;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.indent.StockMadedate;
import com.trenska.longwang.entity.stock.Stock;
import com.trenska.longwang.model.prints.WebPrintModel;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.goods.IGoodsService;
import com.trenska.longwang.service.stock.IStockDetailService;
import com.trenska.longwang.service.stock.IStockService;
import com.trenska.longwang.service.sys.ISysEmpService;
import com.trenska.longwang.util.PDFUtil;
import com.trenska.longwang.util.PageUtils;
import com.trenska.longwang.util.PrintSingleton;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Owen
 * @since 2019-04-17
 */
@RestController
@RequestMapping("/stock/out")
@Api(description = "出库接口")
@CrossOrigin
@Slf4j
public class StockoutController {

	@Autowired
	private IStockDetailService stockoutService;

	@Autowired
	private IStockService stockService;

	@Autowired
	private ISysEmpService empService;

	@Autowired
	private IGoodsService goodsService;

	@Value("${template.path}")
	private String templatePath;

	@PostMapping("/add")
	@CheckDuplicateSubmit
	@ApiOperation("商品出库")
	public ResponseModel stockout(@RequestBody @ApiParam Stock stock, HttpServletRequest request) {

		if (Objects.isNull(stock)) {
			return ResponseModel.getInstance().succ(false).msg("出库失败:请输入有效的库存信息");
		}
		if (Objects.isNull(stock.getStockouts())) {
			return ResponseModel.getInstance().succ(false).msg("出库信息不能为null");
		}
		if (stock.getStockouts().isEmpty()) {
			return ResponseModel.getInstance().succ(false).msg("出库的商品信息不能为空");
		}
		// 出库
		return stockoutService.stockout(stock, request);
	}

	@CheckDuplicateSubmit
	@PutMapping("/cancel/{stockNo}")
	@ApiOperation("作废出库单")
	public ResponseModel cancelStockout(@PathVariable("stockNo") String stockNo) {
		if (StringUtils.isEmpty(stockNo)) {
			return ResponseModel.getInstance().succ(false).msg("无效的出库单");
		}
		return stockoutService.cancelStockout(stockNo);
	}

	@GetMapping("/list/page/made-date/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "goodsId", value = "商品id", required = true, paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "stockType", value = "入库/出库", required = true, paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "all", value = "是否显示库存小于等于0的批次,需要显示传true，不需要传递false", required = true, paramType = "query", dataType = "boolean")
	})
	@ApiOperation("查询商品的库存分页")
	public PageHelper<StockMadedate> listStockDetailPage(
			@PathVariable(value = "current") Integer current,
			@PathVariable(value = "size") Integer size,
			@RequestParam(name = "goodsId") Integer goodsId,
			@RequestParam(name = "stockType") String stockType,
			@RequestParam(name = "all") Boolean all
	) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("all", all);
		params.put("goodsId", goodsId);
		params.put("stockType", stockType);
		Page<StockMadedate> pageInfo = stockoutService.getGoodsMadeDateDetail(params, page);
		return PageHelper.getInstance().pageData(pageInfo);
	}

	/**
	 * http://localhost/stock/out/exportPDF/3/BY
	 *
	 * @param stockId
	 * @param
	 * @return
	 */
	@ApiOperation(value = "导出 出库单 入库单 报溢单 报损单 为PDF")
	@RequestMapping(value = "/exportPDF/{stockId}/{type}", method = RequestMethod.GET)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "stockId", value = "单据ID", required = true, paramType = "path", dataType = "long"),
			@ApiImplicitParam(name = "type", value = "单据类型 出库单:CK  入库单:RK  报溢单:BY  报损单:BS", required = true, paramType = "path", dataType = "string")

	})
	public ResponseEntity<?> exportPDF(@PathVariable Long stockId, @PathVariable String type) {
		HttpHeaders headers = new HttpHeaders();
		log.info("template = " + templatePath);
		try {
			Map<String, Object> params = stockService.prinAndPdf(stockId);
			if (null == params || params.isEmpty()) {
				return new ResponseEntity<String>("{ \"succ\" : \"false\", \"msg\" : \"获取数据失败\" }",
						headers, HttpStatus.NOT_FOUND);
			}

			String htmlStr = "";
			String title = "";
			if ("CK".equals(type.toUpperCase())) {
				htmlStr = PDFUtil.freemarkerRender(params, templatePath + File.separator + "ckdpdftpl/tpl.ftl");
				title = new String(("出库单-" + params.get("stockNo")).getBytes("gb2312"), "ISO8859-1") + ".pdf";
			} else if ("RK".equals(type.toUpperCase())) {
				htmlStr = PDFUtil.freemarkerRender(params, templatePath + File.separator + "rkdpdftpl/tpl.ftl");
				title = new String(("入库单-" + params.get("stockNo")).getBytes("gb2312"), "ISO8859-1") + ".pdf";
			} else if ("BY".equals(type.toUpperCase())) {
				htmlStr = PDFUtil.freemarkerRender(params, templatePath + File.separator + "bydpdftpl/tpl.ftl");
				title = new String(("报溢单-" + params.get("stockNo")).getBytes("gb2312"), "ISO8859-1") + ".pdf";
			} else if ("BS".equals(type.toUpperCase())) {
				htmlStr = PDFUtil.freemarkerRender(params, templatePath + File.separator + "bsdpdftpl/tpl.ftl");
				title = new String(("报损单-" + params.get("stockNo")).getBytes("gb2312"), "ISO8859-1") + ".pdf";
			}
			log.info("html= " + htmlStr);
			byte[] pdfBytes = PDFUtil.createPDF(htmlStr, templatePath + File.separator + "simsun.ttc");
			if (pdfBytes != null && pdfBytes.length > 0) {
				headers.setContentDispositionFormData("attachment", title);
				headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
				return new ResponseEntity<byte[]>(pdfBytes, headers, HttpStatus.OK);
			}

		} catch (UnsupportedEncodingException e) {
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			return new ResponseEntity<String>("{ \"code\" : \"404\", \"message\" : \"not found\" }",
					headers, HttpStatus.NOT_FOUND);

		}
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		return new ResponseEntity<String>("{ \"succ\" : \"false\", \"msg\" : \"获取数据失败\" }",
				headers, HttpStatus.NOT_FOUND);
	}

	/**
	 * http://localhost/stock/out/exportPDF/3/BY
	 *
	 * @param stockId
	 * @param type
	 * @return
	 */
	@ApiOperation(value = "打印出库单")
	@RequestMapping(value = "/printCkd/{stockId}/{type}", method = RequestMethod.GET)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "stockId", value = "单据ID", required = true, paramType = "path", dataType = "long"),
			@ApiImplicitParam(name = "type", value = "单据类型 出库单:CK  入库单:RK  报溢单:BY  报损单:BS", required = true, paramType = "path", dataType = "string")

	})
	public ResponseModel printCkd(@PathVariable Long stockId, @PathVariable String type) {

		Map<String, Object> params = stockService.prinAndPdf(stockId);

		String htmlContent = "";
		if ("CK".equals(type)) {
			htmlContent = PDFUtil.freemarkerRender(params, templatePath + File.separator + "ckdpdftpl/tpl.ftl");
		} else if ("RK".equals(type)) {
			htmlContent = PDFUtil.freemarkerRender(params, templatePath + File.separator + "rkdpdftpl/tpl.ftl");
		} else if ("BY".equals(type)) {
			htmlContent = PDFUtil.freemarkerRender(params, templatePath + File.separator + "bydpdftpl/tpl.ftl");
		} else if ("BS".equals(type)) {
			htmlContent = PDFUtil.freemarkerRender(params, templatePath + File.separator + "bsdpdftpl/tpl.ftl");
		}

		WebPrintModel wm = PrintSingleton.INSTNACE.getInstance().retOk(htmlContent, "24.1", "13");

		return ResponseModel.getInstance().succ(true).data(wm);
	}
}