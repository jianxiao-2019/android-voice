package com.kikatech.voice.core.webservice.message;

import org.json.JSONObject;

/**
 * Created by ryanlin on 28/11/2017.
 */

public class ConfigMessage extends Message {

    public int packetInterval;

    @Override
    protected void parseData(JSONObject dataObj) {
        packetInterval = dataObj.optInt("packetInterval");
    }
}
