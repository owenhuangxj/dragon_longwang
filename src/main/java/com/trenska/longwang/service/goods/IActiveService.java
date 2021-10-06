package com.trenska.longwang.service.goods;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.goods.Active;
import com.trenska.longwang.model.sys.CommonResponse;

import java.util.Collection;

/**
 * 商品活动服务类接口
 * @author Owen
 * @since 2019-04-12
 */
public interface IActiveService extends IService<Active> {

	CommonResponse saveActive(Active active);

	CommonResponse removeActiveById(Integer activeId);

	CommonResponse removeActiveByIds(Collection<Integer> activeIds);

	Active getInfoById(Integer activeId);

	Page<Active> getActivePage(Page page);

	Page<Active> getActivePageSelective(Active active, Page page);
}
