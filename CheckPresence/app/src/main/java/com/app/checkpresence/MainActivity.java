package com.app.checkpresence;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.app.database.AndroidDatabaseManager;
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
        drawerToggle = setupDrawerToggle();

        // Find our drawer view
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(nvDrawer);

        //mDrawer.addDrawerListener(drawerToggle);

        openCamera();

        if(mCamera != null) {
            mCameraView = new CameraView(this, this, this, mCamera);//create a SurfaceView to show camera data
            FrameLayout camera_view = (FrameLayout)findViewById(R.id.camera_view);
            camera_view.addView(mCameraView);//add the SurfaceView to the layout
        }

        openDB();
        //restoreActionBar();
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open,  R.string.drawer_close);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // NOTE! Make sure to override the method with only a single `Bundle` argument
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
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
            case R.id.nav_Dodaj_grupę:
                mCameraView.stopPreview();
                addGroupDialog();
                //fragmentClass = AddUserActivity.class;
                break;
            case R.id.nav_Dodaj_zajęcia:
                mCameraView.stopPreview();
                addClassesDialog();
                //fragmentClass = AddUserActivity.class;
                break;
            case R.id.nav_obecnosci:
                mCameraView.stopPreview();
                startActivityUserPresences();
                //fragmentClass = ThirdFragment.class;
                break;
            case R.id.nav_Database_Manager:
                //fragmentClass = AndroidDatabaseManager.class;
                mCameraView.stopPreview();
                startDatabaseManager();
                break;
            case R.id.nav_load_database:
                menuItem.setChecked(false);
                mCameraView.stopPreview();
                CopyManager.loadBackupOfDatabase(context);
                mCameraView.startPreview();
                break;
            case R.id.nav_create_copy_database:
                menuItem.setChecked(false);
                mCameraView.stopPreview();
                CopyManager.addCopyOfDatabase(context);
                mCameraView.startPreview();
                break;
            default:
                fragmentClass = MainActivity.class;
        }
/*
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
*/
        // Insert the fragment by replacing any existing fragment
        //FragmentManager fragmentManager = getSupportFragmentManager();
        //fragmentManager.beginTransaction().replace(R.id.main_frame, fragment).commit();
        /*LayoutInflater inflater = getLayoutInflater();
        FrameLayout container = (FrameLayout) findViewById(R.id.main_frame);
        inflater.inflate(R.layout.activity_main, container);*/

        // Highlight the selected item has been done by NavigationView
        //menuItem.setChecked(true);
        // Set action bar title
        //setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }

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

    public void startActivityUserPresences(){
        Intent intent = new Intent(this, UserPresencesActivity.class);
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

    public void addGroupDialog(){
        MyAlertDialog myAlertDialog = new MyAlertDialog();
        myAlertDialog.setMainActivity(this);
        myAlertDialog.setContext(this);
        myAlertDialog.addGroupDialog();
    }

    public void addClassesDialog(){
        MyAlertDialog myAlertDialog = new MyAlertDialog();
        myAlertDialog.setMainActivity(this);
        myAlertDialog.setContext(this);
        myAlertDialog.addClassesDialog();
    }
}
