package cl.theroot.passbank;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.util.Collections;

import cl.theroot.passbank.datos.CategoriaCuentaDAO;
import cl.theroot.passbank.datos.CategoriaDAO;
import cl.theroot.passbank.datos.ContrasennaDAO;
import cl.theroot.passbank.datos.CuentaDAO;
import cl.theroot.passbank.datos.DBOpenHelper;
import cl.theroot.passbank.datos.ParametroDAO;
import cl.theroot.passbank.datos.nombres.NombreBD;
import cl.theroot.passbank.datos.nombres.NombreParametro;
import cl.theroot.passbank.dominio.Contrasenna;
import cl.theroot.passbank.dominio.Parametro;

import static cl.theroot.passbank.App.CHANNEL_ID_RESPALDAR;

public class CargarRespaldoService extends Service {
    private static final String TAG = "BdC-CargarRespaldoService";

    private final IBinder mBinder = new MiBinder();
    private ICargarRespaldo iCargarRespaldo;

    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder notification;

    private boolean running;
    private String contrRespaldo;
    private String contrNueva;

    @Override
    public void onCreate() {
        super.onCreate();
        running = false;

        Intent notificationIntent = new Intent(this, ActividadPrincipal.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Drawable vectorDrawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_strongbox, getTheme());
        Bitmap bitmap = null;
        if (vectorDrawable != null) {
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
        }

        notificationManager = NotificationManagerCompat.from(getApplicationContext()); //(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification = new NotificationCompat.Builder(this, CHANNEL_ID_RESPALDAR)
                .setContentTitle(getString(R.string.cargRespConfIniTitulo))
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setColor(getColor(R.color.azul04))
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setLargeIcon(bitmap)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.cargRespConfIniMensaje)));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand(...) - Proceso del servicio iniciado.");
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
        return mBinder;
    }

    public void setICargarRespaldo (ICargarRespaldo iCargarRespaldo) {
        this.iCargarRespaldo = iCargarRespaldo;
    }

    public boolean isRunning() {
        return running;
    }

    private void ocurrioUnError(String mensaje) {
        running = false;
        stopForeground(false);

        if (iCargarRespaldo != null) {
            iCargarRespaldo.ocurrioError(mensaje);
        } else {
            stopSelf();
        }

        notification.setStyle(new NotificationCompat.BigTextStyle().bigText(mensaje));
        notification.setProgress(0, 0, false);
        notification.setOnlyAlertOnce(false);
        notificationManager.notify(2, notification.build());
    }

    private void actualizarNotificacion(String mensaje, String titulo) {
        notification.setContentTitle(titulo);
        notification.setOnlyAlertOnce(true);
        actualizarNotificacion(0, mensaje);
    }

    private void actualizarNotificacion(int porcentaje, String mensaje) {
        notification.setStyle(new NotificationCompat.BigTextStyle().bigText(mensaje));
        notification.setProgress(100, porcentaje, false);
        notificationManager.notify(2, notification.build());
    }

    public void cargarRespaldo(String contrRespaldo, String contrNueva) {
        if (!running) {
            running = true;

            startForeground(2, notification.build());

            this.contrRespaldo = contrRespaldo;
            this.contrNueva = contrNueva;

            CargarRespaldoRunnable runnable = new CargarRespaldoRunnable(this);
            new Thread(runnable).start();

        } else {
            Log.i(TAG, "onStartCommand(...) - Intento de iniciar el servicio ignorado, éste ya se encuentra en ejecución.");
        }
    }

    class CargarRespaldoRunnable implements Runnable {
        Context context;

        public CargarRespaldoRunnable(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            if (iCargarRespaldo != null) iCargarRespaldo.cargarIniciada();

            Log.i(TAG, "CargarRespaldoRunnable.run() - Iniciando servicio de carga de respaldo...");
            actualizarNotificacion(getString(R.string.cargRespEtapInic), getString(R.string.cargRespServTitulo));

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, "CargarRespaldoRunnable.run() - Error al dormir por un segundo.", e);
            }

            actualizarNotificacion(33, getString(R.string.cargRespEtapBusc));

            GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context);
            if (googleSignInAccount == null) {
                Log.e(TAG, "CargarRespaldoRunnable.run() - No se pudo obtener última cuenta de google iniciada.");
                ocurrioUnError(getString(R.string.cargRespEtapInicError));
            } else {
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(DriveScopes.DRIVE_APPDATA));
                credential.setSelectedAccount(googleSignInAccount.getAccount());
                Drive gooleDriveService = new Drive.Builder(
                        new NetHttpTransport(),
                        new GsonFactory(),
                        credential
                ).setApplicationName("Banco de Contraseñas").build();
                DriveServiceHelper driveServiceHelper = new DriveServiceHelper(gooleDriveService);
                Log.i(TAG, String.format("CargarRespaldoRunnable.run() - Definiendo cuenta %s para uso de Google Drive.", googleSignInAccount.getEmail()));

                Log.i(TAG, "driveServiceHelper.queryFiles() - Obteniendo listado de respaldos existentes...");
                driveServiceHelper.queryFiles()
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "driveServiceHelper.queryFiles() - Error al obtener listado de respaldos existentes.", e);
                            ocurrioUnError(getString(R.string.cargRespEtapBuscError));
                        })
                        .addOnSuccessListener(fileList -> {
                            actualizarNotificacion(66, getString(R.string.cargRespEtapDesc));

                            DateTime fechaMasNuevo = null;
                            String idMasNuevo = null;
                            for (File file : fileList.getFiles()) {
                                Log.i(TAG, String.format("driveServiceHelper.queryFiles() - Leyendo metadata del archivo %s con fecha de creación %s.", file.getId(), file.getCreatedTime()));
                                if (fechaMasNuevo == null || fechaMasNuevo.getValue() < file.getCreatedTime().getValue()) {
                                    fechaMasNuevo = file.getCreatedTime();
                                    idMasNuevo = file.getId();
                                }
                            }
                            Log.i(TAG, String.format("driveServiceHelper.queryFiles() - Se determina que último respaldo es el archivo %s con fecha de creación %s.", idMasNuevo, fechaMasNuevo));

                            if (idMasNuevo == null) {
                                Log.e(TAG, "driveServiceHelper.queryFiles() - No se encontró un respaldo para cargar en Google Drive.");
                                ocurrioUnError(getString(R.string.cargRespErrorSinResp));
                            } else {
                                Log.i(TAG, "driveServiceHelper.downloadFile(...) - Se inicia proceso de descarga del respaldo...");
                                DBOpenHelper.getInstance(getApplicationContext(), NombreBD.BANCO_CONTRASENNAS_RESPALDO).cerrarConexiones();
                                DBOpenHelper.getInstance(getApplicationContext(), NombreBD.BANCO_CONTRASENNAS_RESPALDO);
                                java.io.File dbFileRespaldo = getApplicationContext().getDatabasePath(NombreBD.BANCO_CONTRASENNAS_RESPALDO.toString());
                                driveServiceHelper.downloadFile(dbFileRespaldo, idMasNuevo)
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "driveServiceHelper.downloadFile(...) - Ocurrió un error al descargar respaldo de Google Drive.", e);
                                            ocurrioUnError(getString(R.string.cargRespEtapDescError));
                                        })
                                        .addOnSuccessListener(aVoid -> {
                                            Log.i(TAG, "driveServiceHelper.downloadFile(...) - Se valida que llave maestra ingresada para el respaldo sea correcta.");
                                            ParametroDAO parametroDAORespaldo = new ParametroDAO(getApplicationContext(), true);
                                            Parametro parSaltHash = parametroDAORespaldo.seleccionarUno(NombreParametro.SAL_HASH);
                                            Parametro parResultHash = parametroDAORespaldo.seleccionarUno(NombreParametro.RESULTADO_HASH);
                                            String hash = Cifrador.genHashedPass(contrRespaldo, parSaltHash.getValor())[1];
                                            if (!parResultHash.getValor().equals(hash)) {
                                                DBOpenHelper.getInstance(getApplicationContext(), NombreBD.BANCO_CONTRASENNAS_RESPALDO).cerrarConexiones();
                                                DBOpenHelper.deleteBackup(getApplicationContext());

                                                Log.e(TAG, "driveServiceHelper.downloadFile(...) - La llave maestra del respaldo no es correcta.");
                                                ocurrioUnError(getString(R.string.cargRespErrorContInc));
                                            } else {
                                                try {
                                                    Parametro parSaltEncr = parametroDAORespaldo.seleccionarUno(NombreParametro.SAL_ENCRIPTACION);
                                                    String antiguaLlaveEncr = Cifrador.genHashedPass(contrRespaldo, parSaltEncr.getValor())[1];

                                                    Log.i(TAG, "driveServiceHelper.downloadFile(...) - Se copian datos de la base de datos de respaldo a la original.");
                                                    DBOpenHelper.getInstance(getApplicationContext(), NombreBD.BANCO_CONTRASENNAS).cerrarConexiones();
                                                    DBOpenHelper.deleteOriginal(getApplicationContext());
                                                    DBOpenHelper.getInstance(getApplicationContext(), NombreBD.BANCO_CONTRASENNAS);

                                                    CategoriaDAO.cargarRespaldo(getApplicationContext(), NombreBD.BANCO_CONTRASENNAS_RESPALDO, NombreBD.BANCO_CONTRASENNAS);
                                                    CuentaDAO.cargarRespaldo(getApplicationContext(), NombreBD.BANCO_CONTRASENNAS_RESPALDO, NombreBD.BANCO_CONTRASENNAS);
                                                    ParametroDAO.cargarRespaldo(getApplicationContext(), NombreBD.BANCO_CONTRASENNAS_RESPALDO, NombreBD.BANCO_CONTRASENNAS);
                                                    CategoriaCuentaDAO.cargarRespaldo(getApplicationContext(), NombreBD.BANCO_CONTRASENNAS_RESPALDO, NombreBD.BANCO_CONTRASENNAS);
                                                    ContrasennaDAO.cargarRespaldo(getApplicationContext(), NombreBD.BANCO_CONTRASENNAS_RESPALDO, NombreBD.BANCO_CONTRASENNAS);

                                                    DBOpenHelper.getInstance(getApplicationContext(), NombreBD.BANCO_CONTRASENNAS_RESPALDO).cerrarConexiones();
                                                    DBOpenHelper.deleteBackup(getApplicationContext());

                                                    Log.i(TAG, "driveServiceHelper.downloadFile(...) - Se crea nueva llave de encriptación y hash correspondiente, se reencriptan las contraseñas, y se graban los nuevos parámetros.");
                                                    String[] saltYHash = Cifrador.genHashedPass(contrNueva, null);
                                                    String saltEncr = Cifrador.genSalt();
                                                    String llaveEncrNueva = Cifrador.genHashedPass(contrNueva, saltEncr)[1];

                                                    ContrasennaDAO contrasennaDAO = new ContrasennaDAO(getApplicationContext());
                                                    for (Contrasenna contrasenna : contrasennaDAO.seleccionarTodas()) {
                                                        String valorDesencriptado = Cifrador.desencriptar(contrasenna.getValor(), antiguaLlaveEncr);
                                                        String valorEncriptado = Cifrador.encriptar(valorDesencriptado, llaveEncrNueva);
                                                        contrasenna.setValor(valorEncriptado);
                                                        if (valorDesencriptado.length() == 0 || valorEncriptado.length() == 0 || contrasennaDAO.actualizarUna(contrasenna) != 1) {
                                                            throw new ExcepcionBancoContrasennas(null, getString(R.string.cargRespErrorReencrContr, contrasenna.getNombreCuenta()));
                                                        }
                                                    }

                                                    parSaltHash = new Parametro(NombreParametro.SAL_HASH, saltYHash[0], null);
                                                    parResultHash = new Parametro(NombreParametro.RESULTADO_HASH, saltYHash[1], null);
                                                    parSaltEncr = new Parametro(NombreParametro.SAL_ENCRIPTACION, saltEncr, null);
                                                    ParametroDAO parametroDAO = new ParametroDAO(getApplicationContext());
                                                    if (parametroDAO.actualizarUna(parSaltHash) != 1 || parametroDAO.actualizarUna(parResultHash) != 1 || parametroDAO.actualizarUna(parSaltEncr) != 1) {
                                                        throw new ExcepcionBancoContrasennas(null, getString(R.string.cargRespErrorCrearContr));
                                                    } else {
                                                        Log.i(TAG, "driveServiceHelper.downloadFile(...) - Respaldo SQLite cargado exitosamente!");
                                                        actualizarNotificacion(100, getString(R.string.cargRespExitoMensaje));

                                                        try {
                                                            Thread.sleep(1000);
                                                        } catch (InterruptedException e) {
                                                            Log.e(TAG, "driveServiceHelper.downloadFile(...) - Error al dormir por un segundo.", e);
                                                        }

                                                        running = false;
                                                        stopForeground(false);

                                                        if (iCargarRespaldo != null) {
                                                            iCargarRespaldo.cargaTerminada();
                                                        } else {
                                                            stopSelf();
                                                        }

                                                        notification.setContentTitle(getString(R.string.cargRespExitoTitulo));
                                                        notification.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.cargRespExitoMensaje)));
                                                        notification.setProgress(0, 0, false);
                                                        notification.setOnlyAlertOnce(false);
                                                        notificationManager.notify(2, notification.build());
                                                    }
                                                } catch (ExcepcionBancoContrasennas e) {
                                                    Log.e(TAG, "driveServiceHelper.downloadFile(...) - " + e.getMensaje());

                                                    DBOpenHelper.getInstance(getApplicationContext(), NombreBD.BANCO_CONTRASENNAS).cerrarConexiones();
                                                    DBOpenHelper.deleteOriginal(getApplicationContext());

                                                    ocurrioUnError(e.getMensaje());
                                                }
                                            }
                                        });
                            }
                        });
            }
        }
    }

    public class MiBinder extends Binder {
        public CargarRespaldoService getService() {
            return CargarRespaldoService.this;
        }
    }

    public interface ICargarRespaldo {
        void cargarIniciada();
        void cargaTerminada();
        void ocurrioError(String mensajeError);
    }
}
