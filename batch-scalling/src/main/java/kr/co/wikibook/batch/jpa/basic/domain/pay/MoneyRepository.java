package kr.co.wikibook.batch.jpa.basic.domain.pay;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MoneyRepository extends JpaRepository<Money, Long> {
}
