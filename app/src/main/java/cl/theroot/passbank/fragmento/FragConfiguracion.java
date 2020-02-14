package cl.theroot.passbank.fragmento;


import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cl.theroot.passbank.ActividadPrincipal;
import cl.theroot.passbank.CustomFragment;
import cl.theroot.passbank.CustomToast;
import cl.theroot.passbank.R;
import cl.theroot.passbank.adaptador.AdapParametros;
import cl.theroot.passbank.datos.CategoriaDAO;
import cl.theroot.passbank.datos.ParametroDAO;
import cl.theroot.passbank.datos.nombres.NombreParametro;
import cl.theroot.passbank.dominio.Parametro;
import cl.theroot.passbank.dominio.ParametroSeleccionable;


public class FragConfiguracion extends CustomFragment {
    //private static final String TAG = "BdC-FragConfiguracion";
    private AdapParametros adapter;
    private List<ParametroSeleccionable> parametros;

    public static final String[] PARAMETROS_NUMERICOS = {
            NombreParametro.CANT_PALABRAS_GENERADOR.toString(),
            NombreParametro.CANT_CARACTERES_GENERADOR.toString(),
            NombreParametro.VALIDEZ_DEFECTO.toString()
    };

    public static final String[] PARAMETROS_NO_TRIMEABLES = {
            NombreParametro.SEPARADOR_GENERADOR.toString(),
            NombreParametro.COMPOSICION_GENERADOR.toString()
    };

    public static final String[] PARAMETROS_NO_REPETIBLES = {
            NombreParametro.COMPOSICION_GENERADOR.toString()
    };

    private boolean parametrosCambiados = false;

    private CategoriaDAO categoriaDAO;
    private ParametroDAO parametroDAO;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        parametroDAO = new ParametroDAO(getActivity().getApplicationContext());
        categoriaDAO = new CategoriaDAO(getActivity().getApplicationContext());

        View view = inflater.inflate(R.layout.fragmento_configuracion, container, false);

        ListView listView = view.findViewById(R.id.listConfiguration);

        parametros = new ArrayList<>();
        for (Parametro parametro : parametroDAO.seleccionarVisibles()) {
            parametros.add(new ParametroSeleccionable(parametro.getNombre(), parametro.getValor(), parametro.getPosicion(), false));
        }
        adapter = new AdapParametros(getActivity(), parametros, this);
        listView.setAdapter(adapter);

        return view;
    }

    //Creación del submenu del fragmento
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //Log.d(TAG, "onCreateOptionsMenu executed!");
        inflater.inflate(R.menu.sub_menu_configuracion, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        //Log.d(TAG, "onPrepareOptionsMenu executed!");
        menu.findItem(R.id.sub_menu_configuration_save).setEnabled(parametrosCambiados);
        super.onPrepareOptionsMenu(menu);
    }

    //Creación de la funcionalidad del submenu del fragmento
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!((ActividadPrincipal) getActivity()).isSesionIniciada()) {
            return false;
        }
        switch (item.getItemId()) {
            case R.id.sub_menu_configuration_change_master_key:
                return ((ActividadPrincipal) getActivity()).cambiarFragmento(new FragCambioLlaveMaestra());
            case R.id.sub_menu_configuration_export_database:
                return ((ActividadPrincipal) getActivity()).cambiarFragmento(new FragExportar());
            case R.id.sub_menu_configuration_save:
                if (!parametrosCambiados) {
                    CustomToast.Build(getActivity().getApplicationContext(), "¡Acción Cancelada!\nNo hay cambios que se deban almacenar.");
                    return true;
                }
                parametros = adapter.getParametros();
                String salida = parametrosValidos(parametros);
                if (salida == null) {
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                    alertDialog.setTitle("Configuración Actualizada");
                    alertDialog.setMessage("Los cambios ejecutados fueron guardados exitosamente.");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> dialog.dismiss());
                    alertDialog.show();
                    int titleDividerId = getResources().getIdentifier("titleDivider", "id", "android");
                    View titleDivider = alertDialog.findViewById(titleDividerId);
                    if (titleDivider != null) {
                        titleDivider.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.letraAtenuada, null));
                    }

                    for (Parametro parametro : parametros) {
                        parametroDAO.actualizarUna(parametro);
                    }

                    parametros = new ArrayList<>();
                    for (Parametro parametro : parametroDAO.seleccionarVisibles()) {
                        parametros.add(new ParametroSeleccionable(parametro.getNombre(), parametro.getValor(), parametro.getPosicion(), false));
                    }
                    adapter.updateParametros(parametros);
                    return true;
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                    alertDialog.setTitle("Error, Datos Inválidos");
                    alertDialog.setMessage(salida);
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", (dialog, which) -> dialog.dismiss());
                    alertDialog.show();
                    int titleDividerId = getResources().getIdentifier("titleDivider", "id", "android");
                    View titleDivider = alertDialog.findViewById(titleDividerId);
                    if (titleDivider != null) {
                        titleDivider.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.letraAtenuada, null));
                    }
                }
                return super.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String parametrosValidos(List<ParametroSeleccionable> parametros) {
        for (Parametro parametro : parametros){
            if (parametro.getValor() == null) {
                return parametro.getNombre() + "\nNo puede ser nulo.";
            }

            if (parametro.getValor().length() == 0) {
                return parametro.getNombre() + "\nNo puede estar vacío.";
            }

            if (!Arrays.asList(FragConfiguracion.PARAMETROS_NO_TRIMEABLES).contains(parametro.getNombre())) {
                if (parametro.getValor().trim().isEmpty()) {
                    return parametro.getNombre() + ":\nNo puede estar vacío.";
                }
            }

            if (Arrays.asList(PARAMETROS_NUMERICOS).contains(parametro.getNombre())) {
                try {
                    Integer.parseInt(parametro.getValor());
                } catch(NumberFormatException ex) {
                    return parametro.getNombre() + "\nDebe ser numérico.";
                }
            }

            if (parametro.getNombre().equals(NombreParametro.NOMBRE_CATEGORIA_COMPLETA.toString())) {
                if (categoriaDAO.seleccionarUna(parametro.getValor()) != null) {
                    return parametro.getNombre() + "\nDebe tener un nombre distinto al resto de categorías.";
                }
            }
        }
        return null;
    }


    public void habilitarCambios(Boolean estado) {
        if (parametrosCambiados != estado) {
            parametrosCambiados = estado;
            getActivity().invalidateOptionsMenu();
        }
    }

}
