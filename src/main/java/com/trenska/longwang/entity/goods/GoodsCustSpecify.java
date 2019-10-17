package com.trenska.longwang.entity.goods;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Owen
 * @since 2019-04-23
 */
@Data
@TableName("t_goods_cust_specify")
@ApiModel("客户-商品特价对应实体类")
public class GoodsCustSpecify extends Model<GoodsCustSpecify> {

    @TableId(value = "specify_id", type = IdType.AUTO)
    @ApiModelProperty("创建商品时前端不传递")
    private Integer specifyId;

    @ApiModelProperty("商品id，创建商品时前端不传递")
    private Integer goodsId;

    @ApiModelProperty("客户id,创建商品时前端传递")
    private Integer custId;

    @TableField(exist = false)
    @ApiModelProperty("客户名称")
    private String custName;

    @ApiModelProperty("会员特价，创建商品时前端传递")
    private String price;

}