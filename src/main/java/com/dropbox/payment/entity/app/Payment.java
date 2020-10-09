package com.dropbox.payment.entity.app;

import com.dropbox.payment.entity.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of={"id"})
public class Payment extends BaseEntity{

    private String code;

    private String name;

    private String status;
}
