package com.laptrinhjavaweb.orm.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ObjectAccessUtil {
    public static Object getFieldData(Object object, Field field) throws ReflectiveOperationException {
        String fieldName = field.getName();
//            upper case the first letter of field name
        fieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

//            build getter method name
        String getterMethodName = "get" + fieldName;

        Method getterMethod = object.getClass().getMethod(getterMethodName);
        return getterMethod.invoke(object);
    }

    public static void setFieldData(Object object, Object fieldData, Field field) throws ReflectiveOperationException {
        String fieldName = field.getName();
//            upper case the first letter of field name
        fieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

//            build setter method name
        String setterMethodName = "set" + fieldName;

//            get setter method and invoke
        Method setterMethod = object.getClass().getMethod(setterMethodName, field.getType());
        setterMethod.invoke(object, fieldData);
    }

    public static Field getFieldByName(Class clazz, String fieldName) throws NoSuchFieldException {
        return clazz.getDeclaredField(fieldName);
    }

    public static void copyProperties(Object source, Object destination) throws ReflectiveOperationException {
        Field[] fields = source.getClass().getDeclaredFields();

        for (Field field : fields) {
            Object fieldData = getFieldData(source, field);
            setFieldData(destination, fieldData, field);
        }
    }
}
