package com.trenska.longwang.service.impl.financing;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trenska.longwang.dao.financing.DealDetailMapper;
import com.trenska.longwang.entity.financing.DealDetail;
import com.trenska.longwang.entity.financing.DealDetailSummarizing;
import com.trenska.longwang.service.financing.IDealDetailService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * <p>
 * 交易明细 服务实现类
 * </p>
 *
 * @author Owen
 * @since 2019-05-20
 */
@Service
public class DealDetailServiceImpl extends ServiceImpl<DealDetailMapper, DealDetail> implements IDealDetailService {

	@Override
	public DealDetailSummarizing getDealDetailSummarizing(Map<String, Object> params) {
		DealDetailSummarizing dealDetailSummarizing = super.baseMapper.selectDealDetailSummarizing(params);
		return dealDetailSummarizing;
	}
}
