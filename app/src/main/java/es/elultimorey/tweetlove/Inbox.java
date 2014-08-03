package es.elultimorey.tweetlove;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;


public class Inbox extends Activity {

    private final Activity mActivity = this;
    public final static int SHOW_LOVED = 42;
    public final static int USER_NOT_EXIST = 21;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        final EditText usernameInbox = (EditText) findViewById(R.id.username_inbox);
        final Button btnTwitter = (Button) findViewById(R.id.btnSearch);
        final TextView report = (TextView) findViewById(R.id.report);
        report.setAlpha(0);

        btnTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (usernameInbox.getText() == null || usernameInbox.getText().toString().isEmpty()) {
                    // havent an username
                    report.setText(getResources().getString(R.string.report_blank));
                    report.setAlpha(100);
                    YoYo.with(Techniques.Tada).duration(700).playOn(findViewById(R.id.username_inbox));
                }
                else {
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
                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

                inputMethodManager.hideSoftInputFromWindow(usernameInbox.getWindowToken(), 0);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.inbox, menu);
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
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SHOW_LOVED) {
            if (resultCode == USER_NOT_EXIST) {
                // user dont exist or private
                TextView report = (TextView) findViewById(R.id.report);
                report.setText(getResources().getString(R.string.report_user));
                report.setAlpha(100);
                YoYo.with(Techniques.Tada).duration(700).playOn(findViewById(R.id.report));
            }
        }
    }
}
