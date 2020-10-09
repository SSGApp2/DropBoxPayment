package com.dropbox.payment.util;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AppUtil {

    public static boolean isNotEmpty(List checkList) {
        Boolean returnValue = false;
        if (checkList != null && !checkList.isEmpty() && checkList.size() > 0) {
            returnValue = true;
        }
        return returnValue;
    }

    public static boolean isNotEmpty(String checkString) {
        boolean returnValue = false;
        if (checkString != null && checkString.length() > 0) {
            returnValue = true;
        }
        return returnValue;
    }

    public static boolean isNotEmptyStr(String checkString) {
        boolean returnValue = false;
        if (checkString != null && checkString.length() > 0) {
            if (checkString.equalsIgnoreCase("null")) {
                returnValue = false;
            } else {
                returnValue = true;
            }
        }
        return returnValue;
    }

    public static boolean isEmpty(String checkString) {
        boolean returnValue = true;
        if (checkString != null && checkString.length() > 0) {
            returnValue = false;
        }
        return returnValue;
    }

    public static boolean isNotNull(Object object) {
        boolean returnValue = false;
        if (object != null) {
            returnValue = true;
        }
        return returnValue;
    }

    public static boolean isNull(Object object) {
        boolean returnValue = false;
        if (object == null) {
            returnValue = true;
        }
        return returnValue;
    }

    public static List<String> getObjectAttr(Object obj) {
        List<String> result = new ArrayList<>();
        List<Object> valueList = new ArrayList<>();
        try {
            Class cls = obj.getClass();
            Object target = obj;
            for (Field field : cls.getDeclaredFields()) {
                Field strField = ReflectionUtils.findField(cls, field.getName());
                if (!strField.getType().equals(List.class) && !strField.getType().equals(Set.class)) {
                    result.add(field.getName());
                    strField.setAccessible(true);
                    Object value = ReflectionUtils.getField(strField, target);
                    if (AppUtil.isNotNull(value) && AppUtil.isNotEmpty(value.toString())) {
                        valueList.add(value);
                        ReflectionUtils.makeAccessible(strField); //set null when emptyString
                        ReflectionUtils.setField(strField, target, null);
                    }
                }
            }
        } catch (Exception e) {

        }
        valueList.hashCode();
        return result;
    }

    public static boolean isNotNullStr(String checkString) {
        boolean returnValue = false;
        if (checkString != null && checkString.length() > 0) {
            if (checkString.equalsIgnoreCase("")) {
                returnValue = false;
            } else {
                returnValue = true;
            }
        }
        return returnValue;
    }

}
