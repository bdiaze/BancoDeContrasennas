package cl.theroot.passbank;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cl.theroot.passbank.datos.ContrasennaDAO;
import cl.theroot.passbank.datos.CuentaDAO;
import cl.theroot.passbank.datos.ParametroDAO;
import cl.theroot.passbank.datos.nombres.ColCategoria;
import cl.theroot.passbank.datos.nombres.ColCuenta;
import cl.theroot.passbank.datos.nombres.NombreParametro;
import cl.theroot.passbank.dominio.Categoria;
import cl.theroot.passbank.dominio.Contrasenna;
import cl.theroot.passbank.dominio.Cuenta;
import cl.theroot.passbank.dominio.CuentaConFecha;
import cl.theroot.passbank.dominio.Parametro;
import cl.theroot.passbank.dominio.TipoNotificacion;
import cl.theroot.passbank.fragmento.FragAcercaDe;
import cl.theroot.passbank.fragmento.FragAgregarEditarCategoria;
import cl.theroot.passbank.fragmento.FragAgregarEditarCuenta;
import cl.theroot.passbank.fragmento.FragCambioLlaveMaestra;
import cl.theroot.passbank.fragmento.FragCategorias;
import cl.theroot.passbank.fragmento.FragConfiguracion;
import cl.theroot.passbank.fragmento.FragCrearLlaveMaestra;
import cl.theroot.passbank.fragmento.FragCuentas;
import cl.theroot.passbank.fragmento.FragDetalleCategoria;
import cl.theroot.passbank.fragmento.FragDetalleCuenta;
import cl.theroot.passbank.fragmento.FragExportar;
import cl.theroot.passbank.fragmento.FragHistorialCuenta;
import cl.theroot.passbank.fragmento.FragInicioSesion;

public class ActividadPrincipal extends AppCompatActivity {
    private static final String TAG = "BdC-ActividadPrincipal";
    private FragmentManager administradorFragmentos = getFragmentManager();
    private CustomFragment fragmentoActual;

    private final String estadoLogin = "Sesión Iniciada";
    private final String llaveEncriptacion = "Llave Encriptación";
    private final String onStopTime = "Tiempo Stop";
    private final String historialFragmentos = "Historial Fragmentos";
    private final String historialBundles = "Historial Argumentos";
    private final String ultimoFragmento = "Último Fragmento";
    private final String ultimoBundle = "Último Bundle";

