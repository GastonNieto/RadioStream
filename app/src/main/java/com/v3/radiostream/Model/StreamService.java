package com.v3.radiostream.Model;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.v3.radiostream.R;
import com.v3.radiostream.View.RadioView;

import java.io.IOException;

import static android.media.AudioManager.AUDIOFOCUS_GAIN;

public class StreamService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnCompletionListener {
    MediaPlayer mediaPlayer = new MediaPlayer();
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    SharedPreferences prefs;
    private Intent intent;
    String a;
    String opcion;
    public static final String INTENT_RECEIVER = "LISTENER";
    public static final String KEY_BC = "KEY_BC";
    public static final String KEY_STOP = "KEY_STOP";
    private final String CHANNEL_ID = "com.v3.RadioStream";
    private final int NOTIFICATION_ID = 001;
    AudioManager audioManager;
    private boolean ispausecall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    public StreamService() {
    }


    @Override
    public void onCreate() {

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        intent = new Intent(INTENT_RECEIVER);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        updateUI("paro");
        if (mediaPlayer != null)
            mediaPlayer.release();
        audioManager.abandonAudioFocus(this);
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);

        }

        super.onDestroy();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                super.onCallStateChanged(state, phoneNumber);
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pause();
                            ispausecall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (mediaPlayer != null) {
                            if (true) {
                                ispausecall = false;
                                mediaPlayer.start();
                            }
                        }
                        break;
                }
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        int requestAudiofocusResult = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AUDIOFOCUS_GAIN);
        if (requestAudiofocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            createnotificationchannel();
            Intent activityIntent = new Intent(this, RadioView.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent contentIntent = PendingIntent.getActivity(this,
                    0, activityIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            opcion = "chau";
            Intent broadcastIntent = new Intent(this, NotificationReciber.class);
            broadcastIntent.putExtra("toastMessage", opcion);
            PendingIntent actionIntent = PendingIntent.getBroadcast(this,
                    0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            RemoteViews custom = new RemoteViews(getPackageName(), R.layout.notification_custom);
            custom.setOnClickPendingIntent(R.id.btnparar, actionIntent);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_play)
                    //.setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                  //  .setContentTitle("Radio")
                   // .setContentText("En vivo")
                   .setCustomContentView(custom)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    ///.addAction(R.drawable.ic_stop, "Detener", actionIntent)
                    .build();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource("http://cast3.radiohost.ovh:8352/stream");
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.setOnCompletionListener(this);
                mediaPlayer.setOnErrorListener(this);

            } catch (IOException e) {
                e.printStackTrace();
                Log.i("erroraca", e.getMessage());

            }
            startForeground(1, notification);
            return super.START_STICKY;
        }
        return super.START_STICKY;

    }

    private void stop() {

        mediaPlayer.stop();

    }

    private void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }


    }


    private void createnotificationchannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Radio notificación";
            String descripcion = "Notificacíon para detener el streaming.";
            int importans = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, importans);
            notificationChannel.setDescription(descripcion);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
        updateUI("Reproduciendo..");

    }

    private void updateUI(String s) {
        intent.putExtra(KEY_BC, s);
        sendBroadcast(intent);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e("holamundo", String.format("Error(%s%s)", what, extra));
        // playlist="ERROR";

        if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            mediaPlayer.reset();
            Log.e("entroaca2", "entroaca");

            updateUI("ERROR");
        } else if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
            mediaPlayer.reset();
            Log.e("entroaca1", "entroaca");
            updateUI("ERROR");
        }
        // Deal with any other errors you need to.

        // I'm under the assumption you set the path to the song
        // and handle onPrepare, start(), etc with this function
        //  playSong(getApplicationContext(),currenturl);
        //   mediaPlayer.setOnErrorListener(this);
        // mediaPlayer.setOnCompletionListener(this);
        //    mediaPlayer.setOnPreparedListener(this);

        return true;

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    private void mediaplay() {

        mediaPlayer.start();

    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT):
                pause();
                break;

            case (AudioManager.AUDIOFOCUS_LOSS):
                pause();
                break;

            case (AudioManager.AUDIOFOCUS_GAIN):
                // Return the volume to normal and resume if paused.
                mediaPlayer.setVolume(1f, 1f);
                mediaPlayer.start();
                break;
            default:
                break;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }
}
