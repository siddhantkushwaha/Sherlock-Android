package com.sherlock.androidapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class AddItem extends AppCompatActivity {

    private int PICK_IMAGE = 1;
    private int MAX_SIZE = 500;

    private ImageView itemImage;
    private EditText itemName;

    private ItemData itemData;

    private GoogleMap mMap;
    private Boolean mLocationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        itemImage = findViewById(R.id.itemImage);
        itemName = findViewById(R.id.itemName);

        mLocationPermissionGranted = false;
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        itemData = new ItemData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == UCrop.REQUEST_CROP) {

                Uri croppedUri = UCrop.getOutput(data);
                System.out.println(croppedUri);
                handleCropResult(croppedUri);
            } else if (requestCode == PICK_IMAGE) {

                Uri selectedUri = data.getData();
                System.out.println(selectedUri);
                startCrop(selectedUri);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {


        mLocationPermissionGranted = false;
        switch (requestCode) {
            case 0: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }

        if (mLocationPermissionGranted)
            getDeviceLocation();
    }

    public void submit(View view) {

        if (itemData.getItemPhoto() == null || itemName.getText().toString().length() == 0)
            return;

        startSaving();
    }

    public void uploadImage(View view) {
        openImageIntent();
    }

    private void startCrop(@NonNull Uri uri) {


        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), "item_image.png")));
        uCrop.withMaxResultSize(MAX_SIZE, MAX_SIZE);
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        uCrop.withOptions(options);
        uCrop.start(this);
    }

    private void handleCropResult(Uri uri) {

        itemData.setItemPhoto(uri.toString());
    }


    private void openImageIntent() {

        ArrayList<Intent> otherOptions = new ArrayList<Intent>();

        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);


        Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, otherOptions.toArray(new Parcelable[otherOptions.size()]));
        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    private void startSaving() {

        DateTime now = DateTime.now();
        itemData.setItemName(itemName.getText().toString());
        itemData.setTimestamp(now.toString());

        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String key = FirebaseRTDB.getInstance().getReference("users/" + uid).push().getKey();
        itemData.setItemId(key);

        FirebaseRTDB.getInstance().getReference("users/" + uid + "/" + key).setValue(itemData);


        StorageReference ref = FirebaseStorage.getInstance().getReference(uid + "/itemImage/" + key + ".png");
        UploadTask uploadTask = ref.putFile(Uri.parse(itemData.getItemPhoto()));
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                task.addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if (task.isSuccessful()) {

                            String downloadUrl = task.getResult().toString();

                            itemData.setItemPhoto(downloadUrl);
                            FirebaseRTDB.getInstance().getReference("users/" + uid + "/" + key).setValue(itemData);

                            CommonUtils.loadImage(AddItem.this, downloadUrl, new RequestOptions(), itemImage);

                        } else if (task.getException() != null) {
                            Log.i(AddItem.class.toString(), task.getException().toString());
                            Toast.makeText(AddItem.this, "Failed to add item.", Toast.LENGTH_LONG).show();
                            FirebaseRTDB.getInstance().getReference("users/" + uid + "/" + key).setValue(null);
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Log.i(AddItem.class.toString(), e.toString());
                Toast.makeText(AddItem.this, "Failed to add item.", Toast.LENGTH_LONG).show();
                FirebaseRTDB.getInstance().getReference("users/" + uid + "/" + key).setValue(null);
            }
        });
    }

    public void addLocation(View view) {

        if (mLocationPermissionGranted)
            getDeviceLocation();
        else
            getLocationPermission();
    }

    private void getDeviceLocation() {

        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {

                            android.location.Location mLastKnownLocation = (android.location.Location) task.getResult();
                            if (mLastKnownLocation == null)
                                return;
                            LatLng latLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());

                            Location location = new Location(latLng);
                            itemData.setLocation(location);

                            if (itemData.getItemId() != null)
                                FirebaseRTDB.getInstance().getReference("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + itemData.getItemId()).setValue(null);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getDeviceLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }
    }
}
