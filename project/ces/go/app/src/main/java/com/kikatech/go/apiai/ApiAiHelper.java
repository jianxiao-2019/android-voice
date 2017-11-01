package com.kikatech.go.apiai;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.kikatech.go.intention.Intention;
import com.kikatech.go.intention.IntentionBuilder;
import com.kikatech.go.intention.IntentionManager;
import com.kikatech.go.util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIContext;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Entity;
import ai.api.model.EntityEntry;
import ai.api.model.Metadata;
import ai.api.model.Result;
import ai.api.model.Status;

/**
 * @author jasonli Created on 2017/10/27.
 */

public class ApiAiHelper {

    private static final String TAG = "ApiAiHelper";

    private static final String CLIENT_ACCESS_TOKEN = "cda255c3a7284b6e927eec6a06d086ea";

    private static ApiAiHelper sApiAiHelper;

    private AIService aiService;

    private ExecutorService mExecutor;

    public static ApiAiHelper getInstance(Context context) {
        if (sApiAiHelper == null) {
            sApiAiHelper = new ApiAiHelper(context);
        }
        return sApiAiHelper;
    }

    private ApiAiHelper(Context context) {
        final AIConfiguration config = new AIConfiguration(CLIENT_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(context.getApplicationContext(), config);
        mExecutor = Executors.newSingleThreadExecutor();
    }

    public void resetContext() {
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                aiService.resetContexts();
            }
        });
    }

    public void queryIntention(String speech) {
        queryIntention(null, speech);
    }

    public void queryIntention(final String context, final String speech) {
        queryIntention(context, speech, null);
    }

    public static List<Entity> mCustomEntities;
    public void queryIntention(final String context, final String speech, final Map<String, List<String>> entities) {

        final List<Entity> customEntities = createEntities(entities);
        //Collection<T> collection = new ArrayList<T>(myList);
        /*if (mCustomEntities == null) {
            try {
                aiService.uploadUserEntities(new ArrayList<>(customEntities));
            } catch (Exception e) {
                e.printStackTrace();
            }
            mCustomEntities = customEntities;
        }*/


        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                final AIRequest request = new AIRequest();

                if (customEntities != null) {
                    request.setEntities(customEntities);
                }

                if (!TextUtils.isEmpty(speech)) request.setQuery(speech);
                if (!TextUtils.isEmpty(context)) {
                    final List<AIContext> contexts = Collections.singletonList(new AIContext(context));
                    request.setContexts(contexts);
                }
                AIResponse aiResponse = null;
                try {
                    aiResponse = aiService.textRequest(request);
                } catch (final AIServiceException e) {
                    e.printStackTrace();
                }



                printResponse(aiResponse);
                Intention intention = fromResponse(aiResponse);

                IntentionManager.getInstance().processIntention(intention);
            }
        });
    }

    private Intention fromResponse(AIResponse response) {
        if (response.isError()) return null;

        Result result = response.getResult();

        String action = result.getAction();
        IntentionBuilder builder = new IntentionBuilder(action);

        final Map<String, JsonElement> params = result.getParameters();
        if (params != null && !params.isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                try {
                    String key = entry.getKey();
                    String value = entry.getValue().toString();
                    if (!TextUtils.isEmpty(value)) {
                        builder.addParam(key, value);
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }

        return builder.build();
    }





    private List<Entity> createEntities(Map<String, List<String>> entities) {
        List<Entity> entityList = new ArrayList<>();
        if (entities == null) {
            return null;
        }

        for (String entityName : entities.keySet()) {
            List<String> entries = entities.get(entityName);

            Entity entity = new Entity();
            entity.setName(entityName);

            for (String entryName : entries) {
                EntityEntry entry = new EntityEntry();
                entry.setValue(entryName);
                List<String> synonyms = new ArrayList<>();
                synonyms.add(entryName);
                entry.setSynonyms(synonyms);

                entity.addEntry(entry);
            }

            entityList.add(entity);
        }
        return entityList;
    }





    private void printResponse(AIResponse response) {
        if (LogUtil.DEBUG) {
            LogUtil.log(TAG, "------- [START] Received success response -------");

            // this is example how to get different parts of result object
            final Status status = response.getStatus();
            LogUtil.logv(TAG, "Status code: " + status.getCode());
            LogUtil.logv(TAG, "Status type: " + status.getErrorType());

            final Result result = response.getResult();
            LogUtil.logv(TAG, "Resolved query: " + result.getResolvedQuery());

            LogUtil.logv(TAG, "Action: " + result.getAction());

            final String speech = result.getFulfillment().getSpeech();
            LogUtil.logv(TAG, "Speech: " + speech);

            final Metadata metadata = result.getMetadata();
            if (metadata != null) {
                LogUtil.logv(TAG, "Intent id: " + metadata.getIntentId());
                LogUtil.logv(TAG, "Intent name: " + metadata.getIntentName());
            }

            final HashMap<String, JsonElement> params = result.getParameters();
            if (params != null && !params.isEmpty()) {
                LogUtil.logv(TAG, "Parameters: ");
                for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                    LogUtil.logv(TAG, String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
                }
            }
            LogUtil.log(TAG, "------- [END] Received success response -------");
        }
    }
}
