package cl.theroot.passbank.adaptador;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import cl.theroot.passbank.Cifrador;
import cl.theroot.passbank.CustomFragment;
import cl.theroot.passbank.R;
import cl.theroot.passbank.dominio.Contrasenna;

public class AdapContrasennas extends BaseAdapter{
    private LayoutInflater inflater;
    private List<Contrasenna> passwords;
    private CustomFragment customFragment;

    public AdapContrasennas(CustomFragment customFragment, @NonNull Context context, @NonNull List<Contrasenna> passwords) {
        this.customFragment = customFragment;
        this.inflater = LayoutInflater.from(context);
        updatePasswordsList(passwords);
    }

    private class ViewHolder {
        TextView passwordValue;
        TextView passwordDate;
    }

    @Override
    public int getCount() {
        return passwords.size();
    }

    @Override
    public Contrasenna getItem(int position) {
        return passwords.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.lista_historial_cuenta, viewGroup, false);

        }

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (viewHolder == null) {
            viewHolder = new ViewHolder();
            viewHolder.passwordValue = view.findViewById(R.id.TV_valor_contrasenna);
            viewHolder.passwordDate = view.findViewById(R.id.TV_fecha_contrasenna);
            view.setTag(viewHolder);
        }

        viewHolder.passwordValue.setText(Cifrador.desencriptar(getItem(i).getValor(), customFragment.actividadPrincipal().getLlaveEncrip()));

        StringBuilder fechaFormateada = null;
        String[] elementosFecha = getItem(i).getFecha().split("/");
        for (String elemento : elementosFecha) {
            if (fechaFormateada == null) {
                fechaFormateada = new StringBuilder(elemento);
            } else {
                fechaFormateada.insert(0, elemento + "/");
            }
        }

        viewHolder.passwordDate.setText(fechaFormateada != null ? fechaFormateada.toString() : "");

        return view;
    }

    public void updatePasswordsList(List<Contrasenna> passwords) {
        this.passwords = passwords;
        this.notifyDataSetChanged();
    }
}
