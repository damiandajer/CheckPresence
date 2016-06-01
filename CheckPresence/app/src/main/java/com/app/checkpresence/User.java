package com.app.checkpresence;

import java.util.List;

/**
 * Created by Szymon on 2016-05-25.
 */
public class User {

    private long id;

    private String firstName;
    private String secondName;
    private int indexNumber;
    private String groupName;

    private List<float[]> traits;

    public User(long id, String firstName, String secondName, int indexNumber, String groupName, List<float[]> traits) {
        this.id = id;
        this.firstName = firstName;
        this.secondName = secondName;
        this.indexNumber = indexNumber;
        this.groupName = groupName;
        this.traits = traits;
    }

    public long getId(){ return id; }

    public String getGroupName() {
        return groupName;
    }

    public String getFirstName() {
        return firstName;
    }

    public int getIndexNumber() {
        return indexNumber;
    }

    public String getSecondName() {
        return secondName;
    }

    /**
     * Zwraca listę z tablicami double - cechami użytkownika
     * @return List<float[]>
     */
    public List<float[]> getTraits() {
        return traits;
    }

}
