package com.sametaylak.notpaylasim;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class OnboardActivity extends AppCompatActivity {

    private AppCompatSpinner spinner, spinnerBolum;
    private Button btnConfirm;
    private List<Universite> universiteler = new ArrayList<>();
    private List<Bolum> bolumler = new ArrayList<>();
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboard);

        ctx = this;

        spinner = (AppCompatSpinner) findViewById(R.id.spinner);
        spinnerBolum = (AppCompatSpinner) findViewById(R.id.spinnerBolum);
        btnConfirm = (Button) findViewById(R.id.btnConfirm);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bolumler.clear();
                Universite u = (Universite) parent.getItemAtPosition(position);
                int uID = u.getID();
                new GetBolum().execute(String.valueOf(uID));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Universite selectedU = (Universite) spinner.getSelectedItem();
                Bolum selectedB = (Bolum) spinnerBolum.getSelectedItem();

                SharedPreferences preferences =
                        getSharedPreferences("my_preferences", MODE_PRIVATE);

                preferences.edit().putInt("universite_id", selectedU.getID()).apply();
                preferences.edit().putInt("bolum_id", selectedB.getID()).apply();
                preferences.edit().putString("universite_adi", selectedU.getAd()).apply();
                preferences.edit().putString("bolum_adi", selectedB.getAd()).apply();

                preferences.edit().putBoolean("onboarding_complete",true).apply();

                Intent main = new Intent(ctx, MainActivity.class);
                startActivity(main);

                finish();
            }
        });

        new GetUniversite().execute();
    }

    public class GetUniversite extends AsyncTask<Void, Void, Void> {

        private final String USER_AGENT = "Mozilla/5.0";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected Void doInBackground(Void... params) {
            String url = "http://www.whatsapping.org/api/universite";

            URL obj = null;
            try {
                obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                con.setRequestMethod("GET");

                con.setRequestProperty("User-Agent", USER_AGENT);

                int responseCode = con.getResponseCode();
                System.out.println("\nSending 'GET' request to URL : " + url);
                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONArray jsonArray = new JSONArray(String.valueOf(response));
                for (int i=0; i < jsonArray.length(); i++) {
                    JSONObject getObj = jsonArray.getJSONObject(i);
                    int id = Integer.parseInt(getObj.getString("id"));
                    String ad = getObj.getString("universite_adi");
                    universiteler.add(new Universite(id, ad));
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            spinner.setAdapter(new UniversiteAdapter(ctx, R.layout.universite_spinner, universiteler));
        }
    }

    public class GetBolum extends AsyncTask<String, Void, Void> {

        private final String USER_AGENT = "Mozilla/5.0";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected Void doInBackground(String... params) {
            String url = "http://www.whatsapping.org/api/bolum/" + params[0];

            URL obj = null;
            try {
                obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                con.setRequestMethod("GET");

                con.setRequestProperty("User-Agent", USER_AGENT);

                int responseCode = con.getResponseCode();
                System.out.println("\nSending 'GET' request to URL : " + url);
                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONArray jsonArray = new JSONArray(String.valueOf(response));
                for (int i=0; i < jsonArray.length(); i++) {
                    JSONObject getObj = jsonArray.getJSONObject(i);
                    int id = Integer.parseInt(getObj.getString("id"));
                    int universiteID = Integer.parseInt(getObj.getString("universite_id"));
                    String ad = getObj.getString("bolum_adi");
                    bolumler.add(new Bolum(id, universiteID, ad));
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            spinnerBolum.setAdapter(new BolumlerAdapter(ctx, R.layout.universite_spinner, bolumler));
        }
    }

    public class UniversiteAdapter extends ArrayAdapter {

        private List<Universite> universiteList;

        public UniversiteAdapter(Context context, int resource, List<Universite> mList) {
            super(context, resource, mList);
            this.universiteList = mList;
        }

        public View getCustomView(int position, View convertView,
                                  ViewGroup parent) {

            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.universite_spinner, parent, false);

            TextView tvUniversite = (TextView) layout.findViewById(R.id.tvUniversite);

            Universite universite = universiteList.get(position);

            tvUniversite.setText(universite.getAd());

            return layout;
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }
    }

    public class BolumlerAdapter extends ArrayAdapter {

        private List<Bolum> bolumlerList;

        public BolumlerAdapter(Context context, int resource, List<Bolum> mList) {
            super(context, resource, mList);
            this.bolumlerList = mList;
        }

        public View getCustomView(int position, View convertView,
                                  ViewGroup parent) {

            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.universite_spinner, parent, false);

            TextView tvUniversite = (TextView) layout.findViewById(R.id.tvUniversite);

            Bolum bolum = bolumlerList.get(position);

            tvUniversite.setText(bolum.getAd());

            return layout;
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }
    }
}
