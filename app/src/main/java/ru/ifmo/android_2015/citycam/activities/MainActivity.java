package ru.ifmo.android_2015.citycam.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import ru.ifmo.android_2015.citycam.Constants;
import ru.ifmo.android_2015.citycam.R;
import ru.ifmo.android_2015.citycam.fragments.OnFragmentInteractionListener;
import ru.ifmo.android_2015.citycam.fragments.SelectCityFragment;

public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener {
    private SelectCityFragment fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);
        if (savedInstanceState == null) {
            fragment = new SelectCityFragment();
            openFragment(fragment);
        }
    }

    public void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.content, fragment).commit();
    }

    @Override
    public void onFragmentInteraction(String id) {
        Log.d(Constants.TAG, "onFragmentInteraction - id="+id);
    }
}
