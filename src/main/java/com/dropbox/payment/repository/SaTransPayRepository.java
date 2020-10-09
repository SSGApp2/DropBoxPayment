package com.dropbox.payment.repository;

import com.dropbox.payment.entity.app.SaTransPay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface SaTransPayRepository extends JpaRepository<SaTransPay, Long> {
    SaTransPay findBySaTrans(@Param("saTrans") Long saTrans);
    SaTransPay findBySaTransId(@Param("saTrans") Long saTrans);
    SaTransPay findByPaymentRefCode(@Param("paymentRefCode") String paymentRefCode);
}
