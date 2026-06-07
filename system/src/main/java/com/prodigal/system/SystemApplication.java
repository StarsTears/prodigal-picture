package com.prodigal.system;

import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
//关闭分库分表,启动类排除依赖(配置文件可以不注释;要注释分表相关的代码)
@SpringBootApplication //(exclude = {ShardingSphereAutoConfiguration.class})
public class SystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  prodigal-picture👉system模块启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                "oooooooooo ooooo  oooooooo8 ooooooooooo ooooo  oooo oooooooooo  ooooooooooo \n" +
                " 888    888 888 o888     88 88  888  88  888    88   888    888  888    88  \n" +
                " 888oooo88  888 888             888      888    88   888oooo88   888ooo8    \n" +
                " 888        888 888o     oo     888      888    88   888  88o    888    oo  \n" +
                "o888o      o888o 888oooo88     o888o      888oo88   o888o  88o8 o888ooo8888 ");
    }

}
