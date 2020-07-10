package cl.theroot.passbank.fragmento;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes;

import butterknife.BindView;
import butterknife.ButterKnife;
import cl.theroot.passbank.CargarRespaldoService;
import cl.theroot.passbank.Cifrador;
import cl.theroot.passbank.CustomFragment;
import cl.theroot.passbank.ExcepcionBancoContrasennas;
import cl.theroot.passbank.R;
import cl.theroot.passbank.datos.DBOpenHelper;
import cl.theroot.passbank.datos.ParametroDAO;
import cl.theroot.passbank.datos.nombres.NombreBD;
import cl.theroot.passbank.datos.nombres.NombreParametro;
import cl.theroot.passbank.dominio.Parametro;

import static android.app.Activity.RESULT_OK;

public class FragCrearLlaveMaestra extends CustomFragment implements View.OnClickListener, AlertDialogSiNoOk.iProcesarBotonSiNoOk, CargarRespaldoService.ICargarRespaldo {
    private static final String TAG = "BdC-FragCrearLlaveMa...";
    private static final int REQUEST_CODE_SIGN_IN = 1;

    @BindView(R.id.ET_newPassword)
    EditText ET_newPassword;
    @BindView(R.id.ET_newRepPassword)
    EditText ET_newRepPassword;
    @BindView(R.id.ET_cuenta_seleccionada)
    EditText ET_cuentaSeleccionada;
    @BindView(R.id.ET_respaldoLlaveMaestra)
    EditText ET_llaveMaestraRespaldo;
    @BindView(R.id.B_crear_llave_maestra)
    Button B_crear_llave_maestra;
    @BindView(R.id.IV_vaciar_usuario)
    ImageView IV_vaciarCuenta;
    @BindView(R.id.PB_creandoLlave)
    ProgressBar PB_creandoLlave;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmento_crear_llave_maestra, container, false);
        ButterKnife.bind(this, view);

        B_crear_llave_maestra.setOnClickListener(this);
        ET_cuentaSeleccionada.setOnClickListener(v -> seleccionarCuenta());
        IV_vaciarCuenta.setOnClickListener(v -> ET_cuentaSeleccionada.setText(""));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ParametroDAO parametroDAO = new ParametroDAO(getApplicationContext());
        Parametro parametro = parametroDAO.seleccionarUno(NombreParametro.RESULTADO_HASH.toString());
        if (parametro != null && parametro.getValor().length() > 0) {
            actividadPrincipal().cambiarFragmento(new FragInicioSesion());
        } else {
            Intent serviceIntent = new Intent(actividadPrincipal(), CargarRespaldoService.class);
            actividadPrincipal().startService(serviceIntent);
            actividadPrincipal().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isBound) {
            if (!respaldoService.isRunning()) {
                respaldoService.stopSelf();
            }

            respaldoService.setICargarRespaldo(null);
            actividadPrincipal().unbindService(connection);
            isBound = false;
        }
    }

    @Override
    public void onClick(View view) {
        try {
            String newPass = ET_newPassword.getText().toString();
            String newRepPass = ET_newRepPassword.getText().toString();
            String llaveMaestraRespaldo = ET_llaveMaestraRespaldo.getText().toString();
            if (newPass.isEmpty()) {
                ET_newPassword.requestFocus();
                throw new ExcepcionBancoContrasennas("Error - Campo Vacío", "La aplicación requiere de una Llave Maestra, favor de rellenar el campo de Nueva Llave Maestra.");
            }
            if (newPass.length() < Cifrador.LARGO_MINIMO_LLAVE_MAESTRA) {
                ET_newPassword.requestFocus();
                ET_newPassword.setSelection(0, ET_newPassword.getText().length());
                throw new ExcepcionBancoContrasennas("Error - Llave Maestra Muy Corta", "La Llave Maestra elegida es muy corta, debería tener al menos " + Cifrador.LARGO_MINIMO_LLAVE_MAESTRA + " caracteres.");
            }
            if (!newPass.equals(newRepPass)) {
                ET_newRepPassword.requestFocus();
                ET_newRepPassword.setSelection(0, ET_newRepPassword.getText().length());
                throw new ExcepcionBancoContrasennas("Error - No Coincidencia", "No coinciden las Llaves Maestras ingresadas, favor reingresar los datos.");
            }

            String email = ET_cuentaSeleccionada.getText().toString();
            if (!email.isEmpty()) {
                Log.i(TAG, "B_crear_llave_maestra.onClick(...) - Iniciando importación de respaldo en Google Drive.");
                if (llaveMaestraRespaldo.isEmpty()) {
                    ET_llaveMaestraRespaldo.requestFocus();
                    throw new ExcepcionBancoContrasennas("Error - Campo Vacío", "Para cargar su respaldo, debe ingresar la Llave Maestra asociada a dichos datos.");
                }

                Log.i(TAG, "B_crear_llave_maestra.onClick(...) - Iniciando servicio cargar respaldo...");
                respaldoService.cargarRespaldo(llaveMaestraRespaldo, newPass);

                AlertDialogSiNoOk dialogSiNoOk = new AlertDialogSiNoOk();
                dialogSiNoOk.setTitulo(getString(R.string.cargRespServTitulo));
                dialogSiNoOk.setMensaje(getString(R.string.cargarRespaldoIniciado));
                dialogSiNoOk.setTargetFragment(FragCrearLlaveMaestra.this, 1);
                if (getFragmentManager() != null) dialogSiNoOk.show(getFragmentManager(), TAG);
            } else {
                //Se crea la llave maestra, y se guarda en la base de datos
                String[] saltYHash = Cifrador.genHashedPass(newPass, null);
                String saltEncr = Cifrador.genSalt();

                Parametro parSalt = new Parametro(NombreParametro.SAL_HASH.toString(), saltYHash[0], null);
                Parametro parHash = new Parametro(NombreParametro.RESULTADO_HASH.toString(), saltYHash[1], null);
                Parametro parSaltEncr = new Parametro(NombreParametro.SAL_ENCRIPTACION.toString(), saltEncr, null);

                ParametroDAO parametroDAO = new ParametroDAO(getApplicationContext());
                if (parametroDAO.actualizarUna(parSalt) != 1 || parametroDAO.actualizarUna(parHash) != 1 || parametroDAO.actualizarUna(parSaltEncr) != 1) {
                    Log.e(TAG, "B_crear_llave_maestra.onClick(...) - No se pudo actualizar la salt del hash, el hash, o la salt de encriptación de la llave maestra.");

                    DBOpenHelper.getInstance(getApplicationContext(), NombreBD.BANCO_CONTRASENNAS).cerrarConexiones();
                    DBOpenHelper.deleteOriginal(getApplicationContext());

                    AlertDialogSiNoOk dialogSiNoOk = new AlertDialogSiNoOk();
                    dialogSiNoOk.setTitulo(getString(R.string.excepcionTituloDefecto));
                    dialogSiNoOk.setMensaje(getString(R.string.cargRespErrorCrearContr));
                    dialogSiNoOk.setTargetFragment(FragCrearLlaveMaestra.this, 1);
                    if (getFragmentManager() != null) dialogSiNoOk.show(getFragmentManager(), TAG);
                } else {
                    Log.i(TAG, "B_crear_llave_maestra.onClick(...) - Llave maestra creada exitosamente.");

                    AlertDialogSiNoOk dialogSiNoOk = new AlertDialogSiNoOk();
                    dialogSiNoOk.setTitulo(getString(R.string.llaveCreadaTitulo));
                    dialogSiNoOk.setMensaje(getString(R.string.llaveCreadaMensaje));
                    dialogSiNoOk.setTargetFragment(FragCrearLlaveMaestra.this, 1);
                    if (getFragmentManager() != null) dialogSiNoOk.show(getFragmentManager(), TAG);

                    actividadPrincipal().cambiarFragmento(new FragInicioSesion());
                }
            }
        } catch (ExcepcionBancoContrasennas ex) {
            Log.i(TAG, String.format("CrearLlaveMaestra.onClick(...) - Error al crear llave maestra. Mensaje: %s", ex.getMensaje()));
            ex.alertDialog(this);
        }
    }

    private void uiProcesando() {
        B_crear_llave_maestra.setText(R.string.creandoLlaveMaestra);
        B_crear_llave_maestra.setEnabled(false);
        PB_creandoLlave.setVisibility(View.VISIBLE);
    }

    private void uiProcesado() {
        B_crear_llave_maestra.setText(R.string.crearLlaveMaestra);
        B_crear_llave_maestra.setEnabled(true);
        PB_creandoLlave.setVisibility(View.GONE);
    }

    private void seleccionarCuenta() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_APPDATA))
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getApplicationContext(), signInOptions);

        googleSignInClient.signOut()
                .addOnCompleteListener(actividadPrincipal(), task -> {
                    ET_cuentaSeleccionada.setText(null);
                    startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
                });
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent resultData) {
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            if (resultCode == RESULT_OK && resultData != null) {
                GoogleSignIn.getSignedInAccountFromIntent(resultData)
                        .addOnSuccessListener(googleAccount -> ET_cuentaSeleccionada.setText(googleAccount.getEmail()))
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "GoogleSignIn.getSignedInAccountFromIntent(...) - Error al obtener cuenta de google.", e);

                            AlertDialogSiNoOk dialogSiNoOk = new AlertDialogSiNoOk();
                            dialogSiNoOk.setTitulo(getString(R.string.excepcionTituloDefecto));
                            dialogSiNoOk.setMensaje(getString(R.string.inicioSesionDriveFallido));
                            dialogSiNoOk.setTargetFragment(FragCrearLlaveMaestra.this, 1);
                            if (getFragmentManager() != null) dialogSiNoOk.show(getFragmentManager(), TAG);
                        });
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData);
    }

    @Override
    public void procesarBotonSiNoOk(int boton) {

    }

    private CargarRespaldoService respaldoService;
    private boolean isBound = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isBound = true;
            CargarRespaldoService.MiBinder binder = (CargarRespaldoService.MiBinder) service;
            respaldoService = binder.getService();
            respaldoService.setICargarRespaldo(FragCrearLlaveMaestra.this);
            if (respaldoService.isRunning()) {
                FragCrearLlaveMaestra.this.uiProcesando();
            } else {
                FragCrearLlaveMaestra.this.uiProcesado();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    public void cargarIniciada() {
        actividadPrincipal().runOnUiThread(this::uiProcesando);
    }

    @Override
    public void cargaTerminada() {
        actividadPrincipal().runOnUiThread(actividadPrincipal()::userLogOut);
    }

    @Override
    public void ocurrioError(String mensajeError) {
        actividadPrincipal().runOnUiThread(() -> {
            uiProcesado();
            AlertDialogSiNoOk dialogSiNoOk = new AlertDialogSiNoOk();
            dialogSiNoOk.setTitulo(getString(R.string.excepcionTituloDefecto));
            dialogSiNoOk.setMensaje(mensajeError);
            dialogSiNoOk.setTargetFragment(FragCrearLlaveMaestra.this, 1);
            if (getFragmentManager() != null) dialogSiNoOk.show(getFragmentManager(), TAG);
        });
    }
}
