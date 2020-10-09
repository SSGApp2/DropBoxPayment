package com.dropbox.payment.entity.base;
import com.dropbox.payment.util.AppUtil;
import com.dropbox.payment.util.DateUtil;
import com.dropbox.payment.util.UserMapDetails;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

@Component
public class EntityListener {

    private Logger LOGGER = LoggerFactory.getLogger(EntityListener.class);

    private static UserMapDetails userMapDetails;

    @Autowired
    public void setUserMapDetails(UserMapDetails service) {
        this.userMapDetails = service;
    }

    @PrePersist
    public void prePersistFunction(Object object) {
        String user = null;
        String progId = "KLOG";
        Date currentDate = DateUtil.getCurrentDate();
        try {
            user = userMapDetails.getUsername();
        } catch (Exception e) {

        }
        try {
            this.assignValueToCommonFields(object, user, progId, currentDate, "CREATE");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @PreUpdate
    public void preUpdateFunction(Object object) {
        String user = null;
        String progId = "KLOG";
        Date currentDate = DateUtil.getCurrentDate();
        try {
            user = userMapDetails.getUsername();
        } catch (Exception e) {

        }
        try {
            this.assignValueToCommonFields(object, user, progId, currentDate, "UPDATE");
        } catch (Exception e) {

        }

    }

    private void assignValueToCommonFields(Object arg, String user, String progId, Date currentDate, String status) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        BeanUtils.setProperty(arg, "updatedBy", user);
        BeanUtils.setProperty(arg, "updatedDate", currentDate);
        BeanUtils.setProperty(arg, "updateBy", user);
        BeanUtils.setProperty(arg, "updateDate", currentDate);
        if (status.equals("CREATE")) {
            BeanUtils.setProperty(arg, "createdBy", user);
            BeanUtils.setProperty(arg, "createdDate", currentDate);
            BeanUtils.setProperty(arg, "createBy", user);
            BeanUtils.setProperty(arg, "createDate", currentDate);
        }
        Class cls = arg.getClass();
        Object target = arg;
        for (Field field : cls.getDeclaredFields()) {
            Field strField = ReflectionUtils.findField(cls, field.getName());
            if (strField.getType().equals(String.class)) {
                strField.setAccessible(true);
                Object value = ReflectionUtils.getField(strField, target);
                if (AppUtil.isNotNull(value) && AppUtil.isEmpty(value.toString())) {
                    ReflectionUtils.makeAccessible(strField); //set null when emptyString
                    ReflectionUtils.setField(strField, target, null);
                }
            }
        }
    }
}
