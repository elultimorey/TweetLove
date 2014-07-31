package es.elultimorey.tweetlove.Twitter;

import android.util.Log;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by jose on 31/07/14.
 */
public class Controlador {
    private static Controlador INSTANCE = new Controlador();

    //TODO: Añadir y borrar keys
    private static final String CONSUMER_KEY = " ";
    private static final String CONSUMER_SECRET = " ";

    private Twitter twitter;

    public Twitter getTwitter() {
        return twitter;
    }

    private Controlador() {

        OAuth2Token token;

        token = getOAuth2Token();

        ConfigurationBuilder cb = new ConfigurationBuilder();

        cb.setApplicationOnlyAuthEnabled(true);
        cb.setOAuthConsumerKey(CONSUMER_KEY);
        cb.setOAuthConsumerSecret(CONSUMER_SECRET);
        cb.setOAuth2TokenType(token.getTokenType());
        cb.setOAuth2AccessToken(token.getAccessToken());

        twitter = new TwitterFactory(cb.build()).getInstance();
    }

    public static Controlador getInstance() {
        return INSTANCE;
    }

    private OAuth2Token getOAuth2Token() {
        OAuth2Token token = null;
        ConfigurationBuilder cb;

        cb = new ConfigurationBuilder();
        cb.setApplicationOnlyAuthEnabled(true);

        cb.setOAuthConsumerKey(CONSUMER_KEY).setOAuthConsumerSecret(CONSUMER_SECRET);

        try {
            token = new TwitterFactory(cb.build()).getInstance().getOAuth2Token();
        } catch (Exception e) {
            Log.d("Exception", "TwitterFactory->getInstace");
        }

        return token;
    }

}
