package com.java.ccs.secondkill.vo;

import com.java.ccs.secondkill.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author caocs
 * @date 2021/11/2
 * 详情返回对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailVo {

    private User user;

    private GoodsVo goodsVo;

    private int secondKillStatus;

    private int remainSeconds;


}
