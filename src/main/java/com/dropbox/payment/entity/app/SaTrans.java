package com.dropbox.payment.entity.app;

import com.dropbox.payment.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@EqualsAndHashCode(of = {"id"})
public class SaTrans extends BaseEntity {

    private String dropboxCode;

    private String docNo;

    private Date docDate;

    private String custCode;

    private Integer printReceipt;

    private Double totalExcVat;

    private Double totalVatAmt;

    private Double totalAmt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dropbox")
    private Dropbox dropbox;

    @OneToOne(fetch = FetchType.LAZY)
    private SaTransPay saTransPay;

    @OneToOne(fetch = FetchType.LAZY)
    private SaTransDtl saTransDtl;
}
