package com.example.franciscojavier2.TalkTalk;

/**
 * Created by FranciscoJavier2 on 23/12/2017.
 */

public class FriendRequest {

    public String from, request_type;

    public FriendRequest(){}

    public FriendRequest(String from, String request_type){
        this.from=from;
        this.request_type=request_type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }
}
