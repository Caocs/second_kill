package com.java.ccs.secondkill.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author caocs
 * @date 2021/11/4
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecondKillMessage {

    private User user;

    private Long goodsId;

}
