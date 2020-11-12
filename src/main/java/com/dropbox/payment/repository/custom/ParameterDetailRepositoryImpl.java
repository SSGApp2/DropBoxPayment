package com.dropbox.payment.repository.custom;

import com.dropbox.payment.entity.app.ParameterDetail;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;

@Repository
public class ParameterDetailRepositoryImpl implements ParameterDetailRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public ParameterDetail findByAppParameterCodeAndCode(String apCode,String code) {
        Criteria criteria = ((Session) entityManager.getDelegate()).createCriteria(ParameterDetail.class);
        criteria.createAlias("appParameter", "appParameter");
        criteria.add(Restrictions.eq("appParameter.code", apCode));
        criteria.add(Restrictions.eq("code", code));
        return (ParameterDetail)criteria.uniqueResult();
    }
}
