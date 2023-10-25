package kr.co.wikibook.batch.jpa.basic.domain.pay;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    @Query("SELECT MIN(c.id) FROM Coupon c WHERE c.txDateTime BETWEEN :startDate AND :endDate")
    long findMinId(LocalDate startDate, LocalDate endDate);

    @Query("SELECT MAX(c.id) FROM Coupon c WHERE c.txDateTime BETWEEN :startDate AND :endDate")
    long findMaxId(LocalDate startDate, LocalDate endDate);
}
