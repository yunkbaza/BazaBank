package br.com.banco.transferencia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MotorTransferenciaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MotorTransferenciaApplication.class, args);
	}

}