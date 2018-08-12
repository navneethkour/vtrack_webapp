package com.virtualpairprogrammers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.EnableMBeanExport;

@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
public class FleetmanApplication {

	public static void main(String[] args) {
		SpringApplication.run(FleetmanApplication.class, args);
	}
	
}
