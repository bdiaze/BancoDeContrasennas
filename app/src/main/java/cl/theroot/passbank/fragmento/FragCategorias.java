package cl.theroot.passbank.fragmento;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.core.content.res.ResourcesCompat;

import java.util.List;

import cl.theroot.passbank.ActividadPrincipal;
import cl.theroot.passbank.CustomFragment;
import cl.theroot.passbank.R;
import cl.theroot.passbank.adaptador.AdapCategorias;
import cl.theroot.passbank.datos.CategoriaDAO;
import cl.theroot.passbank.datos.nombres.ColCategoria;
import cl.theroot.passbank.dominio.Categoria;

public class FragCategorias extends CustomFragment {
    //private static final String TAG = "BdC-Categorías";
    private AdapCategorias adapter;
    private List<Categoria> listItems;

    private ListView listView;

    private CategoriaDAO categoriaDAO;

    public Categoria selectedCategory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        categoriaDAO = new CategoriaDAO(getActivity().getApplicationContext());
        View view = inflater.inflate(R.layout.fragmento_categorias, container, false);

        listItems = categoriaDAO.seleccionarTodas();

        adapter = new AdapCategorias(getActivity(), listItems);
        listView = view.findViewById(R.id.listView);

        listView.setAdapter(adapter);
        adapter.setOcultarFlechas(true);
        registerForContextMenu(listView);

        listView.setOnItemClickListener(mOnItemClickListener);
        getActivity().invalidateOptionsMenu();
        return view;
    }

    //Creación de click normal
    AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Categoria categoria = adapter.getItem(i);
            FragDetalleCategoria fragDetalleCategoria = new FragDetalleCategoria();
            Bundle bundle = new Bundle();
            bundle.putString(ColCategoria.NOMBRE.toString(), categoria.getNombre());
            fragDetalleCategoria.setArguments(bundle);
            ((ActividadPrincipal) getActivity()).cambiarFragmento(fragDetalleCategoria);
        }
    };

    //Creación del submenu del fragmento
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sub_menu_categorias, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        if (adapter != null) {
            menu.findItem(R.id.sub_menu_categorias_habilitar_orden).setEnabled(adapter.getCount() > 1);
        }
        super.onPrepareOptionsMenu(menu);
    }

    //Creación de funcionalidad del submenu
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!((ActividadPrincipal) getActivity()).isSesionIniciada()) {
            return false;
        }
        switch (item.getItemId()) {
            case R.id.sub_menu_categorias_agregar:
                return ((ActividadPrincipal) getActivity()).cambiarFragmento(new FragAgregarEditarCategoria());
            case R.id.sub_menu_categorias_habilitar_orden:
                item.setChecked(!item.isChecked());
                if (item.isChecked()) {
                    unregisterForContextMenu(listView);
                    listView.setOnItemClickListener(null);
                    adapter.setOcultarFlechas(false);
                } else {
                    registerForContextMenu(listView);
                    listView.setOnItemClickListener(mOnItemClickListener);
                    adapter.setOcultarFlechas(true);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Creación del menu contextual del fragmento
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.cont_menu_categorias, menu);
    }

    //Creación de la funcionalidad del menu contextual
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!((ActividadPrincipal) getActivity()).isSesionIniciada()) {
            return false;
        }
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;
        selectedCategory = adapter.getItem(index);

        switch (item.getItemId()) {
            case R.id.cont_menu_categories_edit:
                FragAgregarEditarCategoria fragAgregarEditarCategoria = new FragAgregarEditarCategoria();
                Bundle args = new Bundle();
                args.putString(ColCategoria.NOMBRE.toString(), selectedCategory.getNombre());
                fragAgregarEditarCategoria.setArguments(args);
                return ((ActividadPrincipal) getActivity()).cambiarFragmento(fragAgregarEditarCategoria);
            case R.id.cont_menu_categories_delete:
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("Eliminacón de una Categoría");
                alertDialog.setMessage("¿Está seguro que desea eliminar la categoria " + selectedCategory.getNombre() + "? Las cuentas asociadas no se eliminarán, pero ya no se relacionarán con esta categoría.");
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", (dialog, which) -> dialog.dismiss());
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SÍ", (dialog, which) -> {
                    if (categoriaDAO.eliminarUna(selectedCategory.getNombre()) > 0) {
                        listItems = categoriaDAO.seleccionarTodas();
                        adapter.updateCategoryList(listItems);
                        ((ActividadPrincipal) getActivity()).actualizarBundles(Categoria.class, selectedCategory.getNombre(), null);
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
            default:
                return super.onContextItemSelected(item);
        }
    }
}
