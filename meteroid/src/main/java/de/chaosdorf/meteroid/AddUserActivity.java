package de.chaosdorf.meteroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.UrlQuerySanitizer;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.chaosdorf.meteroid.controller.UserController;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOCallback;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOPost;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOTask;
import de.chaosdorf.meteroid.model.User;

public class AddUserActivity extends Activity implements LongRunningIOCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        Button addButton = (Button) findViewById(R.id.add_user_button);
        final TextView usernameText = (TextView) findViewById(R.id.username);
        final TextView emailText = (TextView) findViewById(R.id.email);
        final TextView balanceText = (TextView) findViewById(R.id.balance);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String hostname = prefs.getString("hostname", null);
                User user = new User(0,
                        usernameText.getText().toString(),
                        emailText.getText().toString(),
                        Long.parseLong(balanceText.getText().toString()),
                        new Date(),
                        new Date());

                new LongRunningIOPost(
                        AddUserActivity.this,
                        LongRunningIOTask.ADD_USER,
                        hostname + "users",
                        UserController.userToPostParams(user)
                ).execute();
            }
        });
    }

    @Override
    public void displayErrorMessage(LongRunningIOTask task, String message) {

    }

    @Override
    public void processIOResult(final LongRunningIOTask task, final String json)
    {
        if (task == LongRunningIOTask.ADD_USER && json != null)
        {
            startActivity(new Intent(getApplicationContext(), PickUsername.class));
        }
    }
}
