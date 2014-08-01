package es.elultimorey.tweetlove.Twitter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by jose on 31/07/14.
 */
public class Mentioned {
    private HashMap<String, Integer> mentioned;
    private String userScreenName;

    public Mentioned (String screenName) {
        this.mentioned = new HashMap<String, Integer>();
        this.userScreenName = screenName;
    }

    public void addMentioned(String mention) {
        Integer num = 1;
        if (this.mentioned.containsKey(mention)) {
            num = this.mentioned.get(mention)+1;
        }
        mentioned.put(mention, num);
    }
    public void addMentioned (List<String> mentions) {
        for (int i=0;i<mentions.size();i++) {
            this.addMentioned(mentions.get(i));
        }
    }

    public String getMoreMentioned() {
        String screenName = "";
        Integer max = 0;
        Iterator it = this.mentioned.entrySet().iterator();
        for (Map.Entry e: this.mentioned.entrySet()) {
            if (((Integer) e.getValue() > max) && !(((String)e.getKey()).equals(" @"+userScreenName))) {
                screenName = (String) e.getKey();
                max = (Integer) e.getValue();
            }
        }
        return screenName;
    }
}
