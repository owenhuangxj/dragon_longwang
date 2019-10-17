package com.trenska.longwang.entity.goods;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Owen
 * @since 2019-04-07
 */
@Data
@TableName("t_spec")
@ApiModel("商品规格实体类")
@NoArgsConstructor
public class Spec extends Model<Spec> {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("商品规格id")
    @Min(0)
    private Integer specId;

    @ApiModelProperty("商品规格名称")
    private String specName;

    @ApiModelProperty("商品规格状态 0:无效;1:有效")
    private Boolean stat;

    @ApiModelProperty("商品规格是否可以删除标志位，true 可以删除,false 不可以删除")
    private Boolean deletable;

    @TableLogic
    private Boolean deleted;

    @TableField(exist = false)
    @ApiModelProperty(notes = "用于接收商品规格参数的属性,接收通过specId从t_spec_property表中获取的所有商品规格属性值")
    private Set<SpecProperty> specProperties = new HashSet<>();

    public Spec(@Min(0) Integer specId, Boolean deletable) {
        this.specId = specId;
        this.deletable = deletable;
    }
}
