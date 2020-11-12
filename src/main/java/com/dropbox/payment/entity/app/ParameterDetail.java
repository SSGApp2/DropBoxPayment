package com.dropbox.payment.entity.app;

import com.dropbox.payment.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Data
@Entity
@EqualsAndHashCode(of = {"id"})
public class ParameterDetail extends BaseEntity{
    @NotNull
    private String code;

    private String parameterDescription;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appParameter")
    private AppParameter appParameter;

    /**
     */
    private String parameterValue1;

    /**
     */
    private String parameterValue2;

    /**
     */
    private String parameterValue3;

    /**
     */
    private String parameterValue4;

    /**
     */
    private String parameterValue5;

    /**
     */
    private String parameterValue6;

    /**
     */
    private String parameterValue7;

    /**
     */
    private String parameterValue8;

    /**
     */
    private String parameterValue9;

    /**
     */
    private String parameterValue10;
}
