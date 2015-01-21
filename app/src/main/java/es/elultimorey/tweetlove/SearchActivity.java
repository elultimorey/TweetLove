package es.elultimorey.tweetlove;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.manuelpeinado.fadingactionbar.extras.actionbarcompat.FadingActionBarHelper;
import com.nvanbenschoten.motion.ParallaxImageView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import es.elultimorey.tweetlove.Twitter.ControladorTwitter;
import es.elultimorey.tweetlove.Twitter.MentionParser;
import es.elultimorey.tweetlove.Twitter.Mentioned;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by jose on 23/12/14.
 */
public class SearchActivity extends ActionBarActivity {

    public final static int SHOW_LOVED = 01;
    public final static int USER_PRIVATE = 21;
    public final static int USER_HAVENT_MENTIONS = 22;
    public final static int USER_DOESNT_EXIST = 23;

    ActionBarActivity mActivity = this;
    FadingActionBarHelper helper;
    String username;
    Boolean checkbox;

    private User lovedGlobal;
    private boolean haventMentions = false;


    ParallaxImageView background;
    LinearLayout progress;
    LinearLayout headerOverlay;
    RelativeLayout searchLayout;
    RelativeLayout locationLayout;
    RelativeLayout webLayout;


    private ShareActionProvider mShareActionProvider;
    private String mShareString;

    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set view
        helper = new FadingActionBarHelper()
                .actionBarBackground(R.drawable.action_bar_background)
                .headerLayout(R.layout.header)
                .contentLayout(R.layout.activity_search)
                .headerOverlayLayout(R.layout.header_overlay);
        setContentView(helper.createView(mActivity));
        helper.initActionBar(mActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progress = (LinearLayout) findViewById(R.id.layout_header_progress_bar);
        headerOverlay = (LinearLayout) findViewById(R.id.layout_header_overlay);
        searchLayout = (RelativeLayout) findViewById(R.id.search_layout);

        progress.setAlpha(100);
        headerOverlay.setAlpha(0);
        searchLayout.setAlpha(0);

        // get username
        SharedPreferences sharedPreferences = getSharedPreferences("MP", Context.MODE_PRIVATE);

        username = sharedPreferences.getString("username", "@@@@@");
        checkbox = sharedPreferences.getBoolean("checkbox", false);


        // get references

        background = (ParallaxImageView) findViewById(R.id.image_header);
        ImageView profileImage = (ImageView) findViewById(R.id.image_overlay);
        TextView name = (TextView) findViewById(R.id.name);
        TextView screenName = (TextView) findViewById(R.id.screenName);
        TextView description = (TextView) findViewById(R.id.description);
        TextView location = (TextView) findViewById(R.id.location_text);
        TextView urlTV = (TextView) findViewById(R.id.url_text);
        TextView created = (TextView) findViewById(R.id.created_text);
        TextView tweets = (TextView) findViewById(R.id.numbers_tweets_number);
        TextView following = (TextView) findViewById(R.id.numbers_following_number);
        TextView followers = (TextView) findViewById(R.id.numbers_followers_number);
        ImageView protectedProfile = (ImageView) findViewById(R.id.protected_profile);

        locationLayout = (RelativeLayout) findViewById(R.id.location_layout);
        webLayout = (RelativeLayout) findViewById(R.id.url_layout);


        String[] array = {username};
        SearchAsyncTask mSearchAsyncTask = new SearchAsyncTask(background, profileImage, name,
                screenName, description, location, urlTV, created, tweets, following, followers,
                protectedProfile);
        mSearchAsyncTask.execute(array);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lovedGlobal!=null) {
                    openLovedProfile();
                }
            }
        });

        RelativeLayout namesLayout = (RelativeLayout) findViewById(R.id.names_layout);
        namesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lovedGlobal!=null) {
                    openLovedProfile();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem item = menu.findItem(R.id.menu_search_action_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search_action_about:
                startActivity(new Intent(getApplicationContext(), About.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        // animación header
        if (background!=null)
            background.registerSensorManager();
        // share
        if (mShareActionProvider != null) {
            Intent myIntent = new Intent();
            myIntent.setAction(Intent.ACTION_SEND);
            myIntent.putExtra(Intent.EXTRA_TEXT, mShareString);
            myIntent.setType("text/plain");
            mShareActionProvider.setShareIntent(myIntent);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (background!=null)
            background.unregisterSensorManager();
        super.onPause();
    }

    private class SearchAsyncTask extends AsyncTask<String, Float, User> {

        private final WeakReference<ParallaxImageView> backgroundWeakReference;
        private final WeakReference<ImageView> profileImageWeakReference;
        private final WeakReference<TextView> nameWeakReference;
        private final WeakReference<TextView> screenNameWeakReference;
        private final WeakReference<TextView> descriptionWeakReference;
        private final WeakReference<TextView> locationWeakReference;
        private final WeakReference<TextView> urlWeakReference;
        private final WeakReference<TextView> createdWeakReference;
        private final WeakReference<TextView> tweetsWeakReference;
        private final WeakReference<TextView> followingWeakReference;
        private final WeakReference<TextView> followersWeakReference;
        private final WeakReference<ImageView> protectedProfileWeakReference;


        private Bitmap image = null;
        private Bitmap background = null;

        public SearchAsyncTask (ParallaxImageView background, ImageView profileImage,
                                TextView name, TextView screenName, TextView description,
                                TextView location, TextView urlTextView, TextView created,
                                TextView tweets, TextView following, TextView followers,
                                ImageView protectedProfile) {
            super();
            backgroundWeakReference = new WeakReference<ParallaxImageView>(background);
            profileImageWeakReference = new WeakReference<ImageView>(profileImage);
            nameWeakReference = new WeakReference<TextView>(name);
            screenNameWeakReference = new WeakReference<TextView>(screenName);
            descriptionWeakReference = new WeakReference<TextView>(description);
            locationWeakReference = new WeakReference<TextView>(location);
            urlWeakReference = new WeakReference<TextView>(urlTextView);
            createdWeakReference = new WeakReference<TextView>(created);
            tweetsWeakReference = new WeakReference<TextView>(tweets);
            followingWeakReference = new WeakReference<TextView>(following);
            followersWeakReference = new WeakReference<TextView>(followers);
            protectedProfileWeakReference = new WeakReference<ImageView>(protectedProfile);

        }

        @Override
        protected User doInBackground(String... users) {
            User user = null;
            try {
                Twitter twitter = ControladorTwitter.getInstance().getTwitter();
                user = twitter.showUser(users[0]);
                Mentioned mentioned = new Mentioned(user.getScreenName());
                if (!checkbox) {        // Get loved
                    Paging paging = new Paging(1, 100); // Para más peticiones paging.setPage(2)...
                    List<twitter4j.Status> tweets = null;
                    tweets = twitter.getUserTimeline(user.getScreenName(), paging);
                    twitter4j.Status status;
                    List<String> mentionedList;
                    for (int i = 0; i < tweets.size(); i++) {
                        status = tweets.get(i);
                        mentionedList = new MentionParser().getMention(status);
                        if (mentionedList.size() > 0) {
                            mentioned.addMentioned(mentionedList);
                        }
                    }
                } else {
                    try {
                        Query query = new Query("@" + username);
                        QueryResult result;
                        int count = 0;
                        do {
                            result = twitter.search(query);
                            List<twitter4j.Status> tweets = result.getTweets();
                            for (twitter4j.Status tweet : tweets) {
                                mentioned.addMentioned("@" + tweet.getUser().getScreenName());
                            }
                            count += tweets.size();
                        } while ((query = result.nextQuery()) != null && count <= 100);
                    } catch (TwitterException te) {
                        Log.d("###", "exception query");
                    }
                }
                if (!mentioned.isEmpty()) {
                    User mMentioned = twitter.showUser(mentioned.getMoreMentioned().substring(1, mentioned.getMoreMentioned().length()));
                    lovedGlobal = mMentioned;
                    image = downloadBitmap(mMentioned.getOriginalProfileImageURL());
                    try {
                        // The banner comes always cutted
                        background = downloadBitmap(mMentioned.getProfileBannerURL().substring(0, mMentioned.getProfileBannerURL().length() - 3) + "1500x500");
                    } catch (Exception e) {
                        // BACKGROUND DOWNLOAD EXCEPTION
                        background = null;
                    }
                    if (mMentioned.getURL() != null) {
                        HttpURLConnection con = (HttpURLConnection) new URL(mMentioned.getURL()).openConnection();
                        con.setInstanceFollowRedirects(false);
                        con.connect();
                        url = con.getHeaderField("Location").toString();
                    }
                    return mMentioned;
                } else {

                    haventMentions = true;
                    setResult(USER_HAVENT_MENTIONS);
                    finish();
                    return null;
                }
            } catch (Exception e) {
                if (user != null && user.isProtected()) {
                    // private user
                    setResult(USER_PRIVATE);
                    finish();
                    return null;
                } else {
                    // user doesnt exist
                    setResult(USER_DOESNT_EXIST);
                    finish();
                    return null;
                }
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
                        parallaxImageView.setImageBitmap(background);
                        // Adjust the Parallax forward tilt adjustment
                        parallaxImageView.setForwardTiltOffset(.35f);
                        parallaxImageView.setParallaxIntensity(1.2f);
                        // Register a SensorManager to begin effect
                        parallaxImageView.registerSensorManager();

                    }
                }
                if (profileImageWeakReference != null) {
                    ImageView imageView = profileImageWeakReference.get();
                    if (imageView != null && image != null) {
                        imageView.setImageBitmap(image);
                        headerOverlay.setAlpha(100);
                    }
                }
                if (nameWeakReference != null) {
                    TextView textView = nameWeakReference.get();
                    if (textView != null) {
                        textView.setText(user.getName());
                    }
                }
                if (user.isProtected()) {
                    ImageView imageView = protectedProfileWeakReference.get();
                    if (imageView != null) {
                        imageView.setVisibility(View.VISIBLE);
                    }
                }
                if (screenNameWeakReference != null) {
                    TextView textView = screenNameWeakReference.get();
                    if (textView != null) {
                        textView.setText("@" + user.getScreenName());
                    }
                }
                if (descriptionWeakReference != null) {
                    TextView textView = descriptionWeakReference.get();
                    if (textView != null && user.getDescription()!=null)
                        textView.setText(user.getDescription());
                }
                if (locationWeakReference != null) {
                    TextView textView = locationWeakReference.get();
                    if (textView != null && !user.getLocation().isEmpty())
                        textView.setText(user.getLocation());
                    else {
                        locationLayout.setVisibility(View.GONE);
                    }
                }
                if (urlWeakReference != null) {
                    TextView textView = urlWeakReference.get();
                    if (textView != null && url!=null && !url.isEmpty()) {
                        SpannableString content = new SpannableString(url);
                        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                        textView.setText(content);
                        textView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(url));
                                startActivity(i);
                            }
                        });
                    }
                    else
                        webLayout.setVisibility(View.GONE);
                }
                if (createdWeakReference != null) {
                    TextView textView = createdWeakReference.get();
                    if (textView != null && user.getCreatedAt() != null )

                        textView.setText(getResources().getString(R.string.joined)+ " " +
                                getResources().getString(getMonth(user.getCreatedAt().getMonth()))+ " " +
                                getResources().getString(R.string.de)+ " " +
                                (user.getCreatedAt().getYear()+1900));
                }
                if (tweetsWeakReference != null) {
                    TextView textView = tweetsWeakReference.get();
                    if (textView != null)
                        textView.setText(String.valueOf(user.getStatusesCount()));
                }
                if (followingWeakReference != null) {
                    TextView textView = followingWeakReference.get();
                    if (textView != null)
                        textView.setText(String.valueOf(user.getFriendsCount()));
                }
                if (followersWeakReference != null) {
                    TextView textView = followersWeakReference.get();
                    if (textView != null)
                        textView.setText(String.valueOf(user.getFollowersCount()));
                }

                // Manage layouts
                searchLayout.setAlpha(100);
                YoYo.with(Techniques.BounceInUp).duration(500).playOn(findViewById(R.id.image_overlay));
                YoYo.with(Techniques.BounceInDown).duration(500).playOn(findViewById(R.id.names_layout));

                mActivity.getSupportActionBar().setTitle("@"+ username);
                if (checkbox)
                    mActivity.getSupportActionBar().setSubtitle(getResources().getString(R.string.search_loved) + " @" + lovedGlobal.getScreenName());
                else
                    mActivity.getSupportActionBar().setSubtitle(getResources().getString(R.string.search_loves) + " @" + lovedGlobal.getScreenName());


                // Manage ShareActionProvider
                Intent myIntent = new Intent();
                myIntent.setAction(Intent.ACTION_SEND);
                mShareString = ".@" + username + " " +
                        getResources().getText(R.string.search_loves) + " @" +
                        lovedGlobal.getScreenName() + " via " + getResources().getText(R.string.app_url);
                myIntent.putExtra(Intent.EXTRA_TEXT, mShareString);
                myIntent.setType("text/plain");
                mShareActionProvider.setShareIntent(myIntent);


                progress.setAlpha(0);
                headerOverlay.setAlpha(100);
            }
            else {
                if (haventMentions)
                    setResult(00);
                else
                    setResult(00);
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
                Toast.makeText(getApplicationContext(), "Error cargando la imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return null;
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

    private int getMonth(int m) {
        switch (m) {
            case 0:
                return R.string.january;
            case 1:
                return R.string.february;
            case 2:
                return R.string.march;
            case 3:
                return R.string.april;
            case 4:
                return R.string.may;
            case 5:
                return R.string.june;
            case 6:
                return R.string.july;
            case 7:
                return R.string.august;
            case 8:
                return R.string.september;
            case 9:
                return R.string.october;
            case 10:
                return R.string.november;
            case 11:
                return R.string.december;
        }
        return R.string.blank;
    }
}