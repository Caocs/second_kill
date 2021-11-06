package com.java.ccs.secondkill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.java.ccs.secondkill.pojo.Goods;
import com.java.ccs.secondkill.vo.GoodsVo;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author ccs
 * @since 2021-10-25
 */
@Repository
public interface GoodsMapper extends BaseMapper<Goods> {

    /**
     * @return 查询所有商品及秒杀信息。
     */
    List<GoodsVo> findGoodsVo();

    /**
     * 根据goodsId查询商品详情及秒杀信息。
     */
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
