/*
 * Copyright (c) 2013-2015 by appPlant UG. All rights reserved.
 *
 * @APPPLANT_LICENSE_HEADER_START@
 *
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apache License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at
 * http://opensource.org/licenses/Apache-2.0/ and read it before using this
 * file.
 *
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 *
 * @APPPLANT_LICENSE_HEADER_END@
 */

package de.appplant.cordova.plugin.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import de.appplant.cordova.plugin.localnotification.SnoozeReceiver;

/**
 * Builder class for local notifications. Build fully configured local
 * notification specified by JSON object passed from JS side.
 */
public class Builder {

  // Application context passed by constructor
  private final Context context;

  // Notification options passed by JS
  private final Options options;

  // Receiver to handle the trigger event
  private Class<?> triggerReceiver;

  // Receiver to handle the clear event
  private Class<?> clearReceiver = ClearReceiver.class;

  // Activity to handle the click event
  private Class<?> clickActivity = ClickActivity.class;

  /**
   * Constructor
   *
   * @param context Application context
   * @param options Notification options
   */
  public Builder(Context context, JSONObject options) {
    this.context = context;
    this.options = new Options(context).parse(options);
  }

  /**
   * Constructor
   *
   * @param options Notification options
   */
  public Builder(Options options) {
    this.context = options.getContext();
    this.options = options;
  }

  /**
   * Set trigger receiver.
   *
   * @param receiver Broadcast receiver
   */
  public Builder setTriggerReceiver(Class<?> receiver) {
    this.triggerReceiver = receiver;
    return this;
  }

  /**
   * Set clear receiver.
   *
   * @param receiver Broadcast receiver
   */
  public Builder setClearReceiver(Class<?> receiver) {
    this.clearReceiver = receiver;
    return this;
  }

  /**
   * Set click activity.
   *
   * @param activity Activity
   */
  public Builder setClickActivity(Class<?> activity) {
    this.clickActivity = activity;
    return this;
  }

  /**
   * Creates the notification with all its options passed through JS.
   */
  public Notification build() {
    Uri sound = options.getSoundUri();
    int smallIcon = options.getSmallIcon();
    int ledColor = options.getLedColor();
    NotificationCompat.Builder builder;

    builder = new NotificationCompat.Builder(context)
      .setDefaults(0)
      .setContentTitle(options.getTitle())
      .setContentText(options.getText())
      .setNumber(options.getBadgeNumber())
      .setTicker(options.getText())
      .setAutoCancel(options.isAutoClear())
      .setOngoing(options.isOngoing())
      .setColor(options.getColor());
     // .setDefaults(android.app.Notification.DEFAULT_ALL); // requires VIBRATE permission

    //builder.setStyle(new NotificationCompat.BigTextStyle().bigText("Snooze Notification when required"));
    builder.setStyle(new NotificationCompat.BigTextStyle());


    if (ledColor != 0) {
      builder.setLights(ledColor, options.getLedOnTime(), options.getLedOffTime());
    }

    if (sound != null) {
      builder.setSound(sound);
    }

    if (smallIcon == 0) {
      builder.setSmallIcon(options.getIcon());
    } else {
      builder.setSmallIcon(options.getSmallIcon());
      builder.setLargeIcon(options.getIconBitmap());
    }

    applyDeleteReceiver(builder);
    applyContentReceiver(builder);
    try {
      JSONObject j = new JSONObject(options.getDict().getString("data"));
      if (j.has("reschedule")) {
        applySnoozeReceiver(builder);
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return new Notification(context, options, builder, triggerReceiver);
  }

  /**
   * Set intent to handle the delete event. Will clean up some persisted
   * preferences.
   *
   * @param builder Local notification builder instance
   */
  private void applyDeleteReceiver(NotificationCompat.Builder builder) {

    if (clearReceiver == null)
      return;

    Intent intent = new Intent(context, clearReceiver)
      .setAction(options.getIdStr())
      .putExtra(Options.EXTRA, options.toString());

    PendingIntent deleteIntent = PendingIntent.getBroadcast(
      context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    builder.setDeleteIntent(deleteIntent);
  }

  /**
   * Set intent to handle the click event. Will bring the app to
   * foreground.
   *
   * @param builder Local notification builder instance
   */
  private void applyContentReceiver(NotificationCompat.Builder builder) {

    if (clickActivity == null)
      return;

    Intent intent = new Intent(context, clickActivity)
      .putExtra(Options.EXTRA, options.toString())
      .setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

    int reqCode = new Random().nextInt();

    PendingIntent contentIntent = PendingIntent.getActivity(
      context, reqCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    builder.setContentIntent(contentIntent);
  }

  private void applySnoozeReceiver(NotificationCompat.Builder builder) {


    String snoozeText = "Snooze";
    String dismissText = "Dismiss";
    try {
      JSONObject data = new JSONObject(options.getDict().getString("data"));
      if (data.has("snoozeText")) {
        snoozeText = data.getString("snoozeText");
      }
      if (data.has("dismissText")) {
        dismissText = data.getString("dismissText");
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }

    Intent clearIntent = new Intent(context, SnoozeReceiver.class)
      .setAction(options.getIdStr())
      .putExtra(Options.EXTRA, options.toString())
      .putExtra("type", "dismiss");

    PendingIntent deleteIntent = PendingIntent.getBroadcast(
      context, 0, clearIntent, PendingIntent.FLAG_UPDATE_CURRENT);
/*
    Resources resources = context.getResources();

    final int resourceId = resources.getIdentifier("ic_action_remove", "drawable",
      context.getPackageName());
      */

   // Log.e("bimal", "::"+resourceId);



    builder.addAction(0, dismissText, deleteIntent);

    Intent intent = new Intent(context, SnoozeReceiver.class)
      .setAction(options.getIdStr())
      .putExtra(Options.EXTRA, options.toString())
      .putExtra("type", "snooze");

    PendingIntent snoozeIntent = PendingIntent.getBroadcast(
      context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    builder.addAction(0, snoozeText, snoozeIntent);


  }

}
