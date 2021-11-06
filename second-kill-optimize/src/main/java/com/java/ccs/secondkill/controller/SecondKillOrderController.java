package com.java.ccs.secondkill.controller;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.java.ccs.secondkill.config.AccessLimit;
import com.java.ccs.secondkill.exception.GlobalException;
import com.java.ccs.secondkill.pojo.Order;
import com.java.ccs.secondkill.pojo.SecondKillMessage;
import com.java.ccs.secondkill.pojo.SecondKillOrder;
import com.java.ccs.secondkill.pojo.User;
import com.java.ccs.secondkill.rabbitmq.MQSender;
import com.java.ccs.secondkill.service.IGoodsService;
import com.java.ccs.secondkill.service.IOrderService;
import com.java.ccs.secondkill.service.ISecondKillOrderService;
import com.java.ccs.secondkill.vo.GoodsVo;
import com.java.ccs.secondkill.vo.ResponseBean;
import com.java.ccs.secondkill.vo.ResponseBeanEnum;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author ccs
 * @since 2021-10-25
 */
@Slf4j
@Controller
@RequestMapping("/secondKill")
public class SecondKillOrderController implements InitializingBean {
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private ISecondKillOrderService secondKillOrderService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MQSender mqSender;

    @Autowired
    private DefaultRedisScript<Long> stockScript;

    /**
     * 内存标记，减少Redis访问
     */
    private Map<Long, Boolean> emptyStockMap = new ConcurrentHashMap<>();

