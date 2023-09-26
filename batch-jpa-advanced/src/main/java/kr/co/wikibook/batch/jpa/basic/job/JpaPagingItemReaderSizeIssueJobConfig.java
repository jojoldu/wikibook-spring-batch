package kr.co.wikibook.batch.jpa.basic.job;

import kr.co.wikibook.batch.jpa.basic.domain.teacher.SchoolClass;
import kr.co.wikibook.batch.jpa.basic.domain.teacher.Teacher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

@Configuration
public class JpaPagingItemReaderSizeIssueJobConfig {
    private static final Logger log = LoggerFactory.getLogger(JpaPagingItemReaderSizeIssueJobConfig.class);
    public static final String JOB_NAME = "jpaPagingItemReaderJob";
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory; // (1)

    public JpaPagingItemReaderSizeIssueJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory) {
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

    @Bean(name = JOB_NAME+"_step")
    public Step step() {
        return stepBuilderFactory.get(JOB_NAME+"_step")
                .<Teacher, SchoolClass>chunk(100) // (1)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean(name = JOB_NAME +"_reader")
    @StepScope
    public JpaPagingItemReader<Teacher> reader() { // (2)
        return new JpaPagingItemReaderBuilder<Teacher>()
                .name(JOB_NAME +"_reader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT t FROM Teacher t")
                .build();
    }

    public ItemProcessor<Teacher, SchoolClass> processor() {
        return teacher -> new SchoolClass(teacher.getId(), teacher.getStudents());
    }

    private ItemWriter<SchoolClass> writer() {
        return list -> {
            for (SchoolClass schoolClass: list) {
                log.info("Current schoolClass={}", schoolClass);
            }
        };
    }
}