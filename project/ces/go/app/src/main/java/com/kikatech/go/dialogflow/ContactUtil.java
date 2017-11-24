package com.kikatech.go.dialogflow;

import android.content.Context;

import com.kikatech.go.util.LogUtil;
import com.kikatech.voice.util.contact.ContactManager;

import java.util.List;

/**
 * Created by brad_chang on 2017/11/24.
 */

public class ContactUtil {

    public static class MatchedContact {
        public boolean isContactMatched = false;
        public String contactMatchedName = "";
        public List<ContactManager.NumberType> phoneNumbers;
    }

    /**
     * Check if user said contact matched the name in Contact
     *
     * @return is contact matched
     */
    public static MatchedContact matchContact(Context ctx, String contactName) {
        MatchedContact mc = new MatchedContact();
        ContactManager.PhoneBookContact pbc = ContactManager.getIns().findName(ctx, contactName);
        if (pbc != null) {
            mc.contactMatchedName = pbc.displayName;
            mc.phoneNumbers = pbc.phoneNumbers;
            mc.isContactMatched = true;

            if (LogUtil.DEBUG) {
                StringBuilder numb = new StringBuilder();
                for (ContactManager.NumberType n : mc.phoneNumbers) {
                    numb.append(n.number).append(":").append(n.type).append(", ");
                }
                LogUtil.log("ContactUtil", "Find " + mc.contactMatchedName + ", numbers:" + numb);
            }
        } else {
            if (LogUtil.DEBUG) LogUtil.log("ContactUtil", "findName fail");
            mc.isContactMatched = false;
        }

        if (LogUtil.DEBUG)
            LogUtil.log("ContactUtil", "matchContact:" + mc.isContactMatched);

        return mc;
    }
}
