package kr.co.wikibook.batch.jpa.basic.domain.pay;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Pay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long amount;
    private String txName;
    private LocalDateTime txDateTime;

    public Pay() {}

    public Pay(Long amount, String txName, LocalDateTime txDateTime) {
        this.amount = amount;
        this.txName = txName;
        this.txDateTime = txDateTime;
    }

    public Long getId() {
        return id;
    }

    public Long getAmount() {
        return amount;
    }

    public String getTxName() {
        return txName;
    }

    public LocalDateTime getTxDateTime() {
        return txDateTime;
    }

    @Override
    public String toString() {
        return "Pay{" +
                "id=" + id +
                ", amount=" + amount +
                ", txName='" + txName + '\'' +
                ", txDateTime=" + txDateTime +
                '}';
    }
}
