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

	private Logger logger = LoggerFactory.getLogger(StockinController.class);
	@Autowired
	private IStockDetailService stockinService;

	@Resource(name = "redisTemplate")
	private RedisTemplate redisTemplate;

	@Resource(name="redisJsonTemplate")
	private RedisTemplate<String ,Object> jsonRedisTemplate;

	@Value("${template.path}")
	private String templatePath;

	@Autowired
	private IStockService stockService;

	@PostMapping("/add")
	@DuplicateSubmitToken
	@ApiOperation("商品入库")
	public ResponseModel stockin(@Valid @RequestBody @ApiParam Stock stock, HttpServletRequest request) {
		// 入库
		if (Objects.isNull(stock)){
			return ResponseModel.getInstance().succ(false).msg("无效的库存信息");
		}

		if(Objects.isNull(stock.getStockins())){
			return ResponseModel.getInstance().succ(false).msg("请输入商品信息");
		}

		if (stock.getStockins().isEmpty()){
			return ResponseModel.getInstance().succ(false).msg("请输入商品信息");

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

//	/**
//	 * http://localhost:80/stock/out/exportCkdPDF/1
//	 * @param stockId
//	 * @param response
//	 * @return
//	 */
//	@ApiOperation(value = "导出出库单为PDF")
//	@RequestMapping(value = "/exportRkdPDF/{stockId}", method = RequestMethod.GET)
//	public ResponseModel exportRkdPDF(@PathVariable Long stockId, HttpServletResponse response) {
//		OutputStream out = null;
//
//		try {
//			Map<String, Object> params = stockService.prinAndPdf(stockId);
//
//			String path = FileUtil.getServletClassesPath();
//			//生成的html文件
//			String htmlFilePath = templatePath + "test" + File.separator + params.get("stockNo") + ".html";
//			String tplPath =    "/tpl/rkdpdftpl/tpl.html";
//			//字体
//			String fontPath = path  + "/tpl/cgdpdftpl/simsun.ttc";
//
//			//根据模板生成html
//			String content = FileUtil.getViewByBuffer(params, tplPath);
//
//			//生成html文件
//			FileUtil.writeFile(content, htmlFilePath, "UTF-8");
//
//			response.setContentType("application/pdf");
//			String title = new String(( "出库单-" +  params.get("stockNo")).getBytes("gb2312"), "ISO8859-1") + ".pdf";
//			response.addHeader("Content-Disposition", "attachment;filename=" + title);		//转码防止乱码
//
//			out = response.getOutputStream();
//
//			PrintSingleton.INSTNACE.getInstance().createPdf(htmlFilePath,fontPath,out);
//
//			return ResponseModel.getInstance();
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			if (out != null) {
//				try {
//					out.close();
//				} catch (IOException e) {
//					return ResponseModel.getInstance().succ(false);
//				}
//			}
//			return ResponseModel.getInstance().succ(false);
//
//		}
//
//	}
//
//	/**
//	 * http://localhost:80/stock/out/printCkd/5
//	 * @param stockId
//	 * @param response
//	 * @return
//	 */
//	@ApiOperation(value = "打印入库单")
//	@RequestMapping(value = "/printRkd/{stockId}", method = RequestMethod.GET)
//	public ResponseModel printRkd(@PathVariable Long stockId, HttpServletResponse response) {
//
//		Map<String, Object> params = stockService.prinAndPdf(stockId);
//
//		//每个模板 这里替换下
//		String tplPath =    "/tpl/rkdpdftpl/tpl.html";
//
//		//根据模板生成html
//		String htmlContent = FileUtil.getViewByBuffer(params, tplPath);
//
//		WebPrintModel wm = PrintSingleton.INSTNACE.getInstance().retOk(htmlContent, "21", "29.7");
//
//		return ResponseModel.getInstance().data(wm);
//
//	}

}