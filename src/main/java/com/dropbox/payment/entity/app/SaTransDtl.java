package com.dropbox.payment.entity.app;

import com.dropbox.payment.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Data
@Entity
@EqualsAndHashCode(of = {"id"})
public class SaTransDtl extends BaseEntity {

    private String boxNo;

    private Double postage;

    private Double postageExtVat;

    private Double vatAmt;

    private Double vatRate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saTrans")
    private SaTrans saTrans;
}
