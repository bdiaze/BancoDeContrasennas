package cl.theroot.passbank.fragmento;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
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

public class FragCambioLlaveMaestra extends CustomFragment implements AlertDialogSiNoOk.iProcesarBotonSiNoOk {
    private static final String TAG = "BdC-FragCambioLlaveMaestra";

    private ParametroDAO parametroDAO;

    @BindView(R.id.ET_oldPassword)
    EditText ET_oldPassword;
    @BindView(R.id.ET_newPassword)
    EditText ET_newPassword;
    @BindView(R.id.ET_newRepPassword)
    EditText ET_newRepPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragmento_cambio_llave_maestra, container, false);
        ButterKnife.bind(this, view);

        parametroDAO = new ParametroDAO(getApplicationContext());

        return view;
    }

    //Creación del submenú del fragmento
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.sub_menu_cambio_llave_maestra, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
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
                        throw new ExcepcionBancoContrasennas(getString(R.string.errorLlaveReqTitulo), getString(R.string.errorLlaveReqMensaje));
                    }
                    Parametro parSalt = parametroDAO.seleccionarUno(NombreParametro.SAL_HASH.toString());
                    Parametro parHash = parametroDAO.seleccionarUno(NombreParametro.RESULTADO_HASH.toString());
                    String[] saltYHashObt = Cifrador.genHashedPass(contVieja, parSalt.getValor());
                    if (!saltYHashObt[1].equals(parHash.getValor())) {
                        throw new ExcepcionBancoContrasennas(getString(R.string.errorLlaveIncTitulo), getString(R.string.errorLlaveIncMensaje));
                    }

                    //Validar contraseña nueva
                    if (contNueva.isEmpty()) {
                        throw new ExcepcionBancoContrasennas(getString(R.string.errorLlaveVaciaTitulo), getString(R.string.errorLlaveVaciaMensaje));
                    }
                    if (contNueva.length() < Cifrador.LARGO_MINIMO_LLAVE_MAESTRA) {
                        throw new ExcepcionBancoContrasennas(getString(R.string.errorLlaveCortaTitulo),  getString(R.string.errorLlaveCortaMensaje, Cifrador.LARGO_MINIMO_LLAVE_MAESTRA));
                    }
                    if (!contNuevaConf.equals(contNueva)) {
                        throw new ExcepcionBancoContrasennas(getString(R.string.errorLlaveNoCoincTitulo), getString(R.string.errorLlaveNoCoincMensaje));
                    }

                    //Validar que la contraseña nueva sea distinta a la anterior
                    if (contNueva.equals(contVieja)) {
                        throw new ExcepcionBancoContrasennas(getString(R.string.errorLlaveUtilizTitulo), getString(R.string.errorLlaveUtilizMensaje));
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
                                CustomToast.Build(getApplicationContext(), getString(R.string.llaveMaestraActualizada));
                                actividadPrincipal().cambiarFragmento(new FragConfiguracion());
                            }
                        }
                    }
                } catch(ExcepcionBancoContrasennas ex) {
                    ex.alertDialog(this);
                }
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void reencriptarContrasennas(String llaveEncrVieja, String llaveEncrNueva) {
        ContrasennaDAO contrasennaDAO = new ContrasennaDAO(getApplicationContext());
        for (Contrasenna contrasenna : contrasennaDAO.seleccionarTodas()) {
            String valorDesencriptado = Cifrador.desencriptar(contrasenna.getValor(), llaveEncrVieja);
            String valorEncriptado = Cifrador.encriptar(valorDesencriptado, llaveEncrNueva);
            contrasenna.setValor(valorEncriptado);
            contrasennaDAO.actualizarUna(contrasenna);
        }
    }

    @Override
    public void procesarBotonSiNoOk(int boton) {

    }
}
