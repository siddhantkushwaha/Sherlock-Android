package com.sherlock.androidapp;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.GridView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView navigationView;
    private DrawerLayout mDrawerLayout;

    GridView grid;
    ImageAdapter imageAdapter;
    ArrayList<ItemData> itemDataArrayList;

    DatabaseReference databaseReference;
    private ChildEventListener itemEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        navigationView = findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_logout) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.action_show_all_items) {
                    Intent intent = new Intent(HomeActivity.this, MapActivity.class);
                    startActivity(intent);
                }
                return false;
            }
        });

        grid = findViewById(R.id.grid);
        itemDataArrayList = new ArrayList<>();
        imageAdapter = new ImageAdapter(HomeActivity.this, itemDataArrayList);
        grid.setAdapter(imageAdapter);


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
                imageAdapter.notifyDataSetChanged();
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
                imageAdapter.notifyDataSetChanged();
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
                imageAdapter.notifyDataSetChanged();
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

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    public void addItem(View view) {
        Intent intent = new Intent(HomeActivity.this, AddItem.class);
        startActivity(intent);
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
}
