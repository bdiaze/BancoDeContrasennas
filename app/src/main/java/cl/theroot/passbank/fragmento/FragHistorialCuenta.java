package cl.theroot.passbank.fragmento;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cl.theroot.passbank.CustomFragment;
import cl.theroot.passbank.CustomToast;
import cl.theroot.passbank.R;
import cl.theroot.passbank.adaptador.AdapContrasennas;
import cl.theroot.passbank.datos.ContrasennaDAO;
import cl.theroot.passbank.datos.nombres.ColCuenta;
import cl.theroot.passbank.dominio.Contrasenna;

public class FragHistorialCuenta extends CustomFragment implements AlertDialogSiNoOk.iProcesarBotonSiNoOk {
    private static final String TAG = "BdC-FragHistorialCuenta";

    private ContrasennaDAO contrasennaDAO;
    private AdapContrasennas adapter;

    @BindView(R.id.TV_titule)
    TextView TV_titule;
    @BindView(R.id.listView)
    ListView listView;

    private static final String KEY_STR_CNT_NOM = "KEY_STR_CNT_NOM";

    private String accountName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            accountName = savedInstanceState.getString(KEY_STR_CNT_NOM);
        }

        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragmento_historial_cuenta, container, false);
        ButterKnife.bind(this, view);

        contrasennaDAO = new ContrasennaDAO(getApplicationContext());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            accountName = bundle.getString(ColCuenta.NOMBRE.toString());
        }

        TV_titule.setText(getResources().getString(R.string.histDe, accountName));

        List<Contrasenna> contrasennas = contrasennaDAO.seleccionarPorCuenta(accountName);
        adapter = new AdapContrasennas(this, getContext(), contrasennas);
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(KEY_STR_CNT_NOM, accountName);
        super.onSaveInstanceState(outState);
    }

    //Creación del submenu del fragmento
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sub_menu_historial_cuenta, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //Creación de la funcionalidad del submenu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sub_menu_history_account_back:
                getActivity().onBackPressed();
                return true;

            case R.id.sub_menu_history_account_clear:
                AlertDialogSiNoOk alertDialogSiNoOk = new AlertDialogSiNoOk();
                alertDialogSiNoOk.setTipo(AlertDialogSiNoOk.TIPO_SI_NO);
                alertDialogSiNoOk.setTitulo(getString(R.string.elimHistTitulo));
                alertDialogSiNoOk.setMensaje(getString(R.string.elimHistMensaje, accountName));
                alertDialogSiNoOk.setTargetFragment(this, 1);
                alertDialogSiNoOk.show(getFragmentManager(), TAG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void procesarBotonSiNoOk(int boton) {
        int cantOriginal = adapter.getCount();
        if (contrasennaDAO.eliminarNoUltimasPorCuenta(accountName) > 0) {
            adapter.updatePasswordsList(contrasennaDAO.seleccionarPorCuenta(accountName));
            CustomToast.Build(this, R.string.elimHistExitosa);
        } else if (cantOriginal == 1) {
            CustomToast.Build(this, R.string.elimHistSinContr);
        } else {
            CustomToast.Build(this, R.string.elimHistFallida);
        }
    }
}
