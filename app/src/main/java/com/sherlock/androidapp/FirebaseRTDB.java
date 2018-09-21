package com.sherlock.androidapp;

import com.google.firebase.database.FirebaseDatabase;

public class FirebaseRTDB {

    private static FirebaseDatabase mData;

    public static FirebaseDatabase getInstance() {
        if (mData == null) {

            mData = FirebaseDatabase.getInstance();
            mData.setPersistenceEnabled(true);
        }
        return mData;
    }
}
