package com.dropbox.payment.service;

import com.dropbox.payment.entity.app.ParameterDetail;
import com.dropbox.payment.repository.custom.ParameterDetailRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentParameterService {
    @Autowired
    ParameterDetailRepositoryCustom parameterDetailRepositoryCustom;

    final String prodCode = "PROD";

    public String getENVConnection(){
        ParameterDetail parameterDetail = parameterDetailRepositoryCustom.findByAppParameterCodeAndCode("PAYMENT_ENV_MODE","PAYMENT_ENV_MODE");
        if (parameterDetail != null && parameterDetail.getParameterValue1() != null){
            return parameterDetail.getParameterValue1();
        } else {
            return null;
        }
    }

    public String get123NotificationURL(){
        ParameterDetail parameterDetail = parameterDetailRepositoryCustom.findByAppParameterCodeAndCode("PAYMENT_CONFIG","123_NOTI_URL");
        if (parameterDetail != null && parameterDetail.getParameterValue1() != null){
            return parameterDetail.getParameterValue1();
        } else {
            return null;
        }
    }

    public String get123URL(String envMode){
        ParameterDetail parameterDetail = parameterDetailRepositoryCustom.findByAppParameterCodeAndCode("PAYMENT_CONFIG","123_URL");
        if (parameterDetail != null){
            if (!prodCode.equals(envMode)){
                if (parameterDetail.getParameterValue1() != null){
                    return parameterDetail.getParameterValue1();
                } else {
                    return null;
                }
            } else { // PROD
                if (parameterDetail.getParameterValue2() != null){
                    return parameterDetail.getParameterValue2();
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    public String get2C2PURL(String envMode){
        ParameterDetail parameterDetail = parameterDetailRepositoryCustom.findByAppParameterCodeAndCode("PAYMENT_CONFIG","2C2P_URL");
        if (parameterDetail != null){
            if (!prodCode.equals(envMode)){
                if (parameterDetail.getParameterValue1() != null){
                    return parameterDetail.getParameterValue1();
                } else {
                    return null;
                }
            } else { // PROD
                if (parameterDetail.getParameterValue2() != null){
                    return parameterDetail.getParameterValue2();
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    public String getPaymentURL(String envMode,String code){
        ParameterDetail parameterDetail = parameterDetailRepositoryCustom.findByAppParameterCodeAndCode("PAYMENT_CONFIG",code);
        if (parameterDetail != null){
            if (!prodCode.equals(envMode)){
                if (parameterDetail.getParameterValue1() != null){
                    return parameterDetail.getParameterValue1();
                } else {
                    return null;
                }
            } else { // PROD
                if (parameterDetail.getParameterValue2() != null){
                    return parameterDetail.getParameterValue2();
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    public String getMerchantID(String dropBoxType,String envMode){
        ParameterDetail parameterDetail = parameterDetailRepositoryCustom.findByAppParameterCodeAndCode("MERCHANT_MAP",dropBoxType);
        if (parameterDetail != null){
            if (!prodCode.equals(envMode)){
                if (parameterDetail.getParameterValue1() != null){
                    return parameterDetail.getParameterValue1();
                } else {
                    return null;
                }
            } else { // PROD
                if (parameterDetail.getParameterValue4() != null){
                    return parameterDetail.getParameterValue4();
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    public ParameterDetail getParameterMerchant(String dropBoxType){
        ParameterDetail parameterDetail = parameterDetailRepositoryCustom.findByAppParameterCodeAndCode("MERCHANT_MAP",dropBoxType);
        return parameterDetail;
    }

    public ParameterDetail findParameterMerchantByMerchantId(String merchantId,String envMode){
        return parameterDetailRepositoryCustom.findParameterMerchantByMerchantId(merchantId,envMode);
    }
}
