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
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.util.List;

import cl.theroot.passbank.ActividadPrincipal;
import cl.theroot.passbank.CustomFragment;
import cl.theroot.passbank.CustomToast;
import cl.theroot.passbank.R;
import cl.theroot.passbank.adaptador.AdapContrasennas;
import cl.theroot.passbank.datos.ContrasennaDAO;
import cl.theroot.passbank.datos.nombres.ColCuenta;
import cl.theroot.passbank.dominio.Contrasenna;

public class FragHistorialCuenta extends CustomFragment {
    private AdapContrasennas adapter;

    private List<Contrasenna> listItems;

    private String accountName;
    private ContrasennaDAO contrasennaDAO;

    public FragHistorialCuenta() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        contrasennaDAO = new ContrasennaDAO(getActivity().getApplicationContext());

        View view = inflater.inflate(R.layout.fragmento_historial_cuenta, container, false);
        TextView TV_titule = view.findViewById(R.id.TV_titule);
        ListView listView = view.findViewById(R.id.listView);

        accountName = null;
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            accountName = bundle.getString(ColCuenta.NOMBRE.toString());
            TV_titule.setText(getResources().getString(R.string.histDe, accountName));
        }

        listItems = contrasennaDAO.seleccionarPorCuenta(accountName);

        adapter = new AdapContrasennas(this, getActivity(), listItems);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((adapterView, view1, i, l) -> {

        });
        getActivity().invalidateOptionsMenu();
        return view;
    }

    //Creación del submenu del fragmento
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sub_menu_historial_cuenta, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        if (adapter != null) {
            if (adapter.getCount() > 1) {
                menu.findItem(R.id.sub_menu_history_account_clear).setEnabled(true);
            } else {
                menu.findItem(R.id.sub_menu_history_account_clear).setEnabled(false);
            }
        }
        super.onPrepareOptionsMenu(menu);
    }

    //Creación de la funcionalidad del submenu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!((ActividadPrincipal) getActivity()).isSesionIniciada()) {
            return false;
        }
        switch (item.getItemId()) {
            case R.id.sub_menu_history_account_back:
                getActivity().onBackPressed();
                return true;

            case R.id.sub_menu_history_account_clear:
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("Eliminación del Historial de Cuenta");
                alertDialog.setMessage("¿Está seguro que desea eliminar el historial de la cuenta " + accountName + "? Todas las contraseñas, excepto la última, serán removidas permanentemente.");
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", (dialog, which) -> dialog.dismiss());
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SÍ", (dialog, which) -> {
                    if (contrasennaDAO.eliminarNoUltimasPorCuenta(accountName) > 0) {
                        listItems = contrasennaDAO.seleccionarPorCuenta(accountName);
                        adapter.updatePasswordsList(listItems);
                        CustomToast.Build(getActivity().getApplicationContext(), "Su historial de contraseñas fue limpiado exitosamente.");

                    }
                    getActivity().invalidateOptionsMenu();
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
                return super.onOptionsItemSelected(item);
        }
    }
}
