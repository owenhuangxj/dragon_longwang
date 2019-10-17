package com.trenska.longwang.entity.stock;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Owen
 * @since 2019-04-17
 */
@Data
@TableName("t_stock")
@ApiModel("库存实体类")
@NoArgsConstructor
public class Stock extends Model<Stock> {

    @ApiModelProperty("库存id")
    @TableId(type =  IdType.AUTO)
    private Long stockId;

    @ApiModelProperty("库存单号，由系统生成，前端不传递")
    private String stockNo;

    @ApiModelProperty(value = "库存操作前缀，比如入库->RKD_CHINESE,报溢入库->BYRK ；出库->CKD_CHINESE,报溢出库->BYCK,前端输入",required = true)
    @TableField(exist = false)
    @NotNull
    private String prefix;

    @ApiModelProperty("客户id，新建出库单时必须传递")
    private Integer custId;

//    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
//    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @ApiModelProperty("库存操作的具体时间,系统生成，前端不传递")
    private String stockTime;

    @ApiModelProperty("关联业务单号")
    private String busiNo;

    @ApiModelProperty(value = "变更类型：入库/出库，为了查询时出库/入库都调用一个接口->调用接口方便,分页查询时前端传递",required = true)
    private String stockType;

    @ApiModelProperty(value = "操作类型：采购入库、报溢入库、其它入库，报损出库，销售出库...",required = true)
    private String operType;

    @ApiModelProperty(value = "审核状态(预留，本系统无用) 1 通过 0 待审核 -1 审核不通过", readOnly = true, hidden = true)
    private Boolean auditStat;

    @ApiModelProperty("库存单状态 true : 已完成 ; false : 已作废,新建时不传递")
    private Boolean stat;

    @ApiModelProperty(value = "操作者id，关联sys_emp表的emp_id,由后台获取",required = true)
    private Integer empId;

    @ApiModelProperty("开单人，前端不传递")
    @TableField(exist = false)
    private String empName;

    @ApiModelProperty("送货人id")
    private Integer shipmanId;

    @TableField(exist = false)
    @ApiModelProperty("送货人,前端不传递")
    private String shipman;

    @ApiModelProperty("库存操作备注")
    private String stockRemarks;

//    @TableLogic
    @ApiModelProperty("记录逻辑删除位，前端不处理")
    private Boolean deleted;

    @ApiModelProperty("总额合计,前端不传递")
    @TableField(exist = false)
    private String total;

    @TableField(exist = false)
    @ApiModelProperty("人库记录详情，用于入库时接收前端传递的入库数据，出库时不传递")
    private List<StockDetail> stockins = new ArrayList<>();

    @TableField(exist = false)
    @ApiModelProperty("出库记录详情，用于出库时接收前端传递的出库数据，入库时不传递")
    private List<StockDetail> stockouts = new ArrayList<>();

    @TableField(exist = false)
    @ApiModelProperty("用于查询时向前端传递库存详情")
    private List<StockDetail> stockDetails = new ArrayList<>();

    public Stock(Long stockId,boolean stat){
        this.stockId = stockId;
        this.stat = stat;
    }

    public Stock(Long stockId){
        this.stockId = stockId;
    }

    public Stock(boolean stat){
        this.stat = stat;
    }
}
