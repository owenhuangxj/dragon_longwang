package com.trenska.longwang.service.impl.stock;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trenska.longwang.dao.stock.StockDetailsMapper;
import com.trenska.longwang.entity.stock.StockDetails;
import com.trenska.longwang.service.stock.IStockDetailsService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 库存明细表 服务实现类
 * </p>
 *
 * @author Owen
 * @since 2019-08-07
 */
@Service
public class StockDetailsServiceImpl extends ServiceImpl<StockDetailsMapper, StockDetails> implements IStockDetailsService {

}
