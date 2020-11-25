package com.dropbox.payment.repository;

import com.dropbox.payment.entity.app.PaymentTemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface PaymentTempRepository extends JpaRepository<PaymentTemp, Long> {
    PaymentTemp findByPaymentRefCode(@Param("paymentRefCode") String paymentRefCode);
}
