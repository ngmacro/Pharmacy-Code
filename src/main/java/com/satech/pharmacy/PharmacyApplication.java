package com.satech.pharmacy;

import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PharmacyApplication {

    public static void main(String[] args) {
        SpringApplication.run(PharmacyApplication.class, args);
    }

    @Bean
    public NioSocketAcceptor nioSocketAcceptor() {
        NioSocketAcceptor acceptor = new NioSocketAcceptor();
        return acceptor;
    }
}
