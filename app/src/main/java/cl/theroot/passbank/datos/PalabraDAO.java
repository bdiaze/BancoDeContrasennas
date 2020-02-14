package cl.theroot.passbank.datos;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

/**
 * Created by Benjamin on 12/10/2017.
 */

public class PalabraDAO {
    private static final String TAG = "BdC-PalabraDAO";

    private DBOpenHelperDiccionario dbOpenHelperDiccionario;

    public PalabraDAO(@NonNull Context context) {
        dbOpenHelperDiccionario = DBOpenHelperDiccionario.getInstance(context);
    }

    public int[] getRango() {
        String SN_MAXIMO = "MAXIMO";
        String SN_MINIMO = "MINIMO";
        SQLiteDatabase db = dbOpenHelperDiccionario.getReadableDatabase();
        int[] resultado = {-1, -1};
        String select = String.format("SELECT MAX(%s) AS %s, MIN(%s) AS %s FROM %s",
                DBOpenHelperDiccionario.COLUMNA_ID, SN_MAXIMO,
                DBOpenHelperDiccionario.COLUMNA_ID, SN_MINIMO,
                DBOpenHelperDiccionario.NOMBRE_TABLA);
        Cursor cursor = db.rawQuery(select, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            resultado[1] = cursor.getInt(cursor.getColumnIndex(SN_MAXIMO));
            resultado[0] = cursor.getInt(cursor.getColumnIndex(SN_MINIMO));
        }
        cursor.close();
        //db.close();
        //Log.i(TAG, "Max: " + resultado[1]);
        //Log.i(TAG, "Min: " + resultado[0]);
        return resultado;
    }

    public String seleccionarPalabra(int id) {
        SQLiteDatabase db = dbOpenHelperDiccionario.getReadableDatabase();
        String select = "SELECT * FROM " + DBOpenHelperDiccionario.NOMBRE_TABLA + " WHERE " + DBOpenHelperDiccionario.COLUMNA_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        Cursor cursor = db.rawQuery(select, whereArgs);
        String resultado = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            resultado = cursor.getString(cursor.getColumnIndex(DBOpenHelperDiccionario.COLUMNA_VALOR));
        }
        cursor.close();
        //db.close();
        return resultado;
    }
}
