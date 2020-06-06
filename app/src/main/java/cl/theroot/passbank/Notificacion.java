package cl.theroot.passbank;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import static cl.theroot.passbank.App.CHANNEL_ID_GENERAL;


public class Notificacion {
    private static final String TAG = "BdC-Notificacion";

    public static void Mostrar(Context context, int id, int idTitulo, int idMensaje) {
        Mostrar(context, id, context.getString(idTitulo), context.getString(idMensaje));
    }

    public static void Mostrar(Context context, int idTitulo, int idMensaje) {
        Mostrar(context, 1, context.getString(idTitulo), context.getString(idMensaje));
    }

    public static void Mostrar(Context context, String titulo, String mensaje) {
        Mostrar(context, 1, titulo, mensaje);
    }

    public static void Mostrar(Context context, int id, String titulo, String mensaje) {
        Intent notificationIntent = new Intent(context, ActividadPrincipal.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        Drawable vectorDrawable = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_strongbox, context.getTheme());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setLargeIcon(bitmap)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensaje))
                .build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(id, notification);
    }
}
