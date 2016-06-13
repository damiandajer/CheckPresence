package com.app.checkpresence.backgroundmenage;

import com.app.handfeatures.HandFeatures;

/**
 * Created by bijat on 11.06.2016.
 */

public class HandFeatureRaportManager {
    private static int notCalculatedInARow = 0;

    public HandFeatureRaportManager(HandFeaturesRaport report) {
        //notCalculatedInARow = 0;
        m_report = report;
        m_matching = HandMatchingLevel.NO;
    }

    public void add(HandFeaturesRaport.CalculationRaport raport) {
        if (m_report != null)
            m_report.cal = raport;
    }

    public boolean isNeedToTakeNewBackground() {
        boolean b = isNeedToTakeNewBackground_implement();
        if (b)
            HandFeatureRaportManager.notCalculatedInARow = 0;

        return b;
    }

    private boolean isNeedToTakeNewBackground_implement() {
        // binaryzation step
        if (m_report.bin == null)
            return true;

        int binPixels = m_report.bin.width * m_report.bin.height;
        if (m_report.bin.elPixels < binPixels * HandFeatureProcessConfig.MIN_BINARYZATION)
            return true;

        if (m_report.bin.elPixels > binPixels * HandFeatureProcessConfig.MAX_BINARYZATION)
            return true;

        // semgentation step
        if (m_report.seg == null)
            return true;

        int segPixels = m_report.seg.width * m_report.seg.height;
        if (m_report.seg.theBiggestAreaPixels < binPixels * 0.01)
            return true;

        if (m_report.seg.numAreas > HandFeatureProcessConfig.MAX_ALLOWED_AREAS_SEGMENTATION)
            return true;

        // calculate step
        if (HandFeatureRaportManager.notCalculatedInARow > 6) {
            HandFeatureRaportManager.notCalculatedInARow = 0;
            return true;
        }

        if (m_report.cal == null)
            return false;

        if(m_report.cal.isGood) {
            HandFeatureRaportManager.notCalculatedInARow = 0;
            return false;
        }
        else {
            ++HandFeatureRaportManager.notCalculatedInARow;
        }

        return false;
    }

    public boolean isReadyToCalculateFeatures() {
        if (m_report == null || m_report.bin == null || m_report.seg == null)
            return false;

        if (m_report.seg.theBiggestAreaCoverage > HandFeatureProcessConfig.MIN_AREA_TO_CEVERAGE_SEGMENTATION
                && m_report.seg.theBiggestAreaCoverage < HandFeatureProcessConfig.MAX_AREA_TO_CEVERAGE_SEGMENTATION) {
            m_matching = HandMatchingLevel.MATCHED;
            return true;
        } else if (m_report.seg.theBiggestAreaCoverage < HandFeatureProcessConfig.MAX_AREA_CEVERAGE_FOR_NEW_BACKGROUND_SEGMENTATION) {
            m_matching = HandMatchingLevel.LOW;
            ++HandFeatureRaportManager.notCalculatedInARow;
        } else {
            m_matching = HandMatchingLevel.CLOSE;
        }

        return false;
    }

    public HandMatchingLevel getMatchingLevel() { return m_matching; }

    private HandFeaturesRaport m_report;
    private HandMatchingLevel m_matching;
}
