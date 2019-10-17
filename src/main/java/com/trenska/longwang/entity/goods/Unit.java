package com.trenska.longwang.entity.goods;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author Owen
 * @since 2019-04-07
 */
@Data
@TableName("t_unit")
@ApiModel("商品单位实体类")
@NoArgsConstructor
public class Unit extends Model<Unit> {
    /**
     * 单位id
     */
    @TableId(type = IdType.AUTO)
    @Min(1)
    @ApiModelProperty("商品单位id")
    private Integer unitId;
    /**
     * 单位名称
     */
    @NotNull
    @ApiModelProperty("商品单位名称")
    private String unitName;
    /**
     * 单位状态，是否有效 : 1 有效 0 无效
     */
    @ApiModelProperty("商品单位状态标识 : 1 有效 0 无效")
    private Boolean stat;

    @ApiModelProperty("商品单位是否可以删除标志位，1 可以删除,0  不可以删除")
    private Boolean deletable;

    @TableLogic
    private Boolean deleted;

    public Unit(Integer unitId){
        this.unitId = unitId;
    }

    public Unit(@Min(1) Integer unitId, Boolean deletable) {
        this.unitId = unitId;
        this.deletable = deletable;
    }
}
