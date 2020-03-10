package cn.jeremy.wechat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WechatPublicNumberApplication {

	public static void main(String[] args) {
		SpringApplication.run(WechatPublicNumberApplication.class, args);
	}
}
