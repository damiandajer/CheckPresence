package com.app.checkpresence;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.app.database.DataBase;
import com.app.memory.CopyManager;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    private Camera mCamera = null;
    private CameraView mCameraView = null;
    private static DataBase dataBase;
    private Context context;
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;


    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    //private NavigationDrawerFragment mNavigationDrawerFragment;


    //private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Find our drawer view
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(nvDrawer);

        /*mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));*/

        openCamera();

        if(mCamera != null) {
            mCameraView = new CameraView(this, this, this, mCamera);//create a SurfaceView to show camera data
            FrameLayout camera_view = (FrameLayout)findViewById(R.id.camera_view);
            camera_view.addView(mCameraView);//add the SurfaceView to the layout
        }

        Button copyDatabaseButton = (Button) findViewById(R.id.loadDatabaseButton);
        copyDatabaseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mCameraView.stopPreview();
                CopyManager.addCopyOfDatabase(context);
                mCameraView.startPreview();
            }
        });

        Button loadDatabaseButton = (Button) findViewById(R.id.copyDatabaseButton);
        loadDatabaseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                /*mCameraView.stopPreview();
                CopyManager.loadBackupOfDatabase(context);
                mCameraView.startPreview();*/
                startActivityAddNewUser();
            }
        });

        openDB();
        //restoreActionBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // NOTE! Make sure to override the method with only a single `Bundle` argument
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass = null;
        switch(menuItem.getItemId()) {
            case R.id.nav_PaPa_Mobile:
                fragmentClass = MainActivity.class;
                break;
            case R.id.nav_Dodaj_użytkownika:
                //fragmentClass = AddUserActivity.class;
                startActivityAddNewUser();
                break;
            case R.id.nav_obecnosci:
                //fragmentClass = ThirdFragment.class;
                break;
            case R.id.nav_Kopia_zapasowa:
                //fragmentClass = ThirdFragment.class;
                break;
            case R.id.nav_Database_Manager:
                //fragmentClass = AndroidDatabaseManager.class;
                mCameraView.stopPreview();
                startDatabaseManager();
                break;
            default:
                fragmentClass = MainActivity.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        //FragmentManager fragmentManager = getSupportFragmentManager();
        //fragmentManager.beginTransaction().replace(R.id.main_frame, fragment).commit();
        /*LayoutInflater inflater = getLayoutInflater();
        FrameLayout container = (FrameLayout) findViewById(R.id.main_frame);
        inflater.inflate(R.layout.activity_main, container);*/

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }
/*
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.camera_view, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = "PaPa Mobile";
                break;
            case 2:
                mTitle = "Dodaj użytkownika";
                startActivityAddNewUser();
                break;
            case 3:
                mTitle = "Wyświetl obecności";
                break;
            case 4:
                mTitle = "Kopia zapasowa";
                break;
            case 5:
                mTitle = "Database Manager";
                mCameraView.stopPreview();
                startDatabaseManager();
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.rgb(0,168,0)));
    }

    /**
     * A placeholder fragment containing a simple view.
     */
 /*   public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
    /*    private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
     /*   public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
*/
    static {
        System.loadLibrary("native");
    }

    /**
     * Metoda tworzy i otwiera bazę danych
     */
    private void openDB() {
        dataBase = new DataBase(context);
        dataBase.open();

    }

    /**
     * Metoda zamyka aplikację i bazę danych
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataBase.close();
    }

    @Override
    public void onBackPressed(){
        //mCameraView.saveFeaturesToFile();
        if(mCamera != null)
            mCamera.release();
        System.exit(0);
    }

    @Override
    public void onResume(){
        super.onResume();
        //openCamera();
    }

    public void openCamera(){
        try{
            mCamera = Camera.open(1);//you can use open(int) to use different cameras
        } catch (Exception e){
            Log.d("ERROR", "Failed to get camera: " + e.getMessage());
        }
    }

    public static DataBase getDataBase(){
        return dataBase;
    }

    public void startActivityAddNewUser(){
        if(mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        mCameraView.setMarkerCameraNull();
        Intent intent = new Intent(this, AddUserActivity.class);
        startActivity(intent);
    }

    public void startDatabaseManager(){
        Intent intent = new Intent(this, AndroidDatabaseManager.class);
        startActivity(intent);
    }

    public void pushFoundUserToScreen(List<Integer> usersList, List<float[]> actualHandFeatures){
        MyAlertDialog myAlertDialog = new MyAlertDialog();
        myAlertDialog.setMainActivity(this);
        myAlertDialog.setContext(this);
        myAlertDialog.setmCameraView(mCameraView);
        myAlertDialog.setActualHandFeatures(actualHandFeatures);
        myAlertDialog.convertUsersListToListOfStrings(usersList);
        myAlertDialog.pushFoundUserToScreen();
    }
}
