package es.elultimorey.tweetlove;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
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

        String[] array = {nombre};
        TextView screenName = (TextView) findViewById(R.id.auxTextView);
        ImageView profileImage = (ImageView) findViewById(R.id.profileImage);
        MyAsyncTask mt = new MyAsyncTask(screenName, profileImage);
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

        private final WeakReference<TextView> screenNameWeakReference;
        private final WeakReference<ImageView> profileImageWeakReference;

        private Bitmap image = null;

        public MyAsyncTask(TextView screenName, ImageView profileImage) {
            screenNameWeakReference = new WeakReference<TextView>(screenName);
            profileImageWeakReference = new WeakReference<ImageView>(profileImage);
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

                String screenNameMMentioned = mentioned.getMoreMentioned();
                User mMentioned = twitter.showUser(screenNameMMentioned.substring(1, screenNameMMentioned.length()));
                image = downloadBitmap(mMentioned.getOriginalProfileImageURL());
                return screenNameMMentioned;
            } catch (Exception e) {
                return null;
            }
        }
        @Override
        protected void onPostExecute(String user) {
            if (isCancelled()) {
                user = null;
            }
            if (profileImageWeakReference != null) {
                ImageView imageView = profileImageWeakReference.get();
                if (imageView != null && image != null) {
                    imageView.setImageBitmap(image);
                }
            }
            if (screenNameWeakReference != null) {
                TextView textView = screenNameWeakReference.get();
                if (textView != null) {
                    textView.setText(user);
                }
            }
        }

        private Bitmap downloadBitmap(String url) {
            URL imageUrl = null;
            try {
                imageUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
                conn.connect();
                return BitmapFactory.decodeStream(conn.getInputStream());
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Error cargando la imagen: "+e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return null;
        }

    }
}
