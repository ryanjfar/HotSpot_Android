package com.example.ticknardif.hotspot;

/**
 * Created by Vatsal on 11/8/2014.
 */
public class ChatRoomCreationResponse {
    int Room_Admin;
    int Latitude;
    int Longitude;
    int chat_id;
    String Chat_title;
    String Chat_Dscrpn;

    public ChatRoomCreationResponse(int room_Admin, int latitude, int longitude, int chat_id, String chat_title, String chat_Dscrpn) {
        Room_Admin = room_Admin;
        Latitude = latitude;
        Longitude = longitude;
        this.chat_id = chat_id;
        Chat_title = chat_title;
        Chat_Dscrpn = chat_Dscrpn;
    }

    @Override
    public String toString() {
        return "ChatRoomCreationResponse{" +
                "Room_Admin=" + Room_Admin +
                ", Latitude=" + Latitude +
                ", Longitude=" + Longitude +
                ", chat_id=" + chat_id +
                ", Chat_title='" + Chat_title + '\'' +
                ", Chat_Dscrpn='" + Chat_Dscrpn + '\'' +
                '}';
    }
}
