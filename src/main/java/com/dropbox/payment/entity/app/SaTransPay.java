package com.dropbox.payment.entity.app;

import com.dropbox.payment.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@Entity
@EqualsAndHashCode(of = {"id"})
public class SaTransPay extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment")
    private Payment payment;

    private Double paymentExcVat;

    private Double vatRate;

    private Double vatAmt;

    private Double paymentAmt;

    private String referNo;

    private String paymentRefCode;

    private String paymentStatus;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saTrans")
    private SaTrans saTrans;

    private String cardBank;
}
