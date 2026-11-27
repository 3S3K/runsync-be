package com._s3k.runsync;

import com._s3k.runsync.global.config.ClockConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class RunsyncApplication {

	public static void main(String[] args) {
		// 컨테이너의 시간대(UTC)와 무관하게 생성·수정 시각과 @PastOrPresent 같은 검증이 한국 시간 기준으로 동작하게 한다
		TimeZone.setDefault(TimeZone.getTimeZone(ClockConfig.SERVICE_ZONE));
		SpringApplication.run(RunsyncApplication.class, args);
	}

}
