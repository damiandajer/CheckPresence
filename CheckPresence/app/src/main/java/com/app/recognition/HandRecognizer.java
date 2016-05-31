/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.app.recognition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author kbaran
 */
public class HandRecognizer {


    Normalizer norm;
    /**
     * Normalizer class which provides "process" normalization method
     * @param norm Normalizer
     */
    public HandRecognizer(Normalizer norm) {
        this.norm = norm;
    }

    public HandRecognizer() {
        norm = new Normalizer();
    }

    /**
     * Classifies the taken sample, matching it to user samples from database
     * @param values float[]
     * @param data Map<Integer,List<float[]>>
     * @return result List<Integer> .Ranking(maximum 5 results) of matches that fits best. It contains
     * user ids provided by data param. Result list is sorted by probability of proper match.
     * e.g. result.get(0)  has scales that matches pattern best. result.get(1) had less proper
     * matches etc.
     */
    public List<Integer> recognise(float[] values, Map<Integer,List<float[]>> data)
    {
        values = norm.process(values);
        for (Map.Entry<Integer, List<float[]>> entry : data.entrySet())
        {
            List<float[]> list = entry.getValue();
            for(int i = 0; i < list.size(); i++)
                list.set(i, norm.process(list.get(i)));
        }
        List<InnerPair> results = new ArrayList<>();
        for (Map.Entry<Integer, List<float[]>> entry : data.entrySet())
        {
            results.add(new InnerPair(entry.getKey(),findSmallestDistance(values, entry.getValue())));
        }
        Collections.sort(results, new Comparator<InnerPair>(){
            @Override
            public int compare(InnerPair o1, InnerPair o2) {
                return Double.compare(o1.getDistance(), o2.getDistance());
            }
        });
        List<Integer> integerResults = new ArrayList<>();
        for(InnerPair i : results)
            integerResults.add(i.toInteger());
        return integerResults;
    }
    /**
     * Finds the smallest Euclidean Distance between pattern vector "values" and vectors stored in data list
     * @param values float[]
     * @param data List<float[]> data
     * @return minDistance double smallest distance
     */
    private double findSmallestDistance(float[] values, List<float[]> data)
    {
        double minDistance = 0;
        double d;
        minDistance = calcEuclideanDistanceBetweenVectors(values, data.get(0)); //initializing minDistance with first difference
        for(int i = 1; i < data.size(); i++)
        {

            d = calcEuclideanDistanceBetweenVectors(values, data.get(i));
            if( minDistance > d )
                minDistance = d;
        }
        return minDistance;
    }
    /**
     * calculates Euclidean Distance between two vectors
     * @param d float[] pattern vector
     * @param a float[] sample vector
     * @return result double Euclidean Distance between two vectors
     */
    private double calcEuclideanDistanceBetweenVectors(float[] d, float[] a)
    {
        if(d.length != a.length) throw new IllegalArgumentException("Different parameter's lengths");
        double result = 0;
        double difference;
        for(int i = 0; i < d.length; i++)
        {
            difference = d[i] - a[i];
            result += difference * difference;
        }
        result = Math.sqrt(result);
        return result;
    }
    /**
     * Class for storing result pairs <userID, distance between his best-matching result and vector>
     */
    private class InnerPair{
        private int id;
        private double distance;
        InnerPair(int id, double distance)
        {
            this.id = id;
            this.distance = distance;
        }

        public int getId() {
            return id;
        }

        public double getDistance() {
            return distance;
        }

        public Integer toInteger()
        {
            return new Integer(id);
        }
    }
}
