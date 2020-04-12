package com.trenska.longwang.service.impl.goods;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trenska.longwang.dao.goods.ActiveGoodsMapper;
import com.trenska.longwang.entity.goods.ActiveGoods;
import com.trenska.longwang.service.goods.IActiveGoodsService;
import org.springframework.stereotype.Service;

/**
 * 2020/4/6
 * 创建人:Owen
 */
@Service
public class ActiveGoodsServiceImpl extends ServiceImpl<ActiveGoodsMapper, ActiveGoods> implements IActiveGoodsService {
}
