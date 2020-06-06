package cl.theroot.passbank.fragmento;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cl.theroot.passbank.Cifrador;
import cl.theroot.passbank.CustomFragment;
import cl.theroot.passbank.CustomToast;
import cl.theroot.passbank.ExcepcionBancoContrasennas;
import cl.theroot.passbank.GeneradorContrasennas;
import cl.theroot.passbank.R;
import cl.theroot.passbank.adaptador.AdapCategoriasCheckBox;
import cl.theroot.passbank.datos.CategoriaCuentaDAO;
import cl.theroot.passbank.datos.CategoriaDAO;
import cl.theroot.passbank.datos.ContrasennaDAO;
import cl.theroot.passbank.datos.CuentaDAO;
import cl.theroot.passbank.datos.ParametroDAO;
import cl.theroot.passbank.datos.nombres.ColCuenta;
import cl.theroot.passbank.datos.nombres.NombreParametro;
import cl.theroot.passbank.dominio.Categoria;
import cl.theroot.passbank.dominio.CategoriaCuenta;
import cl.theroot.passbank.dominio.CategoriaSeleccionable;
import cl.theroot.passbank.dominio.Contrasenna;
import cl.theroot.passbank.dominio.Cuenta;
import cl.theroot.passbank.dominio.Parametro;

public class FragAgregarEditarCuenta extends CustomFragment implements AlertDialogSiNoOk.iProcesarBotonSiNoOk, AlertDialogContMenuModoGenerador.iProcesarSeleccion {
    private static final String TAG = "BdC-FragAgrEdtCuenta";

    private GeneradorContrasennas genContrasennas;
    private AdapCategoriasCheckBox adapter;
    private ParametroDAO parametroDAO;
    private CuentaDAO cuentaDAO;
    private ContrasennaDAO contrasennaDAO;
    private CategoriaCuentaDAO categoriaCuentaDAO;
    private CategoriaDAO categoriaDAO;

    @BindView(R.id.TV_titule)
    TextView TV_titule;
    @BindView(R.id.ET_name)
    EditText ET_name;
    @BindView(R.id.ET_description)
    EditText ET_description;
    @BindView(R.id.IV_passVisibility)
    ImageView IV_passVisibility;
    @BindView(R.id.IV_passGenerator)
    ImageView IV_passGenerator;
    @BindView(R.id.ET_password)
    EditText ET_password;
    @BindView(R.id.ET_validez)
    EditText ET_validez;
    @BindView(R.id.SB_validez)
    SeekBar SB_validez;
    @BindView(R.id.TV_subTitulo)
    TextView TV_subTitulo;
    @BindView(R.id.IV_agregarCategoria)
    ImageView IV_agregarCategoria;
    @BindView(R.id.ET_nombreNuevaCategoria)
    EditText ET_nombreNuevaCategoria;
    @BindView(R.id.listview_categories_checkboxs)
    ListView listView;

    private static final String KEY_STR_NOM_ANT = "KEY_STR_NOM_ANT";
    private static final String KEY_LNG_CNT_IDN = "KEY_LNG_CNT_IDN";
    private static final String KEY_BYT_MOD_GEN = "KEY_BYT_MOD_GEN";

