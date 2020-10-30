package com.duncboi.realsquabble.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
           "Content-Type:application/json",
           "Authorization:key=AAAAVs1i6-A:APA91bGSx_UrwiEaHoPGKK8MYazOJBF60dGIgKD6ob-AnMuQHc4TC5Z-kY00v1tehpDC36pKXc2jsgxioJNxhIaW8if-BajsnVfyOlFdG30OmBzSd222IT7_qTNbQyny5cCMt2CtetEL"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotificaiton(@Body Sender body);
}
