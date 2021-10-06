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
import com.trenska.longwang.model.sys.CommonResponse;

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

	CommonResponse saveIndent(Indent indent);

	CommonResponse removeIndentById(Long indentId, String indentType);

	CommonResponse removeIndentByIds(Collection<Long> indentIds, String indentType);

	IndentInfoModel getIndentInfoModel(Integer custId, Integer goodsId);

	CommonResponse updateIndent(Indent indent);

	Page<Indent> getIndentPageSelective(Map<String, Object> params, Page page);

	CommonResponse confirmIndent(String indentNo);

	CommonResponse cancelIndentByNo(String indentNo);

	CommonResponse repealIndent(long indentId);

	CommonResponse stockoutIndent(Indent indent);

	Indent getIndentInfo(String indentNo);

	CommonResponse invalidIndent(Indent indent);

	List<IndentDetail> getIndentDetails(String indentNo);

	CommonResponse saveSalesReturn(Indent indent);

	CommonResponse invalidSalseReturn(Indent indent);

	CommonResponse removeReturnSalesById(Long indentId);

	CommonResponse removeReturnSalesByIds(Collection<Long> indentIds);

	CommonResponse addIou(Long indentId, String iouAmnt);

	CommonResponse addOrUpdateIou(Long indentId, String iouAmnt, String iouTime, String iouRemarks);

	CommonResponse indentPay(Long indentId, String iouAmnt, String oper, String payway, HttpServletRequest request);

	Map<String, Object> pdfAndPrint(Long indentId);

	Map<String, Object> pdfAndPrintThd(Long indentId);

	Page<CustSalesBillModel> getCustSales(Map<String, Object> params, Page page);

	Page<CustSalesSummarizingModel> getCustSalesSummarizing(CustSalesSummarizingSearchModel params, Page page);

	CustSalesStatisticsSummationModel selectCustSalesStatisticsSummation(Map<String, Object> params);

	Page<CustSalesStatisticsModel> getCustSalesStatistics(Map<String, Object> params, Page page);

	Page<CustSalesDetailModel> getCustSalesDetail(Map<String, Object> params, Page page);

	Page<AreaSalesRankModel> getFirstLevelAreaSalesRank(Page page);

	Page<AreaSalesRankModel> getAreaSalesRank(Map<String, Object> params, Page page);

	CommonSummation getAreaSalesRankSummation(Map<String, Object> params);

	Page<CustSalesRankModel> getCustSalesRank(Map<String, Object> params, Page page);

	CommonSummation getCustSalesRankSummation(Map<String, Object> params);

	Page<GoodsSalesSummarizingModel> getGoodsSalesSummarizing(Map<String, Object> params, Page page);

	CommonSummation getGoodsSalesSummation(Map<String, Object> params);

	Page<SingleGoodsSalesDetailModel> getSingleGoodsSalesDetail(Map<String, Object> params, Page page);

	Page<GoodsSalesRankModel> getGoodsSalesRank(Map<String, Object> params, Page page);

	CommonSummation getGoodsSalesRankSummation(Map<String, Object> params);

	Page<IndentStatisticsModel> getIndentStatistics(Map<String, Object> params, Page page);

	IndentStatisticsSummationModel getIndentStatisticsSummation(Map<String, Object> params);

	CustSalesDetailSummarizingModel getCustSalesDetailSummarizing(Map<String, Object> params);

	CustSalesSummationModel getCustSalesSummation(CustSalesSummarizingSearchModel searchModel);

	CommonResponse auditIndentById(Long indentId);

	CommonResponse auditIndentById(Long indentId, String auditRemarks);

	CommonResponse cancelReceipt(Receipt receipt);

	CommonResponse cancelPayReceipt(Receipt pay);

	CommonResponse cancelIndentById(Long indentId);

	CommonResponse changeIndent(Indent indent);

	Page<String> getDiscounts(Page page, Map<String, Object> params);

	DealDetailSummarizing getDealDetailSummarizingForAdd(Map<String, Object> params);

	CommonResponse updateAuditRemarks(String indentNo, String auditRemarks);

	Page<DeliveryStaticsModel> getGoodsDeliveryStatics(Page page, Map<String, Object> params);

	CommonSummation getGoodsDeliveryStaticsSummarizing(Map<String, Object> params);

	DeliveryStaticsModel getGoodsDeliveryDetailsStaticsSummarizing(Map<String, Object> params);

	Page<DeliveryDetailsStaticsModel> getGoodsDeliveryDetailsStatics(Map<String, Object> params, Page page);

	Page<String> getIndentNosSelective(Page page, String str);

	Page<IndentNoCustIdNameModel> getIndentNoCustNameSelective(Page page, String str);

	Page<SalesmanSalesRankModel> getSalesmanSalesRank(Map<String, Object> params, Page page);

	CommonSummation getSalesmanSalesRankSummation(Map<String, Object> params);
}