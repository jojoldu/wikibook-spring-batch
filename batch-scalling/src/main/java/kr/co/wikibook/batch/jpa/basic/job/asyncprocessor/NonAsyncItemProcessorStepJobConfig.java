package kr.co.wikibook.batch.jpa.basic.job.asyncprocessor;

import kr.co.wikibook.batch.jpa.basic.domain.pay.Coupon;
import kr.co.wikibook.batch.jpa.basic.domain.pay.Pay;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class NonAsyncItemProcessorStepJobConfig {
    public static final String JOB_NAME = "asyncItemProcessorStepJob";
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    public NonAsyncItemProcessorStepJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
    }

    private int chunkSize;

    @Value("${chunkSize:5}")
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Bean(name = JOB_NAME)
    public Job job() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(step())
                .preventRestart()
                .build();
    }

    @Bean(name = JOB_NAME + "_step")
    @JobScope
    public Step step() {
        return stepBuilderFactory.get(JOB_NAME + "_step")
                .<Coupon, Pay>chunk(chunkSize)
                .reader(reader(null))
                .processor(processor())
                .writer(writer())
                .build();
    }


    @Bean(name = JOB_NAME + "_reader")
    @StepScope
    public JpaPagingItemReader<Coupon> reader(@Value("#{jobParameters[txName]}") String txName) {

        Map<String, Object> params = new HashMap<>();
        params.put("txName", txName);

        return new JpaPagingItemReaderBuilder<Coupon>()
                .name(JOB_NAME + "_reader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT c FROM Coupon c WHERE c.txName =:txName")
                .parameterValues(params)
                .build();
    }

    private ItemProcessor<Coupon, Pay> processor() {
        return coupon -> {
            Thread.sleep(100); // API 레이턴시 대체
            return new Pay(
                    coupon.getAmount(),
                    coupon.getTxName(),
                    coupon.getTxDateTime()
            );
        };
    }

    private JpaItemWriter<Pay> writer() {
        return new JpaItemWriterBuilder<Pay>()
                .entityManagerFactory(this.entityManagerFactory)
                .usePersist(true)
                .build();
    }
}