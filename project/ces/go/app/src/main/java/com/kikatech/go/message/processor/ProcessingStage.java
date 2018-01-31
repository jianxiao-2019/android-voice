package com.kikatech.go.message.processor;

/**
 * @author jasonli Created on 2017/10/24.
 */

public interface ProcessingStage {

    interface BaseStage {
        String STAGE_DONE                   = "STAGE_DONE";
        String STAGE_FAILED                 = "STAGE_FAILED";
        String STAGE_UNKNOWN                = "STAGE_UNKNOWN";
    }

    interface IMProcessStage extends BaseStage {
        String STAGE_INITIAL                = "STAGE_INITIAL";
        String STAGE_OPEN_SHARE_INTENT      = "STAGE_OPEN_SHARE_INTENT";
        String STAGE_CLICK_SEARCH_BUTTON    = "STAGE_CLICK_SEARCH_BUTTON";
        String STAGE_ENTER_USER_NAME        = "STAGE_ENTER_USER_NAME";
        String STAGE_PICK_USER              = "STAGE_PICK_USER";
        String STAGE_CLICK_SEND_BUTTON      = "STAGE_CLICK_SEND_BUTTON";
    }
}
