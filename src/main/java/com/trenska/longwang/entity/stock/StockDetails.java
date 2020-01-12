package com.trenska.longwang.entity.stock;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 库存明细表
 * </p>
 *
 * @author Owen
 * @since 2019-08-07
 */
@Data
@TableName("t_stock_details")
public class StockDetails extends Model<StockDetails> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 库存编号
     */
    private String stockNo;

    /**
     * 冗余t_stock表的相同字段，方便查询和数据封装返回
     */
    private String busiNo;

    /**
     * 操作者id
     */
    private Integer empId;

    /**
     * 商品id，通过此id找到商品名称，规格信息
     */
    private Integer goodsId;

    /**
     * 库存变更类 : 出库单、入库单
     */
    private String stockType;

    /**
     * 操作类型，对t_stock表的相同字段的冗余，方便查询和数据封装操作
     */
    private String operType;

    private String madeDate;

    /**
     * 库存操作的具体时间
     */
    private String stockTime;

    /**
     * 操作数量
     */
    private String history;

    /**
     * 入库时选择的商品单位id，主/辅单位
     */
    private String unitName;

    /**
     * 库存单价，默认为商品的单价
     */
    private String stockPrice;

    /**
     * 销售价格
     */
    private String salesPrice;

    /**
     * 库存操作后的库存量
     */
    private Integer stock;

    /**
     * 库存操作备注
     */
    private String remarks;
    /**
     * 状态
     */
    private boolean stat = true;

    /**
     * 时间戳，用于排序
     */
    private long timestamp;

}
