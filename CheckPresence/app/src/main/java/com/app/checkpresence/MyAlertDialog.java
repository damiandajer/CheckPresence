package com.app.checkpresence;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.app.database.DataBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Damian on 01.06.2016.
 */
public class MyAlertDialog {
    private Context context;
    private CameraView mCameraView;
    private LayoutInflater li;
    private View promptsView;
    private AlertDialog.Builder alertDialogBuilder;
    private List<String> listOfUsers;
    private MainActivity mainActivity;
    private DataBase dataBase;
    private List<float[]> actualHandFeatures;

    public MyAlertDialog(){}

    public void pushFoundUserToScreen(){
        System.out.println("pushfUtoscr");
        //final List<Integer> usersListFinal = usersList;
        li = LayoutInflater.from(getContext());
        promptsView = li.inflate(R.layout.found_user, null);

        alertDialogBuilder = new AlertDialog.Builder(getContext());

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView foundUser = (TextView) promptsView
                .findViewById(R.id.foundUserTextView);

        foundUser.setText(listOfUsers.get(0));

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                getmCameraView().startPreviewInCameraView();
                                //getmCameraView().startAutoExposure();
                                dialog.cancel();
                            }
                        })
                .setNeutralButton("Jestem nowy.",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mainActivity.startActivityAddNewUser();
                            dialog.cancel();
                        }
                    })
                .setNegativeButton("To nie ja!",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                pushListOfUsersToScreen();
                                //mCameraView.startPreviewInCameraView();

                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void pushListOfUsersToScreen(){
        this.dataBase = new DataBase(getContext());
        convertUsersListToListOfStrings(dataBase.getAllUsersAlbums());
        li = LayoutInflater.from(getContext());
        promptsView = li.inflate(R.layout.found_user_list, null);

        alertDialogBuilder = new AlertDialog.Builder(getContext());

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final Spinner usersSpinner = (Spinner) promptsView
                .findViewById(R.id.usersSpinner);
        ArrayAdapter<String> usersListAdapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, listOfUsers);
        usersListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        usersSpinner.setAdapter(usersListAdapter);
        usersSpinner.setSelection(0);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                addHandFeaturesToDatabase(usersSpinner);
                                getmCameraView().startPreviewInCameraView();
                                //getmCameraView().startAutoExposure();
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Anuluj",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mCameraView.startPreviewInCameraView();
                                //getmCameraView().startAutoExposure();
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void addHandFeaturesToDatabase(Spinner usersSpinner){
        String user = usersSpinner.getSelectedItem().toString();
        long idUser = dataBase.getUserId(Integer.valueOf(user));
        for (float[] traits:actualHandFeatures
                ) {
            dataBase.insertTraits(idUser, traits);
        }
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public CameraView getmCameraView() {
        return mCameraView;
    }

    public void setmCameraView(CameraView mCameraView) {
        this.mCameraView = mCameraView;
    }

    public void convertUsersListToListOfStrings(List<Integer> list){
        listOfUsers = new ArrayList<>();
        for (int i:list
             ) {
            listOfUsers.add(Integer.toString(i));
        }
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void setActualHandFeatures(List<float[]> actualHandFeatures) {
        this.actualHandFeatures = actualHandFeatures;
    }
}
