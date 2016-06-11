package com.app.checkpresence;

import com.app.handfeatures.HandFeatures;
import com.app.handfeatures.HandFeaturesRaport;

/**
 * Created by bijat on 11.06.2016.
 */

public class HandFeatureRaportManager {
    private static int numOfTakeBackgoungInARow = 0;

    HandFeatureRaportManager(HandFeaturesRaport report) {
        m_report = report;
    }

    public void add(HandFeaturesRaport.CalculationRaport raport) {
        if (m_report != null)
            m_report.cal = raport;
    }

    public boolean isNeedToTakeNewBackground() {
        if (++HandFeatureRaportManager.numOfTakeBackgoungInARow > 4) {
            HandFeatureRaportManager.numOfTakeBackgoungInARow = 0;
            return true;
        }

        if (m_report.bin == null)
            return true;

        int binPixels = m_report.bin.width * m_report.bin.height;
        if (m_report.bin.elPixels < binPixels * 0.03)
            return true;

        if (m_report.bin.elPixels > binPixels * 0.70)
            return true;

        if (m_report.seg == null)
            return true;

        int segPixels = m_report.seg.width * m_report.seg.height;
        if (m_report.seg.theBiggestAreaPixels < segPixels * 0.03)
            return true;

        if (m_report.seg.numAreas > HandFeatures.maxAllowedAreas)
            return true;

        HandFeatureRaportManager.numOfTakeBackgoungInARow = 0;
        return false;
    }

    private HandFeaturesRaport m_report;
}
