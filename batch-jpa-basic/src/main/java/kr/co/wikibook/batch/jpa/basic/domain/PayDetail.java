package kr.co.wikibook.batch.jpa.basic.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class PayDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long amount;
    private String payName;

    public PayDetail() {}

    public PayDetail(Long amount, String payName) {
        this.amount = amount;
        this.payName = payName;
    }

    public Long getId() {
        return id;
    }

    public Long getAmount() {
        return amount;
    }

    public String getPayName() {
        return payName;
    }
}
