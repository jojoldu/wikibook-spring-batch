package kr.co.wikibook.batch.jpa.basic.job.parallel;

import kr.co.wikibook.batch.jpa.basic.domain.pay.Coupon;
import kr.co.wikibook.batch.jpa.basic.domain.pay.Money;
import kr.co.wikibook.batch.jpa.basic.domain.pay.Pay;
import kr.co.wikibook.batch.jpa.basic.domain.pay.Point;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.persistence.EntityManagerFactory;

@Configuration
public class ParallelStepJobConfig {
    public static final String JOB_NAME = "parallelStepJob";
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    public ParallelStepJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory) {
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
        Flow flow1 = new FlowBuilder<Flow>("flow1")
                .start(step1())
                .build();

        Flow flow2 = new FlowBuilder<Flow>("flow2")
                .start(step2())
                .build();

        Flow flow3 = new FlowBuilder<Flow>("flow3")
                .start(step3())
                .build();

        Flow parallelFlow = new FlowBuilder<Flow>("parallelFlow")
                .split(new SimpleAsyncTaskExecutor())
                .add(flow1, flow2, flow3)
                .build();

        return jobBuilderFactory.get(JOB_NAME)
                .start(parallelFlow)
                .end()
                .build();
    }

    @Bean(name = JOB_NAME + "_step1")
    public Step step1() {
        return stepBuilderFactory.get(JOB_NAME + "_step1")
                .<Coupon, Pay>chunk(chunkSize)
                .reader(reader1())
                .processor(processor1())
                .writer(writer())
                .build();
    }

    @Bean(name = JOB_NAME + "_reader1")
    @StepScope
    public JpaPagingItemReader<Coupon> reader1() {
        return new JpaPagingItemReaderBuilder<Coupon>()
                .name(JOB_NAME + "_reader1")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT c FROM Coupon c")
                .build();
    }

    private ItemProcessor<Coupon, Pay> processor1() {
        return coupon -> new Pay(
                coupon.getAmount(),
                coupon.getTxName(),
                coupon.getTxDateTime()
        );
    }

    @Bean(name = JOB_NAME + "_step2")
    public Step step2() {
        return stepBuilderFactory.get(JOB_NAME + "_step2")
                .<Point, Pay>chunk(chunkSize)
                .reader(reader2())
                .processor(processor2())
                .writer(writer())
                .build();
    }

    @Bean(name = JOB_NAME + "_reader2")
    @StepScope
    public JpaPagingItemReader<Point> reader2() {
        return new JpaPagingItemReaderBuilder<Point>()
                .name(JOB_NAME + "_reader2")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT p FROM Point p")
                .build();
    }

    private ItemProcessor<Point, Pay> processor2() {
        return point -> new Pay(
                point.getAmount(),
                point.getTxName(),
                point.getTxDateTime()
        );
    }

    @Bean(name = JOB_NAME + "_step3")
    public Step step3() {
        return stepBuilderFactory.get(JOB_NAME + "_step3")
                .<Money, Pay>chunk(chunkSize)
                .reader(reader3())
                .processor(processor3())
                .writer(writer())
                .build();
    }

    @Bean(name = JOB_NAME + "_reader3")
    @StepScope
    public JpaPagingItemReader<Money> reader3() {
        return new JpaPagingItemReaderBuilder<Money>()
                .name(JOB_NAME + "_reader3")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT m FROM Money m")
                .build();
    }


    private ItemProcessor<Money, Pay> processor3() {
        return money -> new Pay(
                money.getAmount(),
                money.getTxName(),
                money.getTxDateTime()
        );
    }

    private JpaItemWriter<Pay> writer() {
        return new JpaItemWriterBuilder<Pay>()
                .entityManagerFactory(this.entityManagerFactory)
                .usePersist(true)
                .build();
    }
}