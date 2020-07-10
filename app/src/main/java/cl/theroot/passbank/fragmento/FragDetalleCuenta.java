package cl.theroot.passbank.fragmento;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import cl.theroot.passbank.Cifrador;
import cl.theroot.passbank.CustomFragment;
import cl.theroot.passbank.CustomToast;
import cl.theroot.passbank.PortapapelesReceiver;
import cl.theroot.passbank.R;
import cl.theroot.passbank.datos.ContrasennaDAO;
import cl.theroot.passbank.datos.CuentaDAO;
import cl.theroot.passbank.datos.ParametroDAO;
import cl.theroot.passbank.datos.nombres.ColCuenta;
import cl.theroot.passbank.datos.nombres.NombreParametro;
import cl.theroot.passbank.dominio.Contrasenna;
import cl.theroot.passbank.dominio.Cuenta;
import cl.theroot.passbank.dominio.Parametro;

public class FragDetalleCuenta extends CustomFragment implements AlertDialogSiNoOk.iProcesarBotonSiNoOk{
    private static final String TAG = "BdC-FragDetalleCuenta";

    private CuentaDAO cuentaDAO;
    private ParametroDAO parametroDAO;
    private Bundle bundle;

    @BindView(R.id.TV_password)
    TextView TV_password;
    @BindView(R.id.IV_passVisibility)
    ImageView IV_passVisibility;
    @BindView(R.id.TV_name)
    TextView TV_name;
    @BindView(R.id.TV_description)
    TextView TV_description;
    @BindView(R.id.TV_clickCopiar)
    TextView TV_clickCopiar;

