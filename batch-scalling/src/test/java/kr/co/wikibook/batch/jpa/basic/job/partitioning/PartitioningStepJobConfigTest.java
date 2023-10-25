package kr.co.wikibook.batch.jpa.basic.job.partitioning;

import kr.co.wikibook.batch.jpa.basic.TestBatchConfig;
import kr.co.wikibook.batch.jpa.basic.domain.pay.*;
import kr.co.wikibook.batch.jpa.basic.job.parallel.ParallelStepJobConfig;
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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBatchTest
@SpringBootTest(classes = {ParallelStepJobConfig.class, TestBatchConfig.class})
class PartitioningStepJobConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private PayRepository payRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private MoneyRepository moneyRepository;


    @BeforeEach
    void setup() {
        payRepository.deleteAllInBatch();
        couponRepository.deleteAllInBatch();
        pointRepository.deleteAllInBatch();
        moneyRepository.deleteAllInBatch();
    }

    @Test
    void test_parallel() throws Exception {
        //given
        for (long i = 1; i <= 10; i++) {
            couponRepository.save(new Coupon(i * 1000, i + "_coupon", LocalDateTime.now()));
            pointRepository.save(new Point(i * 1000, i + "_point", LocalDateTime.now()));
            moneyRepository.save(new Money(i * 1000, i + "_money", LocalDateTime.now()));
        }

        JobParameters jobParameters = jobLauncherTestUtils.getUniqueJobParametersBuilder()
                .toJobParameters();

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        //then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(payRepository.count()).isEqualTo(30);
    }
}
