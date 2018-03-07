package interfaces;

import java.util.ArrayList;
import java.util.List;

import models.LoginBody;
import models.Site;
import models.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by valentin on 04/03/2018.
 */

public interface UserOperations {

    public static final String BASE_URL = "http://desarrollovh.lmvho";

    @Headers({
            "Content-Type: application/json"
    })
    @POST("api/login")
    Call<User> login(@Body LoginBody loginBody);

    @GET("api/sites")
    Call<ArrayList<Site>> getSites(@Header("Authorization") String authorization, @Header("Accept") String accept);
}
