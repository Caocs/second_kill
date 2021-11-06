package com.java.ccs.secondkill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.java.ccs.secondkill.pojo.Goods;
import com.java.ccs.secondkill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ccs
 * @since 2021-10-25
 */
public interface IGoodsService extends IService<Goods> {

    List<GoodsVo> findGoodsVo();

    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
