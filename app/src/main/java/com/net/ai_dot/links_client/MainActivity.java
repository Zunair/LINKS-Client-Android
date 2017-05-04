package com.net.ai_dot.links_client;

import android.app.ProgressDialog;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import android.util.Base64;

import static android.media.AudioFormat.CHANNEL_IN_MONO;
import static android.media.AudioFormat.ENCODING_PCM_16BIT;

public class MainActivity extends AppCompatActivity {

    int frequency = 8000, channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    //int frequency = 11025,channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mFileName = null;

    private static String soundString = "";

    private Button mRecordButton = null;
    private MediaRecorder mRecorder = null;

    private PlayButton mPlayButton = null;
    private MediaPlayer mPlayer = null;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Record to the external cache directory for visibility

        //mFileName = getExternalFilesDir();
        mFileName = getCacheDir().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";

        //if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        //}

        LinearLayout ll = new LinearLayout(this);
        mRecordButton = new RecordButton(this);
        ll.addView(mRecordButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                       0));
        mPlayButton = new PlayButton(this);

        ll.addView(mPlayButton,
               new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
        //setContentView(ll);

        LinearLayout cl =  (LinearLayout)findViewById(R.id.MainLayout);


        cl.addView(ll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        //cl.addView(mRecordButton,
        //        new LinearLayout.LayoutParams(
        //                ViewGroup.LayoutParams.WRAP_CONTENT,
        //                ViewGroup.LayoutParams.WRAP_CONTENT,
        //        new LinearLayout.LayoutParams(
        //                0));
        //cl.addView(mPlayButton,
        //               ViewGroup.LayoutParams.WRAP_CONTENT,
        //                ViewGroup.LayoutParams.WRAP_CONTENT,
        //                0));

        //byte bytes[] = new byte[(int) mFileName.length()];
        //BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        //DataInputStream dis = new DataInputStream(bis);
        //dis.readFully(bytes);

        //byte[] imageBytes = baos.toByteArray();
        //String soundString = Base64.encodeToString(soundBytes, Base64.DEFAULT);

        //decode base64 string to image
        //soundBytes = Base64.decode(soundString, Base64.DEFAULT);
        //Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        //image.setImageBitmap(decodedImage);
    }

    public void ButtonPost_OnClick(View v)
    {
        LongOperation lo = new LongOperation();

        String ip = ((TextView) findViewById(R.id.editTextIP)).getText().toString();
        String port = ((TextView) findViewById(R.id.editTextPort)).getText().toString();

        lo.execute("http://" + ip + ":" + port);
    }

    public void ButtonPost2_OnClick(View v)
    {
        LongOperation lo = new LongOperation();

        TextView tv = ((TextView) findViewById(R.id.editTextCommand));
        tv.setText("[RecognizeBase64AMR(\"SoundByte\",\"Default\")]");

        String ip = ((TextView) findViewById(R.id.editTextIP)).getText().toString();
        String port = ((TextView) findViewById(R.id.editTextPort)).getText().toString();

        byte soundBytes[] = new byte[0];
        try {
            soundBytes = FileUtils.readFileToByteArray(new File(mFileName));
            soundString = Base64.encodeToString(soundBytes, Base64.DEFAULT);
            lo.execute("http://" + ip + ":" + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();

    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            //play();
            startPlaying();
        } else {
            //stop();
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        //record();
        mRecorder = new MediaRecorder();
        //mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        //mRecorder.setOutputFile(mFileName);
        //mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            mRecorder.setOutputFile(mFileName);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setPreviewDisplay(null);
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        //isRecording = false;
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    class RecordButton extends android.support.v7.widget.AppCompatButton {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }

    class PlayButton extends android.support.v7.widget.AppCompatButton {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop playing");
                } else {
                    setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }

    private class LongOperation extends AsyncTask<String, Void, Void> {

        // Required initialization


        private String Content;
        private String Error = null;
        private ProgressDialog Dialog = new ProgressDialog(MainActivity.this);
        String data = "";
        int sizeData = 0;


        protected void onPreExecute() {
            // NOTE: You can call UI Element here.

            //Start Progress Dialog (Message)

            Dialog.setMessage("Please wait..");
            Dialog.show();
            Dialog.setCancelable(false);
            Dialog.setCanceledOnTouchOutside(false);

            try {
                // Set Request parameter
                //data +="&" + URLEncoder.encode("username", "UTF-8") + "="+edittext.getText();

                String key = ((TextView) findViewById(R.id.editTextKey)).getText().toString();
                String command = ((TextView) findViewById(R.id.editTextCommand)).getText().toString().replace("SoundByte", soundString);

                data += "&" + URLEncoder.encode("key=" + key + "&output=error&action=" + command, "UTF-8");

            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        // Call after onPreExecute method
        protected Void doInBackground(String... urls) {

            /************ Make Post Call To Web Server ***********/
            BufferedReader reader = null;

            // Send data
            try {

                // Defined URL  where to send data
                URL url = new URL(urls[0]);
                //Toast.makeText(getApplicationContext(), url.getPort(), Toast.LENGTH_LONG).show();

                // Send POST data request

                URLConnection conn = url.openConnection();
                try
                {
                    conn.getPermission();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                //url.getPort();

                conn.setConnectTimeout(15000);//define connection timeout
                conn.setReadTimeout(15000);//define read timeout
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(data);
                wr.flush();

                // Get the server response

                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;


                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line + " ");
                }

                // Append Server Response To Content String
                Content = sb.toString();


            } catch (Exception ex) {
                Error = ex.getMessage();
            } finally {
                try {

                    reader.close();
                } catch (Exception ex) {
                }
            }


            return null;
        }

        protected void onPostExecute(Void unused) {
            // NOTE: You can call UI Element here.

            // Close progress dialog
            Dialog.dismiss();

            if (Error != null) {

                Toast.makeText(getApplicationContext(), "Error encountered", Toast.LENGTH_LONG).show();
            } else {

                try {

                    JSONObject jsonRootObject = new JSONObject(Content);

                    JSONObject json2 = jsonRootObject.getJSONObject("jsonkey");//pass jsonkey here

                    String id = json2.optString("id").toString();//parse json to string through parameters


                    //the result is stored in string id. you can display it now


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //http://www.java2s.com/Code/Android/Media/UsingAudioRecord.htm

    RecordAudio recordTask;
    PlayAudio playTask;
    boolean isRecording = false, isPlaying = false;

    public void record() {
        recordTask = new RecordAudio();
        recordTask.execute();
    }
    public void play() {
        //startPlaybackButton.setEnabled(true);

        playTask = new PlayAudio();
        playTask.execute();

        //stopPlaybackButton.setEnabled(true);
    }

    public void stop() {
        isPlaying = false;
        //stopPlaybackButton.setEnabled(false);
        //startPlaybackButton.setEnabled(true);
    }
    private class PlayAudio extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            isPlaying = true;

            int bufferSize = AudioTrack.getMinBufferSize(frequency,channelConfiguration, audioEncoding);
            short[] audiodata = new short[bufferSize / 4];

            try {
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(mFileName)));
                AudioTrack audioTrack = new AudioTrack(
                        AudioManager.STREAM_MUSIC, frequency,
                        channelConfiguration, audioEncoding, bufferSize,
                        AudioTrack.MODE_STREAM);

                audioTrack.play();
                while (isPlaying && dis.available() > 0) {
                    int i = 0;
                    while (dis.available() > 0 && i < audiodata.length) {
                        audiodata[i] = dis.readShort();
                        i++;
                    }
                    audioTrack.write(audiodata, 0, audiodata.length);
                }
                dis.close();
                //startPlaybackButton.setEnabled(false);
                //stopPlaybackButton.setEnabled(true);
            } catch (Throwable t) {
                Log.e("AudioTrack", "Playback Failed");
            }
            return null;
        }
    }
    private class RecordAudio extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            isRecording = true;
            try {
                FileUtils.deleteQuietly(new File(mFileName));
                publishProgress(new Integer(0)); // Reset status

                DataOutputStream dos = new DataOutputStream(
                        new BufferedOutputStream(new FileOutputStream(
                                mFileName)));
                int bufferSize = AudioRecord.getMinBufferSize(frequency,
                        channelConfiguration, audioEncoding);
                AudioRecord audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, frequency,
                        channelConfiguration, audioEncoding, bufferSize);

                short[] buffer = new short[bufferSize];

                audioRecord.startRecording();
                int r = 0;
                while (isRecording) {
                    int bufferReadResult = audioRecord.read(buffer, 0,
                            bufferSize);
                    for (int i = 0; i < bufferReadResult; i++) {
                        dos.writeShort(buffer[i]);
                    }
                    publishProgress(new Integer(r));
                    r++;
                }
                audioRecord.stop();
                dos.close();
            } catch (Throwable t) {
                Log.e("AudioRecord", "Recording Failed");
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            ((TextView)findViewById(R.id.StatusTextView)).setText(progress[0].toString());
        }

        protected void onPostExecute(Void result) {
            //startRecordingButton.setEnabled(true);
            //stopRecordingButton.setEnabled(false);
            //startPlaybackButton.setEnabled(true);
        }

        public void getValidSampleRates() {
            for (int rate : new int[] {8000, 11025, 16000, 22050, 44100}) {  // add the rates you wish to check against
                int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
                if (bufferSize > 0) {
                    // buffer size is valid, Sample rate supported

                }
            }
        }
    }
}
