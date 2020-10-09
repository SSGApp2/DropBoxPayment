package com.dropbox.payment.repository;

import com.dropbox.payment.entity.app.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByCode(@Param("code") String code);
}
