package kr.co.wikibook.batch.jpa.basic;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableBatchProcessing
@SpringBootApplication
public class BatchJpaBasicApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchJpaBasicApplication.class, args);
    }

}
