package com.kikatech.voice.core.dialogflow.agent.apiai;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.kikatech.voice.core.dialogflow.Agent;
import com.kikatech.voice.core.dialogflow.intent.Intent;

import java.util.ArrayList;
import java.util.Collections;
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
import ai.api.model.Result;

/**
 * @author SkeeterWang Created on 2017/11/3.
 */
public class ApiAiAgent extends Agent {

    private static final String TAG = "ApiAiAgent";

    private static final String CLIENT_ACCESS_TOKEN = "cda255c3a7284b6e927eec6a06d086ea";

    private AIService mAIService;

    ApiAiAgent(Context context) {
        final AIConfiguration config = new AIConfiguration(CLIENT_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        mAIService = AIService.getService(context, config);
    }

    @Override
    public Intent query(final String words, final Map<String, List<String>> entities) {

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
            e.printStackTrace();
        }

        return fromResponse(aiResponse);
    }

    private Intent fromResponse(AIResponse response) {

        if (response == null || response.isError()) {
            return null;
        }

        Result result = response.getResult();

        if (result == null) {
            return null;
        }

        String apiAiAction = result.getAction();

        if (TextUtils.isEmpty(apiAiAction)) {
            return null;
        }

        String[] sceneAndAction = new String[]{apiAiAction, null};
        try {
            if (apiAiAction.contains(".")) {
                sceneAndAction = apiAiAction.split("\\.");
            } else if (apiAiAction.contains(" ")) {
                sceneAndAction = apiAiAction.split(" ");
            } else if (apiAiAction.contains("_")) {
                sceneAndAction = apiAiAction.split("_");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String scene = sceneAndAction[0];
        String action = sceneAndAction[1];

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
                e.printStackTrace();
            }
        }

        return intent;
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
}
