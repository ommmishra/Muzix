package com.asish.musik.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.asish.musik.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class Lyrics extends Fragment {

    Activity myActivity2;
    Button analyze;
    TextView song_name;
    EditText lyrics_here;


    public Lyrics() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_lyrics, container, false);
        getActivity().setTitle("Muzix Lyrics");
        analyze = (Button) view.findViewById(R.id.search_for);
        song_name = (TextView) view.findViewById(R.id.lyrics);
        lyrics_here= (EditText) view.findViewById(R.id.lyricshere);

        return view;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_search);
        if(item != null) {
            item.setVisible(false);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        analyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = lyrics_here.getText().toString();
                if(query==""){
                    Toast.makeText(myActivity2, "Enter some lines of a song", Toast.LENGTH_SHORT).show();
                }

                else{

                    song_name.setText("Analyzing!!!");

                    try{

                        run(query);

                    }catch (Exception e){
                        song_name.setText("Error Occured");
                    }

                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myActivity2 = (Activity) context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myActivity2 = activity;

    }


    private void run(String songName) throws IOException {
        String url  = "https://api.audd.io/findLyrics/?q=" + songName;
        OkHttpClient client = new OkHttpClient();



        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String myResponse = response.body().string();

                System.out.println(myResponse);

                final String artist_name = jsonParse(myResponse);

                myActivity2.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        song_name.setText(artist_name);
                    }
                });

            }

        });

    }


    public String jsonParse(String responseData){
        String song_name="Artist: ";
        try {
            JSONObject json = new JSONObject(responseData);
            JSONArray song_arr = json.getJSONArray("result");

            String match = song_arr.get(0).toString();
            JSONObject best_match = new JSONObject(match);

            song_name = song_name + best_match.getString("artist");
            song_name = song_name +" \n\n" + "Title: " + best_match.getString("title") + "\n";
            song_name = song_name + best_match.getString("Lyrics") + "\n";

            return song_name;

        } catch (JSONException e) {
            return song_name="Not found";
        }
    }



}
