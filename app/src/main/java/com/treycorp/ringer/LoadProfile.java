package com.treycorp.ringer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoadProfile extends AppCompatActivity {

    SharedPreferences sharedpref;
    ProgressDialog dialog;

    public boolean fileExist(String fname){
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }

    public String getURL() {
        return sharedpref.getString("URL", getString(R.string.DefaultURL));
    }

    public void loadProfile() {
        Log.d("loadProfile", "Loading profile");

        if (!fileExist("profile.json")) {
            //downloadProfile();
            Log.e("loadProfile", "No profile");
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

            JSONObject profile = new JSONObject(profileString);

            Log.d("loadProfile", "Profile loaded successfully");
            Intent trampoline = new Intent(this, MainActivity.class);
            trampoline.putExtra("profileString", profileString);
            trampoline.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(trampoline);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(LoadProfile.this, "Error reading profile", Toast.LENGTH_SHORT);
            File dir = getFilesDir();
            File file = new File(dir, "profile.json");
            boolean deleted = file.delete();
        }


    }

    public void downloadProfile() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(getURL()).build();
        dialog = ProgressDialog.show(LoadProfile.this, "", "Downloading profile...", true);


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                dialog.dismiss();
                Toast.makeText(LoadProfile.this, "Failed to connect to server, check internet connection", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    dialog.dismiss();
                    LoadProfile.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(LoadProfile.this, "Server responded unexpectedly", Toast.LENGTH_SHORT).show();
                        }
                    });
                    throw new IOException("Unexpected code " + response);
                } else {
                    // do something wih the result
                    String profileString = response.body().string();
                    Log.d("profile.json", profileString);
                    try {
                        JSONObject profile = new JSONObject(profileString);

                        FileOutputStream fo = openFileOutput("profile.json", MODE_PRIVATE);
                        fo.write(profileString.getBytes());
                        fo.close();

                        SharedPreferences.Editor sharedEditor = sharedpref.edit();
                        final StringBuilder sb = new StringBuilder(profile.getString("Identity"));
                        sb.append(" ").append(profile.getString("Version")).append(" downloaded successfully");
                        LoadProfile.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(LoadProfile.this, sb, Toast.LENGTH_SHORT).show();
                            }
                        });
                        loadProfile();

                        sharedEditor.putString("URL", profile.getString("Update"));
                        sharedEditor.commit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        LoadProfile.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(LoadProfile.this, "Invalid response from server, check internet connection", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    dialog.dismiss();
                }
            }
        });
        //loadProfile();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_profile);

        sharedpref = LoadProfile.this.getSharedPreferences("com.treycorp.ringer.pref", LoadProfile.this.MODE_PRIVATE);

        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            String newURL = uri.getQueryParameter("URL");

            SharedPreferences.Editor sharedEditor = sharedpref.edit();
            sharedEditor.putString("URL", newURL);
            sharedEditor.commit();

            Log.d("LoadProfile", "Externally loaded profile: " + getURL());

            if (fileExist("profile.json")) {
                File dir = getFilesDir();
                File file = new File(dir, "profile.json");
                boolean deleted = file.delete();
            }
        }

        dialog = ProgressDialog.show(LoadProfile.this, "",
                "Loading profile. Please wait...", true);

        if (!fileExist("profile.json")) {
            dialog.dismiss();
            downloadProfile();
        } else {
            dialog.dismiss();
            loadProfile();
        }
    }
}
