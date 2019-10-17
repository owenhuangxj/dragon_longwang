package com.trenska.longwang.service.indent;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.financing.DealDetailSummarizing;
import com.trenska.longwang.entity.financing.Receipt;
import com.trenska.longwang.entity.indent.Indent;
import com.trenska.longwang.entity.indent.IndentDetail;
import com.trenska.longwang.model.indent.IndentInfoModel;
import com.trenska.longwang.model.indent.IndentNoCustIdNameModel;
import com.trenska.longwang.model.report.*;
import com.trenska.longwang.model.sys.ResponseModel;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 订货单 服务类
 *
 * @author Owen
 * @since 2019-04-22
 */
public interface IIndentService extends IService<Indent> {

	ResponseModel saveIndent(Indent indent, HttpServletRequest request);

	ResponseModel removeIndentById(Long indentId, String indentType);

	ResponseModel removeIndentByIds(Collection<Long> indentIds, String indentType);

	IndentInfoModel getIndentInfoModel(Integer custId, Integer goodsId);

	ResponseModel updateIndent(Indent indent);

	Page<Indent> getIndentPageSelective(Map<String, Object> params, Page page, HttpServletRequest request);

	ResponseModel confirmIndent(String indentNo, Integer custId);

	ResponseModel cancelIndentByNo(String indentNo);

	ResponseModel repealIndent(long indentId, HttpServletRequest request);

	ResponseModel stockoutIndent(Indent indent, HttpServletRequest request);

	Indent getIndentInfo(String indentNo);

	ResponseModel invalidIndent(Indent indent, HttpServletRequest request);

	Page<Indent> getIndentPage(Page page);

	Indent getIndentByNo(String indentNo);

	List<IndentDetail> getIndentDetails(String indentNo);

	ResponseModel saveSalesReturn(Indent indent, HttpServletRequest request);

	ResponseModel invalidSalseReturn(Indent indent, HttpServletRequest request);

	ResponseModel removeReturnSalesById(Long indentId);

	ResponseModel removeReturnSalesByIds(Collection<Long> indentIds);

	ResponseModel addIou(Long indentId, String iouAmnt);

	ResponseModel addOrUpdateIou(Long indentId, String iouAmnt, String iouTime, String iouRemarks);

	ResponseModel indentPay(Long indentId, String iouAmnt, String oper, String payway, HttpServletRequest request);

//	IndentInfoModel getIndentInfoModel(Map<String, Object> params);

	Map<String, Object> pdfAndPrint(Long indentId);

	Map<String, Object> pdfAndPrintThd(Long indentId);

	Page<CustSalesBillModel> getCustSales(Map<String, Object> params, Page page, HttpServletRequest request);

	Page<CustSalesSummarizingModel> getCustSalesSummarizing(Map<String, Object> params, Page page, HttpServletRequest request);

	CustSalesStatisticsSummationModel selectCustSalesStatisticsSummation(Map<String, Object> params, HttpServletRequest request);

	Page<CustSalesStatisticsModel> getCustSalesStatistics(Map<String, Object> params, Page page, HttpServletRequest request);

	Page<CustSalesDetailModel> getCustSalesDetail(Map<String, Object> params, Page page, HttpServletRequest request);

	Page<AreaSalesRankModel> getFirstLevelAreaSalesRank(Page page);

	Page<AreaSalesRankModel> getAreaSalesRank(Map<String, Object> params, Page page);

	CommonSummation getAreaSalesRankSummation(Map<String, Object> params);

	Page<CustSalesRankModel> getCustSalesRank(Map<String, Object> params, Page page);

	CommonSummation getCustSalesRankSummation(Map<String, Object> params);

	Page<GoodsSalesSummarizingModel> getGoodsSalesSummarizing(Map<String, Object> params, Page page, HttpServletRequest request);

	CommonSummation getGoodsSalesSummation(Map<String, Object> params);

	Page<SingleGoodsSalesDetailModel> getSingleGoodsSalesDetail(Map<String, Object> params, Page page, HttpServletRequest request);

	Page<GoodsSalesRankModel> getGoodsSalesRank(Map<String, Object> params, Page page, HttpServletRequest request);

	CommonSummation getGoodsSalesRankSummation(Map<String, Object> params);

	Page<IndentStatisticsModel> getIndentStatistics(Map<String, Object> params, Page page);

	IndentStatisticsSummationModel getIndentStatisticsSummation(Map<String, Object> params);

	CustSalesDetailSummarizingModel getCustSalesDetailSummarizing(Map<String, Object> params, HttpServletRequest request);

	CustSalesSummationModel getCustSalesSummation(Map<String, Object> params);

	ResponseModel auditIndentById(Long indentId);

	ResponseModel auditIndentById(Long indentId, String auditRemarks);

	ResponseModel cancelReceipt(Receipt receipt, HttpServletRequest request);

	ResponseModel cancelPayReceipt(Receipt pay, HttpServletRequest request);

	ResponseModel cancelIndentById(Long indentId);

	ResponseModel changeIndent(Indent indent, HttpServletRequest request);

	Page<String> getDiscounts(Page page, Map<String, Object> params);

	DealDetailSummarizing getDealDetailSummarizingForAdd(Map<String, Object> params);

	ResponseModel updateAuditRemarks(String indentNo, String auditRemarks);

	Page<DeliveryStaticsModel> getGoodsDeliveryStatics(Page page, Map<String, Object> params, HttpServletRequest request);

	CommonSummation getGoodsDeliveryStaticsSummarizing(Map<String, Object> params);

	DeliveryStaticsModel getGoodsDeliveryDetailsStaticsSummarizing(Map<String, Object> params);

	Page<DeliveryDetailsStaticsModel> getGoodsDeliveryDetailsStatics(Map<String, Object> params, Page page, HttpServletRequest request);

	Page<String> getIndentNosSelective(Page page, String str);

	Page<IndentNoCustIdNameModel> getIndentNoCustNameSelective(Page page, String str);

	Page<SalesmanSalesRankModel> getSalesmanSalesRank(Map<String, Object> params, Page page, HttpServletRequest request);

	CommonSummation getSalesmanSalesRankSummation(Map<String, Object> params);
}
