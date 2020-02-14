package cl.theroot.passbank.fragmento;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cl.theroot.passbank.Cifrador;
import cl.theroot.passbank.CustomFragment;
import cl.theroot.passbank.ExcepcionBancoContrasennas;
import cl.theroot.passbank.R;
import cl.theroot.passbank.datos.PalabraDAO;
import cl.theroot.passbank.datos.ParametroDAO;
import cl.theroot.passbank.datos.nombres.NombreParametro;
import cl.theroot.passbank.dominio.Parametro;

public class FragInicioSesion extends CustomFragment implements  View.OnClickListener {
    //private static final String TAG = "BdC-FragInicioSesion";

    @BindView(R.id.ET_password) EditText ET_contrasenna;
    @BindView(R.id.B_login) Button B_login;

    private ParametroDAO parametroDAO;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmento_inicio_sesion, container, false);
        ButterKnife.bind(this, view);

        B_login.setOnClickListener(this);
        ET_contrasenna.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validarContrasenna();
            }
            return false;
        });

        getActivity().invalidateOptionsMenu();

        parametroDAO = new ParametroDAO(getActivity().getApplicationContext());

        //Tratar de crear la base de datos para el diccionario de palabras
        new PalabraDAO(getActivity().getApplicationContext());
        //Tratar de desfragmentar la base de datos de la aplicación
        //Desfragmentador.intentarDesfragmentación(getActivity().getApplicationContext());

        //Imprimir datos de la base de datos para debuggeo
        //new CategoriaDAO(getActivity().getApplicationContext()).imprimir();
        //new CuentaDAO(getActivity().getApplicationContext()).imprimir();
        //new CategoriaCuentaDAO(getActivity().getApplicationContext()).imprimir();
        //new ContrasennaDAO(getActivity().getApplicationContext()).imprimir();
        //new ParametroDAO(getActivity().getApplicationContext()).imprimir();

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.B_login) {
            validarContrasenna();
            //Notificacion.Mostrar(getApplicationContext(), TipoNotificacion.GENERAL, "Titulo", "Mensaje");
        }
    }

    private void validarContrasenna() {
        try {
            String contrasenna = ET_contrasenna.getText().toString();
            if (contrasenna.isEmpty()) {
                throw new ExcepcionBancoContrasennas("Error - Llave Maestra Requerida", "Se requiere el ingreso de su Llave Maestra para el inicio de sesión.");
            }

            Parametro parSalt = parametroDAO.seleccionarUno(NombreParametro.SAL_HASH.toString());
            Parametro parHash = parametroDAO.seleccionarUno(NombreParametro.RESULTADO_HASH.toString());

            String[] saltYHashObt = Cifrador.genHashedPass(contrasenna, parSalt.getValor());
            if (!saltYHashObt[1].equals(parHash.getValor())) {
                ET_contrasenna.setText("");
                throw new ExcepcionBancoContrasennas("Error - Llave Maestra Incorrecta", "La Llave Maestra ingresada no es correcta, favor intentar de nuevo.");
            }

            Parametro parSaltEncr = parametroDAO.seleccionarUno(NombreParametro.SAL_ENCRIPTACION.toString());
            String[] saltYHashEncr = Cifrador.genHashedPass(contrasenna, parSaltEncr.getValor());
            actividadPrincipal().userLogIn(saltYHashEncr[1]);
        } catch (ExcepcionBancoContrasennas ex) {
            ex.alertDialog(this);
        }
    }
}
