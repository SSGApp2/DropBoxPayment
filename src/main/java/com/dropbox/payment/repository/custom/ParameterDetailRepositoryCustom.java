package com.dropbox.payment.repository.custom;

import com.dropbox.payment.entity.app.ParameterDetail;

public interface ParameterDetailRepositoryCustom {
    ParameterDetail findByAppParameterCodeAndCode(String apCode, String code);
    ParameterDetail findParameterMerchantByMerchantId(String merchantId,String envMode);
}
