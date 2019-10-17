package com.trenska.longwang.entity.goods;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Owen
 * @since 2019-04-11
 */
@Data
@TableName("t_goods")
@ApiModel("商品表实体类")
@NoArgsConstructor
@JsonIgnoreProperties({"handler"})
/**
 * @JsonIgnoreProperties({"handler"}) 用于解决
 * No serializer found for class org.apache.ibatis.executor.loader.javassist.JavassistProxyFactory$EnhancedResultObjectProxyImpl and no properties discovered to create BeanSerializer...
 */
public class Goods extends Model<Goods> {

	@TableId(value = "goods_id", type = IdType.AUTO)
	@ApiModelProperty("商品id")
	private Integer goodsId;

	/**
	 * 商品编号,相同商品名称对应多个规格，每个规格就是一个商品
	 */
	@ApiModelProperty("商品编号")
	private String goodsNo;

	/**
	 * 商品条码
	 */
	@ApiModelProperty("商品条码")
	private String barcode;

	/**
	 * 商品名称
	 */
	@NotNull
	@ApiModelProperty("商品名称")
	private String goodsName;

	/**
	 * 商品主单位
	 */
	@ApiModelProperty("商品主单位")
	@TableField(exist = false)
	private String mainUnit;

	@ApiModelProperty("商品主单位id")
	@NotNull(message = "商品主单位不能为空")
	private Integer mainUnitId;

	/**
	 * 商品辅单位名称
	 */
	@ApiModelProperty("商品辅单位")
	@TableField(exist = false)
	private String subUnit;

	@ApiModelProperty("商品辅单位id")
	private Integer subUnitId;

	/**
	 * 主单位和辅助单位的乘积因子
	 */
	@ApiModelProperty("单位和辅助单位的乘积因子")
	private Integer multi;

	/**
	 * 冗余的产品品牌名称，减少多表查询和方便实体类的设计，但是在品牌名称修改时需要级联操作
	 */
	@ApiModelProperty("产品品牌名称")
	private String brandName="无品牌";

	/**
	 * 冗余的商品分类一级名称，减少多表查询和实体类的设计，但是在分类名称修改时需要级联操作
	 */
	@ApiModelProperty("商品分类一级名称")
	private String frtCatName;

	/**
	 * 一级商品分类的id，属于数据库设计的冗余字段，方便前端获取二级分类
	 */
	@ApiModelProperty("一级商品分类的id，属于数据库设计的冗余字段，方便前端获取二级分类")
	private Integer catId;

	/**
	 * 冗余的商品分类二级名称，减少多表查询和实体类的设计，但是在分类名称修改时需要级联操作
	 */
	@ApiModelProperty("商品分类二级名称")
	private String scdCatName;

	/**
	 * 商品状态，1 上架，0 下架 默认为1
	 */
	@ApiModelProperty("商品状态，true 上架，false 下架")
	private Boolean stat;

	/**
	 * 最小起订量
	 */
	@ApiModelProperty("最小起订量")
	private Integer minOdrQtt;

	/**
	 * 商品期初库存
	 */
	@ApiModelProperty("期初库存")
	private Integer initStock;

	@TableField(exist = false)
	@ApiModelProperty("期初入库批次")
	private String initMadeDate;

	/**
	 * 商品实际库存
	 */
	@ApiModelProperty("商品实际库存")
	private Integer stock;

	/**
	 * 待入库库存
	 */
	@ApiModelProperty("待入库库存")
	private Integer stockin;

	@ApiModelProperty("可用库存，作为传递参数到前端的属性，前端接收即可")
	@TableField(exist = false)
	private Integer avbStock;

	/**
	 * 待出库库存
	 */
	@ApiModelProperty("待出库库存")
	private Integer stockout;

	/**
	 * 商品价格
	 */
	@Pattern(message = "商品价格只能为数字",regexp = "^[1-9]?[0-9]{1,9}(\\.[0-9]{1,4})?$")
	@ApiModelProperty("价格")
	private String price;

	/**
	 * 图片地址
	 */
	@ApiModelProperty("图片地址")
	private String img;

	/**
	 * 商品保质期，单位是天
	 */
	@TableField(value = "expir")
	@ApiModelProperty("商品保质期，统一以天做为单位，前端保存时需要换算")
	private Integer expire;

	/**
	 * 商品备注,字数限制12字以内
	 */
	@Size(min = 0, max = 12,message = "备注不能超过12个字")
	@ApiModelProperty("商品备注")
	private String remarks;

	/**
	 * 逻辑删除位 1 删除 0 未删除 默认值 0
	 */
	@ApiModelProperty(notes = "逻辑删除位 1 删除 0 未删除", readOnly = true, hidden = true)
	@TableLogic
	private Boolean deleted;

	@ApiModelProperty("查询时用于接收商品对应的规格值")
	@TableField(exist = false)
	private String propName;

	@ApiModelProperty(value = "名称/编号/条码组合字段,查询时前端传递，新建不传递")
	private String combine;

	@ApiModelProperty("商品对应的规格(参数)集合")
	@TableField(exist = false)
	private Collection<GoodsSpec> goodsSpecs = new ArrayList<>();

	@TableField(exist = false)
	@ApiModelProperty("商品-价格分组，创建商品时前端传递")
	private List<GoodsPriceGrp> priceGrps = new ArrayList<>();

	@TableField(exist = false)
	@ApiModelProperty("商品-客户特价，创建商品时前端传递")
	private List<GoodsCustSpecify> specialPrices = new ArrayList<>();

	public Goods(Integer goodsId, Integer stock) {
		this.goodsId = goodsId;
		this.stock = stock;
	}

	public Goods(Integer goodsId, String brandName, Integer stock) {
		this.stock = stock;
		this.goodsId = goodsId;
		this.brandName = brandName;
	}
	public Goods(String goodsNo, String goodsName, String barcode, String brandName, String frtCatName, String scdCatName, Boolean stat, String propName, Integer minOdrQtt) {
		this.goodsNo = goodsNo;
		this.goodsName = goodsName;
		this.barcode = barcode;
		this.brandName = brandName;
		this.frtCatName = frtCatName;
		this.scdCatName = scdCatName;
		this.stat = stat;
		this.propName = propName;
		this.minOdrQtt = minOdrQtt;
	}

}
