package kr.co.wikibook.batch.jpa.basic.job.partitioning;

import kr.co.wikibook.batch.jpa.basic.domain.pay.CouponRepository;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ProductIdRangePartitioner implements Partitioner {

    private final CouponRepository couponRepository;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public ProductIdRangePartitioner(CouponRepository couponRepository, LocalDate startDate, LocalDate endDate) {
        this.couponRepository = couponRepository;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        long min = couponRepository.findMinId(startDate, endDate);
        long max = couponRepository.findMaxId(startDate, endDate);
        long targetSize = (max - min) / gridSize + 1;

        Map<String, ExecutionContext> result = new HashMap<>();
        long number = 0;
        long start = min;
        long end = start + targetSize - 1;

        while (start <= max) {
            ExecutionContext value = new ExecutionContext();
            result.put("partition" + number, value);

            if (end >= max) {
                end = max;
            }

            value.putLong("minId", start); // 각 파티션마다 사용될 minId
            value.putLong("maxId", end); // 각 파티션마다 사용될 maxId
            start += targetSize;
            end += targetSize;
            number++;
        }

        return result;
    }
}
