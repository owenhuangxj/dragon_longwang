package com.trenska.longwang.service.goods;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.goods.Spec;
import com.trenska.longwang.model.sys.CommonResponse;

import java.util.Collection;
import java.util.Map;

/**
 * @author Owen
 * @since 2019-04-07
 */
public interface ISpecService extends IService<Spec> {

	CommonResponse saveSpec(Spec spec);

	CommonResponse removeSpecById(Integer specId);

	CommonResponse removeSpecByIds(Collection<Integer> specIds);

	Page<Spec> getSpecPageByStat(Page pageParam, Boolean stat);

	Page<Spec> getSpecPageByName(Page pageParam, String specName);

	Page<Spec> getSpecPage(Page page);

//	Page<Spec> getSpecPageSelective(Page page, Spec spec);

	Page<Spec> getSpecPageSelective(Page page, Map<String, Object> params);

	CommonResponse updateSpecById(Spec spec);
}