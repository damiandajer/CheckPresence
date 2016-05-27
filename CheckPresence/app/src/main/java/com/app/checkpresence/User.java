package com.app.checkpresence;

import java.util.List;

/**
 * Created by Szymon on 2016-05-25.
 */
public class User {

    private String firstName;
    private String secondName;
    private int indexNumber;
    private String groupName;

    private List<double[]> traits;

    public User(String firstName, String secondName, int indexNumber, String groupName, List<double[]> traits) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.indexNumber = indexNumber;
        this.groupName = groupName;
        this.traits = traits;
    }

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
     * @return List<double[]>
     */
    public List<double[]> getTraits() {
        return traits;
    }

}
