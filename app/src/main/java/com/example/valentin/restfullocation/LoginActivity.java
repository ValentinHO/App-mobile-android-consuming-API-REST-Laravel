package com.example.valentin.restfullocation;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import utilities.SessionPrefs;
import utilities.Utils;
import interfaces.UserOperations;
import models.ApiError;
import models.LoginBody;
import models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText txtEmail;
    private EditText txtPassword;
    private TextView tvErrors;
    private ProgressBar progressBar;
    private Button btnLogin;
    private Retrofit retrofit;
    private UserOperations userOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        isLogging();

        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        tvErrors = (TextView) findViewById(R.id.txtError);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);

        retrofit = new Retrofit.Builder()
                .baseUrl(UserOperations.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        userOperations = retrofit.create(UserOperations.class);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btnLogin:
                login();
                break;
            default:
                break;
        }
    }

    private void login() {

        if (!Utils.isOnline(this)) {
            Toast.makeText(this,"No hay conexión de red",Toast.LENGTH_LONG).show();
            return;
        }

        String email = String.valueOf(txtEmail.getText());
        String password = String.valueOf(txtPassword.getText());

        String errors = Utils.validacion(email,password);

        if (!TextUtils.isEmpty(errors)){
            tvErrors.setText(errors);
            tvErrors.setVisibility(View.VISIBLE);
        }
        else {
            showProgress(true);
            tvErrors.setText(errors);
            tvErrors.setVisibility(View.GONE);

            Call<User> loginCall = userOperations.login(new LoginBody(email, password));
            loginCall.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    showProgress(false);

                    // Procesar errores
                    try{
                        if (!response.isSuccessful()) {
                            String error;
                            if (response.errorBody()
                                    .contentType()
                                    .subtype()
                                    .equals("application/json")) {
                                ApiError apiError = ApiError.fromResponseBody(response.errorBody());

                                error = apiError.getMessage();
                                Log.d("LoginActivity", apiError.getDeveloperMessage());
                            } else {
                                error = response.message();
                            }

                            Toast.makeText(LoginActivity.this,"Error: "+error,Toast.LENGTH_LONG).show();
                            return;
                        }
                    }catch(Exception ex){
                        Log.d("Error catch",ex.getMessage());
                    }


                    // Guardar usuario en preferencias
                    SessionPrefs.get(LoginActivity.this).saveUser(response.body());

                    // Ir a menu principal
                    showAppointmentsScreen();
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    showProgress(false);
                    Toast.makeText(LoginActivity.this,"Fallo: "+t.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    private void showAppointmentsScreen() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void isLogging(){
        if (SessionPrefs.get(this).isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
    }


}
