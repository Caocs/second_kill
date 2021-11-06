package com.java.ccs.secondkill.vo;

import com.java.ccs.secondkill.pojo.Goods;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author caocs
 * @date 2021/10/25
 * 秒杀商品信息展示的VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsVo extends Goods {
    private BigDecimal secondKillPrice;
    private Integer stockCount;
    private Date startTime;
    private Date endTime;
}
