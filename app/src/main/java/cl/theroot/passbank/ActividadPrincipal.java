package cl.theroot.passbank;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;

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
    private FragmentManager administradorFragmentos = getSupportFragmentManager();
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
    private List<Class> listaFragmentos = new ArrayList<>();
    private List<Bundle> listaBundles = new ArrayList<>();

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
                listaFragmentos = stringsToListaFragmentos(savedInstanceState.getStringArrayList(historialFragmentos));
            }
            if (savedInstanceState.getStringArrayList(historialBundles) != null) {
                listaBundles = stringsToListaBundles(savedInstanceState.getStringArrayList(historialBundles));
            }
            Class clase = stringToClass(savedInstanceState.getString(ultimoFragmento));
            Bundle bundle = stringToBundle(savedInstanceState.getString(ultimoBundle));
            try {
                Log.i(TAG, "onCreate(...) - Se recrea el fragmento que se estaba mostrando anteriormente.");
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStop() {
        Log.i(TAG, "onStop() - Se inicia etapa onStop del ciclo de vida de la aplicación.");
        onStop = Calendar.getInstance().getTimeInMillis();
        Log.i(TAG, String.format("onStop() - Se setea la variable onStop con el valor %d.", onStop));
        super.onStop();
        Log.i(TAG, "onStop() - Se termina la etapa onStop.");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.i(TAG, "onSaveInstanceState(...) - Se inicia etapa onSaveInstanceState del ciclo de vida de la aplicación.");

        savedInstanceState.putBoolean(estadoLogin, sesionIniciada);
        savedInstanceState.putString(llaveEncriptacion, llaveEncrip);
        savedInstanceState.putLong(onStopTime, onStop);
        Log.i(TAG, String.format("onSaveInstanceState(...) - Se guarda el estado de la variable sesionIniciada con valor %s.", sesionIniciada));
        Log.i(TAG, String.format("onSaveInstanceState(...) - Se guarda el estado de la variable onStop con valor %d: ", onStop));

        //Almacenar las listas de historiales
        savedInstanceState.putStringArrayList(historialFragmentos, new ArrayList<>(listaFragmentoToStrings(listaFragmentos)));
        savedInstanceState.putStringArrayList(historialBundles, new ArrayList<>(listaBundlesToStrings(listaBundles)));

        //Almacenar el fragmento actual
        savedInstanceState.putString(ultimoFragmento, fragmentoActual.getClass().getName());
        savedInstanceState.putString(ultimoBundle, bundleToString(fragmentoActual.getArguments()));

        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState(...) - Se termina etapa onSaveInstanceState.");
    }

    @Override
    public void onStart() {
        super.onStart();
        if (onStop != null) {
            // Se obtiene el tiempo transcurrido desde que no se visualiza la ventana...
            long tiempoActual = Calendar.getInstance().getTimeInMillis();
            long diff = Math.abs(tiempoActual - onStop);

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

            Log.i(TAG, String.format("onStart() - onStop: %d", onStop));
            Log.i(TAG, String.format("onStart() - tiempoActual: %d", tiempoActual));
            Log.i(TAG, String.format("onStart() - Tiempo transcurrido desde pérdida de enfoque: %f segundos.", diff / 1000f));
            Log.i(TAG, String.format("onStart() - Tiempo configurado para cierre de sesión: %f segundos.", waitTime / 1000f));

            // Si la diferencia es mayor a la esperada, se cierra sesión...
            if (diff > waitTime) {
                userLogOut();
            }

            onStop = null;
        }
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed() - Se inicia proceso para volver al fragmento anterior.");
        if (listaFragmentos.isEmpty()) {
            Log.i(TAG, "onBackPressed() - El historial está vacío por lo cuál se consulta si se desea cerrar la aplicación.");
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
            Log.i(TAG, String.format("onBackPressed() - Se determina la pantalla anterior. Fragmento anterior: %s - Bundle: %s.", clase, bundle));

            if (clase == FragInicioSesion.class) {
                Log.i(TAG, "onBackPressed() - Como la pantalla anterior es la de inicio de sesión, se consulta si desea cerrar sesión.");
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Cerrar Sesión");
                alertDialog.setMessage("Está a punto de cerrar sesión, ¿Desea continuar?");
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", (dialog, which) -> dialog.dismiss());
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SÍ", (dialog, which) -> {
                    userLogOut();
                    dialog.dismiss();
                });
                alertDialog.show();
            } else {
                Log.i(TAG, "onBackPressed() - Se elimina del historial el fragmento al que se cambiará, y se realiza el cambio.");
                listaFragmentos.remove(listaFragmentos.size() - 1);
                listaBundles.remove(listaBundles.size() - 1);
                try {
                    CustomFragment fragmento = (CustomFragment) clase.getConstructor().newInstance();
                    fragmento.setArguments(bundle);
                    cambiarFragmento(fragmento, false);
                } catch (Exception ex) {
                    Log.e(TAG, "onBackPressed() - Ocurrió un error al volver a la pantalla anterior.", ex);
                }
            }
        }
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

    public boolean cambiarFragmento(CustomFragment fragmento) {
        return cambiarFragmento(fragmento, true);
    }

    public boolean cambiarFragmento(CustomFragment fragmento, boolean registraHistorial) {
        Log.i(TAG, String.format("cambiarFragmento(...) - Se inicia el proceso para cambiar fragmentos - Siguiente Fragmento: %s - Bundle: %s.", fragmento.getClass(), fragmento.getArguments()));
        View view = findViewById(R.id.FL_contenedor);
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        // Se agrega el fragmento actual al listado histórico, o se limpia dicho listado si el siguiente fragmento es el de inicio de sesión...
        if (fragmento instanceof FragInicioSesion) {
            Log.i(TAG, "cambiarFragmento(...) - Se limpia el historial de fragmentos y bundles.");
            listaFragmentos.clear();
            listaBundles.clear();
        } else if (registraHistorial && fragmentoActual != null && (listaFragmentos.size() == 0 || fragmentoActual.getClass() != listaFragmentos.get(listaFragmentos.size() - 1))) {
            Log.i(TAG, String.format("cambiarFragmento(...) - Se agrega fragmento actual al historial - Fragmento: %s - Bundle: %s.",  fragmentoActual.getClass(), fragmentoActual.getArguments()));
            listaFragmentos.add(fragmentoActual.getClass());
            listaBundles.add(fragmentoActual.getArguments());
        }

        // Se cambia el fragmento actual por el siguiente...
        FragmentTransaction fragmentTransaction = administradorFragmentos.beginTransaction();
        fragmentTransaction.replace(R.id.FL_contenedor, fragmento);
        fragmentTransaction.commit();
        fragmentoActual = fragmento;

        Log.i(TAG, "cambiarFragmento(...) - Se termina el proceso para cambiar fragmentos.");
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
        Log.i(TAG, "userLogOut() - Se cierra la sesión del usuario.");
        sesionIniciada = false;
        llaveEncrip = null;
        if (menu != null) {
            menu.close();
        }
        ParametroDAO parametroDAO = new ParametroDAO(getApplicationContext());
        Parametro parametro = parametroDAO.seleccionarUno(NombreParametro.RESULTADO_HASH.toString());
        if (parametro != null && parametro.getValor().length() == 0) {
            cambiarFragmento(new FragCrearLlaveMaestra());
            return;
        }

        cambiarFragmento(new FragInicioSesion());
    }

    private List<String> listaFragmentoToStrings(List<Class> clases) {
        List<String> salida = new ArrayList<>();
        for (Class clase : clases) {
            salida.add(clase.getName());
        }
        return salida;
    }

    private List<Class> stringsToListaFragmentos(List<String> entrada) {
        List<Class> salida = new ArrayList<>();
        for (String nombreFrag : entrada) {
            salida.add(stringToClass(nombreFrag));
        }
        return salida;
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

    private List<String> listaBundlesToStrings(List<Bundle> bundles) {
        List<String> salida = new ArrayList<>();
        for (Bundle bundle : bundles) {
            salida.add(bundleToString(bundle));
        }
        return salida;
    }

    private List<Bundle> stringsToListaBundles(List<String> entrada) {
        List<Bundle> salida = new ArrayList<>();
        for (String parametros : entrada) {
            salida.add(stringToBundle(parametros));
        }
        return salida;
    }

    private String bundleToString(Bundle bundle) {
        if (bundle == null) {
            return null;
        }

        Gson gson = new Gson();
        return gson.toJson(bundle);
    }

    private Bundle stringToBundle(String datos) {
        if (datos == null) {
            return null;
        }

        Gson gson = new Gson();
        return gson.fromJson(datos, Bundle.class);
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
