package es.elultimorey.tweetlove.Twitter;

import android.util.Pair;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by jose on 31/07/14.
 */
public class Mentioned {
    private HashMap<String, Integer> mentioned;

    public Mentioned () {
        this.mentioned = new HashMap<String, Integer>();
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
            if ((Integer) e.getValue() > max) {
                screenName = (String) e.getKey();
                max = (Integer) e.getValue();
            }
        }
        return screenName;
    }
}
