package cl.theroot.passbank.adaptador;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.theroot.passbank.R;
import cl.theroot.passbank.dominio.CategoriaSeleccionable;
import cl.theroot.passbank.fragmento.FragAgregarEditarCuenta;

public class AdapCategoriasCheckBox extends BaseAdapter{
    private LayoutInflater inflater;
    private List<CategoriaSeleccionable> categorias;
    private FragAgregarEditarCuenta fragmento;
    private Map<String, Boolean> estadosOriginales;

    public AdapCategoriasCheckBox(@NonNull Context context, @NonNull List<CategoriaSeleccionable> categorias, FragAgregarEditarCuenta fragmento) {
        this.inflater = LayoutInflater.from(context);
        this.fragmento = fragmento;
        updateCategorias(categorias);
    }

    private class ViewHolder {
        public CheckBox categoryName;
        public Integer referencia;
    }

    @Override
    public int getCount() {
        return categorias.size();
    }

    @Override
    public CategoriaSeleccionable getItem(int i) {
        return categorias.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.lista_categorias_check_box, null);
        }

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (viewHolder == null) {
            viewHolder = new ViewHolder();
            viewHolder.categoryName = view.findViewById(R.id.checkBox);
            view.setTag(viewHolder);
        }

        viewHolder.referencia = i;
        viewHolder.categoryName.setText(categorias.get(i).getNombre());
        viewHolder.categoryName.setChecked(categorias.get(i).isSeleccionado());

        final ViewHolder finalViewHolder = viewHolder;
        viewHolder.categoryName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int i = finalViewHolder.referencia;
                categorias.get(i).setSeleccionado(buttonView.isChecked());
            }
        });

        return view;
    }

    public void updateCategorias(List<CategoriaSeleccionable> categories) {
        this.categorias = categories;

        estadosOriginales = new HashMap<>();
        for (CategoriaSeleccionable categoria : this.categorias) {
            estadosOriginales.put(categoria.getNombre(), categoria.isSeleccionado());
        }

        notifyDataSetChanged();
    }

    public List<CategoriaSeleccionable> getCategorias() {
        return categorias;
    }

    public Boolean checkearCambios() {
        for (CategoriaSeleccionable categoria : categorias) {
            if (estadosOriginales.get(categoria.getNombre()) != categoria.isSeleccionado()) {
                return true;
            }
        }
        return false;
    }
}
