package com.trenska.longwang.excel_import;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trenska.longwang.entity.goods.Goods;
import com.trenska.longwang.entity.goods.Unit;
import com.trenska.longwang.model.goods.GoodsExcelImportModel;
import com.trenska.longwang.service.goods.IGoodsService;
import com.trenska.longwang.util.GoodsUtil;
import com.trenska.longwang.util.ObjectCopier;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 2019/10/21
 * 创建人:Owen
 */
public class GoodsImportListener extends AnalysisEventListener<GoodsExcelImportModel> {

	private IGoodsService goodsService;

	public GoodsImportListener(IGoodsService goodsService) {
		this.goodsService = goodsService;
	}

	/**
	 * 每隔500条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
	 */
	private static final int BATCH_COUNT = 500;

	private List<GoodsExcelImportModel> data = new ArrayList<>();

	@Override
	public void invoke(GoodsExcelImportModel goodsExcelImportModel, AnalysisContext analysisContext) {
		data.add(goodsExcelImportModel);
		if(data.size() >= BATCH_COUNT){
			saveData();
			data.clear();
		}
	}

	@Override
	public void doAfterAllAnalysed(AnalysisContext analysisContext) {
		saveData();
	}

	/**
	 * 将数据存入数据库
	 */
	private void saveData()	{

		List<Goods> goods = new ArrayList<>();
		for (int i = 0; i < data.size(); i++) {
			Goods gd = new Goods();
			GoodsExcelImportModel model = data.get(i);
			ObjectCopier.copyProperties(model,gd);
			String status = model.getStatus();
			if (StringUtils.isEmpty(status) || !Arrays.asList("上架","下架").contains(status) || "下架".equals(status))
				gd.setStat(false);
			else if ("上架".equals(status))
				gd.setStat(true);
			goods.add(gd);
		}

		goodsService.batchImportGoods(goods);

	}
}
