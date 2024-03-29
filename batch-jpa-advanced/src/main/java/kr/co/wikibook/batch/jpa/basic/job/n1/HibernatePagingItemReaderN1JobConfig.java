package kr.co.wikibook.batch.jpa.basic.job.n1;

import kr.co.wikibook.batch.jpa.basic.domain.teacher.Teacher;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.HibernatePagingItemReader;
import org.springframework.batch.item.database.builder.HibernatePagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

@Configuration
public class HibernatePagingItemReaderN1JobConfig {
    private static final Logger log = LoggerFactory.getLogger(HibernatePagingItemReaderN1JobConfig.class);
    public static final String JOB_NAME = "hibernatePagingItemReaderN1Job";
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory; // (1)

    public HibernatePagingItemReaderN1JobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory) {
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
                .build();
    }

    @Bean(name = JOB_NAME + "_step")
    public Step step() {
        return stepBuilderFactory.get(JOB_NAME + "_step")
                .<Teacher, Teacher>chunk(chunkSize) // (1)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean(name = JOB_NAME + "_reader")
    @StepScope
    public HibernatePagingItemReader<Teacher> reader() {
        return new HibernatePagingItemReaderBuilder<Teacher>()
                .name(JOB_NAME + "_reader")
                .sessionFactory(entityManagerFactory.unwrap(SessionFactory.class))
                .queryString("SELECT distinct(t) FROM Teacher t JOIN FETCH t.students")
                .fetchSize(chunkSize)
                .pageSize(chunkSize)
                .useStatelessSession(false)
                .build();
    }

    public ItemProcessor<Teacher, Teacher> processor() {
        return teacher -> {
            log.info("students count={}", teacher.getStudents().size()); // Many인 students를 지연조회(Lazy Loading)
            return teacher;
        };
    }

    private ItemWriter<Teacher> writer() {
        return list -> {
            for (Teacher teacher : list) {
                log.info("Current teacher={}", teacher);
            }
        };
    }
}
