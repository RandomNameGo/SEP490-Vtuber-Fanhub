package com.sep490.vtuber_fanhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class VtuberFanhubApplication {

	public static void main(String[] args) {
		SpringApplication.run(VtuberFanhubApplication.class, args);
	}

}
