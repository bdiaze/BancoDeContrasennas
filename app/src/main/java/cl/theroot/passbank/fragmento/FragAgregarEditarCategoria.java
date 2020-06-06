package cl.theroot.passbank.fragmento;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import cl.theroot.passbank.CustomFragment;
import cl.theroot.passbank.CustomToast;
import cl.theroot.passbank.ExcepcionBancoContrasennas;
import cl.theroot.passbank.R;
import cl.theroot.passbank.datos.CategoriaDAO;
import cl.theroot.passbank.datos.ParametroDAO;
import cl.theroot.passbank.datos.nombres.ColCategoria;
import cl.theroot.passbank.datos.nombres.NombreParametro;
import cl.theroot.passbank.dominio.Categoria;
import cl.theroot.passbank.dominio.Parametro;

public class FragAgregarEditarCategoria extends CustomFragment implements AlertDialogSiNoOk.iProcesarBotonSiNoOk{
    private static final String TAG = "BdC-FragAgregarEditarCa";

    private CategoriaDAO categoriaDAO;
    private ParametroDAO parametroDAO;

    @BindView(R.id.ET_name)
    EditText ET_name;
    @BindView(R.id.TV_titule)
    TextView TV_titule;

    private static final String KEY_STR_NOM_ANT = "KEY_STR_NOM_ANT";

    private String oldName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            oldName = savedInstanceState.getString(KEY_STR_NOM_ANT);
        }

        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragmento_agregar_editar_categoria, null);
        ButterKnife.bind(this, view);

        parametroDAO = new ParametroDAO(getActivity().getApplicationContext());
        categoriaDAO = new CategoriaDAO(getActivity().getApplicationContext());

        TV_titule.setText(getResources().getText(R.string.crearCateg));

        Bundle bundle = getArguments();
        if (bundle != null) {
            oldName = bundle.getString(ColCategoria.NOMBRE.toString());
            if (oldName != null) {
                Categoria categoria = categoriaDAO.seleccionarUna(oldName);
                if (categoria != null) {
                    TV_titule.setText(getResources().getText(R.string.editCateg));
                    ET_name.setText(categoria.getNombre());
                } else {
                    oldName = null;
                }
            }
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(KEY_STR_NOM_ANT, oldName);
        super.onSaveInstanceState(outState);
    }

    //Creación del submenú del fragmento
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sub_menu_agregar_editar_categoria, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //Creación de la funcionalidad del submenú
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sub_menu_add_edit_category_back:
                getActivity().onBackPressed();
                return true;

            case R.id.sub_menu_add_edit_category_save:
                try {
                    String name = ET_name.getText().toString().trim();
                    if (name.length() == 0) {
                        throw new ExcepcionBancoContrasennas("Nombre Requerido", "Para crear una categoría, ésta requiere de un nombre.");
                    }

                    Parametro parCatCompleta = parametroDAO.seleccionarUno(NombreParametro.NOMBRE_CATEGORIA_COMPLETA.toString());
                    if (name.equals(parCatCompleta.getValor())) {
                        throw new ExcepcionBancoContrasennas("Nombre Reservado", "El nombre seleccionado está reservado por la aplicación.");
                    }

                    Categoria categoriaIdentica = categoriaDAO.seleccionarUna(name);
                    if (categoriaIdentica != null && !name.equals(oldName)) {
                        throw new ExcepcionBancoContrasennas("Nombre en Uso", "El nombre seleccionado ya está siendo usado por otra categoría.");
                    }

                    if (oldName == null) {
                        Categoria categoria = new Categoria(name, null);
                        if (categoriaDAO.insertarUna(categoria) == -1) {
                            throw new ExcepcionBancoContrasennas("Error - Categoría No Creada", "Hubo un error con la base de datos, y su categoría no fue creada.");
                        }
                    } else {
                        Categoria categoria = categoriaDAO.seleccionarUna(oldName);
                        categoria.setNombre(name);
                        if (categoriaDAO.actualizarUna(oldName, categoria) == 0) {
                            throw new ExcepcionBancoContrasennas("Error - Categoría No Modificada", "Hubo un error con la base de datos, y su categoría no fue modificada.");
                        }
                    }

                    if (oldName == null) {
                        CustomToast.Build(getApplicationContext(), "Su categoría fue creada exitosamente.");
                    } else {
                        CustomToast.Build(getApplicationContext(), "Su categoría fue modificada exitosamente.");
                    }

                    actividadPrincipal().cambiarFragmento(new FragCategorias());
                    if (oldName != null) {
                        actividadPrincipal().actualizarBundles(Categoria.class, oldName, name);
                    }
                    return true;
                } catch(ExcepcionBancoContrasennas ex) {
                    ex.alertDialog(this);
                    return true;
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void procesarBotonSiNoOk(int boton) {

    }
}
