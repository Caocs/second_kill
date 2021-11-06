package com.java.ccs.secondkill.controller;

import com.java.ccs.secondkill.pojo.User;
import com.java.ccs.secondkill.service.IGoodsService;
import com.java.ccs.secondkill.service.IUserService;
import com.java.ccs.secondkill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author caocs
 * @date 2021/10/23
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    IUserService userService;
    @Autowired
    IGoodsService goodsService;

    /**
     * 跳转到商品列表页
     *
     * @param model 商品列表页可能需要的信息
     * @param user  通过WebMvcConfigurer->HandlerMethodArgumentResolver处理后传过来的user
     * @return 跳转的页面
     */
    @RequestMapping("/toList")
    public String toGoodsList(Model model, User user) {
        if (null == user) {
            return "login";
        }
        List<GoodsVo> goodsVoList = goodsService.findGoodsVo();
        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsVoList);
        return "goods_list"; // 返回的是页面
    }

    /**
     * 跳转到商品详情页
     *
     * @param model 商品列表页可能需要的信息
     * @param user  通过WebMvcConfigurer->HandlerMethodArgumentResolver处理后传过来的user
     * @return 跳转的页面
     */
    @RequestMapping("/toDetail/{goodsId}")
    public String toGoodsDetail(Model model, User user, @PathVariable Long goodsId) {
        if (null == user) {
            return "login";
        }
        GoodsVo goodsVoDetail = goodsService.findGoodsVoByGoodsId(goodsId);

        long startAt = goodsVoDetail.getStartTime().getTime();
        long endAt = goodsVoDetail.getEndTime().getTime();
        long now = System.currentTimeMillis();

        int secondKillStatus = 0; // 0:还未开始，1:进行中，2:已经结束
        int remainSeconds = 0; // 还剩多少秒开始，-1:已结束
        if(now < startAt ) {//秒杀还没开始，倒计时
            remainSeconds = (int)((startAt - now )/1000);
        }else  if(now > endAt){//秒杀已经结束
            secondKillStatus = 2;
            remainSeconds = -1;
        }else {//秒杀进行中
            secondKillStatus = 1;
        }
        model.addAttribute("secondKillStatus", secondKillStatus);
        model.addAttribute("remainSeconds", remainSeconds);

        model.addAttribute("user", user);
        model.addAttribute("goods", goodsVoDetail);
        return "goods_detail"; // 返回的是页面
    }

}
