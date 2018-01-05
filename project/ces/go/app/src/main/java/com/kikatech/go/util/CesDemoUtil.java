package com.kikatech.go.util;

import android.text.TextUtils;

import com.kikatech.voice.util.fuzzy.FuzzySearchManager;

/**
 * @author SkeeterWang Created on 2018/1/5.
 */

public class CesDemoUtil {
    private static final String TAG = "CesDemoUtil";

    private static final String[] messengerContactList = new String[]{
            "Lisa Lee",
            "Charlie White",
            "Angela Rose",
            "Jason Smith"
    };

    public static DemoMatchedContact findMessengerContact(String[] targetNames) {
        if (targetNames == null || targetNames.length == 0) {
            return null;
        }
        DemoMatchedContact result = findFullMatchedName(targetNames, messengerContactList);
        if (result != null) {
            return result;
        }
        result = findFuzzyName(targetNames, messengerContactList);
        return result;
    }

    private static DemoMatchedContact findFullMatchedName(String[] targetNames, String[] contactList) {
        for (String targetName : targetNames) {
            for (String contactName : contactList) {
                if (targetName.equals(contactName)) {
                    return new DemoMatchedContact(DemoMatchedContact.MatchedType.FULL_MATCHED, contactName);
                }
            }
        }
        return null;
    }

    private static DemoMatchedContact findFuzzyName(String[] targetNames, String[] contactList) {
        int confidence;
        String foundName;
        FuzzySearchManager.FuzzyResult fuzzySearchResult;
        for (String targetName : targetNames) {
            fuzzySearchResult = FuzzySearchManager.getIns().search(targetName, contactList);
            if (fuzzySearchResult != null) {
                foundName = fuzzySearchResult.getText();
                confidence = fuzzySearchResult.getConfidence();
                if (LogUtil.DEBUG) {
                    LogUtil.log(TAG, String.format("foundName: %1$s, confidence: %2$s", foundName, confidence));
                }
                if (!TextUtils.isEmpty(foundName)) {
                    if (confidence > FuzzySearchManager.getIns().getLowConfidenceCriteria()) {
                        return new DemoMatchedContact(DemoMatchedContact.MatchedType.FUZZY_MATCHED, foundName);
                    } else {
                        if (LogUtil.DEBUG) {
                            LogUtil.logd(TAG, "low confidence, LOW_CONFIDENCE_CRITERIA:" + FuzzySearchManager.getIns().getLowConfidenceCriteria());
                        }
                    }
                }
            }
        }
        return null;
    }

    public static class DemoMatchedContact {
        public final class MatchedType {
            public static final byte FUZZY_MATCHED = 0x02;
            public static final byte FULL_MATCHED = 0x03;
        }

        public byte matchedType;
        public String matchedName;

        DemoMatchedContact(byte matchType, String matchedName) {
            this.matchedType = matchType;
            this.matchedName = matchedName;
        }
    }
}
