package es.elultimorey.tweetlove;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;


public class MainActivity extends ActionBarActivity {

    //statics
    public final static int SHOW_LOVED = 42;
    public final static int USER_NOT_EXIST = 21;
    public final static int USER_HAVENT_MENTIONS = 22;

    Activity mActivity = this;

    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ImageButton btnTwitter = (ImageButton) findViewById(R.id.btnSearch);
        final EditText usernameInbox = (EditText) findViewById(R.id.username_editText);

        btnTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("MP", Context.MODE_PRIVATE);
                SharedPreferences.Editor sEditor = sharedPreferences.edit();
                sEditor.putString("username", usernameInbox.getText().toString());
                sEditor.commit();

                Intent i = new Intent(mActivity, SearchActivity.class);
                startActivityForResult(i, SHOW_LOVED);
            }
        });

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
