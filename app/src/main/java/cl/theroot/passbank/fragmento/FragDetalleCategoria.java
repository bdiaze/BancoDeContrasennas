package cl.theroot.passbank.fragmento;


import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.List;

import cl.theroot.passbank.ActividadPrincipal;
import cl.theroot.passbank.CustomFragment;
import cl.theroot.passbank.R;
import cl.theroot.passbank.adaptador.AdapCuentas;
import cl.theroot.passbank.datos.CategoriaCuentaDAO;
import cl.theroot.passbank.datos.CategoriaDAO;
import cl.theroot.passbank.datos.CuentaDAO;
import cl.theroot.passbank.datos.nombres.ColCategoria;
import cl.theroot.passbank.dominio.Categoria;
import cl.theroot.passbank.dominio.CategoriaCuenta;
import cl.theroot.passbank.dominio.Cuenta;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragDetalleCategoria extends CustomFragment {
    private AdapCuentas adaptador;
    private List<Cuenta> cuentas = new ArrayList<>();
    private String nombreCategoria = null;

    private CategoriaDAO categoriaDAO;

    public FragDetalleCategoria() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        CuentaDAO cuentaDAO = new CuentaDAO(getActivity().getApplicationContext());
        CategoriaCuentaDAO categoriaCuentaDAO = new CategoriaCuentaDAO(getActivity().getApplicationContext());
        categoriaDAO = new CategoriaDAO(getActivity().getApplicationContext());

        View view = inflater.inflate(R.layout.fragmento_detalle_categoria, container, false);
        TextView TV_subTitulo = view.findViewById(R.id.TV_subTitulo);
        TextView TV_name = view.findViewById(R.id.TV_name);
        ListView listView = view.findViewById(R.id.LV_cuentas);

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

        adaptador = new AdapCuentas(getActivity(), cuentas, nombreCategoria);
        listView.setAdapter(adaptador);
        listView.setSelector(android.R.color.transparent);

        if (cuentas.isEmpty()) {
            TV_subTitulo.setVisibility(View.INVISIBLE);
        }
        getActivity().invalidateOptionsMenu();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sub_menu_detalle_categoria, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        if (adaptador != null) {
            menu.findItem(R.id.sub_menu_detalle_categoria_habilitar_orden).setEnabled(adaptador.getCount() > 1);
        }
        super.onPrepareOptionsMenu(menu);
    }

    //Creación de la funcionalidad del submenu del fragmento
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!((ActividadPrincipal) getActivity()).isSesionIniciada()) {
            return false;
        }
        switch (item.getItemId()) {
            case R.id.sub_menu_detalle_categoria_volver:
                getActivity().onBackPressed();
                return true;
            case R.id.sub_menu_detalle_categoria_editar:
                FragAgregarEditarCategoria fragAgregarEditarCategoria = new FragAgregarEditarCategoria();
                Bundle args = new Bundle();
                args.putString(ColCategoria.NOMBRE.toString(), nombreCategoria);
                fragAgregarEditarCategoria.setArguments(args);
                return ((ActividadPrincipal) getActivity()).cambiarFragmento(fragAgregarEditarCategoria);
            case R.id.sub_menu_detalle_categoria_eliminar:
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("Eliminacón de una Categoría");
                alertDialog.setMessage("¿Está seguro que desea eliminar la categoria " + nombreCategoria + "? Las cuentas asociadas no se eliminarán, pero ya no se relacionarán con esta categoría.");
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", (dialog, which) -> dialog.dismiss());
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SÍ", (dialog, which) -> {
                    if (categoriaDAO.eliminarUna(nombreCategoria) > 0) {
                        ((ActividadPrincipal) getActivity()).cambiarFragmento(new FragCategorias());
                        ((ActividadPrincipal) getActivity()).actualizarBundles(Categoria.class, nombreCategoria, null);
                    }
                    dialog.dismiss();
                });
                alertDialog.show();
                int titleDividerId = getResources().getIdentifier("titleDivider", "id", "android");
                View titleDivider = alertDialog.findViewById(titleDividerId);
                if (titleDivider != null) {
                    titleDivider.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.letraAtenuada, null));
                }
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

}
