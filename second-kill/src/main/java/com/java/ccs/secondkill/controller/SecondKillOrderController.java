package com.java.ccs.secondkill.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.java.ccs.secondkill.pojo.Order;
import com.java.ccs.secondkill.pojo.SecondKillOrder;
import com.java.ccs.secondkill.pojo.User;
import com.java.ccs.secondkill.service.IGoodsService;
import com.java.ccs.secondkill.service.IOrderService;
import com.java.ccs.secondkill.service.ISecondKillOrderService;
import com.java.ccs.secondkill.vo.GoodsVo;
import com.java.ccs.secondkill.vo.ResponseBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author ccs
 * @since 2021-10-25
 */
@Controller
@RequestMapping("/secondKill")
public class SecondKillOrderController {
    @Autowired
    IGoodsService goodsService;
    @Autowired
    ISecondKillOrderService secondKillOrderService;
    @Autowired
    IOrderService orderService;

    /**
     * 执行提交的秒杀表单
     * 秒杀成功：跳转到订单详情页
     * 秒杀失败：跳转到秒杀失败页
     */
    @RequestMapping("/doSecondKill")
    public String doSecondKill(Model model, User user, Long goodsId) {
        if (null == user) {
            return "login";
        }
        model.addAttribute("user", user);
        // 1.校验是否还有库存
        GoodsVo goodsVoDetail = goodsService.findGoodsVoByGoodsId(goodsId);
        if (goodsVoDetail.getStockCount() < 1) {
            model.addAttribute("errMsg", ResponseBeanEnum.STOCK_EMPTY_ERROR.getMessage());
            return "second_kill_error";
        }
        // 2.校验是否重复抢购
        SecondKillOrder secondKillOrder = secondKillOrderService
                .getOne(new QueryWrapper<SecondKillOrder>()
                        .eq("user_id", user.getId())
                        .eq("goods_id", goodsId)
                );
        if (secondKillOrder != null) {
            model.addAttribute("errMsg", ResponseBeanEnum.ORDER_REPEAT_ERROR.getMessage());
            return "second_kill_error";
        }
        // 3.秒杀成功，生成订单。
        Order order = orderService.secondKillOrder( user, goodsVoDetail);
        model.addAttribute("goods", goodsVoDetail);
        model.addAttribute("orderInfo", order);
        return "order_detail"; // 返回的是页面
    }

}
