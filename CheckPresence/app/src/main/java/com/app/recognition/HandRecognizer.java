/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.app.recognition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author kbaran
 */
public class HandRecognizer {

    /** Public access stuff ***********************/

    /**
     * This constructor takes object which implements Normalizer interface
     * (while Normalizer is a functional interface you can pass the lambda
     * expression as well)
     * @param norm Normalizer
     */
    public HandRecognizer(Normalizer norm) {
        this.norm = norm;
    }
    /**
     * Default constructor which provides our default normalization
     */
    public HandRecognizer() {
        // norm = new StandardNormalizer();
        //norm = (List l) -> {};
        norm = new Normalizer() {
            @Override
            public void process(List vector) {
            }
        };
    }

    public List<String> recognise(float[] values, Map<String,List<float[]>> data)
    {
        System.out.println("length " + values.length);
        int numberOfSamples = 0;
        for (Map.Entry<String, List<float[]>> entry : data.entrySet())
        {
            numberOfSamples = entry.getValue().size();
            break; //assumming that vectors are equal in size
        }
        Map<String,float[]> passer = new HashMap<>();
        List<List<String>> result = new ArrayList<>();

        for(int i = 0; i < numberOfSamples; i++)
        {
            for (Map.Entry<String, List<float[]>> entry : data.entrySet())
            {
                passer.put(entry.getKey(),entry.getValue().get(i));

            }
            result.add(recogniseVector(values,passer));
            passer.clear();
        }

        List<String> processedResult = getFinalIdsList(result);
        return processedResult;
    }



    /**
     * It recognizes which of the samples provided by data fits ours scales best
     * @param values float[]  Scales previously taken by our app
     * @param  data Map<String,float[]> Map which pairs unique user id (String) and
     * array with his scales
     * @return List<String> result.Ranking(maximum 5 results) of matches that fits best. It contains
     * user ids provided by data param. Result list is sorted by probability of proper match.
     * e.g. result.get(0)  has scales that matches pattern best. result.get(1) had less proper
     * matches etc.
     */
    private List<String> recogniseVector(float[] values, Map<String,float[]> data)
    {
        List<String> result = new ArrayList<String>();
        for(int i = 0; i < values.length; i++)
            result.add("None"); //default values
        Map<String,List<FloatWrapper>> usersIDandVectors = getStringFloatWrapperMapFromStringFloatMap(data);
        List<FloatWrapper> vector = getFloatWrapperListFromFloatArray(values);

        norm.process(vector);

        /*usersIDandVectors.forEach( (k,v) -> {
            norm.process(v);
        });
        */
        for (Map.Entry<String, List<FloatWrapper>> entry : usersIDandVectors.entrySet())
        {
            norm.process(entry.getValue());
        }
        double min = 0;
        for(int i = 0; i < vector.size(); i++)
        {
            min = -1;
            for (Map.Entry<String, List<FloatWrapper>> entry : usersIDandVectors.entrySet())
            {
                float difference = Math.abs(vector.get(i).getValue() - entry.getValue().get(i).getValue());
                if(min > difference ){
                    min = difference;
                    result.set(i, entry.getKey());
                }
                else if(min == -1)
                {
                    min = difference;
                    result.set(i, entry.getKey());
                }
            }
        }

        return result;
    }
    /** Private stuff *************************/
    /**
     *  Our Normalizer which stores normalizing method
     */
    private Normalizer norm;

