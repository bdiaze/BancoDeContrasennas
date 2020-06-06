package cl.theroot.passbank;

import android.app.Notification;
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
        Log.i(TAG, "onDestroy() - Servicio ");
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
            Log.i(TAG, "onStartCommand(...) - Iniciando servicio de respaldo...");
            String titulo = getString(R.string.respServTitulo);
            String mensaje = getString(R.string.respServMensaje);

            Intent notificationIntent = new Intent(context, ActividadPrincipal.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

            Drawable vectorDrawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_strongbox, getTheme());
            Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);

            Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID_RESPALDAR)
                    .setContentTitle(titulo)
                    .setContentText(mensaje)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setLargeIcon(bitmap)
                    .setContentIntent(pendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(mensaje))
                    .build();
            startForeground(2, notification);

            GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context);
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(DriveScopes.DRIVE_APPDATA));
            credential.setSelectedAccount(googleSignInAccount.getAccount());
            Drive gooleDriveService = new Drive.Builder(
                    new NetHttpTransport(),
                    new GsonFactory(),
                    credential
            ).build();
            DriveServiceHelper driveServiceHelper = new DriveServiceHelper(gooleDriveService);
            Log.i(TAG, String.format("onStartCommand(...) - Definiendo cuenta %s para uso de Google Drive.", googleSignInAccount.getEmail()));

            Log.i(TAG, "driveServiceHelper.queryFiles() - Obteniendo listado de respaldos a eliminar...");
            driveServiceHelper.queryFiles()
                    .addOnSuccessListener(fileList -> {
                        Log.i(TAG, "driveServiceHelper.queryFiles() - Listado de respaldos a eliminar obtenido.");
                        List<String> idsEliminar = new ArrayList<>();
                        for (File file : fileList.getFiles()) {
                            idsEliminar.add(file.getId());
                        }

                        Log.i(TAG, "driveServiceHelper.deleteFiles(...) - Eliminando respaldos anteriores...");
                        driveServiceHelper.deleteFiles(idsEliminar)
                                .addOnSuccessListener(result -> {
                                    Log.i(TAG, "driveServiceHelper.deleteFiles(...) - Respaldos anteriores eliminados exitosamente.");

                                    DBOpenHelper.getInstance(getApplicationContext(), NombreBD.BANCO_CONTRASENNAS).cerrarConexiones();
                                    final java.io.File dbFile = getApplicationContext().getDatabasePath(NombreBD.BANCO_CONTRASENNAS.toString());
                                    Log.i(TAG, String.format("driveServiceHelper.uploadFile(...) - El archivo a respaldar tiene un tamaño de %d bytes.", dbFile.length()));

                                    Log.i(TAG, "driveServiceHelper.uploadFile(...) - Subiendo nuevo respaldo SQLite...");
                                    driveServiceHelper.uploadFile(dbFile, "application/x-sqlite3")
                                            .addOnSuccessListener(file -> {
                                                Log.i(TAG, "driveServiceHelper.uploadFile(...) - Respaldo SQLite subido exitosamente!");
                                                Notificacion.Mostrar(context, 3, R.string.respCreadoTitulo, R.string.respCreadoMensaje);
                                                stopSelf();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "driveServiceHelper.uploadFile(...) - Error al subir nuevo respaldo.", e);
                                                Notificacion.Mostrar(context, 3, R.string.respFallidoTitulo, R.string.creacionRespFallida);
                                                stopSelf();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "driveServiceHelper.deleteFiles(...) - Error al eliminar los respaldos antiguos.", e);
                                    Notificacion.Mostrar(context, 3, R.string.respFallidoTitulo, R.string.creacionRespFallida);
                                    stopSelf();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "driveServiceHelper.queryFiles() - Error al obtener listado de respaldos a eliminar.", e);
                        Notificacion.Mostrar(context, 3, R.string.respFallidoTitulo, R.string.creacionRespFallida);
                        stopSelf();
                    });
        }
    }
}
