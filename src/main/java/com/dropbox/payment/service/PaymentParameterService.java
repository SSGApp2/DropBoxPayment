package com.dropbox.payment.service;

import com.dropbox.payment.entity.app.ParameterDetail;
import com.dropbox.payment.repository.custom.ParameterDetailRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentParameterService {
    @Autowired
    ParameterDetailRepositoryCustom parameterDetailRepositoryCustom;

    public String get123NotificationURL(){
        ParameterDetail parameterDetail = parameterDetailRepositoryCustom.findByAppParameterCodeAndCode("PAYMENT_CONFIG","123_NOTI_URL");
        if (parameterDetail != null && parameterDetail.getParameterValue1() != null){
            return parameterDetail.getParameterValue1()+"/payment/one23/notification";
        } else {
            return null;
        }
    }

    public String get123URL(){
        ParameterDetail parameterDetail = parameterDetailRepositoryCustom.findByAppParameterCodeAndCode("PAYMENT_CONFIG","123_URL");
        if (parameterDetail != null && parameterDetail.getParameterValue1() != null){
            return parameterDetail.getParameterValue1();
        } else {
            return null;
        }
    }

    public String get2C2PURL(){
        ParameterDetail parameterDetail = parameterDetailRepositoryCustom.findByAppParameterCodeAndCode("PAYMENT_CONFIG","2C2P_URL");
        if (parameterDetail != null && parameterDetail.getParameterValue1() != null){
            return parameterDetail.getParameterValue1();
        } else {
            return null;
        }
    }

    public String getMerchantID(String dropBoxType){
        ParameterDetail parameterDetail = parameterDetailRepositoryCustom.findByAppParameterCodeAndCode("MERCHANT_MAP",dropBoxType);
        if (parameterDetail != null && parameterDetail.getParameterValue1() != null){
            return parameterDetail.getParameterValue1();
        } else {
            return null;
        }
    }
}
