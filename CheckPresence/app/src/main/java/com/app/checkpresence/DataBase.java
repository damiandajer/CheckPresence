package com.app.checkpresence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Szymon on 2016-04-10.
 */
public class DataBase extends SQLiteOpenHelper{

    private SQLiteDatabase dataBase;
    private Context context;

    public static final String DATABASE_NAME = "baza.db";
    private static final int DB_VERSION = 3;

    public static final String TABLE_NAME_USERS = "users";
    public static final String COLUMN_NAME_ID_USER = "id_user";
    public static final String COLUMN_NAME_ID_GROUP_IN_USER = "id_group_in_user";
    public static final String COLUMN_NAME_INDEX = "index_number";
    public static final String COLUMN_NAME_FIRST_NAME = "first_name";
    public static final String COLUMN_NAME_SECOND_NAME = "second_name";

    public static final String TABLE_NAME_GROUP = "studentGroup";
    public static final String COLUMN_NAME_ID_GROUP = "id_group";
    public static final String COLUMN_NAME_GROUP_NAME = "group_name";

    public static final String TABLE_NAME_TRAIT = "studentTrait";
    public static final String COLUMN_NAME_ID_TRAIT = "id_Trait";
    public static final String COLUMN_NAME_ID_USER_IN_TRAIT = "id_user_in_trait";

    public static final String ON_CREATE_USERS =
            "CREATE TABLE " + TABLE_NAME_USERS + " (" +
                    COLUMN_NAME_ID_USER + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME_INDEX + " INTEGER UNIQUE, " +
                    COLUMN_NAME_ID_GROUP_IN_USER + " INTEGER, " +
                    COLUMN_NAME_FIRST_NAME + " TEXT, " +
                    COLUMN_NAME_SECOND_NAME + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_NAME_ID_GROUP_IN_USER + ") REFERENCES " +
                    TABLE_NAME_GROUP + "("+COLUMN_NAME_ID_GROUP+")" +
                    " )";

    public static final String ON_CREATE_TRAIT =
            "CREATE TABLE " + TABLE_NAME_TRAIT + " ( " +
                    COLUMN_NAME_ID_TRAIT + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME_ID_USER_IN_TRAIT + " INTEGER, " +
                    "FOREIGN KEY(" + COLUMN_NAME_ID_USER_IN_TRAIT + ") REFERENCES " +
                    TABLE_NAME_USERS + "("+COLUMN_NAME_ID_USER+")" +
                    " )";

    public static final String ON_CREATE_GROUP = String.format(
            "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT UNIQUE)",
            TABLE_NAME_GROUP, COLUMN_NAME_ID_GROUP, COLUMN_NAME_GROUP_NAME);

    public static final String ON_DELETE_USERS =
            "DROP TABLE IF EXISTS " + TABLE_NAME_USERS;

    public static final String ON_DELETE_GROUP =
            "DROP TABLE IF EXISTS " + TABLE_NAME_GROUP;

    public static final String ON_DELETE_TRAIT =
            "DROP TABLE IF EXISTS " + TABLE_NAME_TRAIT;



    public DataBase(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
        dataBase = this.getWritableDatabase();
    }

    public DataBase open(){
        dataBase = this.getWritableDatabase();
        return this;
    }

    public void close() {
        dataBase.close();
        this.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTableGroup(db);
        createTableUsers(db);
        createTableTrait(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropUsers();
        dropGroup();
        dropTrait();
        createTableGroup(db);
        createTableUsers(db);
        createTableTrait(db);
    }

    /**
     * Dodawanie i usuwanie poszczegolnych tabel.
     */
    public void createTableUsers(SQLiteDatabase db){ db.execSQL(ON_CREATE_USERS); }
    public void dropUsers(){ dataBase.execSQL(ON_DELETE_USERS); }
    public void createTableGroup(SQLiteDatabase db){ db.execSQL(ON_CREATE_GROUP);}
    public void dropGroup(){ dataBase.execSQL(ON_DELETE_GROUP);}
    public void createTableTrait(SQLiteDatabase db){ db.execSQL(ON_CREATE_TRAIT);}
    private void dropTrait() { dataBase.execSQL(ON_DELETE_TRAIT);}

    /**
     * Dodaje do bazy grupę o podanej nazwie i zwraca id
     * @param name - nazwa grupy
     * @return long id - id stworzonej grupy, -1 gdy nie udało się jej dodać
     */
    public long insertGroup(String name){
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_GROUP_NAME, name);

        return dataBase.insert(TABLE_NAME_GROUP, null, values);
    }

    /**
     * Zwraca id grupy o podanej nazwie
     * @param name - nazwa grupy do wyszukania
     * @return long id - wyszukane id, -1 gdy podana grupa nie istnieje
     */
    public long getGroupId(String name){
        long id = -1;
        String selectQuery = "SELECT " + COLUMN_NAME_ID_GROUP
                + " FROM " + TABLE_NAME_GROUP
                + " WHERE " + COLUMN_NAME_GROUP_NAME + "='" + name+ "'";

        Cursor cursor = dataBase.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            id = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID_GROUP));
        }
        return id;
    }

    /**
     * Dodaj nowego użytkownika do bazy
     * @param fName - imie użytkownika
     * @param sName - nazwisko użytkownika
     * @param indexNumber - numer indeksu ( wartość unique )
     * @param groupName - nazwa grupy do której należy użytkownika,
     *                    jeżeli nie istnieje to jest automatycznie dodawana
     * @return long id - zwraca id dodanego użytkownika, -1 jeśli nie udało sie dodać
     */
    public long insertUser(String fName, String sName, int indexNumber, String groupName){
        insertGroup(groupName);
        long groupId = getGroupId(groupName);
        if(groupId == -1) { return -1; }

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_INDEX, indexNumber);
        values.put(COLUMN_NAME_FIRST_NAME, fName);
        values.put(COLUMN_NAME_SECOND_NAME, sName);
        values.put(COLUMN_NAME_ID_GROUP_IN_USER, groupId);

        return dataBase.insert(TABLE_NAME_USERS, null, values);
    }

    /**
     * Zwraca id użytkownika o podanym numerze indeksu
     * @param indexNumber - wyszukiwany numer indeksu
     * @return long id - odnalezione id, -1 jeżeli podany użytkownik nie istnieje
     */
    public long getUserId(int indexNumber){
        long id = -1;
        String selectQuery = "SELECT " + COLUMN_NAME_ID_USER
                            + " FROM " + TABLE_NAME_USERS
                            + " WHERE " + COLUMN_NAME_INDEX + "=" +indexNumber;

        Cursor cursor = dataBase.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            id = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID_USER));
        }
        return id;
    }

}
