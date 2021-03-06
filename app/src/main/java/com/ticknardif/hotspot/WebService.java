package com.ticknardif.hotspot;

import com.ticknardif.hotspot.RESTresponses.ChatRoomCreationResponse;
import com.ticknardif.hotspot.RESTresponses.ChatroomResponse;
import com.ticknardif.hotspot.RESTresponses.ChatroomUserResponse;
import com.ticknardif.hotspot.RESTresponses.CreateChatroomResponse;
import com.ticknardif.hotspot.RESTresponses.GCMResponse;
import com.ticknardif.hotspot.RESTresponses.JoinChatroomResponse;
import com.ticknardif.hotspot.RESTresponses.LeaveChatroomResponse;
import com.ticknardif.hotspot.RESTresponses.LoginResponse;
import com.ticknardif.hotspot.RESTresponses.LogoutResponse;
import com.ticknardif.hotspot.RESTresponses.UpdateLocationResponse;
import com.ticknardif.hotspot.RESTresponses.UserResponse;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

public interface WebService {
    @FormUrlEncoded
    @POST("/api/login")
    void login(@Field("email_id") String email, @Field("password") String password, Callback<LoginResponse> res);

    @FormUrlEncoded
    @POST("/api/users")
    void createUser(@Field("email_id") String email, @Field("password") String password,
                    @Field("displayname") String name, @Field("radius") double radius,
                    @Field("longitude") double longitude,@Field("latitude") double latitude, Callback<UserResponse> res);

    @FormUrlEncoded
    @POST("/api/chatroom")
    void createChatroom(@Field("room_admin") String email, @Field("chat_title") String password,
                        @Field("displayname") String name, @Field("chat_dscrpn") double radius,
                        @Field("longitude") double longitude,@Field("latitude") double latitude,
                        @Field("session_id") String session_id, Callback<ChatRoomCreationResponse> res);


    @FormUrlEncoded
    @POST("/api/gcm")
    void regGCM(@Field("session_id") String session_id,@Field("reg_id") String reg_id, Callback<GCMResponse> res);

    @GET("/api/chatroomusers/user_id/{session_id}")
    void getJoinedChatrooms(@Path("session_id") String session_id, Callback<List<ChatroomUserResponse>> res);

    @GET("/api/chatroom/{session_id}")
    void getChatrooms(@Path("session_id") String session_id, Callback<List<ChatroomResponse>> res);

    @FormUrlEncoded
    @POST("/api/chatroom")
    void createChatroom(@Field("room_admin") int roomAdmin, @Field("latitude") double latitude, @Field("longitude") double longitude, @Field("chat_title") String chat_title, @Field("chat_dscrpn") String chat_dscrpn, @Field("session_id") String session_id, Callback<CreateChatroomResponse> res);

    @FormUrlEncoded
    @POST("/api/messages")
    void sendMessage(@Field("session_id") String session_id,@Field("room_id") int room_id, @Field("message") String message, Callback<Message> res);

    @GET("/api/messages/room_id/{room_id}/{session_id}")
    void getMessages(@Path("room_id") int room_id,@Path("session_id") String session_id, Callback<List<Message>> res);

    @GET("/api/messages/room_id/{room_id}/{timestamp}/{session_id}")
    void getLatestMessages(@Path("room_id") int room_id,@Path("timestamp") String timestamp,@Path("session_id") String session_id, Callback<List<Message>> res);

    @FormUrlEncoded
    @POST("/api/chatroomusers/")
    void joinChatroom(@Field("room_id") int roomId, @Field("session_id") String sessionId, Callback<JoinChatroomResponse> res);

    @FormUrlEncoded
    @POST("/api/chatroomusers/delete")
    void leaveChatroom(@Field("room_id") int roomId, @Field("session_id") String sessionId, Callback<LeaveChatroomResponse> res);

    @GET("/api/users/{session_id}")
    void getUser(@Path("session_id") String session_id, Callback<UserResponse> res);

    @FormUrlEncoded
    @POST("/api/logout/")
    void logout(@Field("email_id") String email_id, Callback<LogoutResponse> res);


    @FormUrlEncoded
    @POST("/api/updatelocation")
    void updateLocation(@Field("latitude") double latitude, @Field("longitude") double longitude, @Field("session_id") String session_id, Callback<UpdateLocationResponse> res);

}