    private boolean sesionIniciada;
    private Long onStop;
    private String llaveEncrip = null;
    private final List<Class> listaFragmentos = new ArrayList<>();
    private final List<Bundle> listaBundles = new ArrayList<>();

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_principal);

        if (savedInstanceState != null) {
            sesionIniciada = savedInstanceState.getBoolean(estadoLogin);
            llaveEncrip = savedInstanceState.getString(llaveEncriptacion);
            onStop = savedInstanceState.getLong(onStopTime);
            if (savedInstanceState.getStringArrayList(historialFragmentos) != null) {
                stringsToListaFragmentos(savedInstanceState.getStringArrayList(historialFragmentos));
            }
            if (savedInstanceState.getStringArrayList(historialBundles) != null) {
                stringsToListaBundles(savedInstanceState.getStringArrayList(historialBundles));
            }
            Class clase = stringToClass(savedInstanceState.getString(ultimoFragmento));
            Bundle bundle = stringToBundle(savedInstanceState.getString(ultimoBundle));
            try {
                CustomFragment fragmento = (CustomFragment) clase.getConstructor().newInstance();
                fragmento.setArguments(bundle);
                fragmentoActual = fragmento;
            } catch (Exception ex) {
                Log.e(TAG, "onCreate(...) - Error al tratar de recrear el fragmento actual.", ex);
            }
        } else {
            userLogOut();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (onStop == null) {
            onStop = Calendar.getInstance().getTimeInMillis();
        }
        //Log.i(TAG, "onSaveInstanceState: ");
        //Log.i(TAG, "---" + estadoLogin + ": " + sesionIniciada);
        //Log.i(TAG, "---" + onStopTime + ": " + onStop);
        savedInstanceState.putBoolean(estadoLogin, sesionIniciada);
        savedInstanceState.putString(llaveEncriptacion, llaveEncrip);
        savedInstanceState.putLong(onStopTime, onStop);

        //Almacenar las listas de historiales
        savedInstanceState.putStringArrayList(historialFragmentos, listaFragmentoToStrings());
        savedInstanceState.putStringArrayList(historialBundles, listaBundlesToStrings());

        //Almacenar el fragmento actual
        savedInstanceState.putString(ultimoFragmento, classToString(fragmentoActual.getClass()));
        savedInstanceState.putString(ultimoBundle, bundleToString(fragmentoActual.getArguments()));

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*Fragment frg = getFragmentManager().findFragmentById(R.id.FL_contenedor);
        if (frg != null) {
            frg.onActivityResult(requestCode, resultCode, data);
        }*/
    }

    @Override
    public void onStop() {
        //Log.i(TAG, "onStop():");
        onStop = Calendar.getInstance().getTimeInMillis();
        //Log.i(TAG, "---onStop: " + onStop);
        super.onStop();
    }

    @Override
    public void onStart() {
        if (onStop != null) {
            long onStart = Calendar.getInstance().getTimeInMillis();
            long diff = Math.abs(onStart - onStop);
            onStop = null;

            // Se obtienen los segundos para cierre automático de sesión...
            ParametroDAO parametroDAO = new ParametroDAO(getApplicationContext());
            Parametro segundosCerrarSesion = parametroDAO.seleccionarUno(NombreParametro.SEGUNDOS_CERRAR_SESION);
            int waitTime;
            try {
                waitTime = Integer.parseInt(segundosCerrarSesion.getValor()) * 1000;
            } catch (Exception ex) {
                Log.e(TAG, "onCreate(...) - Error al convertir los segundos para cierre de sesión automático en numérico, se usará valor por defecto.", ex);
                waitTime = 30 * 1000;
            }

            Log.i(TAG, String.format("onStart() - Tiempo transcurrido desde pérdida de enfoque: %d segundos.", diff / 1000));
            Log.i(TAG, String.format("onStart() - Tiempo configurado para cierre de sesión: %d segundos.", waitTime / 1000));
            if (diff > waitTime) {
                userLogOut();
            }
        }
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        if (listaFragmentos.isEmpty()) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Cerrar la Aplicación");
            alertDialog.setMessage("Está a punto de cerrar la aplicación, ¿Desea continuar?");
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", (dialog, which) -> dialog.dismiss());
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SÍ", (dialog, which) -> {
                finish();
                dialog.dismiss();
            });
            alertDialog.show();
        } else {
            Class clase = listaFragmentos.get(listaFragmentos.size() - 1);
            Bundle bundle = listaBundles.get(listaBundles.size() - 1);
            Log.i(TAG, "onBackPressed() - clase: " + clase);
            Log.i(TAG, "onBackPressed() - bundle: " + bundle);

            if (clase == FragInicioSesion.class) {
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Cerrar Sesión");
                alertDialog.setMessage("Está a punto de cerrar sesión, ¿Desea continuar?");
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", (dialog, which) -> dialog.dismiss());
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SÍ", (dialog, which) -> {
                    userLogOut();
                    dialog.dismiss();
                });
                alertDialog.show();
                return;
            }
            listaFragmentos.remove(listaFragmentos.size() - 1);
            listaBundles.remove(listaBundles.size() - 1);
            try {
                CustomFragment fragmento = (CustomFragment) clase.getConstructor().newInstance();
                fragmento.setArguments(bundle);

                View view = findViewById(R.id.FL_contenedor);
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                FragmentTransaction fragmentTransaction = administradorFragmentos.beginTransaction();
                fragmentTransaction.replace(R.id.FL_contenedor, fragmento);
                fragmentTransaction.commit();
                fragmentoActual = fragmento;
                System.out.println(listaFragmentos);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        //super.onBackPressed();
    }

    //Creación del menú de la aplicación
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_actividad, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Cuando se prepare el menú de la aplicación
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Log.d(TAG, "onPrepareOptionsMenu executed!");
        if (!sesionIniciada) {
            menu.close();
            menu.findItem(R.id.menu).setVisible(false);
        } else {
            menu.findItem(R.id.menu).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    //Funcionalidad del menú principal de la aplicación
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Si la sesión no está iniciada, no se hace nada.
        if (!sesionIniciada) {
            return false;
        }
        switch (item.getItemId()) {
            case R.id.menu_accounts:
                return cambiarFragmento(new FragCuentas());

            case R.id.menu_categories:
                return cambiarFragmento(new FragCategorias());

            case R.id.menu_configuration:
                return cambiarFragmento(new FragConfiguracion());

            case R.id.menu_about:
                return cambiarFragmento(new FragAcercaDe());

            case R.id.menu_logout:
                userLogOut();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void actualizarBundleFragmentoActual(String llave, String valor) {
        if (fragmentoActual != null) {
            if (fragmentoActual.getArguments() == null) {
                fragmentoActual.setArguments(new Bundle());
            }
            fragmentoActual.getArguments().remove(llave);
            fragmentoActual.getArguments().putString(llave, valor);
        }
    }

    public boolean cambiarFragmento(CustomFragment fragmento) {
        View view = findViewById(R.id.FL_contenedor);
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        FragmentTransaction fragmentTransaction = administradorFragmentos.beginTransaction();
        fragmentTransaction.replace(R.id.FL_contenedor, fragmento);

        if (fragmentoActual != null) {
            if (listaFragmentos.size() == 0 || fragmentoActual.getClass() != listaFragmentos.get(listaFragmentos.size() - 1)) {
                Log.i(TAG, "cambiarFragmento(...) - Fragmento Anterior: " + fragmentoActual.getClass());
                Log.i(TAG, "cambiarFragmento(...) - Bundle Anterior: " + fragmentoActual.getArguments());
                listaFragmentos.add(fragmentoActual.getClass());
                listaBundles.add(fragmentoActual.getArguments());
            }
        }
        if (fragmento instanceof FragInicioSesion) {
            listaFragmentos.clear();
            listaBundles.clear();
        }

        fragmentTransaction.commit();
        fragmentoActual = fragmento;

        //Log.i(TAG, listaFragmentos.toString());
        return true;
    }

    public void actualizarBundles(Class clase, String antiguoValor, String nuevoValor) {
        List<Class> reqCatName = new ArrayList<>();
        reqCatName.add(FragAgregarEditarCategoria.class);
        reqCatName.add(FragDetalleCategoria.class);

        List<Class> reqCueName = new ArrayList<>();
        reqCueName.add(FragAgregarEditarCuenta.class);
        reqCueName.add(FragDetalleCuenta.class);
        reqCueName.add(FragHistorialCuenta.class);

        if (nuevoValor != null) {
            //Log.i(TAG, "Actualizando bundles...");
            for (int i = 0; i < listaFragmentos.size(); i++) {
                //Log.i(TAG, "Checkeando fragmento: " + listaFragmentos.get(i));
                if ((clase == Categoria.class && reqCatName.contains(listaFragmentos.get(i))) || (clase == Cuenta.class && reqCueName.contains(listaFragmentos.get(i)))) {
                    if (listaBundles.get(i) != null) {
                        if ((clase == Categoria.class && listaBundles.get(i).getString(ColCategoria.NOMBRE.toString()).equals(antiguoValor)) || (clase == Cuenta.class && listaBundles.get(i).getString(ColCuenta.NOMBRE.toString()).equals(antiguoValor))) {
                            //Log.i(TAG, "Cambiando bundle...");
                            if (clase == Categoria.class) {
                                listaBundles.get(i).remove(ColCategoria.NOMBRE.toString());
                                listaBundles.get(i).putString(ColCategoria.NOMBRE.toString(), nuevoValor);
                            } else if (clase == Cuenta.class) {
                                listaBundles.get(i).remove(ColCuenta.NOMBRE.toString());
                                listaBundles.get(i).putString(ColCuenta.NOMBRE.toString(), nuevoValor);
                            }
                        }
                    }
                }
            }
        } else {
            //Log.i(TAG, "Eliminando bundles...");
            List<Class> nuevaListaFragmentos = new ArrayList<>(listaFragmentos);
            List<Bundle> nuevaListaBundles = new ArrayList<>(listaBundles);
            //Log.i(TAG, "Limpiando lista de fragmentos...");
            listaFragmentos.clear();
            listaBundles.clear();
            for (int i = 0; i < nuevaListaFragmentos.size(); i++) {
                //Log.i(TAG, "Checkeando fragmento: " + nuevaListaFragmentos.get(i));
                if ((clase == Categoria.class && reqCatName.contains(nuevaListaFragmentos.get(i))) || (clase == Cuenta.class && reqCueName.contains(nuevaListaFragmentos.get(i)))) {
                    if (nuevaListaBundles.get(i) != null) {
                        if ((clase == Categoria.class && nuevaListaBundles.get(i).getString(ColCategoria.NOMBRE.toString()).equals(antiguoValor)) || (clase == Cuenta.class && nuevaListaBundles.get(i).getString(ColCuenta.NOMBRE.toString()).equals(antiguoValor))) {
                            //Log.i(TAG, "Descartando fragmento: " + nuevaListaFragmentos.get(i));
                        } else {
                            if (listaFragmentos.size() == 0 || nuevaListaFragmentos.get(i) != listaFragmentos.get(listaFragmentos.size() - 1)) {
                                //Log.i(TAG, "Volviendo a agregar fragmento: " + nuevaListaFragmentos.get(i));
                                listaFragmentos.add(nuevaListaFragmentos.get(i));
                                listaBundles.add(nuevaListaBundles.get(i));
                            }
                        }
                    } else {
                        if (listaFragmentos.size() == 0 || nuevaListaFragmentos.get(i) != listaFragmentos.get(listaFragmentos.size() - 1)) {
                           //Log.i(TAG, "Volviendo a agregar fragmento: " + nuevaListaFragmentos.get(i));
                            listaFragmentos.add(nuevaListaFragmentos.get(i));
                            listaBundles.add(nuevaListaBundles.get(i));
                        }
                    }
                } else {
                    if (listaFragmentos.size() == 0 || nuevaListaFragmentos.get(i) != listaFragmentos.get(listaFragmentos.size() - 1)) {
                        //Log.i(TAG, "Volviendo a agregar fragmento: " + nuevaListaFragmentos.get(i));
                        listaFragmentos.add(nuevaListaFragmentos.get(i));
                        listaBundles.add(nuevaListaBundles.get(i));
                    }
                }
            }
        }
    }

    public void userLogIn(String llaveEncrip) {
        //Log.i(TAG, "El usuario ha iniciado sesión");
        sesionIniciada = true;
        this.llaveEncrip = llaveEncrip;
        //Log.i(TAG, "Llave Encriptación: " + llaveEncrip);
        cambiarFragmento(new FragCuentas());
        //invalidateOptionsMenu();
        informarCuentaVencida();
    }

    public void informarCuentaVencida() {
        CuentaDAO cuentaDAO = new CuentaDAO(getApplicationContext());
        ContrasennaDAO contrasennaDAO = new ContrasennaDAO(getApplicationContext());
        CuentaConFecha cuentaAInformar = null;
        int cantCaducada = 0;
        for (Cuenta cuenta : cuentaDAO.seleccionarTodas()) {
            Contrasenna contrasenna = contrasennaDAO.seleccionarUltimaPorCuenta(cuenta.getNombre());
            CuentaConFecha cuentaConFecha = new CuentaConFecha(cuenta.getNombre(), cuenta.getDescripcion(), cuenta.getValidez(), contrasenna.getFecha());
            cuentaConFecha.setVencInf(cuenta.getVencInf());
            if (cuentaConFecha.expiro()) {
                cantCaducada++;
                if (cuentaConFecha.getVencInf() == 0) {
                    if (cuentaAInformar == null || cuentaAInformar.tiempoVencido() < cuentaConFecha.tiempoVencido()) {
                        cuentaAInformar = cuentaConFecha;
                    }
                }
            }
        }

        if (cuentaAInformar != null) {
            String titulo = getResources().getString(R.string.contrasennaVencida);
            String descripcion;
            if (cantCaducada == 1) {
                descripcion = getResources().getString(R.string.detalleContrasennaVencida, cuentaAInformar.getNombre());
            } else {
                descripcion = getResources().getString(R.string.detalleContrasennaVencidaConOtras, cuentaAInformar.getNombre(), String.valueOf(cantCaducada - 1));
            }
            Notificacion.Mostrar(getApplicationContext(), TipoNotificacion.GENERAL, titulo, descripcion);
            cuentaAInformar.setVencInf(1);
            if (cuentaDAO.actualizarUna(cuentaAInformar.getNombre(), cuentaAInformar) != 1) {
                CustomToast.Build(this, "Error al actualizar estado de informe de caducidad de cuenta.");
            }
        }
    }

    public void userLogOut() {
        sesionIniciada = false;
        llaveEncrip = null;
        if (menu != null) {
            menu.close();
        }
        ParametroDAO parametroDAO = new ParametroDAO(getApplicationContext());
        Parametro parametro = parametroDAO.seleccionarUno(NombreParametro.RESULTADO_HASH.toString());
        if (parametro != null) {
            if ("".equals(parametro.getValor())) {
                cambiarFragmento(new FragCrearLlaveMaestra());
                return;
            }
        }

        cambiarFragmento(new FragInicioSesion());
    }

    //Se transforma el historial de fragmentos en una lista de strings
    private ArrayList<String> listaFragmentoToStrings() {
        ArrayList<String> resultado = new ArrayList<>();
        for (Class clase : listaFragmentos) {
            resultado.add(classToString(clase));
        }
        return resultado;
    }

    private String classToString(Class clase) {
        return clase.getName();
    }

    //Se transforma el historial de bundles en una lista de strings
    private ArrayList<String> listaBundlesToStrings() {
        ArrayList<String> resultado = new ArrayList<>();
        for (Bundle bundle : listaBundles) {
            resultado.add(bundleToString(bundle));
        }
        return resultado;
    }

    private String bundleToString(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        if (bundle.getString(ColCategoria.NOMBRE.toString()) != null) {
            return Categoria.class.getName() + ":" + bundle.getString(ColCategoria.NOMBRE.toString());
        }
        if (bundle.getString(ColCuenta.NOMBRE.toString()) != null) {
            return Cuenta.class.getName() + ":" + bundle.getString(ColCuenta.NOMBRE.toString());
        }
        return null;
    }

    //Se carga la lista de strings en el historial de fragmentos
    private void stringsToListaFragmentos(ArrayList<String> entrada) {
        //Log.i(TAG, entrada.toString());
        listaFragmentos.clear();
        for (String nombreFrag : entrada) {
            listaFragmentos.add(stringToClass(nombreFrag));
        }
    }

    private Class stringToClass(String nombre) {
        if (FragAcercaDe.class.getName().equals(nombre)) {
            return FragAcercaDe.class;
        }
        if (FragAgregarEditarCategoria.class.getName().equals(nombre)) {
            return FragAgregarEditarCategoria.class;
        }
        if (FragAgregarEditarCuenta.class.getName().equals(nombre)) {
            return FragAgregarEditarCuenta.class;
        }
        if (FragCambioLlaveMaestra.class.getName().equals(nombre)) {
            return FragCambioLlaveMaestra.class;
        }
        if (FragCategorias.class.getName().equals(nombre)) {
            return FragCategorias.class;
        }
        if (FragConfiguracion.class.getName().equals(nombre)) {
            return FragConfiguracion.class;
        }
        if (FragCrearLlaveMaestra.class.getName().equals(nombre)) {
            return FragCrearLlaveMaestra.class;
        }
        if (FragCuentas.class.getName().equals(nombre)) {
            return FragCuentas.class;
        }
        if (FragDetalleCategoria.class.getName().equals(nombre)) {
            return FragDetalleCategoria.class;
        }
        if (FragDetalleCuenta.class.getName().equals(nombre)) {
            return FragDetalleCuenta.class;
        }
        if (FragExportar.class.getName().equals(nombre)) {
            return FragExportar.class;
        }
        if (FragHistorialCuenta.class.getName().equals(nombre)) {
            return FragHistorialCuenta.class;
        }
        if (FragInicioSesion.class.getName().equals(nombre)) {
            return FragInicioSesion.class;
        }
        return null;
    }

    //Se carga la lista de strings en el historial de bundles
    private void stringsToListaBundles(ArrayList<String> entrada) {
        //Log.i(TAG, entrada.toString());
        listaBundles.clear();
        for (String parametros : entrada) {
            listaBundles.add(stringToBundle(parametros));
        }
    }

    private Bundle stringToBundle(String datos) {
        if (datos == null) {
            return null;
        }
        int posSeparador = datos.indexOf(":");
        String tipo = datos.substring(0, posSeparador);
        String valor = datos.substring(posSeparador + 1);
        if (Categoria.class.getName().equals(tipo)) {
            Bundle nuevo = new Bundle();
            nuevo.putString(ColCategoria.NOMBRE.toString(), valor);
            return nuevo;
        }
        if (Cuenta.class.getName().equals(tipo)) {
            Bundle nuevo = new Bundle();
            nuevo.putString(ColCuenta.NOMBRE.toString(), valor);
            return nuevo;
        }
        return null;
    }

    public boolean isSesionIniciada(){
        return sesionIniciada;
    }

    public String getLlaveEncrip() {
        return llaveEncrip;
    }

    public void setLlaveEncrip(String llaveEncrip) {
        this.llaveEncrip = llaveEncrip;
    }
}
