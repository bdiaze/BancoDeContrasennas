package cl.theroot.passbank;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;

import cl.theroot.passbank.dominio.TipoNotificacion;


public class Notificacion {
    private static final String TAG = "BdC-Notificacion";

    public static void Mostrar(Context context, TipoNotificacion tipo, String titulo, String mensaje) {
        NotificationChannel channel = new NotificationChannel(tipo.getId(), tipo.getNombre(), NotificationManager.IMPORTANCE_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        Notification.Builder builder = new Notification.Builder(context, tipo.getId())
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.strongbox_icon))
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setStyle(new Notification.BigTextStyle().bigText(mensaje))
                .setAutoCancel(true);
        notificationManager.notify(1, builder.build());
    }
}
