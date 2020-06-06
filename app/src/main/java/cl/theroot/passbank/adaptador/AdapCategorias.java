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
import cl.theroot.passbank.datos.CategoriaDAO;
import cl.theroot.passbank.dominio.Categoria;

public class AdapCategorias extends BaseAdapter{
    private LayoutInflater inflater;
    private List<Categoria> categories;
    private boolean ocultarFlechas = true;
    private Context appContext;

    public AdapCategorias(@NonNull Context context, @NonNull List<Categoria> categories) {
        this.inflater = LayoutInflater.from(context);
        appContext = context.getApplicationContext();
        updateCategoryList(categories);
    }

    private class ViewHolder {
        public ImageView bajarElemento;
        public ImageView subirElemento;
        public TextView categoryName;
        public Integer referencia;
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Categoria getItem(int position) {
        return categories.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= categories.size()) {
            return -1;
        }
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.lista_categorias, null);
        }

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (viewHolder == null) {
            viewHolder = new ViewHolder();
            viewHolder.categoryName = view.findViewById(R.id.TV_nombre);
            viewHolder.bajarElemento = view.findViewById(R.id.IB_bajar_elemento);
            viewHolder.subirElemento = view.findViewById(R.id.IB_subir_elemento);
            view.setTag(viewHolder);
        }

        viewHolder.referencia = i;
        viewHolder.categoryName.setText(getItem(i).getNombre());
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
            if (i1 < categories.size() - 1) {
                Categoria buff = categories.get(i1);
                categories.set(i1, categories.get(i1 + 1));
                categories.set(i1 + 1, buff);
                notifyDataSetChanged();

                CategoriaDAO categoriaDAO = new CategoriaDAO(appContext);
                Categoria nuevaPosCategoria = categories.get(i1);
                nuevaPosCategoria.setPosicion(i1 + 1);
                categoriaDAO.actualizarUna(nuevaPosCategoria.getNombre(), nuevaPosCategoria);

                nuevaPosCategoria = categories.get(i1 + 1);
                nuevaPosCategoria.setPosicion(i1 + 2);
                categoriaDAO.actualizarUna(nuevaPosCategoria.getNombre(), nuevaPosCategoria);
            }
        });

        viewHolder.subirElemento.setOnClickListener(v -> {
            int i12 = finalViewHolder.referencia;
            if (i12 > 0) {
                Categoria buff = categories.get(i12);
                categories.set(i12, categories.get(i12 - 1));
                categories.set(i12 - 1, buff);
                notifyDataSetChanged();

                CategoriaDAO categoriaDAO = new CategoriaDAO(appContext);
                Categoria nuevaPosCategoria = categories.get(i12);
                nuevaPosCategoria.setPosicion(i12 + 1);
                categoriaDAO.actualizarUna(nuevaPosCategoria.getNombre(), nuevaPosCategoria);

                nuevaPosCategoria = categories.get(i12 - 1);
                nuevaPosCategoria.setPosicion(i12);
                categoriaDAO.actualizarUna(nuevaPosCategoria.getNombre(), nuevaPosCategoria);
            }
        });

        return view;
    }

    public void updateCategoryList(List<Categoria> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    public void setOcultarFlechas(boolean estado) {
        ocultarFlechas = estado;
        notifyDataSetChanged();
    }

    public boolean getOcultarFlechas() {
        return ocultarFlechas;
    }
}
