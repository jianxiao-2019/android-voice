package com.kikatech.voice.util.fuzzy.provider;

import com.kikatech.voice.util.fuzzy.FuzzySearchManager;

import java.util.Collection;
import java.util.List;

/**
 * @author SkeeterWang Created on 2017/11/9.
 */
public abstract class FuzzySearchProvider {

    public static final int INVALID_CONFIDENCE_CRITERIA = -1;

    public abstract int getLowConfidenceCriteria();

    public abstract FuzzySearchManager.FuzzyResult search(String source, Collection<String> comparision);

    public abstract List<FuzzySearchManager.FuzzyResult> search(String source, Collection<String> comparision, int resultCount);

}
