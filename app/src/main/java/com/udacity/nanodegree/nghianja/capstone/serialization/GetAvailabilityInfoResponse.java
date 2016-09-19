package com.udacity.nanodegree.nghianja.capstone.serialization;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;

/**
 * References:
 * [1] http://www.codeproject.com/Tips/810432/Android-deserialize-KSoap-response-into-Complex-o
 */
public class GetAvailabilityInfoResponse {
    String Status;
    String Message;
    String ErrorMessage;
    String NextRecordPosition;
    String SetId;
    ArrayList<Item> Items;

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getErrorMessage() {
        return ErrorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        ErrorMessage = errorMessage;
    }

    public String getNextRecordPosition() {
        return NextRecordPosition;
    }

    public void setNextRecordPosition(String nextRecordPosition) {
        NextRecordPosition = nextRecordPosition;
    }

    public String getSetId() {
        return SetId;
    }

    public void setSetId(String setId) {
        SetId = setId;
    }

    public ArrayList<Item> getItems() {
        return Items;
    }

    public void setItems(ArrayList<Item> items) {
        Items = items;
    }

    public GetAvailabilityInfoResponse() {}

    public GetAvailabilityInfoResponse(SoapObject object) {
        new Deserialization().SoapDeserialize(this, object);
    }
}
