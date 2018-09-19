package com.treycorp.ringer;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    JSONObject profile;

    TextView tvClock;
    TextView blockView;
    TextView blockRemaining;
    TextView lunchView;
    TextView lunchRemaining;

    DateTimeFormatter df = DateTimeFormat.forPattern("HH:mm");


    public void main() throws JSONException {
        JSONObject schedule = profile.getJSONObject("Schedule");
        JSONObject regular = schedule.getJSONObject("Regular");
        JSONObject lunch = schedule.getJSONObject("Lunch");
        //TODO: Lunch Schedule

        String index = findIndex(regular);
        blockView.setText(index);
        String currentS = new SimpleDateFormat("HH:mm", Locale.US).format(new Date());
        //String currentS = "13:55";
        DateTime current = df.parseLocalTime(currentS).toDateTimeToday();

        if (index != "--") {

            JSONObject period = regular.getJSONObject(index);


            DateTime end = df.parseLocalTime(period.getString("end")).toDateTimeToday();

            int hours;
            int minutes;

            Period remain = new Period(current, end);
            hours = remain.getHours();
            minutes = remain.getMinutes();

            StringBuilder remainingBlock = new StringBuilder();
            if (hours > 0) {
                remainingBlock.append(hours).append(hours == 1 ? " hour" : " hours").append(" and ");
            }
            remainingBlock.append(minutes).append(minutes == 1 ? " minute" : " minutes").append(" remaining");
            blockRemaining.setText(remainingBlock);
        } else {
            blockRemaining.setText("--");
        }

        index = findIndex(lunch);
        lunchView.setText(index);
        if (index != "--") {
            JSONObject group = lunch.getJSONObject(index);
            DateTime endLunch = df.parseLocalTime(group.getString("end")).toDateTimeToday();
            Period remainLunch = new Period(current, endLunch);
            int hours = remainLunch.getHours();
            int minutes = remainLunch.getMinutes();
            StringBuilder remainingLunch = new StringBuilder();
            if (hours > 0) {
                remainingLunch.append(hours).append(hours == 1 ? " hour" : " hours").append(" and ");
            }
            remainingLunch.append(minutes).append(minutes == 1 ? " minute" : " minutes").append(" remaining");
            lunchRemaining.setText(remainingLunch);
        } else {
            lunchRemaining.setText("--");
        }
    }

    public void loadProfile() {
        if (!fileExist("profile.json")) {
            Intent intent = new Intent(MainActivity.this, LoadProfile.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

        String profileString = "";
        FileInputStream fin = null;
        try {
            int c;
            fin = openFileInput("profile.json");

            while( (c = fin.read()) != -1){
                profileString = profileString + Character.toString((char)c);
            }

            profile = new JSONObject(profileString);
            getSupportActionBar().setTitle(profile.getString("Identity"));
            final Handler mainHandler = new Handler(getMainLooper());
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        main();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mainHandler.postDelayed(this, 10000);
                }
            }, 10);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Error reading profile", Toast.LENGTH_SHORT);
            File dir = getFilesDir();
            File file = new File(dir, "profile.json");
            boolean deleted = file.delete();
        }
    }


    public String findIndex(JSONObject schedule) throws JSONException {
        Iterator keys = schedule.keys();

        String currentS = new SimpleDateFormat("HH:mm", Locale.US).format(new Date());
        //String currentS = "13:55";

        DateTimeFormatter df = DateTimeFormat.forPattern("HH:mm");
        DateTime current = df.parseLocalTime(currentS).toDateTimeToday();

        String returnKey = "--";

        while (keys.hasNext()) {
            String key = (String) keys.next();
            JSONObject period = (JSONObject) schedule.getJSONObject(key);

            DateTime start = df.parseLocalTime(period.getString("start")).toDateTimeToday();
            DateTime end = df.parseLocalTime(period.getString("end")).toDateTimeToday();

            if (current.isAfter(start) && current.isBefore(end)) {
                returnKey = key;
                break;
            }
        }

        return returnKey;
    }

    public boolean fileExist(String fname){
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvClock = (TextView) findViewById(R.id.TimeView);
        blockView = (TextView) findViewById(R.id.periodView);
        blockRemaining = (TextView) findViewById(R.id.blockRemaining);
        lunchView = (TextView) findViewById(R.id.lunchView);
        lunchRemaining = (TextView) findViewById(R.id.lunchRemaining);


        tvClock.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent trampoline = new Intent(MainActivity.this, ProfileSettings.class);
                startActivity(trampoline);
                return true;
            }
        });

        loadProfile();

        final Handler clockHandler = new Handler(getMainLooper());
        clockHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvClock.setText(new SimpleDateFormat("HH:mm", Locale.US).format(new Date()));
                clockHandler.postDelayed(this, 2000);
            }
        }, 10);

    }
}
