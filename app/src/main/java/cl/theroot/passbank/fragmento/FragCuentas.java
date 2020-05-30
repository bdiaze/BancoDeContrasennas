package cl.theroot.passbank.fragmento;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.jakewharton.rxbinding3.widget.RxTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import cl.theroot.passbank.ActividadPrincipal;
import cl.theroot.passbank.CustomFragment;
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

public class FragCuentas extends CustomFragment {
    private static final String TAG = "BdC-FragCuentas";
    private AdapCategoriasCuentas adapter;
    private AdapCuentasBusq adapterBusq;

    private CuentaDAO cuentaDAO;
    private CategoriaDAO categoriaDAO;
    private CategoriaCuentaDAO categoriaCuentaDAO;
    private ContrasennaDAO contrasennaDAO;

    private List<CategoriaListaCuentas> listaCategorias = new ArrayList<>();
    private List<CuentaConFecha> resultadosBusqueda = new ArrayList<>();
    private CompositeDisposable disposable;

    @BindView(R.id.expListView)
    ExpandableListView expListView;
    @BindView(R.id.buscarEditText)
    EditText buscarET;
    @BindView(R.id.resultBusqListView)
    ListView resultBusqListView;

    public FragCuentas() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragmento_cuentas, container, false);
        ButterKnife.bind(this, view);

        cuentaDAO = new CuentaDAO(getActivity().getApplicationContext());
        categoriaDAO = new CategoriaDAO(getActivity().getApplicationContext());
        categoriaCuentaDAO = new CategoriaCuentaDAO(getActivity().getApplicationContext());
        contrasennaDAO = new ContrasennaDAO(getActivity().getApplicationContext());

        fillAccountsInfo();

        // Se obtiene última búsqueda de cuentas para carga automática...
        if (this.getArguments() != null) {
            String cuentaBuscada = this.getArguments().getString(ColCuenta.NOMBRE.toString());
            if (cuentaBuscada != null && cuentaBuscada.trim().length() > 0) {
                buscarET.setText(cuentaBuscada);
                buscarCuentas();
            }
        }

        adapter = new AdapCategoriasCuentas(getActivity(), listaCategorias);
        adapterBusq = new AdapCuentasBusq(getActivity(), resultadosBusqueda);

        expListView.setAdapter(adapter);
        resultBusqListView.setAdapter(adapterBusq);
        registerForContextMenu(expListView);
        registerForContextMenu(resultBusqListView);

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
                        buscarCuentas(charSequence.toString());
                        adapterBusq.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

        expListView.setOnGroupClickListener((expandableListView, view13, i, l) -> false);

        expListView.setOnGroupExpandListener(i -> {

        });

        expListView.setOnGroupCollapseListener(i -> {

        });

        expListView.setOnChildClickListener((expandableListView, view12, i, i1, l) -> {
            Cuenta cuenta = adapter.getChild(i, i1);
            FragDetalleCuenta fragDetalleCuenta = new FragDetalleCuenta();
            Bundle bundle = new Bundle();
            bundle.putString(ColCuenta.NOMBRE.toString(), cuenta.getNombre());
            fragDetalleCuenta.setArguments(bundle);
            return ((ActividadPrincipal) getActivity()).cambiarFragmento(fragDetalleCuenta);
        });

        resultBusqListView.setOnItemClickListener((parent, view1, position, id) -> {
            Cuenta cuenta = adapterBusq.getItem(position);
            FragDetalleCuenta fragDetalleCuenta = new FragDetalleCuenta();
            Bundle bundle = new Bundle();
            bundle.putString(ColCuenta.NOMBRE.toString(), cuenta.getNombre());
            fragDetalleCuenta.setArguments(bundle);
            ((ActividadPrincipal) getActivity()).cambiarFragmento(fragDetalleCuenta);
        });

        return view;
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
        if (!((ActividadPrincipal) getActivity()).isSesionIniciada()) {
            return false;
        }
        if (item.getItemId() == R.id.sub_menu_accounts_add) {
            return ((ActividadPrincipal) getActivity()).cambiarFragmento(new FragAgregarEditarCuenta());
        }
        return super.onOptionsItemSelected(item);
    }


    //Creación del menu contextual para click prolongado en una Account
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        String nombreCuenta = "";
        if (v.getId() == R.id.resultBusqListView) {
            ListView.AdapterContextMenuInfo info = (ListView.AdapterContextMenuInfo) menuInfo;
            nombreCuenta = adapterBusq.getItem(info.position).getNombre();
        } else if (v.getId() == R.id.expListView) {
            ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
            int type = ExpandableListView.getPackedPositionType(info.packedPosition);
            if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                int ig = ExpandableListView.getPackedPositionGroup(info.packedPosition);
                int ic = ExpandableListView.getPackedPositionChild(info.packedPosition);
                nombreCuenta = adapter.getChild(ig, ic).getNombre();
            }
        }

        if (nombreCuenta.length() > 0) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.cont_menu_cuentas, menu);

            MenuItem miEdt = menu.findItem(R.id.cont_menu_accounts_edit);
            MenuItem miDel = menu.findItem(R.id.cont_menu_accounts_delete);
            Intent i = new Intent();
            i.putExtra(ColCuenta.NOMBRE.toString(), nombreCuenta);
            miEdt.setIntent(i);
            miDel.setIntent(i);
        }
    }

    //Creación de la funcionalidad del menu contextual
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!((ActividadPrincipal) getActivity()).isSesionIniciada()) {
            return false;
        }

        Intent i = item.getIntent();
        if (i != null) {
            Bundle b = i.getExtras();
            if (b != null) {
                final String nombreCuenta = b.getString(ColCuenta.NOMBRE.toString());

                if (nombreCuenta == null || nombreCuenta.isEmpty()) {
                    return false;
                }

                switch (item.getItemId()) {
                    case R.id.cont_menu_accounts_edit:
                        FragAgregarEditarCuenta fragAgregarEditarCuenta = new FragAgregarEditarCuenta();
                        Bundle args = new Bundle();
                        args.putString(ColCuenta.NOMBRE.toString(), nombreCuenta);
                        fragAgregarEditarCuenta.setArguments(args);
                        ((ActividadPrincipal) getActivity()).cambiarFragmento(fragAgregarEditarCuenta);
                        return true;

                    case R.id.cont_menu_accounts_delete:
                        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                        alertDialog.setTitle("Eliminación de Cuenta");
                        alertDialog.setMessage("¿Está seguro que desea eliminar la cuenta " + nombreCuenta + "?");
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", (dialog, which) -> dialog.dismiss());
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SÍ", (dialog, which) -> {
                            if (cuentaDAO.eliminarUna(nombreCuenta) > 0) {
                                fillAccountsInfo();
                                buscarCuentas();
                                adapter.notifyDataSetChanged();
                                adapterBusq.notifyDataSetChanged();
                                ((ActividadPrincipal) getActivity()).actualizarBundles(Cuenta.class, nombreCuenta, null);
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

        return false;
    }

    //Llenado de los datos a mostrar
    private void fillAccountsInfo() {
        listaCategorias.clear();
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
            listaCategorias.add(new CategoriaListaCuentas(categoria.getNombre(), categoria.getPosicion(), cuentas));
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
        listaCategorias.add(new CategoriaListaCuentas("", null, cuentas));
    }

    private void buscarCuentas() {
        buscarCuentas(buscarET.getText().toString());
    }

    private void buscarCuentas(String busqueda) {
        resultadosBusqueda.clear();
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
                resultadosBusqueda.add(new CuentaConFecha(cuenta.getNombre(), cuenta.getDescripcion(), cuenta.getValidez(), fecha));
            }
            expListView.setVisibility(View.INVISIBLE);
        } else {
            expListView.setVisibility(View.VISIBLE);
        }
    }
}
