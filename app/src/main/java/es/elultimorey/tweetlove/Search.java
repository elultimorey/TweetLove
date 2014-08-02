package es.elultimorey.tweetlove;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

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
    private FloatingActionMenu rightLowerMenu;
    private User userGlobal;
    private User lovedGlobal;

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
            }
        });
        RelativeLayout lineLayout = (RelativeLayout) findViewById(R.id.line);
        lineLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                YoYo.with(Techniques.Pulse).duration(700).playOn(findViewById(R.id.line));
            }
        });
        TextView name = (TextView) findViewById(R.id.name);
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                YoYo.with(Techniques.Pulse).duration(700).playOn(findViewById(R.id.names_layout));
            }
        });
        TextView screenName = (TextView) findViewById(R.id.screenName);
        screenName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                YoYo.with(Techniques.Pulse).duration(700).playOn(findViewById(R.id.names_layout));
            }
        });

        MyAsyncTask mt = new MyAsyncTask(profileImage, name, screenName);
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

    private class MyAsyncTask extends AsyncTask<String, Float, User> {

        private final WeakReference<ImageView> profileImageWeakReference;
        private final WeakReference<TextView> nameWeakReference;
        private final WeakReference<TextView> screenNameWeakReference;

        private Bitmap image = null;

        public MyAsyncTask(ImageView profileImage, TextView name, TextView screenName) {
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
                YoYo.with(Techniques.Pulse).duration(700).playOn(findViewById(R.id.line));
                YoYo.with(Techniques.Tada).duration(700).playOn(findViewById(R.id.profileImage));
                YoYo.with(Techniques.BounceIn).duration(700).playOn(findViewById(R.id.profileImage));
                YoYo.with(Techniques.BounceInDown).duration(700).playOn(findViewById(R.id.names_layout));

                // FloatingActionButton: https://github.com/oguzbilgener/CircularFloatingActionMenu
                int shareActionButtonSize = getResources().getDimensionPixelSize(R.dimen.radius);
                int shareActionButtonMargin = getResources().getDimensionPixelOffset(R.dimen.action_button_margin);

                ImageView fabIcon = new ImageView(mActivity);
                fabIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_share));

                FloatingActionButton.LayoutParams layoutParams = new FloatingActionButton.LayoutParams(shareActionButtonSize, shareActionButtonSize);
                layoutParams.setMargins(shareActionButtonMargin,
                        shareActionButtonMargin,
                        shareActionButtonMargin,
                        shareActionButtonMargin);

                FloatingActionButton rightLowerButton = new FloatingActionButton.Builder(mActivity)
                        .setContentView(fabIcon)
                        .setBackgroundDrawable(R.drawable.button_action_selector)
                        .setLayoutParams(layoutParams)
                        .build();

                SubActionButton.Builder lCSubBuilder = new SubActionButton.Builder(mActivity);
                lCSubBuilder.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_action_selector));
                ImageView rlIconTwitter = new ImageView(mActivity);
                ImageView rlIconShare = new ImageView(mActivity);

                rlIconTwitter.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_twitter));
                rlIconShare.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_share));

                rightLowerMenu = new FloatingActionMenu.Builder(mActivity)
                        .addSubActionView(lCSubBuilder.setContentView(rlIconTwitter).build())
                        .addSubActionView(lCSubBuilder.setContentView(rlIconShare).build())
                        .attachTo(rightLowerButton)
                        .build();
                rlIconTwitter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Create intent using ACTION_VIEW and a normal Twitter url:
                        String tweetUrl =
                                String.format("https://twitter.com/intent/tweet?text=%s&url=%s",
                                        urlEncode("I just discovered that"), urlEncode("URL"));
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tweetUrl));

                        // Narrow down to official Twitter app, if available:
                        List<ResolveInfo> matches = getPackageManager().queryIntentActivities(intent, 0);
                        for (ResolveInfo info : matches) {
                            if (info.activityInfo.packageName.toLowerCase().startsWith("com.twitter")) {
                                intent.setPackage(info.activityInfo.packageName);
                            }
                        }

                        startActivity(intent);
                    }
                });
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
}
