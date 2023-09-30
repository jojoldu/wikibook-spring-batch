package kr.co.wikibook.batch.jpa.basic.job.reader;

import kr.co.wikibook.batch.jpa.basic.TestBatchConfig;
import kr.co.wikibook.batch.jpa.basic.domain.teacher.Student;
import kr.co.wikibook.batch.jpa.basic.domain.teacher.StudentRepository;
import kr.co.wikibook.batch.jpa.basic.domain.teacher.Teacher;
import kr.co.wikibook.batch.jpa.basic.domain.teacher.TeacherRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBatchTest // (1)
@SpringBootTest(classes = {HibernateCursorItemReaderJobConfig.class, TestBatchConfig.class})
class HibernateCursorItemReaderJobConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void setup() {
        studentRepository.deleteAllInBatch();
        teacherRepository.deleteAllInBatch();
    }

    @Test
    void test_hibernate_cursor() throws Exception {
        //given
        for(long i=1;i<=10;i++) {
            String teacherName = i + "선생님";
            Teacher teacher = new Teacher(teacherName, "수학");
            teacher.addStudent(new Student(teacherName+"의 학생1"));
            teacher.addStudent(new Student(teacherName+"의 학생2"));
            teacherRepository.save(teacher);
        }

        JobParameters jobParameters = jobLauncherTestUtils.getUniqueJobParametersBuilder() // (2)
                .toJobParameters();
        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        //then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }
}
