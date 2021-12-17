package com.rvs.simplenetworkmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NorrisActivity extends AppCompatActivity {
    private String stream;
    private String quote;

    private TextView norrisText;
    private Button norrisButton, norrisButtonExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_norris);
        norrisText = findViewById(R.id.norrisText);
        norrisButton = findViewById(R.id.norrisButton);
        norrisButtonExit = findViewById(R.id.norrisButtonExit);
    }

    public String getJson(String link) {
        try {
            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader
                        (connection.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }
                stream = builder.toString();
                connection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stream;
    }

    public void onClickNorris(View v) {
        new QuoteLoader().execute();
    }

    public void onClickExit (View v) {
        NorrisActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @SuppressLint("StaticFieldLeak")
    private class QuoteLoader extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            String jsonString = getJson("https://api.chucknorris.io/jokes/random");
            try {
                JSONObject object = new JSONObject(jsonString);
                quote = object.getString("value");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @SuppressLint("SetTextI18n")
        protected void onPreExecute() {
            super.onPreExecute();
            quote = "";
            norrisText.setText("Loading...");
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if (!quote.equals("")) {
                norrisText.setText(quote);
            }
        }
    }
}