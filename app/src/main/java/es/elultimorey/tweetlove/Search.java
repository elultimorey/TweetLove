package es.elultimorey.tweetlove;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.List;

import es.elultimorey.tweetlove.Twitter.ControladorTwitter;
import es.elultimorey.tweetlove.Twitter.MentionParser;
import es.elultimorey.tweetlove.Twitter.Mentioned;
import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.User;

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

        private final WeakReference<TextView> TVR;

        public MyAsyncTask(TextView name) {
            TVR = new WeakReference<TextView>(name);
        }

        protected String doInBackground(String... users) {
            try {
                Twitter twitter = ControladorTwitter.getInstance().getTwitter();
                User user = twitter.showUser(users[0]);

                Mentioned mentioned = new Mentioned();

                Paging paging = new Paging(1, 100); // Para más peticiones paging.setPage(2)...
                List<twitter4j.Status> tweets = null;
                tweets = twitter.getUserTimeline(user.getScreenName(), paging);
                twitter4j.Status status;
                List<String> mentionedList;
                for (int i = 0; i < tweets.size(); i++) {
                    status = tweets.get(i);
                    mentionedList = new MentionParser().getMention(status);
                    if (mentionedList.size()>0) {
                        mentioned.addMentioned(mentionedList);
                    }
                }

                // TODO favs? contemplar

                String mMentioned = mentioned.getMoreMentioned();

                return mMentioned;
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

    }
}
