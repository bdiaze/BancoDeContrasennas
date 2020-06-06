package cl.theroot.passbank.adaptador;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import cl.theroot.passbank.CustomFragment;
import cl.theroot.passbank.R;
import cl.theroot.passbank.datos.ParametroDAO;
import cl.theroot.passbank.datos.nombres.NombreParametro;
import cl.theroot.passbank.dominio.CategoriaListaCuentas;
import cl.theroot.passbank.dominio.CuentaConFecha;
import cl.theroot.passbank.dominio.Parametro;
import cl.theroot.passbank.fragmento.AlertDialogSiNoOk;


public class AdapCategoriasCuentas extends BaseExpandableListAdapter {
    private static final String TAG = "BdC-AdapCatCuentas";
    private LayoutInflater inflater;
    private CustomFragment fragment;
    private List<CategoriaListaCuentas> listaCategorias;
    private ParametroDAO parametroDAO;

    public AdapCategoriasCuentas(Activity context, CustomFragment fragment, List<CategoriaListaCuentas> listaCategorias) {
        this.inflater = LayoutInflater.from(context);
        this.fragment = fragment;
        this.listaCategorias = listaCategorias;
        parametroDAO = new ParametroDAO(context.getApplicationContext());
    }

    private class GroupViewHolder {
        public TextView CategoryName;
    }

    private class ChildViewHolder {
        public ImageView claveExpirada;
        public LinearLayout soporteTexto;
        public TextView accountName;
        public TextView accountDescription;
    }

    @Override
    public int getGroupCount() {
        return listaCategorias.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return listaCategorias.get(i).getCuentas().size();
    }

    @Override
    public CategoriaListaCuentas getGroup(int i) {
        return listaCategorias.get(i);
    }

    @Override
    public CuentaConFecha getChild(int i, int i1) {
        return listaCategorias.get(i).getCuentas().get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.lista_cuentas_grupos, null);
        }

        GroupViewHolder groupViewHolder = (GroupViewHolder) view.getTag();
        if (groupViewHolder == null) {
            groupViewHolder = new GroupViewHolder();
            groupViewHolder.CategoryName = view.findViewById(R.id.TV_nombreCategoria);
            view.setTag(groupViewHolder);
        }

        if (getGroup(i).getNombre().length() > 0) {
            groupViewHolder.CategoryName.setText(getGroup(i).getNombre());
        } else {
            Parametro parametro = parametroDAO.seleccionarUno(NombreParametro.NOMBRE_CATEGORIA_COMPLETA.toString());
            if (parametro != null) {
                groupViewHolder.CategoryName.setText(parametro.getValor());
            }
        }
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.lista_cuentas_hijos, null);
        }

        ChildViewHolder childViewHolder = (ChildViewHolder) view.getTag();
        if (childViewHolder == null) {
            childViewHolder = new ChildViewHolder();
            childViewHolder.soporteTexto = view.findViewById(R.id.LL_soporteTexto);
            childViewHolder.accountName = view.findViewById(R.id.TV_nombreCuenta);
            childViewHolder.accountDescription = view.findViewById(R.id.TV_descripcionCuenta);
            childViewHolder.claveExpirada = view.findViewById(R.id.IV_claveExpirada);
            view.setTag(childViewHolder);
        }

        childViewHolder.accountName.setText(getChild(i, i1).getNombre());
        if (getChild(i ,i1).getDescripcion() != null && !getChild(i ,i1).getDescripcion().isEmpty()) {
            childViewHolder.accountDescription.setVisibility(View.VISIBLE);
            childViewHolder.accountDescription.setText(getChild(i, i1).getDescripcion());
            ViewGroup.LayoutParams params = childViewHolder.accountName.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            childViewHolder.accountName.setLayoutParams(params);
        } else {
            childViewHolder.accountDescription.setVisibility(View.INVISIBLE);
            ViewGroup.LayoutParams params = childViewHolder.accountName.getLayoutParams();
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            childViewHolder.accountName.setLayoutParams(params);
        }

        if (!getChild(i, i1).expiro()) {
            childViewHolder.claveExpirada.setVisibility(View.INVISIBLE);
        } else {
            childViewHolder.claveExpirada.setVisibility(View.VISIBLE);
            childViewHolder.claveExpirada.setOnClickListener(v -> {
                AlertDialogSiNoOk dialogSiNoOk = new AlertDialogSiNoOk();
                dialogSiNoOk.setTitulo(fragment.getString(R.string.infoContVencTitulo));
                dialogSiNoOk.setMensaje(fragment.getString(R.string.infoContVencMensaje));
                dialogSiNoOk.setTargetFragment(fragment, 1);
                dialogSiNoOk.show(fragment.getFragmentManager(), TAG);
            });
        }
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    public void actualizarCategoriasCuentas(List<CategoriaListaCuentas> listaCategorias) {
        this.listaCategorias = listaCategorias;
        notifyDataSetChanged();
    }
}
