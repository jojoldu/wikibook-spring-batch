package kr.co.wikibook.batch.jpa.basic.job;

import kr.co.wikibook.batch.jpa.basic.TestBatchConfig;
import kr.co.wikibook.batch.jpa.basic.domain.teacher.StudentRepository;
import kr.co.wikibook.batch.jpa.basic.domain.teacher.TeacherRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBatchTest
@SpringBootTest(classes={JpaItemWriterJobConfig.class, TestBatchConfig.class})
class JpaItemWriterJobConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void setup() {
        teacherRepository.deleteAll();
        studentRepository.deleteAll();
    }

    @Test
    void test_jpa_writer() throws Exception {

        //given
        JobParameters jobParameters = jobLauncherTestUtils.getUniqueJobParametersBuilder()
                .toJobParameters();
        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        //then
        Assertions.assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        Assertions.assertThat(teacherRepository.count()).isEqualTo(2); // (1)
        Assertions.assertThat(studentRepository.count()).isEqualTo(2); // (2)
    }
}
