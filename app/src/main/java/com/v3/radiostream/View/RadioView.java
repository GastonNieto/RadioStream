package com.v3.radiostream.View;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.v3.radiostream.Model.NotificationReciber;
import com.v3.radiostream.Model.StreamService;
import com.v3.radiostream.R;

import static com.v3.radiostream.Model.StreamService.KEY_BC;
import static com.v3.radiostream.Model.StreamService.KEY_STOP;

public class RadioView extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private ImageButton btnPS;
    private TextView tvstate;
    private SeekBar sbvolumen;
    private AudioManager audioManager;
    private boolean state = true;
    private BroadcastReceiver broadcastReceiver;
    private LottieAnimationView lottieAnimationView;
    private BroadcastReceiver mIntentReceiver;

    private ListenerStop listenerStop;
    private IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio_view);
        //lottieAnimationView = findViewById(R.id.animation_view);
        ini();
        sbvolumen = findViewById(R.id.sbVolumen);
        tvstate = findViewById(R.id.tvConectando);
        btnPS = findViewById(R.id.btnPS);
        setbs();
        sbvolumen.setOnSeekBarChangeListener(this);
//        lottieAnimationView.playAnimation();
        //     int i = lottieAnimationView.getFrame();
        // Log.e("framelottie", String.valueOf(i));
        btnPS.setOnClickListener(this);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                tvstate.setText(intent.getStringExtra(KEY_BC));
                String a = tvstate.getText().toString();
                if (a.equals("paro")) {
                    state = true;
                    btnPS.setImageResource(R.drawable.ic_play);
                }
            }
        };
        if (isMyServiceRunning(StreamService.class)) {
            state = false;
            btnPS.setImageResource(R.drawable.ic_stop);
        }
    }

    public void ini() {
        intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        intentFilter.addAction(String.valueOf(AudioManager.AUDIOFOCUS_LOSS));
        listenerStop = new ListenerStop();

    }

    public void setbs() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        sbvolumen.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        sbvolumen.setProgress(audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPS:
                if (state) {
                    Intent intent = new Intent(this, StreamService.class);
                    startService(intent);
                    state = false;
                    btnPS.setImageResource(R.drawable.ic_stop);
                } else {
                    Intent intent = new Intent(this, StreamService.class);
                    stopService(intent);
                    state = true;
                    btnPS.setImageResource(R.drawable.ic_play);

                }

                break;
        }
    }

    private boolean isMyServiceRunning(Class<StreamService> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        registerReceiver(broadcastReceiver, new IntentFilter(StreamService.INTENT_RECEIVER));
        registerReceiver(listenerStop, intentFilter);

        IntentFilter intentFilter = new IntentFilter("SmsMessage.intent.MAIN");
        mIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.getStringExtra("get_msg");
                if (msg.equals("lila")) {
                    stopStream();
                }
            }
        };
        this.registerReceiver(mIntentReceiver, intentFilter);
        super.onResume();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                progress, 0);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public class ListenerStop extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String cap = intent.getAction();

            if (cap.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                stopStream();
            }
        }
    }

    private void stopStream() {
        Intent intent = new Intent(this, StreamService.class);
        stopService(intent);
        state = true;
        btnPS.setImageResource(R.drawable.ic_play);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
        {
            int index = sbvolumen.getProgress();
            sbvolumen.setProgress(index + 1);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
        {
            int index = sbvolumen.getProgress();
            sbvolumen.setProgress(index - 1);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}


