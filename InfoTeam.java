package com.example.marvelselect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class InfoTeam extends AppCompatActivity {

    //API VARIABLES - START
    //base URL - can later add more specific paths
    /* REPLACE 'xxx' WITH YOUR OWN CHOICE OF ID */
    String base = "http://gateway.marvel.com/v1/public/characters?events=xxx&";
    String base1 = "http://gateway.marvel.com/v1/public/characters?events=xxx&";
    String base2 = "http://gateway.marvel.com/v1/public/characters?events=xxx&";
    //declaring MD5 digest hash variable of timestamp, private key, and public key
    MessageDigest hash;
    //API VARIABLES - END

    //UI
    static ImageView img1;
    static ImageView img2;
    static ImageView img3;
    static TextView name1;
    static TextView name2;
    static TextView name3;
    static Button back;

    //variables storing superhero names
    static String heroName1;
    static String heroName2;
    static String heroName3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_team);

        //find views
        img1 = findViewById(R.id.id_infoteam_imageView1);
        img2 = findViewById(R.id.id_infoteam_imageView2);
        img3 = findViewById(R.id.id_infoteam_imageView3);
        name1 = findViewById(R.id.id_infoteam_textView1);
        name2 = findViewById(R.id.id_infoteam_textView2);
        name3 = findViewById(R.id.id_infoteam_textView3);
        back = findViewById(R.id.id_infoteam_back);

        //on click listener for back button
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get info from Locker
                Intent anotherIntent = getIntent();
                int hero1Pos = anotherIntent.getIntExtra("hero1Pos", 0);
                int hero2Pos = anotherIntent.getIntExtra("hero2Pos", 0);
                int hero3Pos = anotherIntent.getIntExtra("hero3Pos", 0);
                //go back to Locker class
                Intent intent = new Intent(InfoTeam.this, Locker.class);
                intent.putExtra("hero1Pos", hero1Pos);
                intent.putExtra("hero2Pos", hero2Pos);
                intent.putExtra("hero3Pos", hero3Pos);

                finish();
            }
        });

        //ACCESSING API DATA - START
        /* INSERT YOUR OWN PUBLIC & PRIVATE API KEY STRINGS HERE */

        //creating valid URL to retrieve JSON
        //creating timezone for URL to access
        TimeZone tz = TimeZone.getTimeZone("EST");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        String ts = df.format(new Date());
        //adding timestamp to url
        base += "ts=" + ts + "&";
        base1 += "ts=" + ts + "&";
        base2 += "ts=" + ts + "&";
        //creating MD5 digest hash of timestamp, private key, and public key
        try {
            //looking for MD5 digest hash code
            hash = MessageDigest.getInstance("MD5");
            //update hash
            hash.update((ts + privateKey + publicKey).getBytes(), 0, (ts + privateKey + publicKey).length());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        String hashVal = new BigInteger(1, hash.digest()).toString(16);
        //adding public key and hash to url - now the URL can be used!
        base += "apikey=" + publicKey + "&hash=" + hashVal;
        base1 += "apikey=" + publicKey + "&hash=" + hashVal;
        base2 += "apikey=" + publicKey + "&hash=" + hashVal;

        //declaring url
        URL url = null;
        URL url1 = null;
        URL url2 = null;
        try {
            //setting URL with JSON data to url variable
            url = new URL(base);
            url1 = new URL(base1);
            url2 = new URL(base2);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        //get hero position data
        Intent intent = getIntent();
        int hero1Pos = intent.getIntExtra("hero1Pos", 0);
        int hero2Pos = intent.getIntExtra("hero2Pos", 0);
        int hero3Pos = intent.getIntExtra("hero3Pos", 0);

        //method call
        new InfoTeam.RetrieveTask(0, hero1Pos).execute(url);
        new InfoTeam.RetrieveTask(1, hero2Pos).execute(url1);
        new InfoTeam.RetrieveTask(2, hero3Pos).execute(url2);
        //ACCESSING API DATA - END
    }//onCreate

    //AsyncTask - running url on separate thread
    private static class RetrieveTask extends AsyncTask<URL, Void, URL> {

        //variables
        int id;
        int heroPos;

        //constructor
        public RetrieveTask(int id, int heroPos) {
            //set variables
            this.heroPos = heroPos;
            this.id = id;
        }

        @Override
        protected URL doInBackground(URL... urls) {
            try {
                //create URL object
                URL url = urls[0];
                //create URLConnection object
                URLConnection connection = url.openConnection();
                //create InputStream object
                InputStream stream = connection.getInputStream();
                //create BufferedReader object
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                //concatenate data to one String value
                String line;
                String data = "";
                while((line = reader.readLine()) != null) {
                    data += line;
                }
                //close BufferedReader
                reader.close();

                //converts JSON string to JSONObject
                JSONObject dataObj = new JSONObject(data);
                //JSON object - data
                JSONObject info = dataObj.getJSONObject("data");
                //JSON array - results
                JSONArray results = info.getJSONArray("results");

                //JSON object - sup
                JSONObject sup = results.getJSONObject(heroPos);
                //JSON object - thumbnail
                JSONObject thumb = sup.getJSONObject("thumbnail");
                //accessing superhero's name
                String name = sup.getString("name");
                //checks which hero that is being referred to
                //updates heroName values
                if(id == 0) {
                    heroName1 = name;
                }
                else if(id == 1) {
                    heroName2 = name;
                }
                else if(id == 2) {
                    heroName3 = name;
                }
                //accessing superhero's image
                String img = thumb.getString("path");
                String ext = thumb.getString("extension");

                //finalizing URL to make it valid
                img += "/landscape_incredible." + ext;

                //create URL variables from img String variable
                URL imgUrl = new URL(img);

                //returns ArrayList
                return imgUrl;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(URL url) {
            //if ArrayList is not empty
            if(url != null) {
                //rendering images so that it can be displayed
                new InfoTeam.LoadImages(id).execute(url);
            }
        }
    }//RetrieveTask

    //AsyncTask - loading images from JSON URL
    private static class LoadImages extends AsyncTask<URL, Void, Bitmap> {

        //variables
        int id;

        //constructor
        public LoadImages(int id) {
            //set variables
            this.id = id;
        }

        @Override
        protected Bitmap doInBackground(URL... urls) {
            URL url = urls[0];
            //create URLConnection object
            URLConnection connection = null;
            try {
                connection = url.openConnection();
                //create InputStream object
                InputStream stream = connection.getInputStream();
                //Bitmap object
                return BitmapFactory.decodeStream(stream);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            //distinguishing between superheros taken from different URLs
            if (id == 0) {
                img1.setImageBitmap(bitmap);
                displayName();
            } else if (id == 1) {
                img2.setImageBitmap(bitmap);
                displayName();
            } else if (id == 2) {
                img3.setImageBitmap(bitmap);
                displayName();
            }
        }

        //to display each superhero's name
        private void displayName() {
            //distinguishing between superheros taken from different URLs
            if (id == 0) {
                name1.setText(heroName1);
            } else if (id == 1) {
                name2.setText(heroName2);
            } else if (id == 2) {
                name3.setText(heroName3);
            }
        }
    }//LoadImages
}