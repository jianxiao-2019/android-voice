package com.kikatech.voice.util.fuzzy;

import com.kikatech.voice.util.fuzzy.provider.FuzzyWuzzyProvider;
import com.kikatech.voice.util.fuzzy.provider.FuzzySearchProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author SkeeterWang Created on 2017/11/9.
 */
public class FuzzySearchManager {
    private static final String TAG = "FuzzySearchManager";

    private static FuzzySearchManager sIns;
    private FuzzySearchProvider mSearchProvider;

    public static synchronized FuzzySearchManager getIns() {
        if (sIns == null) {
            sIns = new FuzzySearchManager();
        }
        return sIns;
    }

    private FuzzySearchManager() {
        mSearchProvider = new FuzzyWuzzyProvider();
    }

    public int getLowConfidenceCriteria() {
        if (mSearchProvider != null) {
            return mSearchProvider.getLowConfidenceCriteria();
        }
        return FuzzySearchProvider.INVALID_CONFIDENCE_CRITERIA;
    }

    public FuzzyResult search(String source, String[] comparision) {
        return search(source, Arrays.asList(comparision));
    }

    public FuzzyResult search(String source, Collection<String> comparision) {
        if (mSearchProvider != null) {
            return mSearchProvider.search(source, comparision);
        }
        return null;
    }

    public List<FuzzyResult> search(String source, Collection<String> comparision, int count) {
        if (mSearchProvider != null) {
            return mSearchProvider.search(source, comparision, count);
        }
        return null;
    }

    public static class FuzzyResult {

        private String text;
        private int confidence;

        public FuzzyResult(String text, int confidence) {
            this.text = text;
            this.confidence = confidence;
        }

        public String getText() {
            return text;
        }

        public int getConfidence() {
            return confidence;
        }
    }
}
