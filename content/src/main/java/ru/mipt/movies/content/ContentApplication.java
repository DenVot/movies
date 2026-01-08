package ru.mipt.movies.content;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class ContentApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContentApplication.class, args);
	}

}
