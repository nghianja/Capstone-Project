package com.udacity.nanodegree.nghianja.capstone.serialization;

import android.util.Log;

import org.ksoap2.serialization.SoapObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public class Deserialization {

    @SuppressWarnings("unchecked")
    public ArrayList SoapDeserializeArray(Class<?> itemClass, SoapObject object) {
        ArrayList arrayList = new ArrayList();

        for (int i = 0; i < object.getPropertyCount(); i++) {
            try {
                Object newObject = itemClass.getConstructor(SoapObject.class).newInstance(((SoapObject) object.getProperty(i)));
                arrayList.add(newObject);
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return arrayList;
    }

    public <T> void SoapDeserialize(T item, SoapObject object) {
        Field[] fields = item.getClass().getDeclaredFields();
        //Log.d("fieldsNum",Integer.toString(fields.length));

        for (Field field : fields) {
            try {
                Log.d(field.getName(), field.getType().getName());

                if (field.getType().getName() == String.class.getName()) {
                    field.set(item, object.getProperty(field.getName()).toString());
                } else if (field.getType().getName() == Integer.class.getName() || field.getType().getName() == int.class.getName()) {
                    Log.d("3", "int");
                    field.set(item, Integer.parseInt(object.getProperty(field.getName()).toString()));
                } else if (field.getType().getName() == Float.class.getName()) {
                    Log.d("4", "float");
                    field.set(item, Float.parseFloat(object.getProperty(field.getName()).toString()));
                } else if (field.getType().getName() == Double.class.getName()) {
                    field.set(item, Double.parseDouble(object.getProperty(field.getName()).toString()));
                } else if (field.getType().getName() == Boolean.class.getName()) {
                    field.set(item, Boolean.parseBoolean((object.getProperty(field.getName()).toString())));
                } else if (List.class.isAssignableFrom(field.getType())) {
                    Log.d("array", field.getGenericType().toString());
                    SoapObject fieldArray = (SoapObject) (object.getProperty(field.getName()));
                    Log.d("array", "1");
                    ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                    Class genericClass = (Class) (parameterizedType.getActualTypeArguments()[0]);
                    Log.d("array", "2");
                    //声明Arraylist以备之后初始化传入参数
                    ArrayList list = new ArrayList();
                    Log.d("array", "2.2");
                    for (int i = 0; i < fieldArray.getPropertyCount(); i++) {
                        Log.d("array", "2.8");
                        Object newObject = genericClass.getConstructor(SoapObject.class).newInstance((SoapObject) (fieldArray.getProperty(i)));
                        Log.d("array", "3");
                        list.add(newObject);
                        Log.d("array", "4");
                    }
                    field.set(item, list);
                } else {
                    Class class1 = field.getType();
                    Object newObject = class1.getConstructor(SoapObject.class).newInstance((SoapObject) (object.getProperty(field.getName())));
                    field.set(item, newObject);
                }
            } catch (Exception e) {
                Log.d("FieldNotFound:", " " + e.getMessage());
            }
        }
    }
}
