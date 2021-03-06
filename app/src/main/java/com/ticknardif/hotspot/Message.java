package com.ticknardif.hotspot;

import java.util.Date;

public class Message {


    private int m_id;
    private String DisplayName;
    private int Room_id;
    private int User_id;
    private Date TimeStamp;
    private String Message;
    private boolean success;
    boolean owned;

    public Message(String displayName, int m_id, int room_id, int user_id, Date timeStamp, String message, boolean success) {
        this.m_id = m_id;
        Room_id = room_id;
        User_id = user_id;
        TimeStamp = timeStamp;
        Message = message;
        this.success = success;
        this.DisplayName = displayName;

        owned = false;
    }

    @Override
    public String toString() {
        return "Message{" +
                "Name: " + DisplayName +
                "m_id=" + m_id +
                ", Room_id=" + Room_id +
                ", User_id=" + User_id +
                ", TimeStamp=" + TimeStamp +
                ", Message='" + Message + '\'' +
                ", success=" + success +
                '}';
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
    public boolean isSuccess() { return success;}
    public String getDisplayName() {
        return DisplayName;
    }
    public void setDisplayName(String displayName) {
        DisplayName = displayName;
    }

    public void setOwned(boolean owned) {
        this.owned = owned;
    }
}
