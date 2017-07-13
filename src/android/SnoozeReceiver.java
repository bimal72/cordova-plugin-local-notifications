package de.appplant.cordova.plugin.localnotification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import de.appplant.cordova.plugin.notification.*;
import de.appplant.cordova.plugin.notification.Options;

public class SnoozeReceiver extends BroadcastReceiver {
  public SnoozeReceiver() {
  }
/*
  @Override
  public void onClear (Notification notification) {
   super.onClear(notification);
   notification.schedule1();
    LocalNotification.fireEvent("schedule", notification);

  }*/

  public void onReceive(Context context, Intent intent) {
    Bundle bundle  = intent.getExtras();
    JSONObject options;

    try {
      String data = bundle.getString(Options.EXTRA);
      options = new JSONObject(data);
      String type = bundle.getString("type");
      if(type.equals("snooze")){
        options.put("reschedule", true);
      } else if(type.equals("dismiss")){
        options.put("dismiss", true);
      }
    } catch (JSONException e) {
      e.printStackTrace();
      return;
    }

    Notification notification =
      new Builder(context, options).build();
    notification.forceClear();
    LocalNotification.fireEvent("schedule", notification);

  }
/*
  @Override
  public void onReceive(Context context, Intent intent) {
    // TODO: This method is called when the BroadcastReceiver is receiving
    // an Intent broadcast.

    int notificationId = intent.getIntExtra("notificationId", 0);
    Toast.makeText(context, ""+notificationId, Toast.LENGTH_SHORT).show();

    // if you want cancel notification
    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    manager.cancel(notificationId);

  } */
}
