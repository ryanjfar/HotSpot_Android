package com.example.ticknardif.hotspot;

import java.util.Date;

public class Message {
    private int m_id;

    private int Room_id;
    private int User_id;
    private Date TimeStamp;

    private String Message;

    public Message(int m_id, int room_id, int user_id, Date timeStamp, String message) {
        this.m_id = m_id;
        Room_id = room_id;
        User_id = user_id;
        TimeStamp = timeStamp;
        Message = message;
    }

    public int getM_id() {
        return m_id;
    }

    public int getRoom_id() {
        return Room_id;
    }

    public int getUser_id() {
        return User_id;
    }

    public Date getTimeStamp() {
        return TimeStamp;
    }

    public String getMessage() {
        return Message;
    }
}
