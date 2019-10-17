package com.trenska.longwang.service.impl.customer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.dao.customer.PriceGrpMapper;
import com.trenska.longwang.entity.customer.PriceGrp;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.customer.IPriceGrpService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 2019/4/6
 * 创建人:Owen
 */
@Service
public class PriceGrpServiceImpl extends ServiceImpl<PriceGrpMapper, PriceGrp> implements IPriceGrpService {

	@Override
	public Page<PriceGrp> getPriceGrpPage(Page page) {
		page.setRecords(super.baseMapper.getPriceGrpPage(page));
		page.setTotal(super.baseMapper.selectCount(new QueryWrapper<>()));
		return page;
	}

	@Override
	public Page<PriceGrp> getPriceGrpPageByName(Page page, String priceGrpName) {
		List<PriceGrp> records = super.baseMapper.getPriceGrpPageByName(page, priceGrpName);
		page.setRecords(records);
		page.setTotal(super.baseMapper.selectCount(new QueryWrapper<PriceGrp>().like("price_grp_name",priceGrpName)));
		return page;
	}

	@Override
	public ResponseModel savePriceGrp(PriceGrp priceGrp) {
		PriceGrp oldPriceGrp = super.baseMapper.selectOne(
				new LambdaQueryWrapper<PriceGrp>()
						.eq(PriceGrp::getPriceGrpName,priceGrp.getPriceGrpName())
		);
		if(null != oldPriceGrp){
			return ResponseModel.getInstance().succ(false).msg("价格分组名称重复，不能创建");
		}

		this.save(priceGrp);
		return ResponseModel.getInstance().succ(true).msg("创建价格分组成功");
	}
}
