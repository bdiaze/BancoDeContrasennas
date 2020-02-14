package cl.theroot.passbank.fragmento;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cl.theroot.passbank.CustomFragment;
import cl.theroot.passbank.CustomToast;
import cl.theroot.passbank.DriveServiceHelper;
import cl.theroot.passbank.R;
import cl.theroot.passbank.datos.DBOpenHelper;
import cl.theroot.passbank.datos.nombres.NombreBD;

import static android.app.Activity.RESULT_OK;

public class FragExportar extends CustomFragment {
    private static final String TAG = "BdC-FragExportar";
    private static final int REQUEST_CODE_SIGN_IN = 1;

    @BindView(R.id.ET_cuenta_seleccionada)
    EditText ET_cuentaSeleccionada;
    @BindView(R.id.TV_instruccion)
    TextView TV_instrucciones;
    @BindView(R.id.IV_vaciar_usuario)
    ImageView IV_vaciarCuenta;

    private ProgressDialog mensajeProgreso;

    private DriveServiceHelper mDriveServiceHelper;
    private GoogleSignInClient mGoogleSignClient;

    public FragExportar() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragmento_exportar, container, false);
        ButterKnife.bind(this, view);

        ET_cuentaSeleccionada.setOnClickListener(v -> {
            seleccionarCuenta();
        });
        ET_cuentaSeleccionada.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                getActivity().invalidateOptionsMenu();
            }
        });

        TV_instrucciones.setVisibility(View.INVISIBLE);
        IV_vaciarCuenta.setOnClickListener(v -> {
            ET_cuentaSeleccionada.setText("");
            mDriveServiceHelper = null;
        });

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_APPDATA))
                .build();
        mGoogleSignClient = GoogleSignIn.getClient(getContext(), signInOptions);

        mensajeProgreso = new ProgressDialog(getActivity());
        mensajeProgreso.setTitle("Creando Respaldo");
        mensajeProgreso.setMessage("Su respaldo está siendo creado, favor esperar...");
        mensajeProgreso.setCancelable(false);

        getActivity().invalidateOptionsMenu();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sub_menu_exportar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        menu.findItem(R.id.sub_menu_exportar_respaldar).setEnabled(!ET_cuentaSeleccionada.getText().toString().isEmpty());
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sub_menu_exportar_back:
                getActivity().onBackPressed();
                return true;
            case R.id.sub_menu_exportar_respaldar:
                String email = ET_cuentaSeleccionada.getText().toString();
                if (email.isEmpty()) {
                    CustomToast.Build(getActivity().getApplicationContext(), "¡Acción Cancelada!\nDebe seleccionar una cuenta antes de realizar el respaldo.");
                    return true;
                }

                //Deshabilitar rotación de pantalla...
                if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }

                //parametroDAO.actualizarUna(new Parametro(NombreParametro.CUENTA_GOOGLE.toString(), email, null));
                mensajeProgreso.show();

                // Se cierran conexiones abiertas...
                DBOpenHelper.getInstance(getActivity().getApplicationContext(), NombreBD.BANCO_CONTRASENNAS).cerrarConexiones();

                final java.io.File dbFile = getActivity().getApplicationContext().getDatabasePath(NombreBD.BANCO_CONTRASENNAS.toString());
                //Log.i(TAG, "Tamaño del archivo a respaldar: " + dbFile.length());
                if (dbFile != null && mGoogleSignClient != null) {
                    //Log.i(TAG, "Obteniendo listado de respaldos a eliminar...");
                    mDriveServiceHelper.queryFiles()
                            .addOnSuccessListener(fileList -> {
                                //Log.i(TAG, "Listado de respaldos a eliminar obtenido");
                                List<String> idsEliminar = new ArrayList<>();
                                for (File file : fileList.getFiles()) {
                                    idsEliminar.add(file.getId());
                                }

                                //Log.i(TAG, "Eliminando respaldos anteriores...");
                                mDriveServiceHelper.deleteFiles(idsEliminar)
                                        .addOnSuccessListener(salida -> {
                                            //Log.i(TAG, "Respaldos anteriores eliminados exitosamente");

                                            //Log.i(TAG, "Subiendo nuevo respaldo SQLite...");
                                            mDriveServiceHelper.uploadFile(dbFile, "application/x-sqlite3")
                                                    .addOnSuccessListener(file -> {
                                                        //Log.i(TAG, "Respaldo SQLite subido exitosamente!");
                                                        mensajeProgreso.dismiss();
                                                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                                                        CustomToast.Build(this, R.string.respCreadoExitosamente);
                                                    })
                                                    .addOnFailureListener(exception -> {
                                                        Log.e(TAG, "Error al subir nuevo respaldo", exception);
                                                        mensajeProgreso.dismiss();
                                                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                                                        CustomToast.Build(this, R.string.creacionRespFallida);
                                                    });
                                        })
                                        .addOnFailureListener(exception -> {
                                            Log.e(TAG, "Error al eliminar los respaldos antiguos", exception);
                                            mensajeProgreso.dismiss();
                                            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                                            CustomToast.Build(this, R.string.creacionRespFallida);
                                        });
                            })
                            .addOnFailureListener(exception -> {
                                Log.e(TAG, "Error al obtener listado de respaldos a eliminar", exception);
                                mensajeProgreso.dismiss();
                                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                                CustomToast.Build(this, R.string.creacionRespFallida);
                            });
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void seleccionarCuenta() {
        mGoogleSignClient.signOut()
                .addOnCompleteListener(getActivity(), task -> {
                    ET_cuentaSeleccionada.setText("");
                    mDriveServiceHelper = null;
                    startActivityForResult(mGoogleSignClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
                });
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent resultData) {
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            if (resultCode == RESULT_OK && resultData != null) {
                GoogleSignIn.getSignedInAccountFromIntent(resultData)
                        .addOnSuccessListener(googleSignInAccount -> {
                            ET_cuentaSeleccionada.setText(googleSignInAccount.getEmail());
                            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(getContext(), Collections.singleton(DriveScopes.DRIVE_APPDATA));
                            credential.setSelectedAccount(googleSignInAccount.getAccount());
                            Drive googleDriveService = new Drive.Builder(
                                    new NetHttpTransport(),
                                    new GsonFactory(),
                                    credential
                            ).build();
                            mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
                        })
                        .addOnFailureListener(exception -> CustomToast.Build(this, R.string.inicioSesionDriveFallido));
            } else {
                CustomToast.Build(this, R.string.inicioSesionDriveFallido);
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData);
    }
}
