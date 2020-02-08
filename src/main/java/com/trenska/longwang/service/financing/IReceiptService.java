package com.trenska.longwang.service.financing;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.financing.DealDetail;
import com.trenska.longwang.entity.financing.DealDetailSummarizing;
import com.trenska.longwang.entity.financing.Receipt;
import com.trenska.longwang.model.finaning.AccountCheckingModel;
import com.trenska.longwang.model.report.AccountCheckingSummationModel;
import com.trenska.longwang.model.report.CommonReceiptSummation;
import com.trenska.longwang.model.report.CommonSummation;
import com.trenska.longwang.model.sys.ResponseModel;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 2019/4/3
 * 创建人:Owen
 */
public interface IReceiptService extends IService<Receipt> {

    Page<Receipt> getReceiptPageSelective(Map<String,Object> params, Page page);

    ResponseModel saveReceipt(Receipt receipt);

    ResponseModel cancelReceipt(Receipt receipt, HttpServletRequest request);

    Receipt getReceiptById(Long receiptId);

    ResponseModel savePayReceipt(Receipt pay);

    ResponseModel cancelPayReceiptById(Long receiptId);

	Page<AccountCheckingModel> getAccountChecking(Map<String, Object> params, Page page, HttpServletRequest request);

	AccountCheckingSummationModel getAccountCheckingSummation(Map<String, Object> params, HttpServletRequest request);

	Page<DealDetail> getDealDetail(Map<String, Object> params, Page page);

	String getLastSurplusDebt(Map<String, Object> params);

	DealDetailSummarizing getDealDetailSummarizingForDecrease(Map<String,Object> params);

	Page<Map<String, List<Map<String,String>>>> getReceiptStatics(Map<String,Object> params, Page page ,HttpServletRequest request);

	List<Integer> getLastSurplusCustIds(Map<String,Object> params);

	CommonReceiptSummation getReceiptSelectiveSummation(Map<String, Object> params);
}
