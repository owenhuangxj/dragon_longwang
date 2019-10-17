package com.trenska.longwang.entity.goods;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Owen
 * @since 2019-04-23
 */
@Data
@TableName("t_goods_price_grp")
@ApiModel("商品-价格分组实体类")
@NoArgsConstructor
public class GoodsPriceGrp extends Model<GoodsPriceGrp> {

    @TableId(value = "goods_price_grp_id", type = IdType.AUTO)
    @ApiModelProperty("创建商品时前端不传递")
    private Integer goodsPriceGrpId;

    @ApiModelProperty("创建商品时前端不传递")
    private Integer goodsId;

    @ApiModelProperty("价格分组id,创建商品时前端传递")
    private Integer priceGrpId;

    @TableField(exist = false)
    @ApiModelProperty("查询出的价格分组信息，用于前端获取信息")
    private String priceGrpName;

    @ApiModelProperty("价格分组价格，创建商品时前端传递")
    private String price;

    public GoodsPriceGrp(Integer goodsPriceGrpId, Integer goodsId, Integer priceGrpId, String price) {
        this.goodsPriceGrpId = goodsPriceGrpId;
        this.goodsId = goodsId;
        this.priceGrpId = priceGrpId;
        this.price = price;
    }
}
