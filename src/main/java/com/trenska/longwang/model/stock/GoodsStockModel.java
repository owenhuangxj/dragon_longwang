package com.trenska.longwang.model.stock;

import com.trenska.longwang.entity.goods.Goods;
import com.trenska.longwang.entity.indent.StockMadedate;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 2019/6/26
 * 创建人:Owen
 */
@Data
@ApiModel
public class GoodsStockModel {
	private Goods goods;
	private List<StockMadedate> madedates = new ArrayList<>();
}
