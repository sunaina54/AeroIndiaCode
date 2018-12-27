package com.aero.pojos.response;

import com.aero.pojos.generic.DashboardProviderModel;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dell13 on 05-12-2018.
 */

public class DashboardResponse extends GenericResponse {
    public List<DashboardProviderModel> items = new ArrayList<>();
    static public DashboardResponse create(String serializedData) {
        // Use GSON to instantiate this class using the JSON representation of the state
        Gson gson = new Gson();
        return gson.fromJson(serializedData, DashboardResponse.class);
    }
    public String serialize() {
        // Serialize this class into a JSON string using GSON
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
