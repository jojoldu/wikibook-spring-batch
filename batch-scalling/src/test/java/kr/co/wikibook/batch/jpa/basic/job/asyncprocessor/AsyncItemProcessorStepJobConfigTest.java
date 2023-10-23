package kr.co.wikibook.batch.jpa.basic.job.asyncprocessor;

import kr.co.wikibook.batch.jpa.basic.TestBatchConfig;
import kr.co.wikibook.batch.jpa.basic.domain.pay.Coupon;
import kr.co.wikibook.batch.jpa.basic.domain.pay.CouponRepository;
import kr.co.wikibook.batch.jpa.basic.domain.pay.PayRepository;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBatchTest
@SpringBootTest(classes = {AsyncItemProcessorStepJobConfig.class, TestBatchConfig.class})
@TestPropertySource(properties = {"job.name=" + AsyncItemProcessorStepJobConfig.JOB_NAME})
class AsyncItemProcessorStepJobConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private PayRepository payRepository;

    @Autowired
    private CouponRepository couponRepository;


    @BeforeEach
    void setup() {
        payRepository.deleteAllInBatch();
        couponRepository.deleteAllInBatch();
    }

    @Test
    void test_asyncItemProcessorStep() throws Exception {
        //given
        String txName = "우아한 스프링 배치";
        for (long i = 1; i <= 100; i++) {
            couponRepository.save(new Coupon(i * 1000, txName, LocalDateTime.now()));
        }

        JobParameters jobParameters = jobLauncherTestUtils.getUniqueJobParametersBuilder()
                .addString("txName", txName)
                .toJobParameters();

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        //then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(payRepository.count()).isEqualTo(100);
    }
}
