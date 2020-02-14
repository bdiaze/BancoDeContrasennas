package cl.theroot.passbank.fragmento;

import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.core.content.res.ResourcesCompat;

import cl.theroot.passbank.ActividadPrincipal;
import cl.theroot.passbank.Cifrador;
import cl.theroot.passbank.CustomFragment;
import cl.theroot.passbank.CustomToast;
import cl.theroot.passbank.ExcepcionBancoContrasennas;
import cl.theroot.passbank.R;
import cl.theroot.passbank.datos.ContrasennaDAO;
import cl.theroot.passbank.datos.ParametroDAO;
import cl.theroot.passbank.datos.nombres.NombreParametro;
import cl.theroot.passbank.dominio.Contrasenna;
import cl.theroot.passbank.dominio.Parametro;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragCambioLlaveMaestra extends CustomFragment {
    //private static final String TAG = "BdC-FragCambioLlaveMaestra";
    private EditText ET_oldPassword;
    private EditText ET_newPassword;
    private EditText ET_newRepPassword;
    private ParametroDAO parametroDAO;
    private boolean habilitarCambios = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragmento_cambio_llave_maestra, container, false);
        ET_oldPassword = view.findViewById(R.id.ET_oldPassword);
        ET_newPassword = view.findViewById(R.id.ET_newPassword);
        ET_newRepPassword = view.findViewById(R.id.ET_newRepPassword);
        ET_oldPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                habilitarCambios();
            }
        });
        ET_newPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                habilitarCambios();
            }
        });
        ET_newRepPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                habilitarCambios();
            }
        });
        parametroDAO = new ParametroDAO(getActivity().getApplicationContext());
        return view;
    }

    //Creación del submenú del fragmento
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.sub_menu_cambio_llave_maestra, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        menu.findItem(R.id.sub_menu_cambio_llave_maestra_guardar).setEnabled(habilitarCambios);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (!((ActividadPrincipal) getActivity()).isSesionIniciada()) {
            return false;
        }
        switch(menuItem.getItemId()) {
            case R.id.sub_menu_cambio_llave_maestra_volver:
                getActivity().onBackPressed();
                return true;
            case R.id.sub_menu_cambio_llave_maestra_guardar:
                try {
                    //Procesar cambio de contraseña
                    String contVieja = ET_oldPassword.getText().toString();
                    String contNueva = ET_newPassword.getText().toString();
                    String contNuevaConf = ET_newRepPassword.getText().toString();

                    //Validar contraseña vieja
                    if (contVieja.isEmpty()) {
                        throw new ExcepcionBancoContrasennas("Error - Llave Maestra Requerida", "Se requiere el ingreso de su Llave Maestra Actual para realizar el cambio");
                    }
                    Parametro parSalt = parametroDAO.seleccionarUno(NombreParametro.SAL_HASH.toString());
                    Parametro parHash = parametroDAO.seleccionarUno(NombreParametro.RESULTADO_HASH.toString());
                    String[] saltYHashObt = Cifrador.genHashedPass(contVieja, parSalt.getValor());
                    if (!saltYHashObt[1].equals(parHash.getValor())) {
                        throw new ExcepcionBancoContrasennas("Error - Llave Maestra Incorrecta", "La Llave Maestra Actual ingresada no es correcta, favor intentar de nuevo");
                    }

                    //Validar contraseña nueva
                    if (contNueva.isEmpty()) {
                        throw new ExcepcionBancoContrasennas("Error - Campo Vacío", "Para realizar el cambio, se requiere el ingreso de su Nueva Llave Maestra");
                    }
                    if (contNueva.length() < Cifrador.LARGO_MINIMO_LLAVE_MAESTRA) {
                        throw new ExcepcionBancoContrasennas("Error - Llave Muy Corta", "La Nueva Llave Maestra elegida es muy corta, debería tener al menos " + Cifrador.LARGO_MINIMO_LLAVE_MAESTRA + " caracteres");
                    }
                    if (!contNuevaConf.equals(contNueva)) {
                        throw new ExcepcionBancoContrasennas("Error - No Coincidencia", "No coinciden las Nuevas Llaves Maestras ingresadas, favor reingresar los datos");
                    }

                    //Validar que la contraseña nueva sea distinta a la anterior
                    if (contNueva.equals(contVieja)) {
                        throw new ExcepcionBancoContrasennas("Error - Llave Ya Utilizada", "La Nueva Llave Maestra debe ser distinta a la Llave Maestra Actual");
                    }

                    //Crear Hash y LlaveEncriptacion para la nueva llave maestra
                    String[] saltYHash = Cifrador.genHashedPass(contNueva, null);
                    String[] saltYHashEncr = Cifrador.genHashedPass(contNueva, null);
                    //Reencriptar, configurar base de datos, y configurar ActividadPrincipal
                    reencriptarContrasennas(actividadPrincipal().getLlaveEncrip(), saltYHashEncr[1]);
                    parSalt = new Parametro(NombreParametro.SAL_HASH.toString(), saltYHash[0], null);
                    parHash = new Parametro(NombreParametro.RESULTADO_HASH.toString(), saltYHash[1], null);
                    Parametro parSaltEncr = new Parametro(NombreParametro.SAL_ENCRIPTACION.toString(), saltYHashEncr[0], null);
                    if (parametroDAO.actualizarUna(parSalt) > 0) {
                        if (parametroDAO.actualizarUna(parHash) > 0) {
                            if (parametroDAO.actualizarUna(parSaltEncr) > 0) {
                                actividadPrincipal().setLlaveEncrip(saltYHashEncr[1]);
                                CustomToast.Build(getActivity().getApplicationContext(), "Su Llave Maestra fue actualizada exitosamente");
                                ((ActividadPrincipal) getActivity()).cambiarFragmento(new FragConfiguracion());
                            }
                        }
                    }
                } catch(ExcepcionBancoContrasennas ex) {
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                    alertDialog.setTitle(ex.getTitulo());
                    alertDialog.setMessage(ex.getMensaje());
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", (dialog, which) -> dialog.dismiss());
                    alertDialog.show();
                    int titleDividerId = getResources().getIdentifier("titleDivider", "id", "android");
                    View titleDivider = alertDialog.findViewById(titleDividerId);
                    if (titleDivider != null) {
                        titleDivider.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.letraAtenuada, null));
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void reencriptarContrasennas(String llaveEncrVieja, String llaveEncrNueva) {
        ContrasennaDAO contrasennaDAO = new ContrasennaDAO(getActivity().getApplicationContext());
        for (Contrasenna contrasenna : contrasennaDAO.seleccionarTodas()) {
            String valorDesencriptado = Cifrador.desencriptar(contrasenna.getValor(), llaveEncrVieja);
            String valorEncriptado = Cifrador.encriptar(valorDesencriptado, llaveEncrNueva);
            contrasenna.setValor(valorEncriptado);
            contrasennaDAO.actualizarUna(contrasenna);
        }
    }

    private void habilitarCambios() {
        habilitarCambios = !ET_oldPassword.getText().toString().isEmpty();
        if (ET_newPassword.getText().toString().isEmpty()) {
            habilitarCambios = false;
        }
        if (ET_newRepPassword.getText().toString().isEmpty()) {
            habilitarCambios = false;
        }
        getActivity().invalidateOptionsMenu();
    }
}
