package com.sametaylak.notpaylasim;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import nl.changer.polypicker.Config;
import nl.changer.polypicker.ImagePickerActivity;

public class MainActivity extends AppCompatActivity {

    private GridView gridView;
    private List<Fotograf> fotografs = new ArrayList<>();
    private Context context;
    private String mTitle;
    private SharedPreferences preferences;
    private ProgressDialog dialog;
    private AdView mAdView;


    private static final int INTENT_REQUEST_GET_IMAGES = 13;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-4931702315582966~5966738335");

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);

        preferences =  getSharedPreferences("my_preferences", MODE_PRIVATE);

        if(!preferences.getBoolean("onboarding_complete",false)) {
            Intent onboarding = new Intent(this, OnboardActivity.class);
            startActivity(onboarding);

            finish();
            return;
        }

        context = this;

        gridView = (GridView) findViewById(R.id.galleryLayout);

        ImageButton imageButton = (ImageButton) findViewById(R.id.setPhoto);
        Button btnGaleri = (Button) findViewById(R.id.btnGaleri);
        Button btnHelp = (Button) findViewById(R.id.btnHelp);
        assert btnGaleri != null;
        assert imageButton != null;
        assert btnHelp != null;
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Fotograf mCurrent = (Fotograf) parent.getItemAtPosition(position);
                if(mCurrent.getType() == 0) {
                    boolean status = true;
                    for (Fotograf f : fotografs) {
                        if(f.getType() == 1){
                            status = false;
                        }
                    }
                    if(status) {
                        ArrayList<String> gecici = new ArrayList<String>();
                        for (Fotograf f : fotografs) {
                            gecici.add(f.getPhotos());
                        }
                        Intent i = new Intent(MainActivity.this, FullscreenActivity.class);
                        i.putStringArrayListExtra("url", gecici);
                        i.putExtra("indis", position);
                        startActivity(i);
                    } else {
                        ArrayList<String> gecici = new ArrayList<String>();
                        gecici.add(mCurrent.getPhotos());
                        Intent i = new Intent(MainActivity.this, FullscreenActivity.class);
                        i.putStringArrayListExtra("url", gecici);
                        i.putExtra("indis", 0);
                        startActivity(i);
                    }
                } else {
                    fotografs.clear();
                    String[] mPhotos = mCurrent.getPhotos().split(",");
                    for (String p : mPhotos) {
                        fotografs.add(new Fotograf(mCurrent.getID(), mCurrent.getTitle(), p, 0));
                    }
                    gridView.setAdapter(new ImageAdapter(getApplicationContext(), fotografs));
                }
            }
        });
        btnGaleri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetUrls().execute(preferences.getInt("universite_id", 0) + "", preferences.getInt("bolum_id", 0) + "");
            }
        });
        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

                emailIntent.setType("plain/text");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"eczapirin@gmail.com"});
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Başlık");
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "İçerik");

                startActivity(Intent.createChooser(emailIntent, "İletişim"));
            }
        });
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.prompt, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.myDialog));

                alertDialogBuilder.setView(promptsView);

                final EditText userInput = (EditText) promptsView
                        .findViewById(R.id.etBaslik);

                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Tamam",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        mTitle = userInput.getText().toString();
                                        getImages();
                                    }
                                })
                        .setNegativeButton("İptal",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alertDialog = alertDialogBuilder.create();

                alertDialog.show();
            }
        });

        new GetUrls().execute(preferences.getInt("universite_id", 0) + "", preferences.getInt("bolum_id", 0) + "");
    }

    private void getImages() {
        Intent intent = new Intent(this, ImagePickerActivity.class);
        Config config = new Config.Builder()
                .setTabBackgroundColor(R.color.white)
                .setTabSelectionIndicatorColor(R.color.blue)
                .setCameraButtonColor(R.color.green)
                .setSelectionLimit(10)
                .build();
        ImagePickerActivity.setConfig(config);
        startActivityForResult(intent, INTENT_REQUEST_GET_IMAGES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == INTENT_REQUEST_GET_IMAGES) {
                Parcelable[] parcelableUris = intent.getParcelableArrayExtra(ImagePickerActivity.EXTRA_IMAGE_URIS);

                if (parcelableUris == null) {
                    return;
                }

                Uri[] uris = new Uri[parcelableUris.length];
                System.arraycopy(parcelableUris, 0, uris, 0, parcelableUris.length);
                SharedPreferences preferences =  getSharedPreferences("my_preferences", MODE_PRIVATE);

                int universite_id = preferences.getInt("universite_id", 0);
                int bolum_id = preferences.getInt("bolum_id", 0);

                if (uris != null) {
                    if (uris.length == 1) {
                        // Tek Fotoğraf yani Galeriye
                        ArrayList<String> albums = new ArrayList<>();
                        for (Uri uri : uris) {
                            String encodedImage = Base64.encodeToString(imgToBytes(uri), Base64.DEFAULT);
                            albums.add(encodedImage);
                        }
                        JSONArray jsonArray = new JSONArray(albums);
                        JSONObject jsObj = new JSONObject();
                        try {
                            jsObj.put("title", mTitle);
                            jsObj.put("universite_id", universite_id);
                            jsObj.put("bolum_id", bolum_id);
                            jsObj.put("tip", 0);
                            jsObj.put("files", jsonArray);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        new BackgroundTask().execute(String.valueOf(jsObj));
                    } else {
                        // Birden Fazla Fotoğraf yani Albüme
                        ArrayList<String> albums = new ArrayList<>();
                        for (Uri uri : uris) {
                            String encodedImage = Base64.encodeToString(imgToBytes(uri), Base64.DEFAULT);
                            albums.add(encodedImage);
                        }
                        JSONArray jsonArray = new JSONArray(albums);
                        JSONObject jsObj = new JSONObject();
                        try {
                            jsObj.put("title", mTitle);
                            jsObj.put("universite_id", universite_id);
                            jsObj.put("bolum_id", bolum_id);
                            jsObj.put("tip", 1);
                            jsObj.put("files", jsonArray);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        new BackgroundTask().execute(String.valueOf(jsObj));
                    }
                }
            }
        }
    }

    private byte[] imgToBytes(Uri r) {
        Bitmap bitmap = BitmapFactory.decodeFile(r.toString());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byte_arr = stream.toByteArray();

        return byte_arr;
    }

    public class BackgroundTask extends AsyncTask<String, Void, Void> {

        private final String USER_AGENT = "Mozilla/5.0";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(context, "", "Fotoğraflar Yükleniyor..", true, false);
        }

        @Override
        protected Void doInBackground(String... params) {

            String url = "http://www.whatsapping.org/api/fotograf";
            URL obj = null;
            try {
                obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                //add reuqest header
                con.setRequestMethod("POST");
                con.setRequestProperty("User-Agent", USER_AGENT);
                con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

                // Send post request
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(params[0]);
                wr.flush();
                wr.close();

                int responseCode = con.getResponseCode();
                System.out.println("\nSending 'POST' request to URL : " + url);
                System.out.println("Post parameters : " + params[0]);
                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                }
                in.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dialog.dismiss();
            new GetUrls().execute(preferences.getInt("universite_id", 0) + "", preferences.getInt("bolum_id", 0) + "");
        }
    }

    public class GetUrls extends AsyncTask<String, Void, Void> {

        private final String USER_AGENT = "Mozilla/5.0";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            fotografs.clear();
            dialog = ProgressDialog.show(context, "", "Yükleniyor..", true, false);
        }


        @Override
        protected Void doInBackground(String... params) {
            String url = "http://www.whatsapping.org/api/fotograf/"+params[0]+"/"+params[1];

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
                for (int i = 0 ; i < jsonArray.length() ; i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    int id = Integer.parseInt(jsonObject.getString("id"));
                    String title = jsonObject.getString("baslik");
                    String fotograflar = jsonObject.getString("fotograflar");
                    int type = Integer.parseInt(jsonObject.getString("tip"));
                    fotografs.add(new Fotograf(id, title, fotograflar, type));
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
            gridView.setAdapter(new ImageAdapter(getApplicationContext(), fotografs));
            dialog.dismiss();
        }
    }

    public class GetSearch extends AsyncTask<String, Void, Void> {

        private final String USER_AGENT = "Mozilla/5.0";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            fotografs.clear();
            dialog = ProgressDialog.show(context, "", "Yükleniyor..", true, false);
        }


        @Override
        protected Void doInBackground(String... params) {
            String url = "http://www.whatsapping.org/api/ara";

            URL obj = null;
            try {
                obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                con.setRequestMethod("POST");
                con.setRequestProperty("User-Agent", USER_AGENT);
                con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

                String urlParameters = "universite_id=" + params[0] + "&bolum_id=" + params[1] + "&anahtar=" + params[2];

                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();

                int responseCode = con.getResponseCode();
                System.out.println("\nSending 'POST' request to URL : " + url);
                System.out.println("Post parameters : " + urlParameters);
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
                for (int i = 0 ; i < jsonArray.length() ; i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    int id = Integer.parseInt(jsonObject.getString("id"));
                    String title = jsonObject.getString("baslik");
                    String fotograflar = jsonObject.getString("fotograflar");
                    int type = Integer.parseInt(jsonObject.getString("tip"));
                    fotografs.add(new Fotograf(id, title, fotograflar, type));
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
            gridView.setAdapter(new ImageAdapter(getApplicationContext(), fotografs));
            dialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.search_box, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.myDialog));

                alertDialogBuilder.setView(promptsView);

                final EditText userInput = (EditText) promptsView
                        .findViewById(R.id.etSearch);

                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Tamam",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        new GetSearch().execute(preferences.getInt("universite_id", 0) + "", preferences.getInt("bolum_id", 0) + "", userInput.getText().toString());
                                    }
                                })
                        .setNegativeButton("İptal",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alertDialog = alertDialogBuilder.create();

                alertDialog.show();
                break;
        }
        return true;
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

}
