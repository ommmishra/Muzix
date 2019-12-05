package com.asish.musik.fragments;


import android.app.Activity;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.asish.musik.R;
import com.asish.musik.activities.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class SongFetch extends Fragment {

    TextView song_name;
    Button listen;
    Button analyze;
    Activity myActivity;
    private String outputFile;
    private MediaRecorder myAudioRecorder;




    public SongFetch() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_song_fetch, container, false);
        getActivity().setTitle("Muzix Voice");
        song_name = (TextView) view.findViewById(R.id.lyrics);
        listen = (Button) view.findViewById(R.id.listen);
        analyze = (Button) view.findViewById(R.id.analyze);
        analyze.setEnabled(false);
        listen.setEnabled(true);


        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myAudioRecorder != null) {
                    stopAudio();
                    Toast.makeText(myActivity, "Recording stopped", Toast.LENGTH_SHORT).show();

                }
                else{
                    listen.setBackgroundResource(R.drawable.micred);
                    recordAudio();

                }

            }
        });

        analyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myAudioRecorder != null)
                {

                    Toast.makeText(myActivity, "Recording, can't find song", Toast.LENGTH_SHORT).show();

                }

                else{try{

                    song_name.setText("Analyzing!!!!");

                    run();

                }
                catch (IOException e){

                    song_name.setText("Some error Occured");
                }}
            }
        });



    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myActivity = (Activity) context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myActivity = activity;

    }


    private void recordAudio(){


        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.3gp";
        Log.d("filename", outputFile);
        myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setOutputFile(outputFile);

        try {
            myAudioRecorder.prepare();
            myAudioRecorder.start();
        } catch (IllegalStateException ise) {
            // make something ...
            Log.d("error here", "illegal eceptio");
        } catch (IOException ioe) {
            // make something

        }
        Toast.makeText(myActivity, "Recording started", Toast.LENGTH_SHORT).show();

    }

    public void stopAudio(){


        if(myAudioRecorder != null){

            listen.setBackgroundResource(R.drawable.micpink);

            myAudioRecorder.stop();
            myAudioRecorder.release();
            myAudioRecorder = null;
            listen.setEnabled(false);
            analyze.setEnabled(true);
            Toast.makeText(myActivity, "Audio Recorded successfully", Toast.LENGTH_SHORT).show();



        }}

    private void run() throws  IOException{
        String url  = "https://api.audd.io";
        OkHttpClient client = new OkHttpClient();
        File file = new File(outputFile);
        MediaType media_type_mp3 = MediaType.parse("audio/mpeg");
        RequestBody data= new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("file", file.getName(), RequestBody.create(media_type_mp3, file))
                .addFormDataPart("return", "timecode,apple_music,deezer,spotify,Lyrics")
                .addFormDataPart("api_token", "5a374601a235ee860bfe8498dae77e58")
                .build();

        Request request = new Request.Builder().url(url).post(data).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                listen.setEnabled(true);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String myResponse = response.body().string();



                final String artist_name = jsonParse(myResponse);

                myActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        song_name.setText(artist_name);
                        listen.setEnabled(true);
                    }
                });

            }

        });

    }

    public String jsonParse(String responseData){
        String song_name="Artist: ";
        try {
            JSONObject json = new JSONObject(responseData);
            String artist_name = json.getString("result");
            JSONObject json1 = new JSONObject(artist_name);
            song_name = song_name + json1.getString("artist");
            song_name = song_name +" \n" + "Title: " + json1.getString("title") + "\n";

            try {

                String lyrics = json1.getString("Lyrics");
                JSONObject song_lyrics = new JSONObject(lyrics);
                song_name = song_name + song_lyrics.getString("Lyrics") + "\n";
            }catch (Exception e){
                song_name = song_name + "Lyrics not yet availaible";
            }


            return song_name;

        } catch (JSONException e) {
            return "Not found";
        }

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.action_search);
        if(item != null){
            item.setVisible(false);
        }
    }

}
