package cl.theroot.passbank.fragmento;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes;

import butterknife.BindView;
import butterknife.ButterKnife;
import cl.theroot.passbank.CustomFragment;
import cl.theroot.passbank.CustomToast;
import cl.theroot.passbank.R;
import cl.theroot.passbank.RespaldarService;

import static android.app.Activity.RESULT_OK;

public class FragRespaldar extends CustomFragment {
    private static final String TAG = "BdC-FragExportar";
    private static final int REQUEST_CODE_SIGN_IN = 1;

    @BindView(R.id.ET_cuenta_seleccionada)
    EditText ET_cuentaSeleccionada;
    @BindView(R.id.TV_instruccion)
    TextView TV_instrucciones;
    @BindView(R.id.IV_vaciar_usuario)
    ImageView IV_vaciarCuenta;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragmento_respaldar, container, false);
        ButterKnife.bind(this, view);

        ET_cuentaSeleccionada.setOnClickListener(v -> seleccionarCuenta());
        IV_vaciarCuenta.setOnClickListener(v -> ET_cuentaSeleccionada.setText(null));

        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(getContext());
        if (googleSignInAccount != null) {
            ET_cuentaSeleccionada.setText(googleSignInAccount.getEmail());
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sub_menu_exportar, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
                    CustomToast.Build(getApplicationContext(), getString(R.string.debeSeleccionarCuenta));
                    return true;
                }

                Log.i(TAG, "onOptionsItemSelected(...) - Iniciando servicio respaldar...");
                Intent serviceIntent = new Intent(actividadPrincipal(), RespaldarService.class);
                actividadPrincipal().startForegroundService(serviceIntent);

                CustomToast.Build(getApplicationContext(), R.string.respaldoIniciado);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void seleccionarCuenta() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_APPDATA))
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getContext(), signInOptions);

        googleSignInClient.signOut()
                .addOnCompleteListener(getActivity(), task -> {
                    ET_cuentaSeleccionada.setText(null);
                    startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
                });
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent resultData) {
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            if (resultCode == RESULT_OK && resultData != null) {
                GoogleSignIn.getSignedInAccountFromIntent(resultData)
                        .addOnSuccessListener(googleSignInAccount -> {
                            ET_cuentaSeleccionada.setText(googleSignInAccount.getEmail());
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "GoogleSignIn.getSignedInAccountFromIntent(...) - Error al obtener cuenta de google.", e);
                            CustomToast.Build(this, R.string.inicioSesionDriveFallido);
                        });
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData);
    }
}
