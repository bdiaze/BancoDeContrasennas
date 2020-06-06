package cl.theroot.passbank.fragmento;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import cl.theroot.passbank.CustomFragment;
import cl.theroot.passbank.CustomToast;
import cl.theroot.passbank.R;
import cl.theroot.passbank.adaptador.AdapCategorias;
import cl.theroot.passbank.datos.CategoriaDAO;
import cl.theroot.passbank.datos.nombres.ColCategoria;
import cl.theroot.passbank.dominio.Categoria;

public class FragCategorias extends CustomFragment implements AlertDialogContMenuEditarEliminar.iProcesarBoton, AlertDialogSiNoOk.iProcesarBotonSiNoOk {
    private static final String TAG = "BdC-FragCategorias";

    private CategoriaDAO categoriaDAO;
    private AdapCategorias adapter;
    private AdapterView.OnItemClickListener mOnItemClickListener;

    @BindView(R.id.listView)
    ListView listView;

    private static final String KEY_STR_SEL_CAT = "KEY_STR_SEL_CAT";
    private static final String KEY_STR_FLE_VIS = "KEY_STR_FLE_VIS";

    private String selectedCategory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        boolean ocultarFlechas = true;
        if (savedInstanceState != null) {
            selectedCategory = savedInstanceState.getString(KEY_STR_SEL_CAT);
            ocultarFlechas = savedInstanceState.getBoolean(KEY_STR_FLE_VIS);
        }

        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragmento_categorias, container, false);
        ButterKnife.bind(this, view);

        categoriaDAO = new CategoriaDAO(actividadPrincipal());

        adapter = new AdapCategorias(getActivity(), categoriaDAO.seleccionarTodas());
        listView.setAdapter(adapter);
        adapter.setOcultarFlechas(ocultarFlechas);

        mOnItemClickListener = (parent, view1, position, id) -> {
            Categoria categoria = adapter.getItem(position);
            FragDetalleCategoria fragDetalleCategoria = new FragDetalleCategoria();
            Bundle bundle = new Bundle();
            bundle.putString(ColCategoria.NOMBRE.toString(), categoria.getNombre());
            fragDetalleCategoria.setArguments(bundle);
            actividadPrincipal().cambiarFragmento(fragDetalleCategoria);
        };
        listView.setOnItemClickListener(mOnItemClickListener);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemLongClick(...) - Se realizó un click largo en una categoría.");

                selectedCategory = adapter.getItem(position).getNombre();
                if (selectedCategory.length() == 0) return false;

                AlertDialogContMenuEditarEliminar dialogContMenu = new AlertDialogContMenuEditarEliminar();
                dialogContMenu.setTargetFragment(FragCategorias.this, 1);
                dialogContMenu.show(getFragmentManager(), TAG);

                return true;
            }
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(KEY_STR_SEL_CAT, selectedCategory);
        outState.putBoolean(KEY_STR_FLE_VIS, adapter.getOcultarFlechas());
        super.onSaveInstanceState(outState);
    }

    //Creación del submenu del fragmento
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sub_menu_categorias, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //Creación de funcionalidad del submenu
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sub_menu_categorias_agregar:
                return actividadPrincipal().cambiarFragmento(new FragAgregarEditarCategoria());
            case R.id.sub_menu_categorias_habilitar_orden:
                item.setChecked(!item.isChecked());
                if (item.isChecked()) {
                    listView.setOnItemClickListener(null);
                    adapter.setOcultarFlechas(false);
                } else {
                    listView.setOnItemClickListener(mOnItemClickListener);
                    adapter.setOcultarFlechas(true);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void procesarBoton(int boton) {
        switch (boton) {
            case AlertDialogContMenuEditarEliminar.BOTON_EDITAR:
                Log.i(TAG, String.format("procesarBoton(...) - Procesar Botón Editar - Categoría %s.", selectedCategory));
                FragAgregarEditarCategoria fragAgregarEditarCategoria = new FragAgregarEditarCategoria();
                Bundle args = new Bundle();
                args.putString(ColCategoria.NOMBRE.toString(), selectedCategory);
                fragAgregarEditarCategoria.setArguments(args);
                actividadPrincipal().cambiarFragmento(fragAgregarEditarCategoria);
                break;
            case AlertDialogContMenuEditarEliminar.BOTON_ELIMINAR:
                Log.i(TAG, String.format("procesarBoton(...) - Procesar Botón Eliminar - Categoría %s.", selectedCategory));
                AlertDialogSiNoOk alertDialogSiNoOk = new AlertDialogSiNoOk();
                alertDialogSiNoOk.setTipo(AlertDialogSiNoOk.TIPO_SI_NO);
                alertDialogSiNoOk.setTitulo(getString(R.string.elimCategTitulo));
                alertDialogSiNoOk.setMensaje(getString(R.string.elimCategMensaje, selectedCategory));
                alertDialogSiNoOk.setTargetFragment(this, 1);
                alertDialogSiNoOk.show(getFragmentManager(), TAG);
                break;
        }
    }

    @Override
    public void procesarBotonSiNoOk(int boton) {
        if (boton == AlertDialogSiNoOk.BOTON_SI) {
            if (categoriaDAO.eliminarUna(selectedCategory) > 0) {
                adapter.updateCategoryList(categoriaDAO.seleccionarTodas());
                actividadPrincipal().actualizarBundles(Categoria.class, selectedCategory, null);
                CustomToast.Build(this, R.string.elimCategExitosa);
            } else {
                CustomToast.Build(this, R.string.elimCategFallida);
            }
        }
    }

}
