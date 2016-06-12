package com.app.checkpresence;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.app.database.DataBase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Damian on 01.06.2016.
 */
public class MyAlertDialog {
    private Context context;
    private CameraView mCameraView;
    private AddUserCameraView addUserCameraView;
    private LayoutInflater li;
    private View promptsView;
    private AlertDialog.Builder alertDialogBuilder;
    private List<String> listOfUsers, listOfGroups;
    private MainActivity mainActivity;
    private AddUserActivity addUserActivity;
    private DataBase dataBase;
    private List<float[]> actualHandFeatures;

    public MyAlertDialog(){}

    public void pushFoundUserToScreen(){
        //final List<Integer> usersListFinal = usersList;
        li = LayoutInflater.from(getContext());
        promptsView = li.inflate(R.layout.found_user, null);

        alertDialogBuilder = new AlertDialog.Builder(getContext());

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
        //alertDialogBuilder.setTitle("Znaleziono użytkownika");

        final TextView foundUser = (TextView) promptsView
                .findViewById(R.id.foundUserTextView);

        foundUser.setText(listOfUsers.get(0));

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                getmCameraView().startPreview();
                                getmCameraView().startAutoExposure(200);
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
                                getmCameraView().startPreview();
                                getmCameraView().startAutoExposure(200);
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Anuluj",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                getmCameraView().startPreview();
                                getmCameraView().startAutoExposure(200);
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void addGroupDialog(){
        this.dataBase = new DataBase(getContext());
        //final List<Integer> usersListFinal = usersList;
        li = LayoutInflater.from(getContext());
        promptsView = li.inflate(R.layout.set_new_group, null);

        alertDialogBuilder = new AlertDialog.Builder(getContext());

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText groupName = (EditText) promptsView
                .findViewById(R.id.EditTextGetGroupName);
        groupName.setHint("Nazwa grupy");

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                addGroupToDatabase(groupName);
                                getmCameraView().startPreview();
                                getmCameraView().startAutoExposure(200);
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Anuluj",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mCameraView.startPreview();
                                getmCameraView().startAutoExposure(200);
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void addClassesDialog(){
        this.dataBase = new DataBase(getContext());
        //final List<Integer> usersListFinal = usersList;
        li = LayoutInflater.from(getContext());
        promptsView = li.inflate(R.layout.set_new_classes, null);

        alertDialogBuilder = new AlertDialog.Builder(getContext());

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        setListOfGroups();

        final Spinner usersSpinner = (Spinner) promptsView
                .findViewById(R.id.usersSpinner);
        ArrayAdapter<String> usersListAdapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, listOfGroups);
        usersListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        usersSpinner.setAdapter(usersListAdapter);
        usersSpinner.setSelection(0);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                addClassesToDatabase(usersSpinner);
                                getmCameraView().startPreview();
                                getmCameraView().startAutoExposure(200);
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Anuluj",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mCameraView.startPreview();
                                getmCameraView().startAutoExposure(200);
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void addUserData(){
        this.dataBase = new DataBase(getContext());
        li = LayoutInflater.from(getContext());
        promptsView = li.inflate(R.layout.get_user_data, null);

        alertDialogBuilder = new AlertDialog.Builder(getContext());

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        setListOfGroups();

        final EditText userInputFirstName = (EditText) promptsView
                .findViewById(R.id.EditTextGetFirstName);
        userInputFirstName.setHint("Imię");
        final EditText userInputSecondName = (EditText) promptsView
                .findViewById(R.id.EditTextGetSecondName);
        userInputSecondName.setHint("Nazwisko");
        final EditText userInputIndex = (EditText) promptsView
                .findViewById(R.id.EditTextGetIndex);
        userInputIndex.setHint("Nr albumu");

        final Spinner usersSpinner = (Spinner) promptsView
                .findViewById(R.id.usersSpinner);
        ArrayAdapter<String> usersListAdapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, listOfGroups);
        usersListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        usersSpinner.setAdapter(usersListAdapter);
        usersSpinner.setSelection(0);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                addUserCameraView.setFirstName(userInputFirstName.getText().toString());
                                addUserCameraView.setSecondName(userInputSecondName.getText().toString());
                                addUserCameraView.setIndexUser(Integer.valueOf(userInputIndex.getText().toString()));
                                addUserCameraView.setGroupName(usersSpinner.getSelectedItem().toString());
                                addUserCameraView.createUser();
                                addUserCameraView.closeActivity();
                            }
                        })
                .setNegativeButton("Anuluj",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                addUserCameraView.closeActivity();
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

    private void addGroupToDatabase(EditText groupName){
        dataBase.insertGroup(groupName.getText().toString());
    }

    private void addClassesToDatabase(Spinner groupsSpinner){
        Date date = Calendar.getInstance().getTime();
        dataBase.insertClass(groupsSpinner.getSelectedItem().toString(), date);
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

    public void setAddUserCameraView(AddUserCameraView mCameraView) {
        this.addUserCameraView = mCameraView;
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

    public void setAddUserActivity(AddUserActivity addUserActivity) {
        this.addUserActivity = addUserActivity;
    }

    public void setActualHandFeatures(List<float[]> actualHandFeatures) {
        this.actualHandFeatures = actualHandFeatures;
    }

    private void setListOfGroups(){
        this.listOfGroups = dataBase.getAllGroups();
    }
}
