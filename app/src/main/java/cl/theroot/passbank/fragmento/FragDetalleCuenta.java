package cl.theroot.passbank.fragmento;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import cl.theroot.passbank.ActividadPrincipal;
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

public class FragDetalleCuenta extends CustomFragment {
    private static final String TAG = "BdC-FragDetalleCuenta";

    private static final String PASS_INPUT_TYPE = "PassInputType";

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

    private Bundle bundle;

    private CuentaDAO cuentaDAO;
    private ParametroDAO parametroDAO;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragmento_detalle_cuenta, container, false);
        ButterKnife.bind(this, view);

        ContrasennaDAO contrasennaDAO = new ContrasennaDAO(getActivity().getApplicationContext());
        cuentaDAO = new CuentaDAO(getActivity().getApplicationContext());
        parametroDAO = new ParametroDAO(getApplicationContext());

        if (savedInstanceState != null) {
            int inputType = savedInstanceState.getInt(PASS_INPUT_TYPE);
            if (inputType != 0x00000081) {
                cambiarVisualizacion(true);
            } else {
                cambiarVisualizacion(false);
            }
        } else {
            cambiarVisualizacion(false);
        }

        IV_passVisibility.setOnClickListener(v -> {
            if (TV_password.getInputType() == 0x00000081) {
                cambiarVisualizacion(true);
            } else {
                cambiarVisualizacion(false);
            }
        });

        TV_name.setText(getResources().getText(R.string.nombreNoDefin));
        TV_description.setText(getResources().getText(R.string.descripNoDefin));
        TV_password.setText(getResources().getText(R.string.contraNoDefin));

        bundle = this.getArguments();
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
                            if (TV_password.getInputType() != 0x00000081) {
                                ClipboardManager clipboard = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText(PortapapelesReceiver.LABEL_CLIPBOARD, TV_password.getText());
                                clipboard.setPrimaryClip(clip);

                                CustomToast.Build(getActivity(), R.string.contraCopiada);
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
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            savedInstanceState.putInt(PASS_INPUT_TYPE, TV_password.getInputType());
        }

        super.onSaveInstanceState(savedInstanceState);
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
        if (!((ActividadPrincipal) getActivity()).isSesionIniciada()) {
            return false;
        }
        switch (item.getItemId()) {
            case R.id.sub_menu_show_account_back:
                getActivity().onBackPressed();
                return true;
            //return ((ActividadPrincipal) getActivity()).cambiarFragmento(new AdapCuentas());

            case R.id.sub_menu_show_account_edit:
                if (bundle != null) {
                    FragAgregarEditarCuenta fragAgregarEditarCuenta = new FragAgregarEditarCuenta();
                    fragAgregarEditarCuenta.setArguments(bundle);
                    return ((ActividadPrincipal) getActivity()).cambiarFragmento(fragAgregarEditarCuenta);
                } else {
                    return true;
                }

            case R.id.sub_menu_show_account_delete:
                if (bundle != null) {
                    final String name = bundle.getString(ColCuenta.NOMBRE.toString());
                    if (name == null) {
                        Log.e(TAG, "El nombre de la cuenta no vienen en el bundle.");
                        return false;
                    }
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                    alertDialog.setTitle("Eliminación de Cuenta");
                    alertDialog.setMessage("¿Está seguro que desea eliminar la cuenta " + name + "?");
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", (dialog, which) -> dialog.dismiss());
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SÍ", (dialog, which) -> {
                        if (cuentaDAO.eliminarUna(name) > 0) {
                            ((ActividadPrincipal) getActivity()).cambiarFragmento(new FragCuentas());
                            ((ActividadPrincipal) getActivity()).actualizarBundles(Cuenta.class, name, null);
                        }
                        dialog.dismiss();
                    });
                    alertDialog.show();
                }
                return true;

            case R.id.sub_menu_show_account_record:
                if (bundle != null) {
                    FragHistorialCuenta fragHistorialCuenta = new FragHistorialCuenta();
                    fragHistorialCuenta.setArguments(bundle);
                    return ((ActividadPrincipal) getActivity()).cambiarFragmento(fragHistorialCuenta);
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
            TV_password.setInputType(0x00080001);
            TV_clickCopiar.setVisibility(View.VISIBLE);
        } else {
            IV_passVisibility.setImageResource(R.drawable.baseline_visibility_24);
            TV_password.setInputType(0x00000081);
            TV_clickCopiar.setVisibility(View.INVISIBLE);
        }
    }

    private void definirLimpiezaPortapapeles() {
        int segundosEsperar = PortapapelesReceiver.TIEMPO_DEFECTO;
        try {
            Parametro parametro = parametroDAO.seleccionarUno(NombreParametro.SEGUNDOS_PORTAPAPELES);
            if (parametro != null) {
                segundosEsperar = Integer.parseInt(parametro.getValor());
            }
        } catch (NumberFormatException ex) {
            Log.e(TAG, "Error al formatear Segundos en Portapapeles", ex);
        }

        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), PortapapelesReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + segundosEsperar * 1000, pendingIntent);
    }
}
