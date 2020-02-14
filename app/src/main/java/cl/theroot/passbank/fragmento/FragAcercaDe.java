package cl.theroot.passbank.fragmento;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cl.theroot.passbank.CustomFragment;
import cl.theroot.passbank.R;

public class FragAcercaDe extends CustomFragment {

    @BindView(R.id.acercaDeDetalle)
    TextView acercaDeDetalle;

    public FragAcercaDe() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragmento_acerca_de, container, false);
        ButterKnife.bind(this, view);

        acercaDeDetalle.setMovementMethod(LinkMovementMethod.getInstance());

        return view;
    }

}
