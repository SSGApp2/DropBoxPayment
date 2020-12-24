package com.dropbox.payment.entity.app;

import com.dropbox.payment.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@EqualsAndHashCode(of = {"id"})
public class Dropbox extends BaseEntity{

    private String dropboxCode;

    private String dropboxName;

    private String taxID;

    private String address;

    private String subDistrict;

    private String district;

    private String province;

    private String postCode;

    private String latitude;

    private String longtitude;

    private String status;

    private String type;

    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "dropbox")
    private List<SaTrans> saTrans= new ArrayList<>();

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "area")
//    private Area area;
}
