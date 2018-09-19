package com.treycorp.ringer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;

public class ProfileSettings extends AppCompatActivity {

    EditText profileAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        profileAddress = findViewById(R.id.ProfileAddress);

        getSupportActionBar().setTitle("Profile Settings");

        SharedPreferences sharedPref = ProfileSettings.this.getSharedPreferences("com.treycorp.ringer.pref", ProfileSettings.this.MODE_PRIVATE);

        profileAddress.setText(sharedPref.getString("URL", getString(R.string.DefaultURL)));


        Button reset = findViewById(R.id.ProfileReset);

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileSettings.this, "Profile deleted", Toast.LENGTH_SHORT);
                File dir = getFilesDir();
                File file = new File(dir, "profile.json");
                boolean deleted = file.delete();
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        });
    }
}