    /**
     * 系统初始化，把商品库存数量加载到redis
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsVoList = goodsService.findGoodsVo();
        if (CollectionUtils.isEmpty(goodsVoList)) {
            return;
        }
        goodsVoList.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("secondKillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
            // 初始化时，为false，表示不是空库存
            emptyStockMap.put(goodsVo.getId(), false);
        });
    }

    /**
     * 生成验证码，将图片返回给前端，把结果缓存在redis
     */
    @RequestMapping(value = "/captcha", method = RequestMethod.GET)
    public void verifyCode(User user, Long goodsId, HttpServletResponse response) {
        if (user == null || goodsId < 0) {
            throw new GlobalException(ResponseBeanEnum.ERROR);
        }
        // 设置请求头为输出图片的类型
        response.setContentType("image/jpg");
        response.setHeader("Pargam", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        // 生成验证码，将结果放在redis中
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3);
        redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId, captcha.text(), 300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            log.error("生成验证码失败！", e.getMessage());
        }

    }

    /**
     * 使用@AccessLimit注解来拦截请求，做用户校验和访问频率校验。
     * 验证码校验
     */
    @AccessLimit(second = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public ResponseBean getSecondKillPath(User user, Long goodsId, String captchaResult, HttpServletRequest request) {
//        if (null == user) {
//            return ResponseBean.error(ResponseBeanEnum.ERROR);
//        }
//        // 校验每个人的访问频率
//        String url = request.getRequestURI();
//        ValueOperations valueOperations = redisTemplate.opsForValue();
//        Integer count = (Integer) valueOperations.get(url + ":" + user.getId());
//        if (count == null) {
//            // 第一次请求
//            valueOperations.set(url + ":" + user.getId(), 1, 5, TimeUnit.SECONDS);
//        } else if (count < 5) {
//            // 后续每次访问+1
//            valueOperations.increment(url + ":" + user.getId());
//        } else {
//            // 5秒内超过5次，返回“过于频繁”
//            return ResponseBean.error(ResponseBeanEnum.ACCESS_LIMIT_ERROR);
//        }

        // 校验验证码
        boolean checkCaptcha = orderService.checkCaptcha(user, goodsId, captchaResult);
        if (!checkCaptcha) {
            return ResponseBean.error(ResponseBeanEnum.CAPTCHA_ERROR);
        }
        String path = orderService.createSecondKillPath(user, goodsId);
        return ResponseBean.success(path); // 如果是0，前端应该展示正在排队中。
    }


    /**
     * 执行提交的秒杀表单
     * 秒杀成功：跳转到订单详情页
     * 秒杀失败：跳转到秒杀失败页
     */
    @RequestMapping(value = "/{path}/doSecondKill", method = RequestMethod.POST)
    @ResponseBody
    public ResponseBean doSecondKill(User user, Long goodsId, @PathVariable String path) {
        if (null == user) {
            return ResponseBean.error(ResponseBeanEnum.ERROR);
        }
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 校验隐藏地址的path是否正确
        boolean checkPath = orderService.checkPath(user, goodsId, path);
        if (!checkPath) {
            return ResponseBean.error(ResponseBeanEnum.ERROR);
        }
        // 内存标记，减少redis访问
        if (emptyStockMap.get(goodsId)) {
            return ResponseBean.error(ResponseBeanEnum.STOCK_EMPTY_ERROR);
        }
        // 1.校验是否还有库存
        // 获取递减之后的库存（原子操作）
        Long stock = valueOperations.decrement("secondKillGoods:" + goodsId);
        // Long stock = (Long) redisTemplate.execute(stockScript, Collections.singletonList("secondKillGoods:" + goodsId), Collections.EMPTY_LIST);
        if (stock < 0) {
            // 内存标记卖完
            emptyStockMap.put(goodsId, true);
            valueOperations.increment("secondKillGoods:" + goodsId);
            return ResponseBean.error(ResponseBeanEnum.STOCK_EMPTY_ERROR);
        }
        // 2.校验是否重复抢购
        SecondKillOrder secondKillOrder = (SecondKillOrder) valueOperations.get("order:" + user.getId() + ":" + goodsId);
        if (secondKillOrder != null) {
            return ResponseBean.error(ResponseBeanEnum.ORDER_REPEAT_ERROR);
        }
        // 3.秒杀成功，生成订单。(通过消息队列异步下单)
        SecondKillMessage message = new SecondKillMessage(user, goodsId);
        String messageStr = JSON.toJSONString(message);
        mqSender.sendSecondKillMessage(messageStr);
        return ResponseBean.success(0); // 如果是0，前端应该展示正在排队中。
    }


    /**
     * 执行提交的秒杀表单
     * 秒杀成功：跳转到订单详情页
     * 秒杀失败：跳转到秒杀失败页
     */
    @RequestMapping(value = "/doSecondKill3", method = RequestMethod.POST)
    @ResponseBody
    public ResponseBean doSecondKill3(User user, Long goodsId) {
        if (null == user) {
            return ResponseBean.error(ResponseBeanEnum.ERROR);
        }
        // 1.校验是否还有库存
        GoodsVo goodsVoDetail = goodsService.findGoodsVoByGoodsId(goodsId);
        if (goodsVoDetail.getStockCount() < 1) {
            return ResponseBean.error(ResponseBeanEnum.STOCK_EMPTY_ERROR);
        }
        // 2.校验是否重复抢购
//        SecondKillOrder secondKillOrder = secondKillOrderService
//                .getOne(new QueryWrapper<SecondKillOrder>()
//                        .eq("user_id", user.getId())
//                        .eq("goods_id", goodsId)
//                );
        SecondKillOrder secondKillOrder = (SecondKillOrder) redisTemplate.opsForValue()
                .get("order:" + user.getId() + ":" + goodsId);
        if (secondKillOrder != null) {
            return ResponseBean.error(ResponseBeanEnum.ORDER_REPEAT_ERROR);
        }
        // 3.秒杀成功，生成订单。
        Order order = orderService.secondKillOrder(user, goodsVoDetail);
        return ResponseBean.success(order);
    }

    /**
     * 执行提交的秒杀表单
     * 秒杀成功：跳转到订单详情页
     * 秒杀失败：跳转到秒杀失败页
     */
    @RequestMapping("/doSecondKill2")
    public String doSecondKill2(Model model, User user, Long goodsId) {
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
        Order order = orderService.secondKillOrder(user, goodsVoDetail);
        model.addAttribute("goods", goodsVoDetail);
        model.addAttribute("orderInfo", order);
        return "order_detail"; // 返回的是页面
    }


}
