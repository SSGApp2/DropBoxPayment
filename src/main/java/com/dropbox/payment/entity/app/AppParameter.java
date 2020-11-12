package com.dropbox.payment.entity.app;

import com.dropbox.payment.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@EqualsAndHashCode(of = {"id"})
public class AppParameter extends BaseEntity{

    @Column(unique = true)
    private String code;

    private String parameterDescription;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "appParameter")
    private Set<ParameterDetail> parameterDetails = new HashSet<ParameterDetail>();
}
