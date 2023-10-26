package kr.co.wikibook.batch.jpa.basic.job.partitioning;

import kr.co.wikibook.batch.jpa.basic.domain.pay.Coupon;
import kr.co.wikibook.batch.jpa.basic.domain.pay.Pay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
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

@Configuration
public class PartitioningStepJobConfig {
    private static final Logger log = LoggerFactory.getLogger(PartitioningStepJobConfig.class);
    public static final String JOB_NAME = "partitioningThreadStepJob";
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    public PartitioningStepJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory) {
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

    @Value("${poolSize:5}") // (1)
    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    @Bean(name = JOB_NAME + "_taskPool")
    public TaskExecutor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor(); // (2)
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setThreadNamePrefix("partition-");
        executor.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE);
        executor.initialize();
        return executor;
    }

    public TaskExecutorPartitionHandler partitionHandler() {
        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler(); // (1)
        partitionHandler.setStep(step1()); // (2)
        partitionHandler.setTaskExecutor(executor()); // (3)
        partitionHandler.setGridSize(poolSize); // (4)
        return partitionHandler;
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
                .reader(reader(null, null, null))
                .processor(processor())
                .writer(writer())
                .taskExecutor(executor()) // (2)
                .throttleLimit(poolSize) // (3)
                .build();
    }

    @Bean(name = JOB_NAME + "_step1Manager")
    public Step step1Manager() {
        return stepBuilderFactory.get("step1.manager") // (1)
                .partitioner("step1", partitioner(null, null)) // (2)
                .step(step1()) // (3)
                .partitionHandler(partitionHandler()) // (4)
                .build();
    }


    @Bean(name = JOB_NAME + "_reader")
    @StepScope
    public JpaPagingItemReader<Coupon> reader(@Value("#{jobParameters[txName]}") String txName,
                                              @Value("#{stepExecutionContext[minId]}") Long minId,
                                              @Value("#{stepExecutionContext[maxId]}") Long maxId) {

        Map<String, Object> params = new HashMap<>();
        params.put("txName", txName);
        params.put("minId", minId);
        params.put("maxId", maxId);

        log.info("reader minId={}, maxId={}", minId, maxId);

        return new JpaPagingItemReaderBuilder<Coupon>()
                .name(JOB_NAME + "_reader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString(
                        "SELECT c " +
                                "FROM Coupon c " +
                                "WHERE c.txName =:txName " +
                                "AND (c.id BETWEEN :minId AND :maxId)")
                .parameterValues(params)
                .build();
    }

    private ItemProcessor<Coupon, Pay> processor() {
        return coupon -> new Pay(
                coupon.getAmount(),
                coupon.getTxName(),
                coupon.getTxDateTime()
        );
    }

    private JpaItemWriter<Pay> writer() {
        return new JpaItemWriterBuilder<Pay>()
                .entityManagerFactory(this.entityManagerFactory)
                .usePersist(true)
                .build();
    }
}