package com.java.ccs.secondkill.controller;

import com.java.ccs.secondkill.pojo.User;
import com.java.ccs.secondkill.service.IGoodsService;
import com.java.ccs.secondkill.service.IUserService;
import com.java.ccs.secondkill.vo.DetailVo;
import com.java.ccs.secondkill.vo.GoodsVo;
import com.java.ccs.secondkill.vo.ResponseBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    @Autowired
    RedisTemplate redisTemplate;
    // 可以用来手动渲染Thymeleaf页面
    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    /**
     * 跳转到商品列表页
     *
     * @param model 商品列表页可能需要的信息
     * @param user  通过WebMvcConfigurer->HandlerMethodArgumentResolver处理后传过来的user
     * @return 跳转的页面
     */
    @RequestMapping(value = "/toList", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toGoodsList(Model model, User user,
                              HttpServletRequest request, HttpServletResponse response) {
        // 从redis中获取页面，如果不为空则直接返回页面
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String htmlCache = (String) valueOperations.get("goodsList");
        if (!StringUtils.isEmpty(htmlCache)) {
            return htmlCache;
        }
        // 如果为空，手动渲染，存入Redis并返回
        if (null == user) {
            return "login";
        }
        List<GoodsVo> goodsVoList = goodsService.findGoodsVo();
        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsVoList);

        // 手动渲染Html页面
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        String html = thymeleafViewResolver.getTemplateEngine().process("goods_list", webContext);
        if (!StringUtils.isEmpty(html)) {
            valueOperations.set("goodsList", html, 60, TimeUnit.SECONDS);
        }
        // return "goods_list"; // 返回的是页面
        return html;
    }

    /**
     * 跳转到商品详情页
     *
     * @param model 商品列表页可能需要的信息
     * @param user  通过WebMvcConfigurer->HandlerMethodArgumentResolver处理后传过来的user
     * @return 跳转的页面
     */
    @RequestMapping(value = "/toDetail/{goodsId}", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toGoodsDetail(Model model, User user, @PathVariable Long goodsId,
                                 HttpServletRequest request, HttpServletResponse response) {
        // 从redis中获取页面，如果不为空则直接返回页面
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String htmlCache = (String) valueOperations.get("goodsDetails:" + goodsId);
        if (!StringUtils.isEmpty(htmlCache)) {
            return htmlCache;
        }

        if (null == user) {
            return "login";
        }
        GoodsVo goodsVoDetail = goodsService.findGoodsVoByGoodsId(goodsId);

        long startAt = goodsVoDetail.getStartTime().getTime();
        long endAt = goodsVoDetail.getEndTime().getTime();
        long now = System.currentTimeMillis();

        int secondKillStatus = 0; // 0:还未开始，1:进行中，2:已经结束
        int remainSeconds = 0; // 还剩多少秒开始，-1:已结束
        if (now < startAt) {//秒杀还没开始，倒计时
            remainSeconds = (int) ((startAt - now) / 1000);
        } else if (now > endAt) {//秒杀已经结束
            secondKillStatus = 2;
            remainSeconds = -1;
        } else {//秒杀进行中
            secondKillStatus = 1;
        }
        model.addAttribute("secondKillStatus", secondKillStatus);
        model.addAttribute("remainSeconds", remainSeconds);

        model.addAttribute("user", user);
        model.addAttribute("goods", goodsVoDetail);


        // 手动渲染Html页面
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        String html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", webContext);
        if (!StringUtils.isEmpty(html)) {
            valueOperations.set("goodsDetails:" + goodsId, html, 60, TimeUnit.SECONDS);
        }
        return html;
    }

    /**
     * 跳转到商品详情页
     *
     * @param user 通过WebMvcConfigurer->HandlerMethodArgumentResolver处理后传过来的user
     * @return 跳转的页面
     */
    @RequestMapping("/detail/{goodsId}")
    @ResponseBody
    public ResponseBean getGoodsDetail(User user, @PathVariable Long goodsId) {
        GoodsVo goodsVoDetail = goodsService.findGoodsVoByGoodsId(goodsId);
        long startAt = goodsVoDetail.getStartTime().getTime();
        long endAt = goodsVoDetail.getEndTime().getTime();
        long now = System.currentTimeMillis();
        int secondKillStatus = 0; // 0:还未开始，1:进行中，2:已经结束
        int remainSeconds = 0; // 还剩多少秒开始，-1:已结束
        if (now < startAt) {//秒杀还没开始，倒计时
            remainSeconds = (int) ((startAt - now) / 1000);
        } else if (now > endAt) {//秒杀已经结束
            secondKillStatus = 2;
            remainSeconds = -1;
        } else {//秒杀进行中
            secondKillStatus = 1;
        }
        DetailVo detailVo = new DetailVo();
        detailVo.setUser(user);
        detailVo.setGoodsVo(goodsVoDetail);
        detailVo.setSecondKillStatus(secondKillStatus);
        detailVo.setRemainSeconds(remainSeconds);
        return ResponseBean.success(detailVo);
    }

}
