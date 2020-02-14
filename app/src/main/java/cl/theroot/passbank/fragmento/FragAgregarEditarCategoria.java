package cl.theroot.passbank.fragmento;

import android.app.AlertDialog;
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
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import cl.theroot.passbank.ActividadPrincipal;
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

public class FragAgregarEditarCategoria extends CustomFragment {
    private EditText ET_name;

    private String oldName;
    private String addEdit;

    private CategoriaDAO categoriaDAO;
    private ParametroDAO parametroDAO;

    //Datos originales
    private Boolean algunCambio = false;
    private String nombreOriginal = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        parametroDAO = new ParametroDAO(getActivity().getApplicationContext());
        categoriaDAO = new CategoriaDAO(getActivity().getApplicationContext());

        View view = inflater.inflate(R.layout.fragmento_agregar_editar_categoria, null);
        TextView TV_titule = view.findViewById(R.id.TV_titule);
        ET_name = view.findViewById(R.id.ET_name);

        Bundle bundle = getArguments();
        oldName = null;
        TV_titule.setText(getResources().getText(R.string.crearCateg));
        addEdit = "ADD";

        if (bundle != null) {
            oldName = bundle.getString(ColCategoria.NOMBRE.toString());
            if (oldName != null) {
                Categoria categoria = categoriaDAO.seleccionarUna(oldName);
                if (categoria != null) {
                    TV_titule.setText(getResources().getText(R.string.editCateg));
                    addEdit = "EDIT";
                    ET_name.setText(categoria.getNombre());
                } else {
                    oldName = null;
                }
            }
        }

        //Setear datos para habilitar/deshabilitar el botón guardar
        algunCambio = false;
        nombreOriginal = ET_name.getText().toString();
        ET_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkearCambios();
            }
        });
        return view;
    }

    //Creación del submenú del fragmento
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sub_menu_agregar_editar_categoria, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        menu.findItem(R.id.sub_menu_add_edit_category_save).setEnabled(algunCambio);
        super.onPrepareOptionsMenu(menu);
    }

    //Creación de la funcionalidad del submenú
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!((ActividadPrincipal) getActivity()).isSesionIniciada()) {
            return false;
        }
        switch (item.getItemId()) {
            case R.id.sub_menu_add_edit_category_back:
                getActivity().onBackPressed();
                return true;

            case R.id.sub_menu_add_edit_category_save:
                try {
                    String name = ET_name.getText().toString().trim();
                    if (name.equals("")) {
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

                    String nombreAntiguo = null;
                    if (addEdit.equals("ADD")) {
                        Categoria categoria = new Categoria(name, null);
                        if (categoriaDAO.insertarUna(categoria) == -1) {
                            throw new ExcepcionBancoContrasennas("Error - Categoría No Creada", "Hubo un error con la base de datos, y su categoría no fue creada.");
                        }
                    } else {
                        Categoria categoria = categoriaDAO.seleccionarUna(oldName);
                        categoria.setNombre(name);
                        if (categoriaDAO.actualizarUna(oldName, categoria) == 0) {
                            throw new ExcepcionBancoContrasennas("Error - Categoría No Modificada", "Hubo un error con la base de datos, y su categoría no fue modificada.");
                        } else {
                            nombreAntiguo = oldName;
                        }
                    }

                    if (addEdit.equals("ADD")) {
                        CustomToast.Build(getActivity().getApplicationContext(), "Su categoría fue creada exitosamente.");
                    } else {
                        CustomToast.Build(getActivity().getApplicationContext(), "Su categoría fue modificada exitosamente.");
                    }

                    ((ActividadPrincipal) getActivity()).cambiarFragmento(new FragCategorias());
                    if (nombreAntiguo != null) {
                        actividadPrincipal().actualizarBundles(Categoria.class, nombreAntiguo, name);
                    }
                    return true;
                } catch(ExcepcionBancoContrasennas ex) {
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity().getApplicationContext()).create();
                    alertDialog.setTitle(ex.getTitulo());
                    alertDialog.setMessage(ex.getMensaje());
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
                    alertDialog.show();
                    int titleDividerId = getResources().getIdentifier("titleDivider", "id", "android");
                    View titleDivider = alertDialog.findViewById(titleDividerId);
                    if (titleDivider != null) {
                        titleDivider.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.letraAtenuada, null));
                    }
                    return true;
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkearCambios() {
        algunCambio = false;
        if (!ET_name.getText().toString().trim().equals(nombreOriginal)) {
            algunCambio = true;
            getActivity().invalidateOptionsMenu();
            return;
        }
        getActivity().invalidateOptionsMenu();
    }
}
