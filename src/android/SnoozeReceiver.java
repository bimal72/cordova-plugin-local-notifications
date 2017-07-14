package de.appplant.cordova.plugin.localnotification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import de.appplant.cordova.plugin.notification.*;
import de.appplant.cordova.plugin.notification.Options;

import static java.lang.Integer.parseInt;

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
    Bundle bundle = intent.getExtras();
    JSONObject options;
    try {
      String data = bundle.getString(Options.EXTRA);
      options = new JSONObject(data);
      Manager manager = Manager.getInstance(context);
      try {
        if (options.has("every")) {
          String every = options.getString("every");
          long at = options.getLong("at");
          long currentTime = System.currentTimeMillis()/1000;
          if (every.equals("day")) {
            long newTime = at + (24 * 60 * 60);
            while(currentTime > newTime){
              newTime = newTime + (24 * 60 * 60);
            }
            options.put("at", newTime);
          } else {
            long newTime = at + (parseInt(every) * 60);
            while(currentTime > newTime){
              newTime = newTime + (parseInt(every) * 60);
            }
            options.put("at", newTime);
          }
          Notification notification =
            manager.update(options.getInt("id"), options, TriggerReceiver.class);

          if (notification != null) {
            LocalNotification.fireEvent("update", notification);
          }
        } else {
          manager.cancel(options.getInt("id"));
        }

      } catch (JSONException e) {
        e.printStackTrace();
        return;
      }
      String type = bundle.getString("type");
      if (type.equals("snooze")) {
        //options.put("reschedule", true);

        JSONObject optionsSnooze = new JSONObject(data);
        if (optionsSnooze.has("every")) {
          optionsSnooze.remove("every");
        }
        int snoozeDuration = 300;
        if (optionsSnooze.has("data")) {
          JSONObject dataOptions = new JSONObject(optionsSnooze.getString("data"));
          if (dataOptions.has("snoozeDuration")) {
            snoozeDuration = dataOptions.getInt("snoozeDuration");
          }
        }

        optionsSnooze.put("at", (System.currentTimeMillis()/1000) + snoozeDuration);
        optionsSnooze.put("id", 1000);
        //optionsSnooze.put("text", "bimal................");
        Notification notification =
          manager.schedule(optionsSnooze, TriggerReceiver.class);

        LocalNotification.fireEvent("schedule", notification);
      }
      /*else if (type.equals("dismiss")) {
        options.put("dismiss", true);
      }*/
    } catch (JSONException e) {
      e.printStackTrace();
      return;
    }


   /* Notification notification =
      new Builder(context, options).build();
    notification.forceClear();
    LocalNotification.fireEvent("schedule", notification);*/

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
