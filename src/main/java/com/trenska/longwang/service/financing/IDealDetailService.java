package com.trenska.longwang.service.financing;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.financing.DealDetail;
import com.trenska.longwang.entity.financing.DealDetailSummarizing;

import java.util.Map;

/**
 * <p>
 * 交易明细 服务类
 * </p>
 *
 * @author Owen
 * @since 2019-05-20
 */
public interface IDealDetailService extends IService<DealDetail> {

	DealDetailSummarizing getDealDetailSummarizing(Map<String, Object> params);

	boolean addDebt(DealDetail dealDetail);

	Page<DealDetail> page(Page page, Map<String, Object> params);
}
