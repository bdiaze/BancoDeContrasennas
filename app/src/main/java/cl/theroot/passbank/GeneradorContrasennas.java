package cl.theroot.passbank;

import android.content.Context;

import java.util.Date;
import java.util.Random;

import cl.theroot.passbank.datos.PalabraDAO;
import cl.theroot.passbank.datos.ParametroDAO;
import cl.theroot.passbank.datos.nombres.NombreParametro;

/**
 * Created by Benjamin on 11/10/2017.
 */

public class GeneradorContrasennas {
    private static final String TAG = "BdC-GeneradorContras...";
    private PalabraDAO palabraDAO;

    private String posiblesCaracteres;
    private int cantCaracteres;
    private int cantPalabras;
    private String separadorPalabras;

    public GeneradorContrasennas(Context context) {
        ParametroDAO parametroDAO = new ParametroDAO(context);
        palabraDAO = new PalabraDAO(context);

        posiblesCaracteres = parametroDAO.seleccionarUno(NombreParametro.COMPOSICION_GENERADOR.toString()).getValor();
        cantCaracteres = Integer.parseInt(parametroDAO.seleccionarUno(NombreParametro.CANT_CARACTERES_GENERADOR.toString()).getValor());
        cantPalabras = Integer.parseInt(parametroDAO.seleccionarUno(NombreParametro.CANT_PALABRAS_GENERADOR.toString()).getValor());
        separadorPalabras = parametroDAO.seleccionarUno(NombreParametro.SEPARADOR_GENERADOR.toString()).getValor();
    }

    public String generar(boolean modoCaracteres) {
        StringBuilder resultado = new StringBuilder();
        if (modoCaracteres) {
            Random random = new Random((new Date()).getTime());
            while (resultado.length() < cantCaracteres) {
                int i = random.nextInt(posiblesCaracteres.length());
                resultado.append(posiblesCaracteres.charAt(i));
            }
        } else {
            //Se generan un número aleatorio por cada palabra a generar
            Random random = new Random((new Date()).getTime());
            int[] indicePalabras = new int[cantPalabras];
            int[] rangoIDs = palabraDAO.getRango();

            for (int i = 0; i < indicePalabras.length; i++) {
                indicePalabras[i] = random.nextInt(rangoIDs[1] - rangoIDs[0] + 1) + rangoIDs[0];
                //Log.i(TAG, String.valueOf(indicePalabras[i]));
            }

            //Se obtiene la palabra asociada con cada uno de los números
            String[] palabras = new String[indicePalabras.length];
            for (int i = 0; i < palabras.length; i++) {
                palabras[i] = palabraDAO.seleccionarPalabra(indicePalabras[i]);

                // Se ponen mayusculas...
                StringBuilder salida = new StringBuilder();
                for (char c : palabras[i].toCharArray()) {
                    if (random.nextInt(2) == 0) {
                        c = Character.toUpperCase(c);
                    }
                    salida.append(c);
                }
                palabras[i] = salida.toString();
            }

            //Se concatenan las palabras obtenidas
            for (String palabra : palabras) {
                if (palabra != null) {
                    if (resultado.length() == 0) {
                        resultado = new StringBuilder(palabra);
                    } else {
                        resultado.append(separadorPalabras).append(palabra);
                    }
                }
            }
        }
        return resultado.toString();
    }
}
