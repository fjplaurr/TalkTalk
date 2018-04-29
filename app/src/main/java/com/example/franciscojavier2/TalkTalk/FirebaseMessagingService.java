package com.example.franciscojavier2.TalkTalk;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import com.google.firebase.messaging.RemoteMessage;

//Todo el siguiente código visto en la Api de Android: "Building a notification"
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //Tomo los valores del node.js
        String notification_title= remoteMessage.getNotification().getTitle();
        String notification_message=remoteMessage.getNotification().getBody();
        String click_action=remoteMessage.getNotification().getClickAction();  //He incluido un clickaction
                //en el node.js para poder usar esa variable desde el remoteMessage y utilizarla en el intent
                //de abajo que abrirá el ProfilActivity dado que así lo he indicado en el AndroidManifest.
        String from_user_id=remoteMessage.getData().get("from_user_id"); //Necesito el nombre del user que envía
                //la solicitud ya que tengo q poner un extra en el intent ya q así lo requiere la clase ActivityProfile.

        //Notification Builder es el constructor de la clase.
        NotificationCompat.Builder mBuilder =new NotificationCompat.Builder(this,"notif_channel")
                .setSmallIcon(R.drawable.default_avatar)
                .setContentTitle(notification_title)
                .setContentText(notification_message);


        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("user_id",from_user_id);   //Información que le envío a la clase
                //ActivityProfile en un Bundle.

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        //Información que recibe el user acerca de la notificación.
        // Sets an ID for the notification
        int mNotificationId = (int)System.currentTimeMillis(); //Con ésto consigo un identificador único.
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

        //Intent que se genera al pulsar la notificación.

    }
}