    private String oldName;
    private Long oldPasswordID;
    private Byte modoGenerador = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            oldName = savedInstanceState.getString(KEY_STR_NOM_ANT);
            oldPasswordID = savedInstanceState.getLong(KEY_LNG_CNT_IDN);
            modoGenerador = savedInstanceState.getByte(KEY_BYT_MOD_GEN);
        }

        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragmento_agregar_editar_cuenta, container, false);
        ButterKnife.bind(this, view);

        categoriaDAO = new CategoriaDAO(getApplicationContext());
        categoriaCuentaDAO = new CategoriaCuentaDAO(getApplicationContext());
        contrasennaDAO = new ContrasennaDAO(getApplicationContext());
        cuentaDAO = new CuentaDAO(getApplicationContext());
        parametroDAO = new ParametroDAO(getApplicationContext());
        genContrasennas = new GeneradorContrasennas(getApplicationContext());

        IV_passVisibility.setOnClickListener(v -> {
            if (ET_password.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                int pointerPos = ET_password.getSelectionStart();
                IV_passVisibility.setImageResource(R.drawable.baseline_visibility_off_24);
                ET_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                ET_password.setSelection(pointerPos);
            } else {
                int pointerPos = ET_password.getSelectionStart();
                IV_passVisibility.setImageResource(R.drawable.baseline_visibility_24);
                ET_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ET_password.setSelection(pointerPos);
            }
        });

        IV_passGenerator.setOnClickListener(v -> {
            parametroDAO.actualizarUna(new Parametro(NombreParametro.ULTIMO_MODO_GENERADOR.toString(), String.valueOf(modoGenerador), null));
            if (modoGenerador == 0) {
                ET_password.setText(genContrasennas.generar(true));
            } else {
                ET_password.setText(genContrasennas.generar(false));
            }
        });
        //registerForContextMenu(IV_passGenerator);

        SB_validez.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    ET_validez.setText(String.valueOf(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ET_validez.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int numero = Integer.parseInt(ET_validez.getText().toString());
                    if (numero > SB_validez.getMax()) {
                        SB_validez.setProgress(SB_validez.getMax());
                    } else {
                        if (numero < 0) {
                            SB_validez.setProgress(0);
                        } else {
                            SB_validez.setProgress(numero);
                        }
                    }
                } catch(NumberFormatException ex) {
                    SB_validez.setProgress(0);
                }
            }
        });

        IV_agregarCategoria.setOnClickListener(v -> {
            ET_nombreNuevaCategoria.selectAll();

            String nombreNuevaCategoria = ET_nombreNuevaCategoria.getText().toString().trim();
            if (nombreNuevaCategoria.length() == 0) {
                CustomToast.Build(this, R.string.errorSinNombre);
                return;
            }

            Parametro parCatCompleta = parametroDAO.seleccionarUno(NombreParametro.NOMBRE_CATEGORIA_COMPLETA.toString());
            if (nombreNuevaCategoria.equals(parCatCompleta.getValor())) {
                CustomToast.Build(this, R.string.errorNombreReservado);
                return;
            }

            Categoria categoriaIdentica = categoriaDAO.seleccionarUna(nombreNuevaCategoria);
            if (categoriaIdentica != null) {
                CustomToast.Build(this, R.string.errorNombreUsado);
                return;
            }

            Categoria categoria = new Categoria(nombreNuevaCategoria, null);
            if (categoriaDAO.insertarUna(categoria) == -1) {
                CustomToast.Build(this, R.string.errorNoSePudoCrearCategoria);
                return;
            } else {
                CustomToast.Build(this, R.string.categoriaCreada);
            }

            ET_nombreNuevaCategoria.setText(null);

            // Se vuelve a consultar y presentar la lista de categorías...
            if (adapter != null) {
                List<CategoriaSeleccionable> categorias = adapter.getCategorias();
                categorias.add(new CategoriaSeleccionable(categoria, true));
                adapter.updateCategorias(buscarCategoriasUsandoSeleccionAnterior(categorias));
                setListViewHeightBasedOnChildren(listView);
            }
        });

        TV_titule.setText(getResources().getText(R.string.crearCuenta));
        Parametro parValDefecto = parametroDAO.seleccionarUno(NombreParametro.VALIDEZ_DEFECTO.toString());
        if (parValDefecto != null) {
            ET_validez.setText(parValDefecto.getValor());
        } else {
            ET_validez.setText('0');
        }

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            oldName = bundle.getString(ColCuenta.NOMBRE.toString());
            if (oldName != null) {
                Cuenta cuenta = cuentaDAO.seleccionarUna(oldName);
                if (cuenta != null) {
                    TV_titule.setText(getResources().getText(R.string.editarCuenta));
                    ET_name.setText(cuenta.getNombre());
                    ET_description.setText(cuenta.getDescripcion());
                    ET_validez.setText(String.valueOf(cuenta.getValidez()));

                    Contrasenna contrasenna = contrasennaDAO.seleccionarUltimaPorCuenta(cuenta.getNombre());
                    if (contrasenna != null) {
                        oldPasswordID = contrasenna.getId();
                        ET_password.setText(Cifrador.desencriptar(contrasenna.getValor(), actividadPrincipal().getLlaveEncrip()));
                    } else {
                        oldPasswordID = null;
                    }
                } else {
                    oldName = null;
                }
            }
        }

        List<CategoriaSeleccionable> categories = fillCategoriesInfo(oldName);
        adapter = new AdapCategoriasCheckBox(getActivity(), categories, this);
        listView.setAdapter(adapter);
        setListViewHeightBasedOnChildren(listView);

        if (categories.isEmpty()) {
            TV_subTitulo.setVisibility(View.INVISIBLE);
        }

        //Setear el modo del generador por defecto
        Parametro parametro = parametroDAO.seleccionarUno(NombreParametro.ULTIMO_MODO_GENERADOR.toString());
        if (parametro != null) {
            modoGenerador = Byte.parseByte(parametro.getValor());
        }

        IV_passGenerator.setOnLongClickListener(v -> {
            AlertDialogContMenuModoGenerador dialogContMenu = new AlertDialogContMenuModoGenerador();
            dialogContMenu.setSeleccion(modoGenerador);
            dialogContMenu.setTargetFragment(this,1);
            dialogContMenu.show(getFragmentManager(), TAG);
            return true;
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (oldName != null) outState.putString(KEY_STR_NOM_ANT, oldName);
        if (oldPasswordID != null) outState.putLong(KEY_LNG_CNT_IDN, oldPasswordID);
        if (modoGenerador != null) outState.putByte(KEY_BYT_MOD_GEN, modoGenerador);
        super.onSaveInstanceState(outState);
    }

    //Creación del submenu del fragmento
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sub_menu_agregar_editar_cuenta, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    //Creación de la funcionalidad del submenu del fragmento
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.sub_menu_add_edit_account_back:
                    getActivity().onBackPressed();
                    return true;

                case R.id.sub_menu_add_edit_account_save:
                    String name = ET_name.getText().toString().trim();
                    String description = ET_description.getText().toString().trim();
                    String password = ET_password.getText().toString();
                    String strValidez = ET_validez.getText().toString().trim();
                    int validez;

                    if (description.equals("")) {
                        description = null;
                    }

                    // Revisar si el nombre y contraseña están vacíos.
                    if (name.equals("")) {
                        throw new ExcepcionBancoContrasennas("Error - Campo Vacío", "Toda cuenta requiere de un nombre, favor de rellenar el campo Nombre");
                    }
                    if (password.equals("")) {
                        throw new ExcepcionBancoContrasennas("Error - Campo Vacío", "Toda cuenta requiere de una contraseña, favor de rellenar el campo Contraseña");
                    }

                    try {
                        validez = Integer.parseInt(strValidez);
                    } catch (NumberFormatException ex) {
                        throw new ExcepcionBancoContrasennas("Error - Formato Inválido", "La validez debe ser un número entero");
                    }

                    if (validez < 0) {
                        throw new ExcepcionBancoContrasennas("Error - Formato Inválido", "La validez debe ser un número mayor a 0");
                    }

                    //Revisar si ya existe un cuenta con el mismo nombre
                    Cuenta cuentaIdentica = cuentaDAO.seleccionarUna(name);
                    if (cuentaIdentica != null && !name.equals(oldName)) {
                        throw new ExcepcionBancoContrasennas("Error - Cuenta Existente", "Ya existe una cuenta con el nombre establecido");
                    }

                    Cuenta cuenta = new Cuenta(name, description, validez);
                    //Se actualiza o inserta la cuenta deseada
                    if (oldName == null) {
                        if (cuentaDAO.insertarUna(cuenta) == -1) {
                            throw new ExcepcionBancoContrasennas("Error - Cuenta No Creada", "Hubo un error con la base de datos, y su cuenta no fue creada.");
                        }
                    } else {
                        if (cuentaDAO.actualizarUna(oldName, cuenta) == 0) {
                            throw new ExcepcionBancoContrasennas("Error - Cuenta No Modificada", "Hubo un error con la base de datos, y su cuenta no fue modificada.");
                        }
                    }

                    //Se obtiene la fecha de creación de la contraseña
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                    String formattedDate = simpleDateFormat.format(calendar.getTime());

                    if (oldPasswordID != null) {
                        //Se obtiene la contraseña actual de la cuenta para comparar si es igual a la nueva
                        Contrasenna contAntigua = contrasennaDAO.seleccionarUna(oldPasswordID);
                        if (contAntigua != null) {
                            if (!Cifrador.desencriptar(contAntigua.getValor(), actividadPrincipal().getLlaveEncrip()).equals(password)) {
                                Contrasenna contrasenna = new Contrasenna(null, cuenta.getNombre(), Cifrador.encriptar(password, actividadPrincipal().getLlaveEncrip()), formattedDate);
                                oldPasswordID = contrasennaDAO.insertarUna(contrasenna);
                                cuenta.setVencInf(0);
                                cuentaDAO.actualizarUna(cuenta.getNombre(), cuenta);
                            }
                        } else {
                            Contrasenna contrasenna = new Contrasenna(null, cuenta.getNombre(), Cifrador.encriptar(password, actividadPrincipal().getLlaveEncrip()), formattedDate);
                            oldPasswordID = contrasennaDAO.insertarUna(contrasenna);
                        }
                    } else {
                        Contrasenna contrasenna = new Contrasenna(null, cuenta.getNombre(), Cifrador.encriptar(password, actividadPrincipal().getLlaveEncrip()), formattedDate);
                        oldPasswordID = contrasennaDAO.insertarUna(contrasenna);
                    }

                    //Ya creada/actualizada la cuenta y contraseña, se deben actualizar las relaciones entre categorias y cuentas.
                    for (CategoriaSeleccionable categoria : adapter.getCategorias()) {
                        //Checkear si existe la relación
                        CategoriaCuenta categoriaCuenta = categoriaCuentaDAO.seleccionarUna(categoria.getNombre(), cuenta.getNombre());
                        if (categoria.isSeleccionado()) {
                            //Si no existe, la creamos.
                            if (categoriaCuenta == null) {
                                categoriaCuentaDAO.insertarUna(new CategoriaCuenta(categoria.getNombre(), cuenta.getNombre(), 0));
                            }
                        } else {
                            //Si existe, la eliminamos.
                            if (categoriaCuenta != null) {
                                if (categoriaCuentaDAO.eliminarUna(categoria.getNombre(), cuenta.getNombre()) == 0) {
                                    Log.e(TAG, "No se pudo eliminar la relación Categoría/Cuenta");
                                }
                            }
                        }
                    }

                    if (oldName == null) {
                        CustomToast.Build(getActivity().getApplicationContext(), "Su cuenta fue creada exitosamente.");
                    } else {
                        CustomToast.Build(getActivity().getApplicationContext(), "Su cuenta fue modificada exitosamente.");
                    }

                    actividadPrincipal().cambiarFragmento(new FragCuentas());
                    if (oldName != null) {
                        actividadPrincipal().actualizarBundles(Cuenta.class, oldName, cuenta.getNombre());
                    }
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        } catch(ExcepcionBancoContrasennas ex) {
            ex.alertDialog(this);
            return true;
        }
    }

    private List<CategoriaSeleccionable> fillCategoriesInfo(String nombreCuenta) {
        List<CategoriaSeleccionable> salida = new ArrayList<>();

        for (Categoria categoria : categoriaDAO.seleccionarTodas()) {
            CategoriaSeleccionable categoriaSeleccionable = new CategoriaSeleccionable(categoria.getNombre(), categoria.getPosicion(), false);
            if (nombreCuenta != null) {
                CategoriaCuenta categoriaCuenta = categoriaCuentaDAO.seleccionarUna(categoria.getNombre(), nombreCuenta);
                if (categoriaCuenta != null) {
                    categoriaSeleccionable.setSeleccionado(true);
                }
            }
            salida.add(categoriaSeleccionable);
        }

        return salida;
    }

    private List<CategoriaSeleccionable> buscarCategoriasUsandoSeleccionAnterior(List<CategoriaSeleccionable> seleccionAnterior) {
        // Filtramos solo las categorias seleccionadas...
        HashSet<CategoriaSeleccionable> soloCategoriasSeleccionadas = new HashSet<>();
        for (CategoriaSeleccionable categoria : seleccionAnterior) {
            if (categoria.isSeleccionado()) soloCategoriasSeleccionadas.add(categoria);
        }

        // Obtenemos todas las categorias, y seleccionamos las que estaban seleccionadas anteriormente...
        List<CategoriaSeleccionable> salida = new ArrayList<>();
        for (Categoria categoria : categoriaDAO.seleccionarTodas()) {
            CategoriaSeleccionable categoriaSeleccionable = new CategoriaSeleccionable(categoria, false);
            if (soloCategoriasSeleccionadas.contains(categoriaSeleccionable)) {
                categoriaSeleccionable.setSeleccionado(true);
            }
            salida.add(categoriaSeleccionable);
        }
        return salida;
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0) {
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    @Override
    public void procesarBotonSiNoOk(int boton) {

    }

    @Override
    public void procesarSeleccion(int radioButton) {
        modoGenerador = (byte) radioButton;
    }
}
