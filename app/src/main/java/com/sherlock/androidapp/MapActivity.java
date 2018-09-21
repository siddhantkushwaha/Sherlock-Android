package com.sherlock.androidapp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity {

    private GoogleMap mMap;

    DatabaseReference databaseReference;
    private ChildEventListener itemEventListener;

    ArrayList<ItemData> itemDataArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        itemDataArrayList = new ArrayList<>();

        databaseReference = FirebaseRTDB.getInstance().getReference("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
        itemEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                ItemData itemData = null;
                try {
                    itemData = dataSnapshot.getValue(ItemData.class);

                } catch (Exception e) {
                    Log.e(HomeActivity.class.toString(), e.toString());
                }

                if (itemData == null)
                    return;

                int pos = findInItemList(itemData.getItemId());
                if (pos == -1)
                    itemDataArrayList.add(itemData);
                plotMap();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ItemData itemData = null;
                try {
                    itemData = dataSnapshot.getValue(ItemData.class);

                } catch (Exception e) {
                    Log.e(HomeActivity.class.toString(), e.toString());
                }

                if (itemData == null)
                    return;

                int pos = findInItemList(itemData.getItemId());
                if (pos != -1)
                    itemDataArrayList.get(pos).clone(itemData);
                plotMap();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                ItemData itemData = null;
                try {
                    itemData = dataSnapshot.getValue(ItemData.class);

                } catch (Exception e) {
                    Log.e(HomeActivity.class.toString(), e.toString());
                }

                if (itemData == null)
                    return;

                int pos = findInItemList(itemData.getItemId());
                if (pos != -1)
                    itemDataArrayList.remove(pos);
                plotMap();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        databaseReference.addChildEventListener(itemEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseReference.removeEventListener(itemEventListener);
    }

    private int findInItemList(String itemId) {
        int idx = -1;
        for (int i = 0; i < itemDataArrayList.size(); i++) {
            String id = itemDataArrayList.get(i).getItemId();
            if (id != null && id.equals(itemId)) {
                idx = i;
                break;
            }
        }
        return idx;
    }

    private void plotMap() {

        if (mMap == null)
            return;

        mMap.clear();

        if (itemDataArrayList.isEmpty())
            return;

        LatLng latLng;
        for (ItemData itemData : itemDataArrayList) {

            latLng = new LatLng(itemData.getLocation().getLatitude(), itemData.getLocation().getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(itemData.getItemName());
            mMap.addMarker(markerOptions);
        }

        latLng = new LatLng(itemDataArrayList.get(0).getLocation().getLatitude(), itemDataArrayList.get(0).getLocation().getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }
}
