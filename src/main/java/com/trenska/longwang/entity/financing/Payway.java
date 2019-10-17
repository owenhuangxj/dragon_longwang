package com.trenska.longwang.entity.financing;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 收款/付款方式
 *
 * @author Owen
 * @since 2019-05-19
 */
@Data
@TableName("t_payway")
@ApiModel("收/付款方式")
@NoArgsConstructor
public class Payway extends Model<Payway> {


    @TableId(value = "payway_id", type = IdType.AUTO)
    private Integer paywayId;

    /**
     * 收/付款方式
     */
    @NotNull
    private String payway;

    @ApiModelProperty("是否可以删除标识:true->可以删除；false->不可删除")
    private Boolean deletable;

    @ApiModelProperty("收款/付款")
    private String type;

    /**
     * 描述
     */
    @TableField("pdesc")
    private String pdesc;

    public Payway(@NotNull String payway, String type, String pdesc) {
        this.payway = payway;
        this.type = type;
        this.pdesc = pdesc;
    }

    public Payway(Integer paywayId, @NotNull String payway, String type, String pdesc) {
        this.paywayId = paywayId;
        this.payway = payway;
        this.type = type;
        this.pdesc = pdesc;
    }
}
