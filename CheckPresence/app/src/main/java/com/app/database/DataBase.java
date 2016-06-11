package com.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.app.checkpresence.Classes;
import com.app.checkpresence.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Szymon on 2016-04-10.
 */
public class DataBase extends SQLiteOpenHelper{

    private SQLiteDatabase dataBase;
    private Context context;

    public static final String DATABASE_NAME = "baza.db";
    public static int NUMBER_OF_TRAITS = 30;
    private static final int DB_VERSION = 7;

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

    public static final String TABLE_NAME_CLASS = "classTable";
    public static final String COLUMN_NAME_ID_CLASS = "id_Class";
    public static final String COLUMN_NAME_ID_GROUP_IN_CLASS = "id_group_in_class";
    public static final String COLUMN_NAME_CLASS_YEAR = "class_year";
    public static final String COLUMN_NAME_CLASS_MONTH = "class_month";
    public static final String COLUMN_NAME_CLASS_DAY = "class_day";

    public static final String TABLE_NAME_PRESENCE = "studentPresence";
    public static final String COLUMN_NAME_ID_PRESENCE = "id_Presence";
    public static final String COLUMN_NAME_ID_CLASS_IN_PRESENCE = "id_class_in_presence";
    public static final String COLUMN_NAME_ID_USER_IN_PRESENCE = "id_user_in_presence";
    public static final String COLUMN_NAME_PRESENCE_BOOL = "presence_bool";

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
                    "T0" + " FLOAT, " +
                    "T1" + " FLOAT, " +
                    "T2" + " FLOAT, " +
                    "T3" + " FLOAT, " +
                    "T4" + " FLOAT, " +
                    "T5" + " FLOAT, " +
                    "T6" + " FLOAT, " +
                    "T7" + " FLOAT, " +
                    "T8" + " FLOAT, " +
                    "T9" + " FLOAT, " +
                    "T10" + " FLOAT, " +
                    "T11" + " FLOAT, " +
                    "T12" + " FLOAT, " +
                    "T13" + " FLOAT, " +
                    "T14" + " FLOAT, " +
                    "T15" + " FLOAT, " +
                    "T16" + " FLOAT, " +
                    "T17" + " FLOAT, " +
                    "T18" + " FLOAT, " +
                    "T19" + " FLOAT, " +
                    "T20" + " FLOAT, " +
                    "T21" + " FLOAT, " +
                    "T22" + " FLOAT, " +
                    "T23" + " FLOAT, " +
                    "T24" + " FLOAT, " +
                    "T25" + " FLOAT, " +
                    "T26" + " FLOAT, " +
                    "T27" + " FLOAT, " +
                    "T28" + " FLOAT, " +
                    "T29" + " FLOAT, " +
                    " FOREIGN KEY(" + COLUMN_NAME_ID_USER_IN_TRAIT + ") REFERENCES " +
                    TABLE_NAME_USERS + "("+COLUMN_NAME_ID_USER+")" +
                    " )";

    public static final String ON_CREATE_GROUP = String.format(
            "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT UNIQUE)",
            TABLE_NAME_GROUP, COLUMN_NAME_ID_GROUP, COLUMN_NAME_GROUP_NAME);

    public static final String ON_CREATE_CLASS =
            "CREATE TABLE " + TABLE_NAME_CLASS + " ( " +
                    COLUMN_NAME_ID_CLASS + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME_ID_GROUP_IN_CLASS + " INTEGER, " +
                    COLUMN_NAME_CLASS_YEAR + " INTEGER, " +
                    COLUMN_NAME_CLASS_MONTH + " INTEGER, " +
                    COLUMN_NAME_CLASS_DAY + " INTEGER, " +
                    "FOREIGN KEY(" + COLUMN_NAME_ID_GROUP_IN_CLASS + ") REFERENCES " +
                    TABLE_NAME_GROUP + "("+COLUMN_NAME_ID_GROUP+")" +
                    " )";

    public static final String ON_CREATE_PRESENCE =
            "CREATE TABLE " + TABLE_NAME_PRESENCE + " ( " +
                    COLUMN_NAME_ID_PRESENCE + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME_ID_CLASS_IN_PRESENCE + " INTEGER, " +
                    COLUMN_NAME_ID_USER_IN_PRESENCE + " INTEGER, " +
                    COLUMN_NAME_PRESENCE_BOOL + " INTEGER DEFAULT 0, " +
                    "FOREIGN KEY(" + COLUMN_NAME_ID_CLASS_IN_PRESENCE + ") REFERENCES " +
                    TABLE_NAME_CLASS + "("+COLUMN_NAME_ID_CLASS+")" +
                    "FOREIGN KEY(" + COLUMN_NAME_ID_USER_IN_PRESENCE + ") REFERENCES " +
                    TABLE_NAME_USERS + "("+COLUMN_NAME_ID_USER+")" +
                    " )";

