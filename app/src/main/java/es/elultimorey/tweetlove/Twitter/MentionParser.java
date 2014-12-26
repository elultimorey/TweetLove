package es.elultimorey.tweetlove.Twitter;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.Status;

/**
 * Created by jose on 31/07/14.
 */
public class MentionParser {

    public List<String> getMention(Status tweet) {

        List<String> menciones = new LinkedList<String>();
        // Expresión regular sacada de: https://code.google.com/p/javatweet/source/browse/branches/sospartan/src/com/twitter/Regex.java
        Pattern p = Pattern.compile("(^|[^a-z0-9_])[@\\uFF20]([a-z0-9_]{1,20})(?=(.|$))", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(tweet.getText());
        while(m.find()){
            menciones.add(m.group());
        }
        return menciones;
    }
}
