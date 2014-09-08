package es.elultimorey.tweetlove;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;


public class Inbox extends Activity {

    private final Activity mActivity = this;
    public final static int SHOW_LOVED = 42;
    public final static int USER_NOT_EXIST = 21;
    public final static int USER_HAVENT_MENTIONS = 22;

    private AdView adView;
    private final static String MY_AD_UNIT_ID = " ";

    private ShareActionProvider myShareActionProvider;
    private EditText usernameInbox;
    private Button btnTwitter;
    private TextView report;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        btnTwitter = (Button) findViewById(R.id.btnSearch);
        usernameInbox = (EditText) findViewById(R.id.username_editText);
        report = (TextView) findViewById(R.id.report);
        report.setAlpha(0);

        btnTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goSearch();
            }
        });

        usernameInbox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b)
                    if(adView.getVisibility() == View.VISIBLE)
                        adView.setVisibility(View.GONE);
            }
        });
        usernameInbox.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    goSearch();
                }

                return false;
            }
        });

        adView = new AdView(this);
        adView.setAdUnitId(MY_AD_UNIT_ID);
        adView.setAdSize(AdSize.BANNER);

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.adLayout_inbox);
        layout.addView(adView);

        // Cargar adView con la solicitud de anuncio.
        AdRequest request = new AdRequest.Builder().build();
        adView.loadAd(request);
    }

    private void goSearch() {
        if (usernameInbox.getText() == null || usernameInbox.getText().toString().isEmpty()) {
            // havent an username
            report.setText(getResources().getString(R.string.report_blank));
            report.setAlpha(100);
            YoYo.with(Techniques.Tada).duration(700).playOn(findViewById(R.id.username_inbox));
        } else {
            ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected()) {// online && have an username
                Intent i = new Intent(mActivity, Search.class);
                i.putExtra("user", usernameInbox.getText().toString());
                startActivityForResult(i, SHOW_LOVED);
            } else {
                // offline
                report.setText(getResources().getString(R.string.report_network));
                report.setAlpha(100);
                YoYo.with(Techniques.Tada).duration(700).playOn(findViewById(R.id.report));
            }
        }
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        inputMethodManager.hideSoftInputFromWindow(usernameInbox.getWindowToken(), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.inbox, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);

        myShareActionProvider = (ShareActionProvider) item.getActionProvider();

        Intent myIntent = new Intent();
        myIntent.setAction(Intent.ACTION_SEND);
        String shareText = getResources().getText(R.string.inbox_share)+" "+getResources().getText(R.string.app_url);
        myIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        myIntent.setType("text/plain");

        myShareActionProvider.setShareIntent(myIntent);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SHOW_LOVED) {

            TextView report = (TextView) findViewById(R.id.report);
            switch (resultCode) {
                case USER_HAVENT_MENTIONS:
                    // user havent recent metions
                    report.setText(getResources().getString(R.string.report_mentions));
                    break;
                case USER_NOT_EXIST:
                    // user dont exist or private
                    report.setText(getResources().getString(R.string.report_user));
                    break;
            }
            report.setAlpha(100);
            YoYo.with(Techniques.Tada).duration(700).playOn(findViewById(R.id.report));
        }
    }
    @Override
    public void onResume() {
        // share
        if (myShareActionProvider != null) {
            Intent myIntent = new Intent();
            myIntent.setAction(Intent.ACTION_SEND);
            String shareText = getResources().getText(R.string.inbox_share) + " " + getResources().getText(R.string.app_url);
            myIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            myIntent.setType("text/plain");
            myShareActionProvider.setShareIntent(myIntent);
        }

        super.onResume();
    }

}
