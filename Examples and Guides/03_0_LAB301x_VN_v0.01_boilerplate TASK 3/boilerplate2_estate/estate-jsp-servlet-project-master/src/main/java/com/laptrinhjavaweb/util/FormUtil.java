package com.laptrinhjavaweb.util;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.DateTimeConverter;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

public class FormUtil {

    public static <T> T populate(Class<T> clazz, HttpServletRequest request) {
        T object = null;
        try {
            object = clazz.newInstance();

            DateTimeConverter dateConverter = new DateConverter(null);
            dateConverter.setPatterns(new String[]{"dd/MM/yyyy", "MM/dd/yyyy"});

            ConvertUtilsBean convertUtilsBean = new ConvertUtilsBean();
            convertUtilsBean.register(dateConverter, Date.class);

            BeanUtilsBean beanUtilsBean = new BeanUtilsBean(convertUtilsBean, new PropertyUtilsBean());
            beanUtilsBean.populate(object, request.getParameterMap());
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
        return object;
    }
}
