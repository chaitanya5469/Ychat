package chaitu.android.ychat.notificationUtil;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import chaitu.android.ychat.notificationUtil.ApiClient;
import chaitu.android.ychat.notificationUtil.ApiService;
import okhttp3.MediaType;
import retrofit2.Call;
import retrofit2.Callback;

public class FCMNotificationUtil {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static void sendNotification(Context context, String messageBody) {
        HashMap<String,String>headers=new HashMap<>();
        headers.put("Authorization","key=YOUR_SERVER_KEY");
        headers.put("Content-Type","application/json");
        ApiClient.getClient().create(ApiService.class).sendMessage(headers,messageBody).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJSON=new JSONObject(response.body());
                            JSONArray result=responseJSON.getJSONArray("results");
                            if(responseJSON.getInt("failure")==1){
                                JSONObject error= (JSONObject) result.get(0);
                                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }

                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(context, "Error1"+response.toString(), Toast.LENGTH_SHORT).show();
                    Log.d("error",response.toString());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(context, "Error"+t.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }


}

