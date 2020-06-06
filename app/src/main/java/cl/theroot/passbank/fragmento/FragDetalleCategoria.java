package cl.theroot.passbank.fragmento;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cl.theroot.passbank.CustomFragment;
import cl.theroot.passbank.CustomToast;
import cl.theroot.passbank.R;
import cl.theroot.passbank.adaptador.AdapCuentas;
import cl.theroot.passbank.datos.CategoriaCuentaDAO;
import cl.theroot.passbank.datos.CategoriaDAO;
import cl.theroot.passbank.datos.CuentaDAO;
import cl.theroot.passbank.datos.nombres.ColCategoria;
import cl.theroot.passbank.dominio.Categoria;
import cl.theroot.passbank.dominio.CategoriaCuenta;
import cl.theroot.passbank.dominio.Cuenta;

public class FragDetalleCategoria extends CustomFragment implements AlertDialogSiNoOk.iProcesarBotonSiNoOk{
    private static final String TAG = "BdC-FragDetalleCategoria";

    private CategoriaDAO categoriaDAO;
    private AdapCuentas adaptador;

    @BindView(R.id.TV_subTitulo)
    TextView TV_subTitulo;
    @BindView(R.id.TV_name)
    TextView TV_name;
    @BindView(R.id.LV_cuentas)
    ListView listView;

    private  static final String KEY_STR_NOM_CAT = "KEY_STR_NOM_CAT";

    private String nombreCategoria;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            nombreCategoria = savedInstanceState.getString(KEY_STR_NOM_CAT);
        }

        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragmento_detalle_categoria, container, false);
        ButterKnife.bind(this, view);

        CuentaDAO cuentaDAO = new CuentaDAO(getApplicationContext());
        CategoriaCuentaDAO categoriaCuentaDAO = new CategoriaCuentaDAO(getApplicationContext());
        categoriaDAO = new CategoriaDAO(getApplicationContext());

        List<Cuenta> cuentas = new ArrayList<>();
        Bundle bundle = getArguments();
        if (bundle != null) {
            nombreCategoria = bundle.getString(ColCategoria.NOMBRE.toString());
            if (nombreCategoria != null) {
                TV_name.setText(nombreCategoria);
                for (CategoriaCuenta categoriaCuenta : categoriaCuentaDAO.seleccionarPorCategoria(nombreCategoria)) {
                    Cuenta cuenta = cuentaDAO.seleccionarUna(categoriaCuenta.getNombreCuenta());
                    if (cuenta != null) {
                        cuentas.add(cuenta);
                    }
                }
            }
        }

        adaptador = new AdapCuentas(getContext(), cuentas, nombreCategoria);
        listView.setAdapter(adaptador);
        listView.setSelector(android.R.color.transparent);

        if (cuentas.isEmpty()) {
            TV_subTitulo.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(KEY_STR_NOM_CAT, nombreCategoria);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sub_menu_detalle_categoria, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //Creación de la funcionalidad del submenu del fragmento
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sub_menu_detalle_categoria_volver:
                getActivity().onBackPressed();
                return true;
            case R.id.sub_menu_detalle_categoria_editar:
                FragAgregarEditarCategoria fragAgregarEditarCategoria = new FragAgregarEditarCategoria();
                Bundle args = new Bundle();
                args.putString(ColCategoria.NOMBRE.toString(), nombreCategoria);
                fragAgregarEditarCategoria.setArguments(args);
                return actividadPrincipal().cambiarFragmento(fragAgregarEditarCategoria);
            case R.id.sub_menu_detalle_categoria_eliminar:
                AlertDialogSiNoOk alertDialogSiNoOk = new AlertDialogSiNoOk();
                alertDialogSiNoOk.setTipo(AlertDialogSiNoOk.TIPO_SI_NO);
                alertDialogSiNoOk.setTitulo(getString(R.string.elimCategTitulo));
                alertDialogSiNoOk.setMensaje(getString(R.string.elimCategMensaje, nombreCategoria));
                alertDialogSiNoOk.setTargetFragment(this, 1);
                alertDialogSiNoOk.show(getFragmentManager(), TAG);
                return true;
            case R.id.sub_menu_detalle_categoria_habilitar_orden:
                item.setChecked(!item.isChecked());
                if (item.isChecked()) {
                    adaptador.setOcultarFlechas(false);
                } else {
                    adaptador.setOcultarFlechas(true);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void procesarBotonSiNoOk(int boton) {
        if (boton == AlertDialogSiNoOk.BOTON_SI) {
            if (categoriaDAO.eliminarUna(nombreCategoria) > 0) {
                actividadPrincipal().cambiarFragmento(new FragCategorias());
                actividadPrincipal().actualizarBundles(Categoria.class, nombreCategoria, null);
                CustomToast.Build(this, R.string.elimCategExitosa);
            } else {
                CustomToast.Build(this, R.string.elimCategFallida);
            }
        }
    }
}
