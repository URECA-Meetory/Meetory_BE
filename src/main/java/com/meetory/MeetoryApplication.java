package com.meetory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MeetoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeetoryApplication.class, args);
    }

}
//게시판 부분 테스트용으로 임시로 바꿈
//원래 코드
//package com.meetory;

//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
//public class MeetoryApplication {
//
//	public static void main(String[] args) {
//		SpringApplication.run(MeetoryApplication.class, args);
//	}
//
//}