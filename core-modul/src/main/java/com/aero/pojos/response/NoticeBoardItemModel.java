package com.aero.pojos.response;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * Created by SUNAINA on 26-12-2018.
 */

public class NoticeBoardItemModel implements Serializable {

          /*   "id": 1,
                     "title": "PM Addressing press conference",
                     "dateTime": 1544435165045,
                     "latitude": "26.989797",
                     "longitude": "36.8989",
                     "venue": "HALL A"*/

          private int id;
          private String title,latitude,longitude,venue;
          private long dateTime;

    public NoticeBoardItemModel(int id, String title, String latitude, String longitude, String venue, long dateTime) {
        this.id = id;
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.venue = venue;
        this.dateTime = dateTime;
    }

    public NoticeBoardItemModel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    static public NoticeBoardItemModel create(String serializedData) {
        // Use GSON to instantiate this class using the JSON representation of the state
        Gson gson = new Gson();
        return gson.fromJson(serializedData, NoticeBoardItemModel.class);
    }
    public String serialize() {
        // Serialize this class into a JSON string using GSON
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
