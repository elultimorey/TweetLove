package es.elultimorey.tweetlove;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;


public class MainActivity extends ActionBarActivity {

    Activity mActivity = this;

    private ShareActionProvider mShareActionProvider;

    TextView reportTextView;

    private AdView adView;
    private final static String MY_AD_UNIT_ID = " ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ImageButton btnTwitter = (ImageButton) findViewById(R.id.btnSearch);
        final EditText usernameInbox = (EditText) findViewById(R.id.username_editText);
        final CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox);
        final TextView quoteTextView = (TextView) findViewById(R.id.quote);

        reportTextView = (TextView) findViewById(R.id.report);

        btnTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!usernameInbox.getText().toString().isEmpty()) {
                    SharedPreferences sharedPreferences = getSharedPreferences("MP", Context.MODE_PRIVATE);
                    SharedPreferences.Editor sEditor = sharedPreferences.edit();
                    sEditor.putString("username", usernameInbox.getText().toString());
                    sEditor.commit();
                    sEditor.putBoolean("checkbox", checkBox.isChecked());
                    sEditor.commit();

                    Intent i = new Intent(mActivity, SearchActivity.class);
                    startActivityForResult(i, SearchActivity.SHOW_LOVED);
                }
                else {
                    reportTextView.setVisibility(View.VISIBLE);
                    reportTextView.setText(R.string.report_blank);
                    YoYo.with(Techniques.Tada).duration(500).playOn(findViewById(R.id.report));
                }
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

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    quoteTextView.setText(R.string.quote_to);
                else
                    quoteTextView.setText(R.string.quote);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.menu_main_action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        Intent myIntent = new Intent();
        myIntent.setAction(Intent.ACTION_SEND);
        String shareText = getResources().getText(R.string.inbox_share)+" "+getResources().getText(R.string.app_url);
        myIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        myIntent.setType("text/plain");

        mShareActionProvider.setShareIntent(myIntent);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_action_about:
                startActivity(new Intent(getApplicationContext(), About.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        reportTextView.setVisibility(View.GONE);
        if (requestCode == SearchActivity.SHOW_LOVED) {
            switch (resultCode) {
                case SearchActivity.USER_PRIVATE:
                    // user dont exist or private
                    reportTextView.setText(getResources().getString(R.string.report_user));
                    break;
                case SearchActivity.USER_HAVENT_MENTIONS:
                    // user havent recent metions
                    reportTextView.setText(getResources().getString(R.string.report_mentions));
                    break;
                case SearchActivity.USER_DOESNT_EXIST:
                    // user doesnt exist
                    reportTextView.setText(getResources().getString(R.string.report_user_exist));
                    break;
            }
            reportTextView.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.Tada).duration(700).playOn(findViewById(R.id.report));
        }
    }

    @Override
    public void onResume() {
        // share
        if (mShareActionProvider != null) {
            Intent myIntent = new Intent();
            myIntent.setAction(Intent.ACTION_SEND);
            String shareText = getResources().getText(R.string.inbox_share) + " " + getResources().getText(R.string.app_url);
            myIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            myIntent.setType("text/plain");
            mShareActionProvider.setShareIntent(myIntent);
        }

        super.onResume();
    }
}
