package ru.ifmo.android_2015.citycam.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import ru.ifmo.android_2015.citycam.R;
import ru.ifmo.android_2015.citycam.list.CitiesRecyclerAdapter;
import ru.ifmo.android_2015.citycam.list.CitySelectedListener;
import ru.ifmo.android_2015.citycam.list.RecylcerDividersDecorator;
import ru.ifmo.android_2015.citycam.model.City;

public class MainActivity extends AppCompatActivity implements CitySelectedListener {

    private RecyclerView recyclerView;

    /**
     * The Adapter which will be used to populate the RecyclerView with Views.
     */
    private CitiesRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);

        adapter = new CitiesRecyclerAdapter(getApplicationContext());
        adapter.setCitySelectedListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new RecylcerDividersDecorator(Color.DKGRAY));

        // Set the adapter
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCitySelected(City city) {
        Log.i("CityCam", "onCitySelected: " + city);
        Intent cityCam = new Intent(this, CityCamActivity.class);
        cityCam.putExtra(CityCamActivity.EXTRA_CITY, city);
        startActivity(cityCam);
    }
}
