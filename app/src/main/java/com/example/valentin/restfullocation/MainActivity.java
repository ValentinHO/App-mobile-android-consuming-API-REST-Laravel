package com.example.valentin.restfullocation;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import fragments.CheckFragment;
import fragments.HomeFragment;
import utilities.SessionPrefs;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences mPrefs;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private HomeFragment fragmentHome;
    private CheckFragment fragmentCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isLogging();

        initToolbar();
        initComponents();
        infoUser();

    }

    private void initToolbar(){
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initComponents(){
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        navigationView = (NavigationView)findViewById(R.id.navigation_view);

        if(navigationView != null)
            setupDrawerContent(navigationView);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close)
        {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        cargarFragment(getFragmentHome());
    }

    private void setupDrawerContent(NavigationView navigationView)
    {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener()
                {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item)
                    {
                        drawerLayout.closeDrawers();

                        switch (item.getItemId())
                        {
                            case R.id.nav_home:
                                item.setChecked(true);
                                if(getSupportActionBar().getTitle() != "Inicio")
                                    cargarFragment(getFragmentHome());
                                break;

                            case R.id.nav_location:
                                item.setChecked(true);
                                if(getSupportActionBar().getTitle() != "Check in/out")
                                    cargarFragment(getFragmentCheck());
                                break;

                            case R.id.nav_logout:
                                SessionPrefs.get(MainActivity.this).logOut();
                                isLogging();
                                break;
                        }
                        return true;
                    }
                }
        );
    }

    private void cargarFragment(Fragment fragmento)
    {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.main_content,fragmento);
        ft.commit();
    }

    private HomeFragment getFragmentHome()
    {
        if(fragmentHome == null)fragmentHome = new HomeFragment();
        return fragmentHome;
    }

    private CheckFragment getFragmentCheck()
    {
        if(fragmentCheck == null)fragmentCheck = new CheckFragment();
        return fragmentCheck;
    }

    private void infoUser() {
        mPrefs = getApplicationContext()
                .getSharedPreferences(SessionPrefs.PREFS_NAME, Context.MODE_PRIVATE);

        View hView = navigationView.getHeaderView(0);
        TextView nombre = (TextView) hView.findViewById(R.id.tvUserSession);
        TextView correo = (TextView) hView.findViewById(R.id.tvEmailSession);

        nombre.setText(mPrefs.getString(SessionPrefs.PREF_USER_NAME,"User Name"));
        correo.setText(mPrefs.getString(SessionPrefs.PREF_USER_EMAIL,"email@example.com"));
    }

    private void isLogging(){
        if (!SessionPrefs.get(this).isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
    }

    private void exitApp()
    {
        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        alerta.setTitle("Confirmación");
        alerta.setMessage("¿Deseas salir de la aplicación?");
        alerta.setNegativeButton("No", null);
        alerta.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface arg0, int arg1)
            {
                MainActivity.this.finish();
            }
        });
        alerta.show();
    }

    public boolean onKeyDown(int keyCode, android.view.KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (getFragmentManager().getBackStackEntryCount() == 0) {
                exitApp();
            } else {
                getFragmentManager().popBackStack();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
