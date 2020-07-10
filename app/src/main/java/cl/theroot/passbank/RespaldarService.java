package cl.theroot.passbank;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cl.theroot.passbank.datos.DBOpenHelper;
import cl.theroot.passbank.datos.nombres.NombreBD;

import static cl.theroot.passbank.App.CHANNEL_ID_RESPALDAR;

public class RespaldarService extends Service {
    private static final String TAG = "BdC-RespaldarService";
    private boolean running;

    @Override
    public void onCreate() {
        super.onCreate();
        running = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!running) {
            running = true;

            RespaldarRunnable runnable = new RespaldarRunnable(this);
            new Thread(runnable).start();
        } else {
            Log.i(TAG, "onStartCommand(...) - Intento de iniciar el servicio ignorado, éste ya se encuentra en ejecución.");
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy() - Proceso del servicio terminado.");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class RespaldarRunnable implements Runnable {
        Context context;

        public RespaldarRunnable(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            Log.i(TAG, "RespaldarRunnable.run() - Iniciando servicio de respaldo...");
            Intent notificationIntent = new Intent(context, ActividadPrincipal.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

            Drawable vectorDrawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_strongbox, getTheme());
            Bitmap bitmap = null;
            if (vectorDrawable != null) {
                bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                vectorDrawable.draw(canvas);
            }

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL_ID_RESPALDAR)
                    .setContentTitle(getString(R.string.respServTitulo))
                    .setOnlyAlertOnce(true)
                    .setColor(getColor(R.color.azul04))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setLargeIcon(bitmap)
                    .setContentIntent(pendingIntent)
                    .setProgress(100, 0, false)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.respEtapaInicResp)));
            startForeground(2, notification.build());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, "RespaldarRunnable.run() - Error al dormir por un segundo.", e);
            }

            notification.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.respEtapaBuscResp)));
            notification.setProgress(100, 25, false);
            notificationManager.notify(2, notification.build());

            GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context);
            if (googleSignInAccount == null) {
                Log.e(TAG, "RespaldarRunnable.run() - No se pudo obtener última cuenta de google iniciada.");
                Notificacion.Mostrar(context, 3, R.string.respFallidoTitulo, R.string.respEtapaInicRespError);
                stopSelf();
            } else {
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(DriveScopes.DRIVE_APPDATA));
                credential.setSelectedAccount(googleSignInAccount.getAccount());
                Drive gooleDriveService = new Drive.Builder(
                        new NetHttpTransport(),
                        new GsonFactory(),
                        credential
                ).setApplicationName("Banco de Contraseñas").build();
                DriveServiceHelper driveServiceHelper = new DriveServiceHelper(gooleDriveService);
                Log.i(TAG, String.format("RespaldarRunnable.run() - Definiendo cuenta %s para uso de Google Drive.", googleSignInAccount.getEmail()));

                Log.i(TAG, "driveServiceHelper.queryFiles() - Obteniendo listado de respaldos a eliminar...");
                driveServiceHelper.queryFiles()
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "driveServiceHelper.queryFiles() - Error al obtener listado de respaldos a eliminar.", e);
                            Notificacion.Mostrar(context, 3, R.string.respFallidoTitulo, R.string.respEtapaBuscRespError);
                            stopSelf();
                        })
                        .addOnSuccessListener(fileList -> {
                            notification.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.respEtapaElimResp)));
                            notification.setProgress(100, 50, false);
                            notificationManager.notify(2, notification.build());
                            Log.i(TAG, "driveServiceHelper.queryFiles() - Listado de respaldos a eliminar obtenido.");
                            List<String> idsEliminar = new ArrayList<>();
                            for (File file : fileList.getFiles()) {
                                idsEliminar.add(file.getId());
                            }

                            Log.i(TAG, "driveServiceHelper.deleteFiles(...) - Eliminando respaldos anteriores...");
                            driveServiceHelper.deleteFiles(idsEliminar)
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "driveServiceHelper.deleteFiles(...) - Error al eliminar los respaldos antiguos.", e);
                                        Notificacion.Mostrar(context, 3, R.string.respFallidoTitulo, R.string.respEtapaElimRespError);
                                        stopSelf();
                                    })
                                    .addOnSuccessListener(result -> {
                                        notification.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.respEtapaSubResp)));
                                        notification.setProgress(100, 75, false);
                                        notificationManager.notify(2, notification.build());
                                        Log.i(TAG, "driveServiceHelper.deleteFiles(...) - Respaldos anteriores eliminados exitosamente.");

                                        DBOpenHelper.getInstance(getApplicationContext(), NombreBD.BANCO_CONTRASENNAS).cerrarConexiones();
                                        final java.io.File dbFile = getApplicationContext().getDatabasePath(NombreBD.BANCO_CONTRASENNAS.toString());
                                        Log.i(TAG, String.format("driveServiceHelper.uploadFile(...) - El archivo a respaldar tiene un tamaño de %d bytes.", dbFile.length()));

                                        Log.i(TAG, "driveServiceHelper.uploadFile(...) - Subiendo nuevo respaldo SQLite...");
                                        driveServiceHelper.uploadFile(dbFile, "application/x-sqlite3")
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "driveServiceHelper.uploadFile(...) - Error al subir nuevo respaldo.", e);
                                                    Notificacion.Mostrar(context, 3, R.string.respFallidoTitulo, R.string.respEtapaSubRespError);
                                                    stopSelf();
                                                })
                                                .addOnSuccessListener(file -> {
                                                    notification.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.respCreadoMensaje)));
                                                    notification.setProgress(100, 100, false);
                                                    notificationManager.notify(2, notification.build());
                                                    Log.i(TAG, "driveServiceHelper.uploadFile(...) - Respaldo SQLite subido exitosamente!");

                                                    try {
                                                        Thread.sleep(1000);
                                                    } catch (InterruptedException e) {
                                                        Log.e(TAG, "driveServiceHelper.uploadFile(...) - Error al dormir por un segundo.", e);
                                                    }
                                                    Notificacion.Mostrar(context, 3, R.string.respCreadoTitulo, R.string.respCreadoMensaje);
                                                    stopSelf();
                                                });
                                    });
                        });
            }
        }
    }
}
