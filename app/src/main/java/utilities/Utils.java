package utilities;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Patterns;

import com.example.valentin.restfullocation.LoginActivity;

import java.util.ArrayList;
import java.util.List;

import models.Site;

/**
 * Created by valentin on 04/03/2018.
 */

public class Utils {

    public static ArrayList<Site> sitios = new ArrayList<Site>();

    public static String validacion(String email, String password){
        if (TextUtils.isEmpty(email)) {
            return "El campo email es obligatorio";
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            return "Email inválido";
        }

        if (TextUtils.isEmpty(password)) {
            return "El campo password es obligatorio";
        }
        return "";
    }

    public static boolean isOnline(Activity login) {
        ConnectivityManager cm =
                (ConnectivityManager) login.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