    public static final String ON_DELETE_USERS =
            "DROP TABLE IF EXISTS " + TABLE_NAME_USERS;

    public static final String ON_DELETE_GROUP =
            "DROP TABLE IF EXISTS " + TABLE_NAME_GROUP;

    public static final String ON_DELETE_TRAIT =
            "DROP TABLE IF EXISTS " + TABLE_NAME_TRAIT;

    public static final String ON_DELETE_CLASS =
            "DROP TABLE IF EXISTS " + TABLE_NAME_CLASS;

    public static final String ON_DELETE_PRESENCE =
            "DROP TABLE IF EXISTS " + TABLE_NAME_PRESENCE;


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
        createTableClass(db);
        createTablePresence(db);
        createTraitsTrigger(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion < 5){
            dropTrait(db);
            dropGroup(db);
            dropUsers(db);
            createTableGroup(db);
            createTableUsers(db);
            createTableTrait(db);
        }
        if(oldVersion < 6){
            createTableClass(db);
            createTablePresence(db);
        }
        if(oldVersion < 7){
            createTraitsTrigger(db);
        }
    }

    /**
     * Dodawanie i usuwanie poszczegolnych tabel.
     */
    private void createTableUsers(SQLiteDatabase db){ db.execSQL(ON_CREATE_USERS); }
    private void dropUsers(SQLiteDatabase db){ db.execSQL(ON_DELETE_USERS); }

    private void createTableGroup(SQLiteDatabase db){ db.execSQL(ON_CREATE_GROUP);}
    private void dropGroup(SQLiteDatabase db){ db.execSQL(ON_DELETE_GROUP);}

    private void createTableTrait(SQLiteDatabase db){ db.execSQL(ON_CREATE_TRAIT);}
    private void dropTrait(SQLiteDatabase db) { db.execSQL(ON_DELETE_TRAIT);}

    private void createTableClass(SQLiteDatabase db){ db.execSQL(ON_CREATE_CLASS);}
    private void dropClass(SQLiteDatabase db) { db.execSQL(ON_DELETE_CLASS);}

    private void createTablePresence(SQLiteDatabase db){ db.execSQL(ON_CREATE_PRESENCE);}
    private void dropPresence(SQLiteDatabase db) { db.execSQL(ON_DELETE_PRESENCE);}

    /**
     * Tworzy triggera usuwającego stare wartości z tabeli traits
     */
    public void createTraitsTrigger(SQLiteDatabase db){
        db.execSQL("CREATE TRIGGER IF NOT EXISTS traitTrigger \n" +
                " AFTER INSERT "+
                " ON "+ TABLE_NAME_TRAIT + "\n" +
                " FOR EACH ROW \n" +

                " WHEN ( select count(*) from " + TABLE_NAME_TRAIT + " \n" +
                    " where " + COLUMN_NAME_ID_USER_IN_TRAIT + "= NEW." + COLUMN_NAME_ID_USER_IN_TRAIT + "\n" +
                    " ) > 30 \n" +

                " BEGIN \n"+

                        " delete from " + TABLE_NAME_TRAIT + "\n" +
                        " where " + COLUMN_NAME_ID_USER_IN_TRAIT  + " = (" +
                        " select min("+ COLUMN_NAME_ID_USER_IN_TRAIT +") from " + TABLE_NAME_TRAIT + "\n" +
                        " where " + COLUMN_NAME_ID_USER_IN_TRAIT + "= NEW." + COLUMN_NAME_ID_USER_IN_TRAIT + ");\n" +

                " END;");
    }

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
     * Dodaje nowego użytkownika do bazy
     * @param user - nowy użytkownik
     * @return long id - zwraca id dodanego użytkownika, -1 jeśli nie udało sie dodać
     */
    public long insertUser(User user){
        insertGroup(user.getGroupName());
        long groupId = getGroupId(user.getGroupName());
        if(groupId == -1) { return -1; }

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_INDEX, user.getIndexNumber());
        values.put(COLUMN_NAME_FIRST_NAME, user.getFirstName());
        values.put(COLUMN_NAME_SECOND_NAME, user.getSecondName());
        values.put(COLUMN_NAME_ID_GROUP_IN_USER, groupId);

        long ret =  dataBase.insert(TABLE_NAME_USERS, null, values);

        for( float traits[] : user.getTraits()){
            insertTraits(ret, traits);
        }

        return ret;
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

    /**
     * Zwraca id pierwszego użytkownika o podanym imieniu i nazwisku.
     * Imie i nazwisko w przeciwieństwie do indeksu nie są unikatowe!
     * @param fName - imie
     * @param sName - nazwisko
     * @return long id - odnalezione id, -1 jeżeli podany użytkownik nie istnieje
     */
    public long getUserId(String fName, String sName){
        long id = -1;
        String selectQuery = "SELECT " + COLUMN_NAME_ID_USER
                + " FROM " + TABLE_NAME_USERS
                + " WHERE " + COLUMN_NAME_FIRST_NAME + "='" + fName + "' "
                + " AND " + COLUMN_NAME_SECOND_NAME + "='" + sName + "'";

        Cursor cursor = dataBase.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            id = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID_USER));
        }
        return id;
    }

    /**
     * Zwraca obiekt klasy user - użytkownika o podanym ID
     * @param idUser - ID użytkownika do zwrócenia
     * @return User - Obiekt user o podanym ID
     */
    public User getUser(long idUser){

        List<float[]> traits = new ArrayList<float[]>();
        User user;

        String selectQuery = "SELECT " + COLUMN_NAME_FIRST_NAME
                + ", " + COLUMN_NAME_SECOND_NAME
                + ", " + COLUMN_NAME_INDEX
                + ", " + COLUMN_NAME_GROUP_NAME
                + " FROM " + TABLE_NAME_USERS
                + " join " + TABLE_NAME_GROUP
                + " on " + COLUMN_NAME_ID_GROUP + "=" + COLUMN_NAME_ID_GROUP_IN_USER
                + " WHERE " + COLUMN_NAME_ID_USER + "=" + Long.toString(idUser) + ";";

        Cursor cursor = dataBase.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){
            user = new User(idUser,
                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME_FIRST_NAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME_SECOND_NAME)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_INDEX)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME_GROUP_NAME)),
                    getUserTraits(idUser));
        } else {
            user = null;
        }

        return user;
    }

    /**
     * Zwraca wszystkich użytkowników
     * @return List<User> - lista użytkowników
     */
    public List<User> getAllUsers(){

        List<User> users = new ArrayList<User>();

        String selectQuery = "SELECT " + COLUMN_NAME_ID_USER
                + " FROM " + TABLE_NAME_USERS;

        Cursor cursor = dataBase.rawQuery(selectQuery, null);

        if(cursor != null && cursor.moveToFirst()) {
            do{
                users.add(getUser(cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_ID_USER))));
            }while(cursor.moveToNext());
        }

        return users;
    }

    public List<Integer> getAllUsersAlbums(){

        List<Integer> users = new ArrayList<Integer>();

        String selectQuery = "SELECT " + COLUMN_NAME_INDEX
                + " FROM " + TABLE_NAME_USERS;

        Cursor cursor = dataBase.rawQuery(selectQuery, null);

        if(cursor != null && cursor.moveToFirst()) {
            do{
                users.add(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_INDEX)));
            }while(cursor.moveToNext());
        }

        return users;
    }

    /**
     * Zwraca wszystkich użytkowników z danej grupy
     * @return List<User> - lista użytkowników
     */
    public List<User> getAllUsersFromGroup(String groupName){

        List<User> users = new ArrayList<User>();

        String selectQuery = "SELECT " + COLUMN_NAME_ID_USER
                + " FROM " + TABLE_NAME_USERS
                + " JOIN " + TABLE_NAME_GROUP
                + " ON " + COLUMN_NAME_ID_GROUP + "=" + COLUMN_NAME_ID_GROUP_IN_USER
                + " WHERE " + COLUMN_NAME_GROUP_NAME + "='" + groupName + "';";

        Cursor cursor = dataBase.rawQuery(selectQuery, null);

        if(cursor != null && cursor.moveToFirst()) {
            do{
                users.add(getUser(cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_ID_USER))));
            }while(cursor.moveToNext());
        }

        return users;
    }

    /**
     * Zwraca mapę w której do indeksów przypisane są tablice z cechami użytkowników
     * @return Map<String, List<float[]>>
     */
    public Map<Integer, List<float[]>> getAllUsersWithTraits(){
        List<User> users = new ArrayList<>();
        List<float[]> traits = new ArrayList<>();
        int indexNumber;
        Map<Integer, List<float[]>> usersWithTraits = new HashMap<>();
        users = getAllUsers();

        for (User u : users) {
            usersWithTraits.put(u.getIndexNumber(),  u.getTraits());
        }

        return usersWithTraits;
    }

    /**
     * Dodaje liste cech do użytkownika o podanym ID
     * @param userID - ID użytkownika
     * @param traits - tablica float[] z cechami
     * @return long id - zwraca id dodanych cech, -1 jeśli nie udało sie dodać
     */
    public long insertTraits(long userID, float[] traits){
        System.out.println("Inserting traits");
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_ID_USER_IN_TRAIT, userID);
        for(int i = 0 ; i < NUMBER_OF_TRAITS ; i++) {
            values.put("T" + i, traits[i]);
        }

        return dataBase.insert(TABLE_NAME_TRAIT, null, values);
    }

    /**
     * Usuwa wszystkie wpisy z cechami dla danego użytkownika
     * @param idUser - id Użytkownika którego cechy chcemy usunąć
     */
    public void deleteUserTraits(long idUser){
        String deleteQuery = "DELETE FROM " + TABLE_NAME_TRAIT
                + " WHERE " + COLUMN_NAME_ID_USER_IN_TRAIT + "=" + Long.toString(idUser) + ";";

        dataBase.execSQL(deleteQuery);
    }

    /**
     * Usuwa użytkownika o podanym id
     * @param idUser - id Użytkownika którego chcemy usunąć
     */
    public void deleteUser(long idUser){
        deleteUserTraits(idUser);

        String deleteQuery = "DELETE FROM " + TABLE_NAME_USERS
                + " WHERE " + COLUMN_NAME_ID_USER + "=" + Long.toString(idUser) + ";";

        dataBase.execSQL(deleteQuery);
    }

    /**
     * Zwraca liste tablic float[] z cechami użytkownika
     * @param userID - ID Użytkownika do pobrania
     * @return List<float[]> - lista tablic cech
     */
    public List<float[]> getUserTraits(long userID){
        List<float[]> traits = new ArrayList<float[]>();
        float[] traitArray;

        String selectQuery = "SELECT * "
                + " FROM " + TABLE_NAME_TRAIT
                + " WHERE " + COLUMN_NAME_ID_USER_IN_TRAIT + "=" + userID;

        Cursor cursor = dataBase.rawQuery(selectQuery, null);

        if(cursor != null && cursor.moveToFirst()) {
            do{
                traitArray = new float[NUMBER_OF_TRAITS];
                for(int i = 0 ; i < NUMBER_OF_TRAITS ; i++){
                    traitArray[i] = cursor.getFloat(cursor.getColumnIndex("T" + i));
                }
                traits.add(traitArray);
            }while(cursor.moveToNext());
        }

        return traits;
    }

    /**
     * Dodaje nowe zajęcia do bazy. Zajecia dla podanej grupy w podanym dniu
     * @param groupName - nazwa grupy
     * @param year - rok
     * @param month - miesiąc
     * @param day - dzień
     * @return long id - zwraca id dodanego użytkownika, -1 jeśli nie udało sie dodać
     */
    public long insertClass(String groupName, int year, int month, int day){
        long idGroup =  getGroupId(groupName);

        if( idGroup == -1){
            idGroup = insertGroup(groupName);
        }

        if(idGroup == -1){
            return -1;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_CLASS_DAY, day);
        values.put(COLUMN_NAME_CLASS_MONTH, month);
        values.put(COLUMN_NAME_CLASS_YEAR, year);
        values.put(COLUMN_NAME_ID_GROUP_IN_CLASS, idGroup);

        long ret =  dataBase.insert(TABLE_NAME_CLASS, null, values);

        if(ret == -1){
            return -1;
        }

        List<User> users = getAllUsersFromGroup(groupName);

        for(User u : users){
            insertPresence(ret, u.getId());
        }

        return ret;
    }

    /**
     * Zwraca liste wszystkich zajęć
     * @return List<Classes> - lista z zajęciami
     */
    public List<Classes> getAllClassses(){
        List<Classes> classes = new ArrayList<Classes>();

        String selectQuery = "SELECT * "
                + " FROM " + TABLE_NAME_CLASS
                + " JOIN " + TABLE_NAME_GROUP
                + " on " + COLUMN_NAME_ID_GROUP_IN_CLASS
                + "=" + COLUMN_NAME_ID_GROUP;

        Cursor cursor = dataBase.rawQuery(selectQuery, null);

        if(cursor != null && cursor.moveToFirst()) {
            do{
                Classes c = new Classes( cursor.getString(cursor.getColumnIndex(COLUMN_NAME_GROUP_NAME)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID_CLASS)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_CLASS_YEAR)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_CLASS_MONTH)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_CLASS_DAY))
                );
                classes.add(c);
            }while(cursor.moveToNext());
        }

        return classes;
    }


    /**
     * Zwraca liste zajęć dla podanej grupy
     * @return List<Classes> - lista z zajęciami
     */
    public List<Classes> getClasses(String groupName){
        List<Classes> classes = new ArrayList<Classes>();

        String selectQuery = "SELECT * "
                + " FROM " + TABLE_NAME_CLASS
                + " JOIN " + TABLE_NAME_GROUP
                + " on " + COLUMN_NAME_ID_GROUP_IN_CLASS
                + "=" + COLUMN_NAME_ID_GROUP
                + " WHERE " + COLUMN_NAME_GROUP_NAME + "='" + groupName
                + "'";

        Cursor cursor = dataBase.rawQuery(selectQuery, null);

        if(cursor != null && cursor.moveToFirst()) {
            do{
                Classes c = new Classes( cursor.getString(cursor.getColumnIndex(COLUMN_NAME_GROUP_NAME)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID_CLASS)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_CLASS_YEAR)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_CLASS_MONTH)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_CLASS_DAY))
                );
                classes.add(c);
            }while(cursor.moveToNext());
        }

        return classes;
    }

    /**
     * Dodaje pole obecność dla podaej osoby na podanych zajęciach
     * @param idClass - id zajęć
     * @param idUser - id osoby
     * @return long - id pola dodanego do bazy
     */
    public long insertPresence(long idClass, long idUser){
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_ID_CLASS_IN_PRESENCE, idClass);
        values.put(COLUMN_NAME_ID_USER_IN_PRESENCE, idUser);

        long ret =  dataBase.insert(TABLE_NAME_PRESENCE, null, values);

        return ret;
    }

    /**
     * Zwraca wartość obecności danego użytkownika na danych zajęciach
     * @param idClass - id zajęcia
     * @param idUser - id użytkownika
     * @return boolean - false jeśli nieobecny, true jeśli obecny
     */
    public boolean getPresence(long idClass, long idUser){
        String selectQuery = "SELECT " + COLUMN_NAME_PRESENCE_BOOL
                + " FROM " + TABLE_NAME_PRESENCE
                + " WHERE " + COLUMN_NAME_ID_CLASS_IN_PRESENCE + "=" + Long.toString(idClass)
                + " AND " + COLUMN_NAME_ID_USER_IN_PRESENCE + "=" + Long.toString(idUser) + ";";


        Cursor cursor = dataBase.rawQuery(selectQuery, null);

        if(cursor != null && cursor.moveToFirst()) {
            if (cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_PRESENCE_BOOL)) == 0)
                return false;
            else
                return true;
        }

        return false;
    }

    /**
     * Ustawia obecność na zadaną wartość
     * @param idClass - id zajęć
     * @param idUser - id użytkownika
     * @param present - true - obecny, false - nieobecny
     */
    public void setPresence(long idClass, long idUser, boolean present){
        int p;
        if(present == true)
            p = 1;
        else
            p = 0;

        String query = "UPDATE " + TABLE_NAME_PRESENCE
                + " SET " + COLUMN_NAME_PRESENCE_BOOL
                + "=" + p
                + " WHERE " + COLUMN_NAME_ID_USER_IN_PRESENCE + "=" +  Long.toString(idUser)
                + " AND " + COLUMN_NAME_ID_CLASS_IN_PRESENCE + "=" +  Long.toString(idClass) + ";";

        dataBase.rawQuery(query, null);
    }


    /**
     * Usuwa wszystkie wpisy z obecnością
     */
    public void deleteAllPresence(){
        String deleteQuery = "DELETE FROM " + TABLE_NAME_PRESENCE;

        dataBase.execSQL(deleteQuery);
    }


    /**
     * Usuwa wszystkie wpisy z zajęciami
     */
    public void deleteAllClasses() {
        String deleteQuery = "DELETE FROM " + TABLE_NAME_CLASS;

        dataBase.execSQL(deleteQuery);
    }

    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "mesage" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }

    }

}
