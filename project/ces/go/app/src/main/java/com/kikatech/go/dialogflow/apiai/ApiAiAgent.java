package com.kikatech.go.dialogflow.apiai;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.kikatech.go.util.TimeUtil;
import com.kikatech.voice.core.dialogflow.Agent;
import com.kikatech.voice.core.dialogflow.intent.Intent;
import com.kikatech.voice.util.log.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIOutputContext;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Entity;
import ai.api.model.EntityEntry;
import ai.api.model.Metadata;
import ai.api.model.Result;

/**
 * @author SkeeterWang Created on 2017/11/3.
 */
public class ApiAiAgent extends Agent {

    private static final String TAG = "ApiAiAgent";

    private static final boolean DEBUG_ORIGINAL_DATA = false;

    private static final String CLIENT_ACCESS_TOKEN_JASON = "cda255c3a7284b6e927eec6a06d086ea";
    private static final String CLIENT_ACCESS_TOKEN_BRAD = "cf718669b9534bb1a89b37a2e0f5fc46";
    private static final String CLIENT_ACCESS_TOKEN = CLIENT_ACCESS_TOKEN_BRAD;

    private AIService mAIService;

    private long mLatestContactUploadTime = 0;

    ApiAiAgent(Context context) {
        final AIConfiguration config = new AIConfiguration(CLIENT_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        mAIService = AIService.getService(context, config);
    }

    @Override
    public Intent query(final String words, final Map<String, List<String>> entities, boolean anyContent) {

        if (LogUtil.DEBUG) LogUtil.logd(TAG, "query, words: " + words);

        final List<Entity> customEntities = createEntities(entities);

        final AIRequest request = new AIRequest();

        if (customEntities != null) {
            request.setEntities(customEntities);
        }
        if (!TextUtils.isEmpty(words)) {
            request.setQuery(words);
        }
        /*
        if (!TextUtils.isEmpty(context)) {
            final List<AIContext> contexts = Collections.singletonList(new AIContext(context));
            request.setContexts(contexts);
        }
        */

        AIResponse aiResponse = null;

        try {
            aiResponse = mAIService.textRequest(request);
        } catch (final AIServiceException e) {
            if (LogUtil.DEBUG) LogUtil.printStackTrace(TAG, e.getMessage(), e);
        }

        return fromResponse(aiResponse);
    }

    @Override
    public void resetContexts() {
        mAIService.resetContexts();
    }



    private List<Entity> createEntities(Map<String, List<String>> entities) {

        if (entities == null) {
            return null;
        }

        List<Entity> entityList = new ArrayList<>();

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

    private Intent fromResponse(AIResponse response) {

        if( LogUtil.DEBUG ) LogUtil.log( TAG, "fromResponse" );

        if (DEBUG_ORIGINAL_DATA) {
            printOriginalData(response);
        }

        if (response == null || response.isError()) {
            return null;
        }

        Result result = response.getResult();

        if (result == null) {
            return null;
        }

        final Metadata metadata = result.getMetadata();

        if (metadata == null) {
            return null;
        }

        String name = metadata.getIntentName();

        if (TextUtils.isEmpty(name)) {
            if (LogUtil.DEBUG) LogUtil.logd(TAG, "Err, name is empty");
            return null;
        }

        String scene = SceneType.getScene(name);

        if (TextUtils.isEmpty(scene)) {
            if (LogUtil.DEBUG) LogUtil.logd(TAG, "Err, scene is empty");
            return null;
        }

        String action = result.getAction();

        if (LogUtil.DEBUG) {
            LogUtil.logd(TAG, "scene: " + scene + ", action: " + action);
        }

        checkToUploadContacts(scene);

        Intent intent = new Intent(scene, action);

        final Map<String, JsonElement> params = result.getParameters();

        if (params == null || params.isEmpty()){
            return intent;
        }

        for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
            try {
                String key = entry.getKey();
                String value = entry.getValue().toString();
                if (!TextUtils.isEmpty(value)) {
                    intent.putExtra(key, value);
                }
            } catch (Exception e) {
                if (LogUtil.DEBUG) LogUtil.printStackTrace(TAG, e.getMessage(), e);
            }
        }

        return intent;
    }

    private void checkToUploadContacts(String scene) {
        if(scene.equals(SceneType.SEND_SMS.name())) {
            final long now = System.currentTimeMillis();
            if (now - mLatestContactUploadTime > 30 * TimeUtil.MILLIS_IN_MINUTE) {
                // TODO upload contacts to api.ai server
                mLatestContactUploadTime = now;
            }
        }
    }

    private void printOriginalData(AIResponse response) {
        if (DEBUG_ORIGINAL_DATA) {
            final String TAG = "ApiAiOriginalData";
            if (response == null) {
                LogUtil.logw(TAG, "response is null");
            } else if (response.isError()) {
                LogUtil.logw(TAG, "response with error");
            } else {
                Result result = response.getResult();
                if (result == null) {
                    LogUtil.logw(TAG, "result is null");
                } else {
                    LogUtil.log(TAG, "Source: " + result.getSource());
                    LogUtil.log(TAG, "Resolved Query: " + result.getResolvedQuery());
                    LogUtil.log(TAG, "Action: " + result.getAction());
                    LogUtil.log(TAG, "Score: " + result.getScore());

                    final Metadata metadata = result.getMetadata();
                    if( metadata != null ) {
                        LogUtil.log(TAG, "IntentId: " + metadata.getIntentId());
                        LogUtil.log(TAG, "IntentName: " + metadata.getIntentName());
                    }

                    final Map<String, JsonElement> params = result.getParameters();
                    if (params != null && !params.isEmpty()) {
                        for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                            try {
                                String key = entry.getKey();
                                String value = entry.getValue().toString();
                                LogUtil.logd(TAG, "[params] key: " + key + ", value: " + value);
                            } catch (Exception e) {
                                if (LogUtil.DEBUG) LogUtil.printStackTrace(TAG, e.getMessage(), e);
                            }
                        }
                    }

                    final List<AIOutputContext> contexts = result.getContexts();
                    if (contexts != null && !contexts.isEmpty()) {
                        for (AIOutputContext outputContext : contexts) {
                            LogUtil.log(TAG, "[Context] name: " + outputContext.getName() + ", lifespan: " + outputContext.getLifespan());
                            final Map<String, JsonElement> contextParams = outputContext.getParameters();
                            if (contextParams != null && !contextParams.isEmpty()) {
                                for (final Map.Entry<String, JsonElement> entry : contextParams.entrySet()) {
                                    try {
                                        String key = entry.getKey();
                                        String value = entry.getValue().toString();
                                        LogUtil.logv(TAG, "[Context params] key: " + key + ", value: " + value);
                                    } catch (Exception e) {
                                        if (LogUtil.DEBUG)
                                            LogUtil.printStackTrace(TAG, e.getMessage(), e);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
