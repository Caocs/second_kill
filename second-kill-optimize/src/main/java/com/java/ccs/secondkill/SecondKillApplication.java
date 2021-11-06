package com.java.ccs.secondkill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.java.ccs.secondkill.mapper")
public class SecondKillApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecondKillApplication.class, args);
    }

}
