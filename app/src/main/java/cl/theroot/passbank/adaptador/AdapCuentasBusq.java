package cl.theroot.passbank.adaptador;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import cl.theroot.passbank.R;
import cl.theroot.passbank.dominio.CuentaConFecha;

public class AdapCuentasBusq extends BaseAdapter {
    private static final String TAG = "BdC-AdapCuentasBusq";
    private LayoutInflater inflater;
    private List<CuentaConFecha> listaCuentas;

    public AdapCuentasBusq(Activity context, List<CuentaConFecha> listaCuentas) {
        this.inflater = LayoutInflater.from(context);
        this.listaCuentas = listaCuentas;
    }

    private class ViewHolder {
        public ImageView claveExpirada;
        public LinearLayout soporteTexto;
        public TextView nombreCuenta;
        public TextView descripCuenta;
    }

    @Override
    public int getCount() {
        return listaCuentas.size();
    }

    @Override
    public CuentaConFecha getItem(int position) {
        return listaCuentas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.lista_cuentas_hijos, null);
        }

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        if (viewHolder == null) {
            viewHolder = new ViewHolder();
            viewHolder.soporteTexto = convertView.findViewById(R.id.LL_soporteTexto);
            viewHolder.nombreCuenta = convertView.findViewById(R.id.TV_nombreCuenta);
            viewHolder.descripCuenta = convertView.findViewById(R.id.TV_descripcionCuenta);
            viewHolder.claveExpirada = convertView.findViewById(R.id.IV_claveExpirada);
            convertView.setTag(viewHolder);
        }

        viewHolder.nombreCuenta.setText(getItem(position).getNombre());
        if (getItem(position).getDescripcion() != null && !getItem(position).getDescripcion().isEmpty()) {
            viewHolder.descripCuenta.setVisibility(View.VISIBLE);
            viewHolder.descripCuenta.setText(getItem(position).getDescripcion());
            ViewGroup.LayoutParams params = viewHolder.nombreCuenta.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            viewHolder.nombreCuenta.setLayoutParams(params);
        } else {
            viewHolder.descripCuenta.setVisibility(View.INVISIBLE);
            ViewGroup.LayoutParams params = viewHolder.nombreCuenta.getLayoutParams();
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            viewHolder.nombreCuenta.setLayoutParams(params);
        }

        if (!getItem(position).expiro()) {
            viewHolder.claveExpirada.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.claveExpirada.setVisibility(View.VISIBLE);
        }

        return convertView;
    }
}
