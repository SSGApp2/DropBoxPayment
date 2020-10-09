package com.dropbox.payment.repository;

import com.dropbox.payment.entity.app.SaTrans;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaTransRepository extends JpaRepository<SaTrans, Long> {
}