    /**
     * Function used to transform Map<String,float[]> to Map<String,List<FloatWrapper>>
     * @param  usersIDandVectors Map<String,float[]>
     * @return Map<String,List<FloatWrapper>>
     */
    private Map<String,List<FloatWrapper>> getStringFloatWrapperMapFromStringFloatMap(Map<String,float[]> usersIDandVectors)
    {
        Map<String,List<FloatWrapper>> result = new HashMap<>();
        /*
        usersIDandVectors.forEach( (k,v) -> {
                List<FloatWrapper> vec = getFloatWrapperListFromFloatArray(v);
                result.put(k, vec);
        });
        */
        for (Map.Entry<String, float[]> entry : usersIDandVectors.entrySet())
        {
            List<FloatWrapper> vec = getFloatWrapperListFromFloatArray(entry.getValue());
            result.put(entry.getKey(), vec);
        }
        return result;
    }
    /**
     * Function used to transform float[] to List<FloatWrapper>
     * @param  vector float[]
     * @return List<FloatWrapper>
     */
    private List<FloatWrapper> getFloatWrapperListFromFloatArray(float[] vector)
    {
        List<FloatWrapper> result = new ArrayList<>(30);
        for(float el : vector)
            result.add(new FloatWrapper(el));
        return result;
    }

    /**
     * Class InnerPair is used to store pairs(StudentID,NumberOfFits) that allow us to sort values stored in HashMap fitIdsCounter
     * in method getStringIdsListFromStringFitIdsList. The list is sorted to get a proper result.
     *
     */
    private class InnerPair{
        private String id;
        private int repsCounter;
        InnerPair(String id, int repsCounter)
        {
            this.id = id;
            this.repsCounter = repsCounter;
        }
        public int getRepsCounter()
        {   return repsCounter;}
        public String getId()
        {   return id;}
    }
    /**
     *
     * @param
     * @return
     */
    /*
    private List<String> getStringIdsListFromStringFitIdsList(List<String> fitIdsList)
    {
        List<String> result = new ArrayList<>(5);
        Map<String,Integer> fitIdsCounter = new HashMap<>();
        List<InnerPair> listOfIdCounterPairs = new ArrayList<>();
        for(String tmpId: fitIdsList)
        {
            if(fitIdsCounter.get(tmpId) == null)
                fitIdsCounter.put(tmpId, 1);
            else
                fitIdsCounter.put(tmpId, fitIdsCounter.get(tmpId) + 1);
        }

        for (Map.Entry<String, Integer> entry : fitIdsCounter.entrySet())
        	listOfIdCounterPairs.add(new InnerPair(entry.getKey(),entry.getValue()));
        SortInnerPairList(listOfIdCounterPairs);
        for(int i = 0; i < listOfIdCounterPairs.size() && i < 5 ; i++)
            result.add(listOfIdCounterPairs.get(i).getId());
        return result;
    }
    */
    private List<String> getFinalIdsList(List<List<String>> listOfLists)
    {
        List<String> result = new ArrayList<>(5);
        Map<String,Integer> fitIdsCounter = new HashMap<>();
        List<InnerPair> listOfIdCounterPairs = new ArrayList<>();

        for(List<String> list:listOfLists)
        {
            for(String el: list)
            {
                if(fitIdsCounter.get(el) == null)
                    fitIdsCounter.put(el, 1);
                else
                    fitIdsCounter.put(el, fitIdsCounter.get(el) + 1);
            }
        }
        for (Map.Entry<String, Integer> entry : fitIdsCounter.entrySet())
            listOfIdCounterPairs.add(new InnerPair(entry.getKey(),entry.getValue()));
        SortInnerPairList(listOfIdCounterPairs);
        for(int i = 0; i < listOfIdCounterPairs.size() && i < 5 ; i++)
            result.add(listOfIdCounterPairs.get(i).getId());
        return result;
    }
    /**
     * Sorts results
     * @param InnerPairList List<InnerPairList>
     */
    private void SortInnerPairList(List<InnerPair> InnerPairList)
    {
        int n = InnerPairList.size();
        do
        {
            for(int i = 0 ; i<n-1 ; i++)
            {
                if(InnerPairList.get(i).getRepsCounter() < InnerPairList.get(i+1).getRepsCounter())
                {
                    InnerPair tmpInnerPair = InnerPairList.get(i);
                    InnerPairList.set(i, InnerPairList.get(i+1));
                    InnerPairList.set(i+1, tmpInnerPair);
                }
            }
            n--;
        }while(n > 0);
    }

}
