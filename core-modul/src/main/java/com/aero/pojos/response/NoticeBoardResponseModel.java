package com.aero.pojos.response;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by SUNAINA on 26-12-2018.
 */

public class NoticeBoardResponseModel extends GenericResponse implements Serializable {
    private ArrayList<NoticeBoardItemModel> noticeBoardList;

    public ArrayList<NoticeBoardItemModel> getNoticeBoardList() {
        return noticeBoardList;
    }

    public void setNoticeBoardList(ArrayList<NoticeBoardItemModel> noticeBoardList) {
        this.noticeBoardList = noticeBoardList;
    }

    static public NoticeBoardResponseModel create(String serializedData) {
        // Use GSON to instantiate this class using the JSON representation of the state
        Gson gson = new Gson();
        return gson.fromJson(serializedData, NoticeBoardResponseModel.class);
    }
    public String serialize() {
        // Serialize this class into a JSON string using GSON
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
