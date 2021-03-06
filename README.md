
​     
## 系统介绍
本系统是使用SpringBoot开发的高并发限时抢购秒杀系统，除了实现基本的登录、查看商品列表、秒杀、下单等功能，项目中还针对高并发情况实现了系统缓存、降级和限流。

参考：【https://www.bilibili.com/video/BV1sf4y1L7KE】


## 开发技术
前端技术 ：Bootstrap + jQuery + Thymeleaf

后端技术 ：SpringBoot + MyBatis-plus + MySQL

中间件技术 :  Redis + RabbitMQ 

压力测试：JMeter

## 秒杀优化方向

1. 将请求尽量拦截在系统上游：传统秒杀系统之所以挂，请求都压倒了后端数据层，数据读写锁冲突严重，几乎所有请求都超时，流量虽大，下单成功的有效流量甚小，我们可以通过限流、降级等措施来最大化减少对数据库的访问，从而保护系统。

2. 充分利用缓存：秒杀商品是一个典型的读多写少的应用场景，充分利用缓存将大大提高并发量
## 实现技术点
### 1. 两次MD5加密

将用户输入的密码和固定Salt通过MD5加密生成第一次加密后的密码，再讲该密码和随机生成的Salt通过MD5进行第二次加密，最后将第二次加密后的密码和第一次的固定Salt存数据库

好处：    
1. 第一次作用：防止用户明文密码在网络进行传输
2. 第二次作用：防止数据库被盗，避免通过MD5反推出密码，双重保险

### 2. session共享
验证用户账号密码都正确情况下，通过UUID生成唯一id作为token，再将token作为key、用户信息作为value模拟session存储到redis，同时将token存储到cookie，保存登录状态

好处： 在分布式集群情况下，服务器间需要同步，定时同步各个服务器的session信息，会因为延迟到导致session不一致，使用redis把session数据集中存储起来，解决session不一致问题。

### 3. 自定义参数验证
使用自定义校验器，实现对用户账号、密码的验证，使得验证逻辑从业务代码中脱离出来。

```
自定义校验器：
	自定义校验规则的类，实现ConstraintValidator<注解，注解的定义>接口。
	在自定义注解上，把自定义校验规则的类引入。@Constraint(validatedBy = {IsMobileValidator.class})
```

### 4. 全局异常统一处理
通过拦截所有异常，对各种异常进行相应的处理，当遇到异常就逐层上抛，一直抛到最终由一个统一的、专门负责异常处理的地方处理，这有利于对异常的维护。

```
Springboot统一异常处理有两种：
	1、@ControllerAdvice+@ExceptionHandler注解 -> 只能处理控制器抛出的异常(此时请求已经进入控制器中)
	2、使用ErrorController类 -> 可以处理所有的异常(包括未进入控制器的错误，比如404,401等错误)
注意：
	如果应用中两者共同存在，则@ControllerAdvice方式处理控制器抛出的异常，类ErrorController方式未进入控制器的异常。
	使用@RestControllerAdvice，相当于@ControllerAdvice+@ResponseBody了，这样就不用了在方法上加@ResponseBody注解了
```

### 5. 页面缓存 + 对象缓存
(1) 页面缓存：通过在手动渲染得到的html页面缓存到redis

(2) 对象缓存：包括对用户信息、商品信息、订单信息和token等数据进行缓存，利用缓存来减少对数据库的访问，大大加快查询速度。

### 6. 页面静态化
对商品详情和订单详情进行页面静态化处理，页面是存在html，动态数据是通过接口从服务端获取，实现前后端分离，静态页面无需连接数据库打开速度较动态页面会有明显提高

```
页面静态化：其实目前很多前端框架已经是静态页面方式。
```

### 7. 本地标记 + redis预处理 + RabbitMQ异步下单 + 客户端轮询
描述：通过三级缓冲保护，1、本地标记  2、redis预处理  3、RabbitMQ异步下单，最后才会访问数据库，这样做是为了最大力度减少对数据库的访问。

```
实现：
（1）在Controller中实现InitializingBean接口，在afterPropertiesSet()中，在系统初始化完成时，就将商品库存缓存到Redis中。
（2）所有抢购操作在Redis中进行预减库存操作。-> 减少DB访问
（3）当Redis中库存为0时，做本地标记为售完。-> 减少Redis访问
（4）使用RabbitMQ异步下单。-> 减轻DB并发压力
（5）前端通过JS轮询查询秒杀结果。
```

### 8. 解决超卖

描述：比如某商品的库存为1，此时用户1和用户2并发购买该商品，用户1提交订单后该商品的库存被修改为0，而此时用户2并不知道的情况下提交订单，该商品的库存再次被修改为-1，这就是超卖现象

```
（1）解决超卖问题：（利用排它锁）
  update t_second_kill_goods set stock_count = stock_count-1 where goods_id={} and stock_count>0
（2）避免同一个用户多次秒杀该商品：
  给t_second_kill_order表，建立goods_id-user_id的唯一索引。
```

### 9.秒杀接口隐藏

```
描述：
	在点击秒杀时，并不直接调用秒杀接口，而是调用获取专属秒杀接口的接口，然后执行秒杀操作。
```

```
实现：
（1）获取专属秒杀接口的接口，实现对用户信息、流量控制、验证码等的信息校验，如果都通过之后，生成用户user对应goodsId专属的UUID。
（2）把该UUID传递给秒杀接口，并在接口中校验是否符合。如果符合才会真正执行秒杀操作。
```

```
好处：
（1）可以防止秒杀接口直接被大量访问。（主要是防止对爬虫或者黑客攻击）
```

### 10. 验证码

使用开源验证码工具包【https://gitee.com/ele-admin/EasyCaptcha?_from=gitee_search】

描述：点击秒杀前，先让用户输入数学公式验证码，验证正确才能进行秒杀。

```
实现：
1. 前端通过把商品id作为参数调用服务端创建验证码接口
2. 服务端根据前端传过来的商品id和用户id生成验证码，并将商品id+用户id作为key，生成的验证码作为value存入redis，同时将生成的验证码输入图片写入imageIO让前端展示
3. 将用户输入的验证码与根据商品id+用户id从redis查询到的验证码对比，相同就返回验证成功，进入秒杀；不同或从redis查询的验证码为空都返回验证失败，刷新验证码重试
```

```
好处：
1. 防止恶意的机器人和爬虫 
2. 分散用户的请求
```

### 11. 限流

描述：当我们去秒杀一些商品时，此时可能会因为访问量太大而导致系统崩溃，此时要使用限流来进行限制访问量，当达到限流阀值，后续请求会被降级；降级后的处理方案可以是：返回排队页面（高峰期访问太频繁，等一会重试）、错误页等。

```
实现：
（1）定义@AccessLimit注解，用来描述限制场景。
（2）使用拦截器，对请求中@AccessLimit注解的方法，进行处理。
对访问频率进行校验并返回。
```

**目前常见接口限流方法**

（1）计数器算法

限制某段时间内最大访问次数 -> 存在临界瞬时流量过载的问题。

实现：使用Redis的set设置超时时间，统计次数。

（2）漏桶算法

->存在访问量过大，消费不过来，桶装满的问题

（3）令牌桶算法

以一定速率生成令牌。