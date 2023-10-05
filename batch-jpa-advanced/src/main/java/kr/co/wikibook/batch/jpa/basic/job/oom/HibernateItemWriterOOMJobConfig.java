package kr.co.wikibook.batch.jpa.basic.job.oom;

import kr.co.wikibook.batch.jpa.basic.domain.teacher.Student;
import kr.co.wikibook.batch.jpa.basic.domain.teacher.Teacher;
import org.hibernate.SessionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.HibernateItemWriter;
import org.springframework.batch.item.database.builder.HibernateItemWriterBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Configuration
public class HibernateItemWriterOOMJobConfig {
    public static final String JOB_NAME = "hibernateItemWriterOOMJob";
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    public HibernateItemWriterOOMJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, EntityManagerFactory entityManagerFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
    }

    private int chunkSize;

    @Value("${chunkSize:10000}")
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
    public ListItemReader<Teacher> reader() {
        return new ListItemReader<>(IntStream.range(0, 5_000_000)
                .mapToObj(i -> new Teacher(String.valueOf(i), "Name" + i))
                .collect(Collectors.toList())
        );
    }

    public ItemProcessor<Teacher, Teacher> processor() {
        return teacher -> {
            teacher.addStudent(new Student("신규 학생"));
            return teacher;
        };
    }

    public HibernateItemWriter<Teacher> writer() {
        return new HibernateItemWriterBuilder<Teacher>()
                .sessionFactory(entityManagerFactory.unwrap(SessionFactory.class))
                .build();
    }
}