    private static final String KEY_INT_INP_TIP = "KEY_INT_INP_TIP";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
        if (savedInstanceState != null) {
            inputType = savedInstanceState.getInt(KEY_INT_INP_TIP);
        }

        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragmento_detalle_cuenta, container, false);
        ButterKnife.bind(this, view);

        ContrasennaDAO contrasennaDAO = new ContrasennaDAO(getApplicationContext());
        cuentaDAO = new CuentaDAO(getApplicationContext());
        parametroDAO = new ParametroDAO(getApplicationContext());
        bundle = this.getArguments();

        if (inputType != (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            cambiarVisualizacion(true);
        } else {
            cambiarVisualizacion(false);
        }

        IV_passVisibility.setOnClickListener(v -> {
            if (TV_password.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                cambiarVisualizacion(true);
            } else {
                cambiarVisualizacion(false);
            }
        });

        TV_name.setText(getResources().getText(R.string.nombreNoDefin));
        TV_description.setText(getResources().getText(R.string.descripNoDefin));
        TV_password.setText(getResources().getText(R.string.contraNoDefin));

        if (bundle != null) {
            String accountName = bundle.getString(ColCuenta.NOMBRE.toString());
            if (accountName != null) {
                Cuenta cuenta = cuentaDAO.seleccionarUna(accountName);
                if (cuenta != null) {
                    TV_name.setText(cuenta.getNombre());
                    TV_description.setText(cuenta.getDescripcion());

                    Contrasenna contrasenna = contrasennaDAO.seleccionarUltimaPorCuenta(cuenta.getNombre());
                    if (contrasenna != null) {
                        TV_password.setText(Cifrador.desencriptar(contrasenna.getValor(), actividadPrincipal().getLlaveEncrip()));
                        TV_password.setOnClickListener((View v) -> {
                            // Si se está mostrando la contraseña, se agrega al clipboard...
                            if (TV_password.getInputType() != (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText(PortapapelesReceiver.LABEL_CLIPBOARD, TV_password.getText());
                                clipboard.setPrimaryClip(clip);
                                Log.i(TAG, "TV_password.setOnClickListener(...) - Se copia contraseña a portapapeles.");

                                CustomToast.Build(this, R.string.contraCopiada);
                                definirLimpiezaPortapapeles();
                            }
                        });
                    }
                }
            }
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(KEY_INT_INP_TIP, TV_password.getInputType());
        super.onSaveInstanceState(outState);
    }

    //Creación del submenú del fragmento
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sub_menu_detalle_cuenta, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //Creación de la funcionalidad del fragmento
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sub_menu_show_account_back:
                getActivity().onBackPressed();
                return true;

            case R.id.sub_menu_show_account_edit:
                if (bundle != null) {
                    FragAgregarEditarCuenta fragAgregarEditarCuenta = new FragAgregarEditarCuenta();
                    fragAgregarEditarCuenta.setArguments(bundle);
                    return actividadPrincipal().cambiarFragmento(fragAgregarEditarCuenta);
                } else {
                    return true;
                }

            case R.id.sub_menu_show_account_delete:
                if (bundle != null) {
                    String name = bundle.getString(ColCuenta.NOMBRE.toString());
                    if (name == null) {
                        Log.e(TAG, "El nombre de la cuenta no vienen en el bundle.");
                        return false;
                    }
                    AlertDialogSiNoOk alertDialogSiNoOk = new AlertDialogSiNoOk();
                    alertDialogSiNoOk.setTipo(AlertDialogSiNoOk.TIPO_SI_NO);
                    alertDialogSiNoOk.setTitulo(getString(R.string.elimCuentaTitulo));
                    alertDialogSiNoOk.setMensaje(getString(R.string.elimCuentaMensaje, name));
                    alertDialogSiNoOk.setTargetFragment(this, 1);
                    alertDialogSiNoOk.show(getFragmentManager(), TAG);
                }
                return true;

            case R.id.sub_menu_show_account_record:
                if (bundle != null) {
                    FragHistorialCuenta fragHistorialCuenta = new FragHistorialCuenta();
                    fragHistorialCuenta.setArguments(bundle);
                    return actividadPrincipal().cambiarFragmento(fragHistorialCuenta);
                } else {
                    return true;
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void cambiarVisualizacion(boolean mostrarContrasenna) {
        if (mostrarContrasenna) {
            IV_passVisibility.setImageResource(R.drawable.baseline_visibility_off_24);
            TV_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            TV_password.setTypeface(null, Typeface.BOLD);
            TV_clickCopiar.setVisibility(View.VISIBLE);
        } else {
            IV_passVisibility.setImageResource(R.drawable.baseline_visibility_24);
            TV_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            TV_password.setTypeface(null, Typeface.BOLD);
            TV_clickCopiar.setVisibility(View.INVISIBLE);
        }
    }

    private void definirLimpiezaPortapapeles() {
        Log.i(TAG, "definirLimpiezaPortapapeles() - Se define tiempo de espera para limpieza.");
        int segundosEsperar = PortapapelesReceiver.TIEMPO_DEFECTO;
        try {
            Parametro parametro = parametroDAO.seleccionarUno(NombreParametro.SEGUNDOS_PORTAPAPELES);
            if (parametro != null) {
                segundosEsperar = Integer.parseInt(parametro.getValor());
            }
        } catch (NumberFormatException ex) {
            Log.e(TAG, "Error al formatear Segundos en Portapapeles", ex);
        }

        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), PortapapelesReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, 0);

        Log.i(TAG, "definirLimpiezaPortapapeles() - Se establece alarma para la ejecución de la limpieza.");
        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + segundosEsperar * 1000, pendingIntent);
    }

    @Override
    public void procesarBotonSiNoOk(int boton) {
        Log.i(TAG, String.format("procesarBotonSiNoOk(...) - boton: %d", boton));
        if (boton == AlertDialogSiNoOk.BOTON_SI) {
            String name = bundle.getString(ColCuenta.NOMBRE.toString());
            if (name != null && cuentaDAO.eliminarUna(name) > 0) {
                actividadPrincipal().cambiarFragmento(new FragCuentas());
                actividadPrincipal().actualizarBundles(Cuenta.class, name, null);
                CustomToast.Build(this, R.string.elimCuentaExitosa);
            } else {
                CustomToast.Build(this, R.string.elimCuentaFallida);
            }
        }
    }
}
