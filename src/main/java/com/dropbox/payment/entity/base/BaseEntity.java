package com.dropbox.payment.entity.base;


import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@MappedSuperclass
@EntityListeners(EntityListener.class)
public abstract class BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version")
    private Integer version;

    /**
     */
    private String createdBy;

    /**
     */
    private String updatedBy;

    /**
     */
    private String status;

    /**
     */
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date createdDate;

    /**
     */
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date updatedDate;
}
