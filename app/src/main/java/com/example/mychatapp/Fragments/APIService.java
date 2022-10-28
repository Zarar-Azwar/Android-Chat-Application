package com.example.mychatapp.Fragments;

import com.example.mychatapp.Notification.MyResonse;
import com.example.mychatapp.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA1AIZnFc:APA91bH1vlbukNLjoTmzUeB_qnPcrOXUVP9u_Jm5PLHkMAVPy7H1M76GeM2YvzI_vRSO06Om5e4xoskud1E6GnivaRoCvTjGQMPnIb8J3X-EVQUg1AvEEXDlhzfxASO0BOGsgF8DrrGB"
            }

    )
    @POST("fcm/send")
    Call<MyResonse> sendNotification(@Body Sender body);
}
