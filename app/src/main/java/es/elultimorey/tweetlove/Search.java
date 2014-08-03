package es.elultimorey.tweetlove;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nvanbenschoten.motion.ParallaxImageView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import es.elultimorey.tweetlove.Twitter.ControladorTwitter;
import es.elultimorey.tweetlove.Twitter.MentionParser;
import es.elultimorey.tweetlove.Twitter.Mentioned;
import es.elultimorey.tweetlove.UI.CircularImageView;
import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.User;

public class Search extends Activity {

    //TODO: Solo busca el nombre del usuario que se le pasa

    private final Activity mActivity = this;
    private RelativeLayout lovedLayout;
    private RelativeLayout lovedLayoutWho;
    private User userGlobal;
    private User lovedGlobal;
    private ShareActionProvider mShareActionProvider;
   private ParallaxImageView mBackground=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //TODO: Comprobar que hay internet
        String nombre = null;
        Bundle extras = getIntent().getExtras();
        // se comprueba en Inbox.java que se pasa un usuario
        nombre = extras.getString("user");
        getActionBar().setTitle("@"+nombre+" loves");

        lovedLayout = (RelativeLayout) findViewById(R.id.loved_layout);
        lovedLayout.setAlpha(0);
        lovedLayoutWho = (RelativeLayout) findViewById(R.id.loved_layout_who);

        String[] array = {nombre};
        CircularImageView profileImage = (CircularImageView) findViewById(R.id.profileImage);
        profileImage.addShadow();
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                YoYo.with(Techniques.Pulse).duration(700).playOn(findViewById(R.id.profileImage));
                openLovedProfile();
            }
        });
        TextView name = (TextView) findViewById(R.id.name);
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                YoYo.with(Techniques.Pulse).duration(700).playOn(findViewById(R.id.names_layout));
                openLovedProfile();
            }
        });
        TextView screenName = (TextView) findViewById(R.id.screenName);
        screenName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                YoYo.with(Techniques.Pulse).duration(700).playOn(findViewById(R.id.names_layout));
                openLovedProfile();
            }
        });
        ParallaxImageView backgroundImage = (ParallaxImageView) findViewById(R.id.profileBackground);

        MyAsyncTask mt = new MyAsyncTask(backgroundImage, profileImage, name, screenName);
        mt.execute(array);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);

        ShareActionProvider myShareActionProvider = (ShareActionProvider) item.getActionProvider();

        Intent myIntent = new Intent();
        myIntent.setAction(Intent.ACTION_SEND);
        myIntent.putExtra(Intent.EXTRA_TEXT, "@elultimorey huehuhuehue @elultimorey");
        myIntent.setType("text/plain");

        myShareActionProvider.setShareIntent(myIntent);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBackground!=null)
            mBackground.unregisterSensorManager();
    }

    @Override
    public void onPause() {
        mBackground.unregisterSensorManager();
        super.onPause();
    }

    private class MyAsyncTask extends AsyncTask<String, Float, User> {

        private final WeakReference<ParallaxImageView> backgroundWeakReference;
        private final WeakReference<ImageView> profileImageWeakReference;
        private final WeakReference<TextView> nameWeakReference;
        private final WeakReference<TextView> screenNameWeakReference;

        private Bitmap image = null;
        private Bitmap background = null;

        public MyAsyncTask(ParallaxImageView backgroundImage, ImageView profileImage, TextView name, TextView screenName) {
            backgroundWeakReference = new WeakReference<ParallaxImageView>(backgroundImage);
            profileImageWeakReference = new WeakReference<ImageView>(profileImage);
            nameWeakReference = new WeakReference<TextView>(name);
            screenNameWeakReference = new WeakReference<TextView>(screenName);
        }

        protected User doInBackground(String... users) {
            try {
                Twitter twitter = ControladorTwitter.getInstance().getTwitter();
                User user = twitter.showUser(users[0]);
                userGlobal = user;
                Mentioned mentioned = new Mentioned(user.getScreenName());

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

                User mMentioned = twitter.showUser(mentioned.getMoreMentioned().substring(1, mentioned.getMoreMentioned().length()));
                lovedGlobal = mMentioned;
                image = downloadBitmap(mMentioned.getOriginalProfileImageURL());
                background = downloadBitmap(mMentioned.getProfileBackgroundImageURL());
                return mMentioned;
            } catch (Exception e) {
                return null;
            }
        }
        @Override
        protected void onPostExecute(User user) {
            if (isCancelled()) {
                user = null;
            }
            if (user != null) {
                if (backgroundWeakReference != null) {
                    ParallaxImageView parallaxImageView = backgroundWeakReference.get();
                    if (parallaxImageView != null && background != null) {
                        mBackground = (ParallaxImageView) findViewById(R.id.profileBackground);
                        mBackground.setImageDrawable(new BitmapDrawable(getResources(), background));
                        // Adjust the Parallax forward tilt adjustment
                        mBackground.setForwardTiltOffset(.35f);
                        mBackground.setParallaxIntensity(1.1f);
                        // Register a SensorManager to begin effect
                        mBackground.registerSensorManager();
                    }
                }
                if (profileImageWeakReference != null) {
                    ImageView imageView = profileImageWeakReference.get();
                    if (imageView != null && image != null) {
                        imageView.setImageBitmap(image);
                    }
                }
                if (nameWeakReference != null) {
                    TextView textView = nameWeakReference.get();
                    if (textView != null) {
                        textView.setText(user.getName());
                    }
                }
                if (screenNameWeakReference != null) {
                    TextView textView = screenNameWeakReference.get();
                    if (textView != null) {
                        textView.setText("@" + user.getScreenName());
                    }
                }
                lovedLayoutWho.setAlpha(0);
                lovedLayout.setAlpha(100);
                YoYo.with(Techniques.Tada).duration(700).playOn(findViewById(R.id.profileImage));
                YoYo.with(Techniques.BounceIn).duration(700).playOn(findViewById(R.id.profileImage));
                YoYo.with(Techniques.BounceInDown).duration(700).playOn(findViewById(R.id.names_layout));
            }
            else {
                setResult(Inbox.USER_NOT_EXIST);
                finish();
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

    public String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            Log.wtf("urlEnconde", "UTF-8 should always be supported", e);
            throw new RuntimeException("URLEncoder.encode() failed for " + s);
        }
    }

    public void openLovedProfile() {
        try {
            // Check if the Twitter app is installed
            getPackageManager().getPackageInfo("com.twitter.android", 0);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName("com.twitter.android", "com.twitter.android.ProfileActivity");
            // Don't forget to put the "L" at the end of the id.
            intent.putExtra("user_id", lovedGlobal.getId());
            startActivity(intent);
        }
        catch (PackageManager.NameNotFoundException e) {
            // If Twitter app is not installed, start browser.
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/"+lovedGlobal.getScreenName())));
        }
    }
    private Intent getDefaultIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        return intent;
    }
}
