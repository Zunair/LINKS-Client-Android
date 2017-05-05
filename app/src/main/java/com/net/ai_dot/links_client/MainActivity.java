package com.net.ai_dot.links_client;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
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
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Date;
import java.sql.Time;
import java.util.Calendar;

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
    private static String ip = "";
    private static String port = "";
    private static String key = "";
    private static String command = "";

    private Button mPTT = null;

    private Button mRecordButton = null;
    private MediaRecorder mRecorder = null;

    private PlayButton mPlayButton = null;
    private MediaPlayer mPlayer = null;

    CheckConnection checkConnection = null;
    public static int connectionStatus = -1;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    private SettingsMain settings;
    //private SwitchCompat switchVibrate;


    private void setResources(){

        settings.setKey(key);
        settings.setCommand(command);
        settings.setPort(port);
        settings.setIP(ip);

        //settings.save();
    }

    private void getSettings(){
        settings = new SettingsMain(this);

        key =  settings.getKey();
        command = settings.getCommand();
        port = settings.getPort();
        ip = settings.getIP();

        ((TextView) findViewById(R.id.editTextIP)).setText(ip);
        ((TextView) findViewById(R.id.editTextKey)).setText(key);
        ((TextView) findViewById(R.id.editTextCommand)).setText(command);
        ((TextView) findViewById(R.id.editTextPort)).setText(port);

    }

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
        mPTT = new PTT(this);

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

        ll.addView(mPTT,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
        //setContentView(ll);

        LinearLayout cl =  (LinearLayout)findViewById(R.id.MainLayout);


        cl.addView(ll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
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
        //checkConnection();
        getSettings();
    }

    public void checkConnection()
    {
        if (isNetworkAvailable()) {
            ((TextView)findViewById(R.id.StatusTextView)).setText("Network is available.");
            //Toast.makeText(getApplicationContext(), "Network is available.", Toast.LENGTH_LONG).show();
            checkConnection = new CheckConnection();
            checkConnection.execute();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Network connection was not found");
            builder.setPositiveButton("OK", null);
            builder.show();
        }
    }

    public void ButtonConnect_OnClick(View v) {
        checkConnection();
    }

    public void ButtonPost_OnClick(View v) {
        LongOperation lo = new LongOperation();
        String lastIP = ip, lastPort = port;
        ip = ((TextView) findViewById(R.id.editTextIP)).getText().toString();
        port = ((TextView) findViewById(R.id.editTextPort)).getText().toString();

        try {
            if (isNetworkAvailable()) {
                lo.execute("http://" + ip + ":" + port);
                setResources();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                String msg = "Can not reach LINKS, please make sure the IP and Port is correct.";

                if (!(ip.startsWith("10.") || ip.startsWith("172.16.") || ip.startsWith("192.168."))) {
                    msg += "\r\n" + "TIP: Make sure port forwarding is configured on your router when using external IP.";
                }

                builder.setMessage(msg);

                builder.setPositiveButton("OK", null);
                builder.show();
            }
        }
        catch (Exception error)
        {
            String msg = "Error: " + error.getMessage() + "\r\n";
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if (!(ip.startsWith("10.") || ip.startsWith("172.16.") || ip.startsWith("192.168."))) {
                msg += "TIP: Make sure port forwarding is configured on your router when using external IP.";
            }

            builder.setMessage(msg);

            builder.setPositiveButton("OK", null);
            builder.show();
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public void ButtonPost2_OnClick(View v)
    {
        SendSoundData();
    }

    private void SendSoundData()
    {
        LongOperation lo = new LongOperation();

        TextView tv = ((TextView) findViewById(R.id.editTextCommand));
        tv.setText("[RecognizeBase64AMR(\"SoundByte\",\"Default\")]");

        ip = ((TextView) findViewById(R.id.editTextIP)).getText().toString();
        port = ((TextView) findViewById(R.id.editTextPort)).getText().toString();

        byte soundBytes[] = new byte[0];
        try {
            File f = new File(mFileName);
            if (f.exists()) {
                soundBytes = FileUtils.readFileToByteArray(f);
                soundString = Base64.encodeToString(soundBytes, Base64.DEFAULT);
                FileUtils.forceDelete(new File(mFileName));
                lo.execute("http://" + ip + ":" + port);
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Press and hold PTT button while speaking.", Toast.LENGTH_LONG).show();
            }
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
            MediaPlayer.OnCompletionListener playCompleted = new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            };

            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
            mPlayer.setOnCompletionListener(playCompleted);


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

    class PTT extends android.support.v7.widget.AppCompatButton {
        boolean mStartRecording = true;

        int startSeconds;

        Calendar cDue;

        OnTouchListener touch = new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch ( event.getAction() )
                {
                    case MotionEvent.ACTION_DOWN:
                        cDue = Calendar.getInstance();
                        cDue.add (Calendar.SECOND, 1);

                        setText("Recording");
                        onRecord(mStartRecording);
                        break;

                    case MotionEvent.ACTION_UP:
                        while (cDue.getTimeInMillis() > Calendar.getInstance().getTimeInMillis())
                        {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        setText("PTT");
                        onRecord(mStartRecording);
                        //startPlaying();
                        SendSoundData();
                        break;
                }
                mStartRecording = !mStartRecording;
                return false;
            }
        };

        public PTT(Context ctx) {
            super(ctx);
            setText("PTT");
            setOnTouchListener(touch);
        }
    }

    class RecordButton extends android.support.v7.widget.AppCompatButton {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop Recording");
                } else {
                    setText("Start Recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start Recording");
            setOnClickListener(clicker);
        }
    }

    class PlayButton extends android.support.v7.widget.AppCompatButton {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop Playing");
                } else {
                    setText("Start Playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start Playing");
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

                key = ((TextView) findViewById(R.id.editTextKey)).getText().toString();
                command = ((TextView) findViewById(R.id.editTextCommand)).getText().toString().replace("SoundByte", soundString);

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
            HttpURLConnection conn = null;
            try {

                // Defined URL  where to send data
                URL url = new URL(urls[0]);
                //Toast.makeText(getApplicationContext(), url.getPort(), Toast.LENGTH_LONG).show();

                // Send POST data request

                conn = (HttpURLConnection)url.openConnection();
                try {
                    conn.getPermission();
                } catch (Exception e) {
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
                if (conn.getResponseMessage() != "OK") {
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
                }else{
                    Error = conn.getResponseMessage();
                    Content = null;
                }
            } catch (EOFException ex) {
                Error = "No response from specified IP.\r\nMake sure Key is valid.";

            } catch (Exception ex) {
                Error = ex.getMessage();
            } finally {
                try {
                    if (conn != null)
                        conn.disconnect();
                    if (reader != null)
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

                Toast.makeText(getApplicationContext(), "Error encountered.\r\n" + Error, Toast.LENGTH_LONG).show();
            } else {

                try {

                    if (Content != null && Content.trim() != "") {

                        JSONObject jsonRootObject = new JSONObject(Content);

                        //JSONObject json2 = jsonRootObject.getJSONObject("jsonkey");//pass jsonkey here

                        //String audioAsBase64 = json2.optString("response").toString();//parse json to string through parameters


                        //the result is stored in string id. you can display it now
                    }

                } catch (JSONException e) {
                    //e.printStackTrace();
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

    private class CheckConnection extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            try {
                int timeoutMs = 3000;
                Socket sock = new Socket();
                SocketAddress socketAddress = new InetSocketAddress("8.8.8.8", 53);

                sock.connect(socketAddress, timeoutMs);

                sock.close();
                publishProgress(1);

            } catch (IOException e)
            {
                publishProgress(0);
            }
            return null;
        }

        private boolean isOnTheInternet() {
            try {
                URLConnection urlConnection = new URL("http://yourserver").openConnection();
                urlConnection.setConnectTimeout(400);
                urlConnection.connect();
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        protected void onProgressUpdate(Integer... progress) {
            //connectionStatus = progress[0];
            String msg;
            if (progress[0] == 0) {
                msg = "Network is available but Internet is not.";
            }else {
                msg = "Internet is available.";
            }
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            ((TextView)findViewById(R.id.StatusTextView)).setText(msg);
        }

        protected void onPostExecute(Void result) {
            //startRecordingButton.setEnabled(true);
            //stopRecordingButton.setEnabled(false);
            //startPlaybackButton.setEnabled(true);
        }
    }
}

