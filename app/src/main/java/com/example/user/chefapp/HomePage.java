package com.example.user.chefapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;

public class HomePage extends AppCompatActivity {
    private static final String host = "api.linkedin.com";
    private static final String url = "https://" + host + "/v1/people/~:" +
            "(email-address,formatted-name,phone-numbers,public-profile-url,picture-url,picture-urls::(original))";
    private ProgressDialog progress;
    private TextView user_name, user_email;
    private ImageView profile_picture;
    private String user_profile;
    private VolleyS volley;
    protected RequestQueue fRequestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        getSupportActionBar().hide();

        //Initialize the progressbar
        progress= new ProgressDialog(this);
        progress.setMessage("Retrieve data...");
        progress.setCanceledOnTouchOutside(false);
        progress.show();

        user_email = (TextView) findViewById(R.id.email);
        user_name = (TextView) findViewById(R.id.name);
        profile_picture = (ImageView) findViewById(R.id.profile_picture);

        TabHost tabs=(TabHost)findViewById(android.R.id.tabhost);
        tabs.setup();
        TabHost.TabSpec spec;

        spec=tabs.newTabSpec("mitab1");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Chat");
        tabs.addTab(spec);

        spec=tabs.newTabSpec("mitab2");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Home");
        tabs.addTab(spec);

        spec=tabs.newTabSpec("mitab3");
        spec.setContent(R.id.tab3);
        spec.setIndicator("Recipes");
        tabs.addTab(spec);

        tabs.setCurrentTab(0);


        ImageView img = (ImageView)findViewById(R.id.profile_picture);
        img.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(user_profile));
                startActivity(intent);
            }
        });
        volley = VolleyS.getInstance(this.getApplicationContext());
        fRequestQueue = volley.getRequestQueue();

        ListView listaChat = (ListView) findViewById(R.id.ListaChat);
        final EditText textChat = (EditText) findViewById(R.id.TextChat);
        ArrayList<Message> mMessages = new ArrayList<>();
        listaChat.setTranscriptMode(1);
        ChatListAdapter mAdapter = new ChatListAdapter(HomePage.this, /*userId*/"hi", mMessages);
        listaChat.setAdapter(mAdapter);
        findViewById(R.id.buttonSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = textChat.getText().toString();
                //ParseObject message = ParseObject.create("Message");
                //message.put(Message.USER_ID_KEY, userId);
                //message.put(Message.BODY_KEY, data);
                // Using new `Message` Parse-backed model now

                System.out.println("ENVIAR MENSAJE PRIMERA VEZ");

                textChat.setText(null);
            }});


        makeRequest("lel");


        findViewById(R.id.buttonInventory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(HomePage.this, Inventory.class);
                    startActivity(intent);

                }

        });

        findViewById(R.id.buttonRecipes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, Recipes.class);
                startActivity(intent);

            }

        });
        findViewById(R.id.buttonMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, Menu.class);
                startActivity(intent);

            }

        });

        linkededinApiHelper();

    }

    public void linkededinApiHelper(){
        APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
        apiHelper.getRequest(HomePage.this, url, new ApiListener() {
            @Override
            public void onApiSuccess(ApiResponse result) {
                try {
                    showResult(result.getResponseDataAsJson());
                    progress.dismiss();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onApiError(LIApiError error) {

            }
        });
    }

    public  void  showResult(JSONObject response){

        try {
            user_email.setText(response.get("emailAddress").toString());
            user_name.setText(response.get("formattedName").toString());
            Picasso.with(this).load(response.getString("pictureUrl"))
                    .into(profile_picture);
            user_profile = (String) response.get("publicProfileUrl");

        } catch (Exception e){
            e.printStackTrace();
        }
    }
    private void makeRequest(String IP){

        //String url = "http://192.168.100.8:9080/RestChef/chef";
        String url = "http://maps.googleapis.com/maps/api/geocode/json?latlng=39.476245,-0.349448&sensor=true";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                onConnectionFinished();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                onConnectionFailed(volleyError.toString());
            }
        });
        addToQueue(request);
    }
    public void onPreStartConnection() {
        HomePage.this.setProgressBarIndeterminateVisibility(true);
    }

    public void onConnectionFinished() {
        HomePage.this.setProgressBarIndeterminateVisibility(false);
    }

    public void onConnectionFailed(String error) {
        HomePage.this.setProgressBarIndeterminateVisibility(false);
        Toast.makeText(HomePage.this, error, Toast.LENGTH_SHORT).show();
    }
    public void addToQueue(Request request) {
        if (request != null) {
            request.setTag(this);
            if (fRequestQueue == null)
                fRequestQueue = volley.getRequestQueue();
            request.setRetryPolicy(new DefaultRetryPolicy(
                    60000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));
            onPreStartConnection();
            fRequestQueue.add(request);
        }
    }


}
