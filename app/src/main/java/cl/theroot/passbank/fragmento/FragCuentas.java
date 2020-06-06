package cl.theroot.passbank.fragmento;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jakewharton.rxbinding3.widget.RxTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import cl.theroot.passbank.CustomFragment;
import cl.theroot.passbank.CustomToast;
import cl.theroot.passbank.R;
import cl.theroot.passbank.adaptador.AdapCategoriasCuentas;
import cl.theroot.passbank.adaptador.AdapCuentasBusq;
import cl.theroot.passbank.datos.CategoriaCuentaDAO;
import cl.theroot.passbank.datos.CategoriaDAO;
import cl.theroot.passbank.datos.ContrasennaDAO;
import cl.theroot.passbank.datos.CuentaDAO;
import cl.theroot.passbank.datos.nombres.ColCuenta;
import cl.theroot.passbank.dominio.Categoria;
import cl.theroot.passbank.dominio.CategoriaCuenta;
import cl.theroot.passbank.dominio.CategoriaListaCuentas;
import cl.theroot.passbank.dominio.Contrasenna;
import cl.theroot.passbank.dominio.Cuenta;
import cl.theroot.passbank.dominio.CuentaConFecha;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FragCuentas extends CustomFragment implements AlertDialogContMenuEditarEliminar.iProcesarBoton, AlertDialogSiNoOk.iProcesarBotonSiNoOk {
    private static final String TAG = "BdC-FragCuentas";

    private AdapCategoriasCuentas adapter;
    private AdapCuentasBusq adapterBusq;
    private CuentaDAO cuentaDAO;
    private CategoriaDAO categoriaDAO;
    private CategoriaCuentaDAO categoriaCuentaDAO;
    private ContrasennaDAO contrasennaDAO;

    private CompositeDisposable disposable;

    @BindView(R.id.expListView)
    ExpandableListView expListView;
    @BindView(R.id.buscarEditText)
    EditText buscarET;
    @BindView(R.id.resultBusqListView)
    ListView resultBusqListView;

    private static final String KEY_STR_NOM_CUE = "KEY_STR_NOM_CUE";

    private String nombreCuenta;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            nombreCuenta = savedInstanceState.getString(KEY_STR_NOM_CUE);
        }

        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragmento_cuentas, container, false);
        ButterKnife.bind(this, view);

        cuentaDAO = new CuentaDAO(getActivity().getApplicationContext());
        categoriaDAO = new CategoriaDAO(getActivity().getApplicationContext());
        categoriaCuentaDAO = new CategoriaCuentaDAO(getActivity().getApplicationContext());
        contrasennaDAO = new ContrasennaDAO(getActivity().getApplicationContext());

        adapter = new AdapCategoriasCuentas(getActivity(), this, fillAccountsInfo());
        expListView.setAdapter(adapter);

        // Se obtiene última búsqueda de cuentas para carga automática...
        List<CuentaConFecha> resultadosBusqueda = new ArrayList<>();
        if (this.getArguments() != null) {
            String cuentaBuscada = this.getArguments().getString(ColCuenta.NOMBRE.toString());
            if (cuentaBuscada != null && cuentaBuscada.trim().length() > 0) {
                buscarET.setText(cuentaBuscada);
                resultadosBusqueda = buscarCuentas();
            }
        }

        adapterBusq = new AdapCuentasBusq(getActivity(), resultadosBusqueda);
        resultBusqListView.setAdapter(adapterBusq);

        RxTextView.textChanges(buscarET)
                .debounce(300, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> this.disposable.add(disposable))
                .subscribe(new Observer<CharSequence>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(CharSequence charSequence) {
                        adapterBusq.actualizarCuentas(buscarCuentas(charSequence.toString()));
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

        expListView.setOnChildClickListener((expandableListView, view12, i, i1, l) -> {
            Cuenta cuenta = adapter.getChild(i, i1);
            FragDetalleCuenta fragDetalleCuenta = new FragDetalleCuenta();
            Bundle bundle = new Bundle();
            bundle.putString(ColCuenta.NOMBRE.toString(), cuenta.getNombre());
            fragDetalleCuenta.setArguments(bundle);
            return actividadPrincipal().cambiarFragmento(fragDetalleCuenta);
        });

        expListView.setOnItemLongClickListener((parent, view13, position, id) -> {
            Log.i(TAG, "onItemLongClick(...) - Se realizó un click largo en una cuenta o categoría.");

            long packedPosition = expListView.getExpandableListPosition(position);
            int itemType = ExpandableListView.getPackedPositionType(packedPosition);
            int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
            int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);

            if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                nombreCuenta = adapter.getChild(groupPosition, childPosition).getNombre();
                if (nombreCuenta.length() == 0) { return false; }

                AlertDialogContMenuEditarEliminar dialogContMenu = new AlertDialogContMenuEditarEliminar();
                dialogContMenu.setTargetFragment(FragCuentas.this, 1);
                dialogContMenu.show(getFragmentManager(), TAG);
            }

            return true;
        });

        resultBusqListView.setOnItemClickListener((parent, view1, position, id) -> {
            Cuenta cuenta = adapterBusq.getItem(position);
            FragDetalleCuenta fragDetalleCuenta = new FragDetalleCuenta();
            Bundle bundle = new Bundle();
            bundle.putString(ColCuenta.NOMBRE.toString(), cuenta.getNombre());
            fragDetalleCuenta.setArguments(bundle);
            actividadPrincipal().cambiarFragmento(fragDetalleCuenta);
        });

        resultBusqListView.setOnItemLongClickListener((parent, view14, position, id) -> {
            Log.i(TAG, "onItemLongClick(...) - Se realizó un click largo en una cuenta o categoría.");

            nombreCuenta = adapterBusq.getItem(position).getNombre();
            if (nombreCuenta.length() == 0) return false;

            AlertDialogContMenuEditarEliminar dialogContMenu = new AlertDialogContMenuEditarEliminar();
            dialogContMenu.setTargetFragment(FragCuentas.this, 1);
            dialogContMenu.show(getFragmentManager(), TAG);

            return true;
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(KEY_STR_NOM_CUE, nombreCuenta);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disposable = new CompositeDisposable();
    }

    @Override
    public void onDestroy() {
        disposable.clear();
        super.onDestroy();
    }

    //Creación del submenu principal del fragmento
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sub_menu_cuentas, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //Creación de la funcionalidad del submenu del fragmento
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sub_menu_accounts_add) {
            return actividadPrincipal().cambiarFragmento(new FragAgregarEditarCuenta());
        }
        return super.onOptionsItemSelected(item);
    }

    //Llenado de los datos a mostrar
    private List<CategoriaListaCuentas> fillAccountsInfo() {
        List<CategoriaListaCuentas> salida = new ArrayList<>();

        for (Categoria categoria : categoriaDAO.seleccionarTodas()) {
            List<CuentaConFecha> cuentas = new ArrayList<>();
            for (CategoriaCuenta categoriaCuenta : categoriaCuentaDAO.seleccionarPorCategoria(categoria.getNombre())) {
                Cuenta cuenta = cuentaDAO.seleccionarUna(categoriaCuenta.getNombreCuenta());
                if (cuenta != null) {
                    Contrasenna contrasenna = contrasennaDAO.seleccionarUltimaPorCuenta(cuenta.getNombre());
                    String fecha = "2000/01/01";
                    if (contrasenna != null) {
                        fecha = contrasenna.getFecha();
                    }
                    cuentas.add(new CuentaConFecha(cuenta.getNombre(), cuenta.getDescripcion(), cuenta.getValidez(), fecha));
                } else {
                    Log.e(TAG, "No se encontró la cuenta con el nombre: " + categoriaCuenta.getNombreCuenta());
                }
            }
            salida.add(new CategoriaListaCuentas(categoria.getNombre(), categoria.getPosicion(), cuentas));
        }

        List<CuentaConFecha> cuentas = new ArrayList<>();
        for (Cuenta cuenta : cuentaDAO.seleccionarTodas()) {
            Contrasenna contrasenna = contrasennaDAO.seleccionarUltimaPorCuenta(cuenta.getNombre());
            String fecha = "2000/01/01";
            if (contrasenna != null) {
                fecha = contrasenna.getFecha();
            }
            cuentas.add(new CuentaConFecha(cuenta.getNombre(), cuenta.getDescripcion(), cuenta.getValidez(), fecha));
        }
        salida.add(new CategoriaListaCuentas("", null, cuentas));

        return salida;
    }

    private List<CuentaConFecha> buscarCuentas() {
        return buscarCuentas(buscarET.getText().toString());
    }

    private List<CuentaConFecha> buscarCuentas(String busqueda) {
        List<CuentaConFecha> salida = new ArrayList<>();

        if (busqueda.trim().length() > 0) {
            // Se graba última búsqueda generada...
            if (this.getArguments() == null) { this.setArguments(new Bundle()); }
            this.getArguments().remove(ColCuenta.NOMBRE.toString());
            this.getArguments().putString(ColCuenta.NOMBRE.toString(), busqueda.trim());

            for (Cuenta cuenta : cuentaDAO.buscarCuentas(busqueda)) {
                Contrasenna contrasenna = contrasennaDAO.seleccionarUltimaPorCuenta(cuenta.getNombre());
                String fecha = "2000/01/01";
                if (contrasenna != null) {
                    fecha = contrasenna.getFecha();
                }
                salida.add(new CuentaConFecha(cuenta.getNombre(), cuenta.getDescripcion(), cuenta.getValidez(), fecha));
            }
            expListView.setVisibility(View.INVISIBLE);
        } else {
            expListView.setVisibility(View.VISIBLE);
        }

        return salida;
    }

    @Override
    public void procesarBoton(int boton) {
        switch(boton) {
            case AlertDialogContMenuEditarEliminar.BOTON_EDITAR:
                Log.i(TAG, String.format("procesarBoton(...) - Procesar Botón Editar - Cuenta %s.", nombreCuenta));
                FragAgregarEditarCuenta fragAgregarEditarCuenta = new FragAgregarEditarCuenta();
                Bundle args = new Bundle();
                args.putString(ColCuenta.NOMBRE.toString(), nombreCuenta);
                fragAgregarEditarCuenta.setArguments(args);
                actividadPrincipal().cambiarFragmento(fragAgregarEditarCuenta);
                break;
            case AlertDialogContMenuEditarEliminar.BOTON_ELIMINAR:
                Log.i(TAG, String.format("procesarBoton(...) - Procesar Botón Eliminar - Cuenta %s.", nombreCuenta));
                AlertDialogSiNoOk alertDialogSiNoOk = new AlertDialogSiNoOk();
                alertDialogSiNoOk.setTipo(AlertDialogSiNoOk.TIPO_SI_NO);
                alertDialogSiNoOk.setTitulo(getString(R.string.elimCuentaTitulo));
                alertDialogSiNoOk.setMensaje(getString(R.string.elimCuentaMensaje, nombreCuenta));
                alertDialogSiNoOk.setTargetFragment(this, 1);
                alertDialogSiNoOk.show(getFragmentManager(), TAG);
                break;
        }
    }

    @Override
    public void procesarBotonSiNoOk(int boton) {
        if (boton == AlertDialogSiNoOk.BOTON_SI) {
            Log.i(TAG, String.format("procesarBotonSiNoOk(...) - Procesar Boton Sí - Cuenta %s.", nombreCuenta));
            if (cuentaDAO.eliminarUna(nombreCuenta) > 0) {
                Log.i(TAG, "procesarBotonSiNoOk(...) - Cuenta seleccionada eliminada.");
                adapter.actualizarCategoriasCuentas(fillAccountsInfo());
                adapterBusq.actualizarCuentas(buscarCuentas());
                actividadPrincipal().actualizarBundles(Cuenta.class, nombreCuenta, null);
                CustomToast.Build(this, R.string.elimCuentaExitosa);
            } else {
                Log.e(TAG, "procesarBotonSiNoOk(...) - No se pudo eliminar la cuenta seleccionada.");
                CustomToast.Build(this, R.string.elimCuentaFallida);
            }
        }
    }
}
