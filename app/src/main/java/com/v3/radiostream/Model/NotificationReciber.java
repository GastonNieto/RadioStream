package com.v3.radiostream.Model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationReciber extends BroadcastReceiver {
    private Intent intents;
    public static final String INTENT_RECEIVER = "LISTENER";
    public static final String KEY_BC = "KEY_BC";
    public static final String KEY_STOP2 = "KEY_STOP2";

    @Override
    public void onReceive(Context context, Intent intent) {
       String message = intent.getStringExtra("toastMessage");
       //if(message.equals("hola")){
           Intent intents = new Intent(context, StreamService.class);
           context.stopService(intents);
  /*     }else{
           Intent in = new Intent("SmsMessage.intent.MAIN").
                   putExtra("get_msg", "lila");

           //You can place your check conditions here(on the SMS or the sender)
           //and then send another broadcast
           context.sendBroadcast(in);*/
       }

}
