package es.elultimorey.tweetlove;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

public class Search extends Activity {

    //TODO: Solo busca el nombre del usuario que se le pasa

    private final Activity mActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //TODO: Comprobar que hay internet
        String nombre = null;
        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            nombre = extras.getString("user");
        }

        Toast.makeText(mActivity, nombre, Toast.LENGTH_LONG).show();

        String[] array = {nombre};
        TextView tv = (TextView) findViewById(R.id.auxTextView);
        MyAsyncTask mt = new MyAsyncTask(tv);
        mt.execute(array);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyAsyncTask extends AsyncTask<String, Float, String> {
        //TODO: Añadir keys
        private static final String CONSUMER_KEY = " ";
        private static final String CONSUMER_SECRET = " ";


        private final WeakReference<TextView> TVR;

        public MyAsyncTask(TextView name) {
            TVR = new WeakReference<TextView>(name);
        }

        protected String doInBackground(String... user) {
            OAuth2Token token;

            token = getOAuth2Token();

            ConfigurationBuilder cb = new ConfigurationBuilder();

            cb.setApplicationOnlyAuthEnabled(true);
            cb.setOAuthConsumerKey(CONSUMER_KEY);
            cb.setOAuthConsumerSecret(CONSUMER_SECRET);
            cb.setOAuth2TokenType(token.getTokenType());
            cb.setOAuth2AccessToken(token.getAccessToken());

            Twitter twitter = new TwitterFactory(cb.build()).getInstance();

            // TODO: Obtener los tweets recientes y recorrer.

            try {
                User usuario = twitter.showUser(user[0]);
                return usuario.getName();
            } catch (Exception e) {
                return "Error al comprobar el usuario";
            }
        }
        @Override
        protected void onPostExecute(String nombre) {
            if (isCancelled()) {
                nombre = null;
            }

            if (TVR != null) {
                TextView textView = TVR.get();
                if (textView != null) {
                    textView.setText(nombre);
                }
            }
        }

        public OAuth2Token getOAuth2Token() {
            OAuth2Token token = null;
            ConfigurationBuilder cb;

            cb = new ConfigurationBuilder();
            cb.setApplicationOnlyAuthEnabled(true);

            cb.setOAuthConsumerKey(CONSUMER_KEY).setOAuthConsumerSecret(CONSUMER_SECRET);

            try {
                token = new TwitterFactory(cb.build()).getInstance().getOAuth2Token();
            } catch (Exception e) {
                Toast.makeText(mActivity, "Error: No se ha podido obtener OAuth2 token", Toast.LENGTH_LONG).show();
            }

            return token;
        }

    }
}
