package ru.ifmo.android_2015.citycam;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.gson.Gson;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.Locale;

import ru.ifmo.android_2015.citycam.api.RestClient;
import ru.ifmo.android_2015.citycam.fragments.OnFragmentInteractionListener;
import ru.ifmo.android_2015.citycam.fragments.SelectCityFragment;
import ru.ifmo.android_2015.citycam.list.CitySelectedListener;
import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.model.WebCamsResult;

public class SelectCityActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    public static final String TAG = "CityCam";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);
        openFragment(new SelectCityFragment());
    }

    public void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.content, fragment).addToBackStack(null).commit();
    }

    @Override
    public void onFragmentInteraction(String id) {
        Log.d(TAG, "onFragmentInteraction - id="+id);
    }
}
