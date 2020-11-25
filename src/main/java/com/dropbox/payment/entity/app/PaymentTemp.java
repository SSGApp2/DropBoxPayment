package com.dropbox.payment.entity.app;

import com.dropbox.payment.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

@Data
@Entity
@EqualsAndHashCode(of = {"id"})
public class PaymentTemp extends BaseEntity {

    private String paymentRefCode;

    private String paymentStatus;

    private String paymentType;

    private Double paymentAmount;
}
