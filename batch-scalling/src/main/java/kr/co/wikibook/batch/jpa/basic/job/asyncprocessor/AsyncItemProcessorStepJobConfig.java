package kr.co.wikibook.batch.jpa.basic.job.asyncprocessor;

import kr.co.wikibook.batch.jpa.basic.domain.pay.Coupon;
import kr.co.wikibook.batch.jpa.basic.domain.pay.Pay;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Configuration
public class AsyncItemProcessorStepJobConfig {
    public static final String JOB_NAME = "asyncItemProcessorStepJob";
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    public AsyncItemProcessorStepJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
    }

    private int chunkSize;

    @Value("${chunkSize:5}")
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    private int poolSize;

    @Value("${poolSize:5}")
    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public TaskExecutor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setThreadNamePrefix("asyncItemProcessor-");
        executor.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE);
        executor.initialize();
        return executor;
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
                .<Coupon, Future<Pay>>chunk(chunkSize)
                .reader(reader(null))
                .processor(asyncItemProcessor())
                .writer(asyncItemWriter())
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

    private AsyncItemProcessor<Coupon, Pay> asyncItemProcessor() {
        AsyncItemProcessor<Coupon, Pay> asyncItemProcessor = new AsyncItemProcessor<>();
        asyncItemProcessor.setDelegate(processor()); // 실제 작업할 ItemProcessor
        asyncItemProcessor.setTaskExecutor(executor()); // taskExecutor 세팅

        return asyncItemProcessor;
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

    private AsyncItemWriter<Pay> asyncItemWriter() {
        AsyncItemWriter<Pay> asyncItemWriter = new AsyncItemWriter<>();
        asyncItemWriter.setDelegate(writer());
        return asyncItemWriter;
    }

    private JpaItemWriter<Pay> writer() {
        return new JpaItemWriterBuilder<Pay>()
                .entityManagerFactory(this.entityManagerFactory)
                .usePersist(true)
                .build();
    }
}