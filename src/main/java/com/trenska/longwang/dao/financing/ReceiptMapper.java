package com.trenska.longwang.dao.financing;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.trenska.longwang.entity.financing.DealDetailSummarizing;
import com.trenska.longwang.entity.financing.Receipt;
import com.trenska.longwang.model.finaning.AccountCheckingModel;
import com.trenska.longwang.model.report.AccountCheckingSummationModel;
import com.trenska.longwang.model.report.CommonReceiptSummation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 2019/4/3
 * 创建人:Owen
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface ReceiptMapper extends BaseMapper<Receipt> {


    List<Receipt> selectReceiptPageSelective(Map<String,Object> params,Pagination page);

    Integer selectReceiptCountSelective(Map<String,Object> params);

	Receipt selectReceiptById(Long receiptId);

	List<Receipt> selectByBusiNo(String busiNo);

	AccountCheckingSummationModel selectAccountCheckingSummation(Map<String, Object> params);

	AccountCheckingSummationModel selectAccountCheckingSummationReceiptPart(Map<String, Object> params);

	List<AccountCheckingModel> selectAccountCheckingPageSelective(Map<String, Object> params, Pagination page);

	List<Integer> selectAccountCheckingCount(Map<String, Object> params);

	int selectReceiptCountForCustomerDealDetailSelective(Map<String, Object> params);

	Receipt selectRecordOfMaxId(String type);

	DealDetailSummarizing selectDealDetailSummarizingForDecrease(Map<String,Object> params);

	List<Map<String, String>> selectReceiptStatics(Map<String,Object> params, Pagination page);

	int selectReceiptStaticsCount(Map<String,Object> params);

	CommonReceiptSummation selectReceiptSelectiveSummation(Map<String, Object> params);
}
