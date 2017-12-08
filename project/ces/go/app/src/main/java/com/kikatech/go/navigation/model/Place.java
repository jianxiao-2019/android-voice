package com.kikatech.go.navigation.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kikatech.go.navigation.google.webservice.JsonKeys;
import com.kikatech.go.util.LogUtil;

/**
 * @author SkeeterWang Created on 2017/11/1.
 */
public class Place {
    private static final String TAG = "Place";

    @Expose
    @SerializedName(JsonKeys.KEY_FORMATTED_ADDRESS)
    private String formatted_address;

    @Expose
    @SerializedName(JsonKeys.KEY_NAME)
    private String name;

    @Expose
    @SerializedName(JsonKeys.KEY_GEOMETRY)
    private PlaceGeometry geometry;

    public String getFormattedAddress() {
        return formatted_address;
    }

    public String getName() {
        return name;
    }

    public Double getLatitude() {
        return geometry != null ? geometry.getLatitude() : null;
    }

    public Double getLongitude() {
        return geometry != null ? geometry.getLongitude() : null;
    }

    public void print() {
        if (LogUtil.DEBUG) {
            LogUtil.logv(TAG, "formatted_address: " + formatted_address);
            LogUtil.logv(TAG, "name: " + name);
            if (geometry != null) {
                geometry.print();
            }
        }
    }

    public String getResultText() {
        String result = "";
        if (geometry != null) {
            result = String.format("FORMATTED_ADDRESS:\n%1$s\nNAME: %2$s\nLOCATION: %3$s", formatted_address, name, geometry.getResultText());
        }
        return result;
    }
}
