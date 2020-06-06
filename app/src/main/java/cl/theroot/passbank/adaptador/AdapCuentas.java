package cl.theroot.passbank.adaptador;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import cl.theroot.passbank.R;
import cl.theroot.passbank.datos.CategoriaCuentaDAO;
import cl.theroot.passbank.dominio.CategoriaCuenta;
import cl.theroot.passbank.dominio.Cuenta;

/**
 * Created by Benjamin on 01/10/2017.
 */

public class AdapCuentas extends BaseAdapter{
    private LayoutInflater inflater;
    private List<Cuenta> cuentas;
    private boolean ocultarFlechas = true;
    private String nombreCategoria;
    private Context appContext;

    public AdapCuentas(@NonNull Context context, @NonNull List<Cuenta> cuentas, String nombreCategoria) {
        this.inflater = LayoutInflater.from(context);
        appContext = context.getApplicationContext();
        this.nombreCategoria = nombreCategoria;
        updateCuentas(cuentas);
    }

    private class ViewHolder {
        public ImageView bajarElemento;
        public ImageView subirElemento;
        public TextView nombreCuenta;
        public Integer referencia;
    }

    @Override
    public int getCount() {
        return cuentas.size();
    }

    @Override
    public Cuenta getItem(int i) {
        return cuentas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.lista_categorias, null);
        }

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (viewHolder == null) {
            viewHolder = new ViewHolder();
            viewHolder.nombreCuenta = view.findViewById(R.id.TV_nombre);
            viewHolder.bajarElemento = view.findViewById(R.id.IB_bajar_elemento);
            viewHolder.subirElemento = view.findViewById(R.id.IB_subir_elemento);
            view.setTag(viewHolder);
        }

        viewHolder.referencia = i;
        viewHolder.nombreCuenta.setText(getItem(i).getNombre());
        if (ocultarFlechas) {
            viewHolder.bajarElemento.setVisibility(View.GONE);
            viewHolder.subirElemento.setVisibility(View.GONE);
        } else {
            viewHolder.bajarElemento.setVisibility(View.VISIBLE);
            viewHolder.subirElemento.setVisibility(View.VISIBLE);
            if (i <= 0) {
                viewHolder.subirElemento.setVisibility(View.INVISIBLE);
            }
            if (i >= getCount() - 1) {
                viewHolder.bajarElemento.setVisibility(View.INVISIBLE);
            }
        }

        final ViewHolder finalViewHolder = viewHolder;
        viewHolder.bajarElemento.setOnClickListener(v -> {
            int i1 = finalViewHolder.referencia;
            if (i1 < cuentas.size() - 1) {
                Cuenta buff = cuentas.get(i1);
                cuentas.set(i1, cuentas.get(i1 + 1));
                cuentas.set(i1 + 1, buff);
                notifyDataSetChanged();

                CategoriaCuentaDAO categoriaCuentaDAO = new CategoriaCuentaDAO(appContext);
                String nombreCuenta = cuentas.get(i1).getNombre();
                CategoriaCuenta nuevaPosCatCuenta = categoriaCuentaDAO.seleccionarUna(nombreCategoria, nombreCuenta);
                nuevaPosCatCuenta.setPosicion(i1 + 1);
                categoriaCuentaDAO.actualizarUna(nuevaPosCatCuenta);

                nombreCuenta = cuentas.get(i1 + 1).getNombre();
                nuevaPosCatCuenta = categoriaCuentaDAO.seleccionarUna(nombreCategoria, nombreCuenta);
                nuevaPosCatCuenta.setPosicion(i1 + 2);
                categoriaCuentaDAO.actualizarUna(nuevaPosCatCuenta);
            }
        });

        viewHolder.subirElemento.setOnClickListener(v -> {
            int i12 = finalViewHolder.referencia;
            if (i12 > 0) {
                Cuenta buff = cuentas.get(i12);
                cuentas.set(i12, cuentas.get(i12 - 1));
                cuentas.set(i12 - 1, buff);
                notifyDataSetChanged();

                CategoriaCuentaDAO categoriaCuentaDAO = new CategoriaCuentaDAO(appContext);
                String nombreCuenta = cuentas.get(i12).getNombre();
                CategoriaCuenta nuevaPosCatCuenta = categoriaCuentaDAO.seleccionarUna(nombreCategoria, nombreCuenta);
                nuevaPosCatCuenta.setPosicion(i12 + 1);
                categoriaCuentaDAO.actualizarUna(nuevaPosCatCuenta);

                nombreCuenta = cuentas.get(i12 - 1).getNombre();
                nuevaPosCatCuenta = categoriaCuentaDAO.seleccionarUna(nombreCategoria, nombreCuenta);
                nuevaPosCatCuenta.setPosicion(i12);
                categoriaCuentaDAO.actualizarUna(nuevaPosCatCuenta);
            }
        });

        return view;
    }

    public void updateCuentas(List<Cuenta> cuentas) {
        this.cuentas = cuentas;
        notifyDataSetChanged();
    }

    public void setOcultarFlechas(boolean estado) {
        ocultarFlechas = estado;
        notifyDataSetChanged();
    }
}
