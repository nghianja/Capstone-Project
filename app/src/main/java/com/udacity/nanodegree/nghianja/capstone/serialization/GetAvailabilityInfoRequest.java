package com.udacity.nanodegree.nghianja.capstone.serialization;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

import java.util.Hashtable;

/**
 * References:
 * [1] http://simpligility.github.io/ksoap2-android/tips.html
 * [2] http://stackoverflow.com/questions/9805946/nesting-properties-inside-a-tag-in-ksoap2
 * [3] http://stackoverflow.com/questions/10160719/android-ksoap-object-mapping
 */
public class GetAvailabilityInfoRequest implements KvmSerializable {
    private String APIKey;
    private String ISBN;
    private String Modifiers;

    public GetAvailabilityInfoRequest(String APIKey, String ISBN) {
        this.APIKey = APIKey;
        this.ISBN = ISBN;
        this.Modifiers = "";
    }

    public int getPropertyCount() {
        return 3;
    }

    public void getPropertyInfo(int index, Hashtable hash, PropertyInfo info) {
        switch (index) {
            case 0:
                info.name = "APIKey";
                info.type = PropertyInfo.STRING_CLASS;
                break;
            case 1:
                info.name = "ISBN";
                info.type = PropertyInfo.STRING_CLASS;
                break;
            case 2:
                info.name = "Modifiers";
                info.type = PropertyInfo.STRING_CLASS;
                break;
            default:
                break;
        }
    }

    public Object getProperty(int index) {
        switch (index) {
            case 0:
                return APIKey;
            case 1:
                return ISBN;
            case 2:
                return Modifiers;
            default:
                return null;
        }
    }

    public void setProperty(int index, Object value) {
        switch (index) {
            case 0:
                APIKey = value.toString();
                break;
            case 1:
                ISBN = value.toString();
                break;
            case 2:
                Modifiers = value.toString();
                break;
            default:
                break;
        }
    }
}
