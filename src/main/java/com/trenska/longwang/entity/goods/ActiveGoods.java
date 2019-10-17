package com.trenska.longwang.entity.goods;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Owen
 * @since 2019-04-15
 */
@TableName("t_active_goods")
@Data
@NoArgsConstructor
@ApiModel("满赠细则")
public class ActiveGoods extends Model<ActiveGoods> {

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("满赠细则id,创建活动时不传该参数")
    private Integer id;

    @ApiModelProperty("活动id，创建活动时不传该参数")
    private Integer activeId;

    @ApiModelProperty(value = "活动商品id",required = true)
    private Integer goodsId;

    @ApiModelProperty(value = "活动赠品id", required = true)
    private Integer giftId;

//    @ApiModelProperty("活动赠品信息")
//    @TableField(exist = false)
//    private Goods gift;

}
