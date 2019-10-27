package ca.uqac.alterra;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String CHANNEL_ID = "ca.uqac.alterra.notifications";

    private MapsHandler mMapsHandler;
    private BottomSheetHandler mBottomSheetHandler;
    private FloatingActionButton mCameraButton;
    private PhotoUploader mPhotoUploader;
    private String mCurrentImagePath;



    private FirebaseAuth mAuth;
    private NavigationView mNavigationView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setNavigationViewListener();
        mAuth = FirebaseAuth.getInstance();

    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mMapsHandler = new MapsHandler(this);
        mBottomSheetHandler = new BottomSheetHandler(this);
        mPhotoUploader = new PhotoUploader(getResources().getString(R.string.firebaseBucket), this);
        mCameraButton = findViewById(R.id.cameraButton);
        mCameraButton.setOnClickListener((view) -> dispatchTakePictureIntent());

        //Notification setup
        createNotificationChannel();

        //Monitoring the bottom panel movements
        BottomSheetBehavior bottomPanelBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomPanel));
        bottomPanelBehavior.addBottomSheetCallback(mBottomSheetHandler);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(mMapsHandler);

        DrawerLayout navDrawer = findViewById(R.id.navDrawer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,navDrawer,toolbar,R.string.app_name,R.string.app_name);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.colorPrimaryDark));
        navDrawer.addDrawerListener(toggle);
        toggle.syncState();

        if (currentUser != null) {
            View headerView = mNavigationView.getHeaderView(0);
            TextView navUsername = (TextView) headerView.findViewById(R.id.navUsername);
            navUsername.setText(currentUser.getEmail()); //TODO : change by the login (need to be availible in register)
        } else {
            //TODO : how to handle this kind of error ?!
        }

    }

    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // TODO Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "ca.uqac.alterra.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                System.out.println("Photo saved as" + mCurrentImagePath);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        mCurrentImagePath = image.getAbsolutePath();
        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK){
                    mPhotoUploader.uploadPhoto(mCurrentImagePath);
                }
                break;
        }

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void setNavigationViewListener() {
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_item_profile :
                Toast toastProfile = Toast.makeText(getApplicationContext(), "Profile", Toast.LENGTH_LONG);
                toastProfile.show();
                return true;
            case R.id.nav_item_pictures :
                Toast toastPictures = Toast.makeText(getApplicationContext(), "Pictures", Toast.LENGTH_LONG);
                toastPictures.show();
                startActivity(new Intent(this, PicturesActivity.class));
                finish();
                return true;
            case R.id.nav_item_places :
                Toast toastPlaces = Toast.makeText(getApplicationContext(), "Places", Toast.LENGTH_LONG);
                toastPlaces.show();
                return true;
            case R.id.nav_item_settings :
                Toast toastSettings = Toast.makeText(getApplicationContext(), "Settings", Toast.LENGTH_LONG);
                toastSettings.show();
                return true;
            case R.id.nav_item_about :
                Toast toastAbout = Toast.makeText(getApplicationContext(), "About", Toast.LENGTH_LONG);
                toastAbout.show();
                return true;
            case R.id.nav_item_logout :
                mAuth.signOut();
                startActivity(new Intent(this, AuthActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);


        }
    }
}