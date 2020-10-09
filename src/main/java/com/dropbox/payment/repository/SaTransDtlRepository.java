package com.dropbox.payment.repository;

import com.dropbox.payment.entity.app.SaTransDtl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface SaTransDtlRepository extends JpaRepository<SaTransDtl, Long> {
    SaTransDtl findByBoxNo(@Param("boxNo") String boxNo);
}
