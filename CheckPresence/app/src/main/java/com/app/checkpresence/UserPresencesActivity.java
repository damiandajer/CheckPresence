package com.app.checkpresence;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.app.database.DataBase;

import java.util.List;

public class UserPresencesActivity extends AppCompatActivity {

    private static DataBase dataBase;
    private ListView listView;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_presences);

        openDB();

        List<Classes> classes = dataBase.getAllClassses();
        spinner = (Spinner) findViewById(R.id.group_spinner);
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item,
                classes);

        spinner.setAdapter(adapter);

        listView = (ListView) findViewById(R.id.list_presence);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshClasses();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    private void refreshClasses(){
        Classes cl = (Classes)spinner.getSelectedItem();


        ArrayAdapter adapterList = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item,
                dataBase.getPresence(cl.getId()));

        listView.setAdapter(adapterList);
    }

    private void openDB() {
        dataBase = new DataBase(this);
        dataBase.open();
    }

}
