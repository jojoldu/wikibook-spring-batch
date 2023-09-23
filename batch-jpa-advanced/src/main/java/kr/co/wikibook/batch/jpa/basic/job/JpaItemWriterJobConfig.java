package kr.co.wikibook.batch.jpa.basic.job;

import kr.co.wikibook.batch.jpa.basic.domain.teacher.Student;
import kr.co.wikibook.batch.jpa.basic.domain.teacher.Teacher;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.Arrays;

@Configuration
public class JpaItemWriterJobConfig {
    public static final String JOB_NAME = "jpaItemWriterJob";
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    public JpaItemWriterJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
    }

    private int chunkSize;

    @Value("${chunkSize:10}")
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Bean(name = JOB_NAME)
    public Job job() {
        return jobBuilderFactory.get(JOB_NAME)
                .start(step())
                .build();
    }

    @Bean(name = JOB_NAME +"_step")
    public Step step() {
        return stepBuilderFactory.get(JOB_NAME +"_step")
                .<Teacher, Teacher>chunk(chunkSize)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean(name = JOB_NAME +"_reader")
    @StepScope
    public ListItemReader<Teacher> reader() {
        return new ListItemReader<>(Arrays.asList(
                new Teacher("수학선생님", "수학"),
                new Teacher("영어선생님", "영어")
        ));
    }

    public ItemProcessor<Teacher, Teacher> processor() {
        return teacher -> {
            teacher.addStudent(new Student("신규 학생"));
            return teacher;
        };
    }

    public JpaItemWriter<Teacher> writer() {
        return new JpaItemWriterBuilder<Teacher>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
