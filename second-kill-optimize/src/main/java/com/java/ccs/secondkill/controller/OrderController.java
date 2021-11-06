package com.java.ccs.secondkill.controller;


import com.java.ccs.secondkill.pojo.User;
import com.java.ccs.secondkill.service.IOrderService;
import com.java.ccs.secondkill.service.ISecondKillOrderService;
import com.java.ccs.secondkill.vo.OrderDetailVo;
import com.java.ccs.secondkill.vo.ResponseBean;
import com.java.ccs.secondkill.vo.ResponseBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author ccs
 * @since 2021-10-25
 */
@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;
    @Autowired
    private ISecondKillOrderService secondKillOrderService;

    @RequestMapping("/detail")
    @ResponseBody
    public ResponseBean getOrderDetail(User user, Long orderId) {
        if (null == user) {
            return ResponseBean.error(ResponseBeanEnum.ERROR);
        }
        OrderDetailVo orderDetailVo = orderService.getOrderDetail(orderId);
        return ResponseBean.success(orderDetailVo);
    }

    /**
     * 返回秒杀结果
     *
     * @return 返回秒杀结果
     * 0:还有库存，排队中，（说明秒杀还没结束，暂时没有生成订单但是不代表秒杀失败，可能还在消息队列中也可能是秒杀失败）
     * * -1:没有库存, （说明秒杀结束，没有）
     * * >0:秒杀成功(返回订单号)
     */
    @RequestMapping(value = "/secondKillResult", method = RequestMethod.GET)
    @ResponseBody
    public ResponseBean getSecondKillResult(User user, Long goodsId) {
        if (null == user) {
            return ResponseBean.error(ResponseBeanEnum.ERROR);
        }
        Long result = secondKillOrderService.getSecondKillResult(user, goodsId);
        return ResponseBean.success(result);
    }

}
