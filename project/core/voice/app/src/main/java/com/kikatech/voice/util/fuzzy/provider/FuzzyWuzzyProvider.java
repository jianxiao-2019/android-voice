package com.kikatech.voice.util.fuzzy.provider;

import com.kikatech.voice.util.fuzzy.FuzzySearchManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;

/**
 * @author SkeeterWang Created on 2017/11/9.
 */
public class FuzzyWuzzyProvider extends FuzzySearchProvider {

    private static final String TAG = "FuzzyWuzzyProvider";

    @Override
    public int getLowConfidenceCriteria() {
        return 60;
    }

    @Override
    public FuzzySearchManager.FuzzyResult search(String source, Collection<String> comparision) {
        ExtractedResult extractedResult = FuzzySearch.extractOne(source, comparision);
        if (extractedResult != null) {
            return new FuzzySearchManager.FuzzyResult(extractedResult.getString(), extractedResult.getScore());
        }
        return null;
    }

    @Override
    public List<FuzzySearchManager.FuzzyResult> search(String source, Collection<String> comparision, int resultCount) {
        List<ExtractedResult> extractedResults = FuzzySearch.extractSorted(source, comparision, resultCount);
        if (extractedResults != null && extractedResults.isEmpty()) {
            List<FuzzySearchManager.FuzzyResult> results = new ArrayList<>();
            for (ExtractedResult result : extractedResults) {
                results.add(new FuzzySearchManager.FuzzyResult(result.getString(), result.getScore()));
            }
            return results;
        }
        return null;
    }
}
