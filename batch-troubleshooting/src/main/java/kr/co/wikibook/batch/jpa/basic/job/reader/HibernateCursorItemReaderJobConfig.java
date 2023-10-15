package kr.co.wikibook.batch.jpa.basic.job.reader;

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
import org.springframework.batch.item.database.HibernateCursorItemReader;
import org.springframework.batch.item.database.builder.HibernateCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

@Configuration
public class HibernateCursorItemReaderJobConfig {
    private static final Logger log = LoggerFactory.getLogger(HibernateCursorItemReaderJobConfig.class);

    public HibernateCursorItemReaderJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.sessionFactory = entityManagerFactory.unwrap(SessionFactory.class); // (1)
    }

    public static final String JOB_NAME = "hibernateCursorItemReaderJob"; // (2)
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final SessionFactory sessionFactory;

    private int chunkSize;

    @Value("${chunkSize:5}") // (3)
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
                .<Teacher, Teacher>chunk(chunkSize)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean(name = JOB_NAME + "_reader")
    @StepScope
    public HibernateCursorItemReader<Teacher> reader() {
        return new HibernateCursorItemReaderBuilder<Teacher>()
                .sessionFactory(sessionFactory)
                .queryString("SELECT t FROM Teacher t")
                .name(JOB_NAME + "_reader")
                .fetchSize(chunkSize)
                .useStatelessSession(false) // (4)
                .build();
    }

    public ItemProcessor<Teacher, Teacher> processor() {
        return teacher -> {
            log.info("students count={}", teacher.getStudents().size()); // (5)
            return teacher;
        };
    }

    private ItemWriter<Teacher> writer() {
        return list -> {
            for (Teacher teacher : list) {
                log.info("Current Teacher={}", teacher);
            }
        };
    }
}