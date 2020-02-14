package cl.theroot.passbank.datos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import cl.theroot.passbank.datos.nombres.ColCategoria;
import cl.theroot.passbank.datos.nombres.ColCategoriaCuenta;
import cl.theroot.passbank.datos.nombres.NombreBD;
import cl.theroot.passbank.datos.nombres.Tabla;
import cl.theroot.passbank.dominio.CategoriaCuenta;

/**
 * Created by Benjamin on 07/10/2017.
 */

public class CategoriaCuentaDAO extends DAO {
    private static final String TAG = "BdC-CategoriaCuentaDAO";

    public CategoriaCuentaDAO(@NonNull Context context) {
        dbOpenHelper = DBOpenHelper.getInstance(context, NombreBD.BANCO_CONTRASENNAS);
        //dbOpenHelper = new DBOpenHelper(context, NombreBD.BANCO_CONTRASENNAS);
    }

    public CategoriaCuentaDAO(@NonNull Context context, boolean usarRespaldo) {
        if (usarRespaldo) {
            dbOpenHelper = DBOpenHelper.getInstance(context, NombreBD.BANCO_CONTRASENNAS_RESPALDO);
            //dbOpenHelper = new DBOpenHelper(context, NombreBD.BANCO_CONTRASENNAS_RESPALDO);
        } else {
            dbOpenHelper = DBOpenHelper.getInstance(context, NombreBD.BANCO_CONTRASENNAS);
            //dbOpenHelper = new DBOpenHelper(context, NombreBD.BANCO_CONTRASENNAS);
        }
    }

    public CategoriaCuentaDAO(@NonNull DBOpenHelper dbOpenHelper) {
        this.dbOpenHelper = dbOpenHelper;
    }

    public List<CategoriaCuenta> seleccionarTodas() {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String select = "SELECT * FROM " + Tabla.CATEGORA_CUENTA;
        Cursor cursor = db.rawQuery(select, null);
        List<CategoriaCuenta> resultado = new ArrayList<>();
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String nombreCategoria = cursor.getString(cursor.getColumnIndex(ColCategoriaCuenta.NOMBRE_CATEGORIA.toString()));
                String nombreCuenta = cursor.getString(cursor.getColumnIndex(ColCategoriaCuenta.NOMBRE_CUENTA.toString()));
                Integer posicion = cursor.getInt(cursor.getColumnIndex(ColCategoriaCuenta.POSICION.toString()));
                resultado.add(new CategoriaCuenta(nombreCategoria, nombreCuenta, posicion));
            } while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        return resultado;
    }

    public List<CategoriaCuenta> seleccionarPorCategoria(@NonNull String nombreCategoria) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String select = "SELECT * FROM " + Tabla.CATEGORA_CUENTA + " WHERE " + ColCategoriaCuenta.NOMBRE_CATEGORIA + " = ? ORDER BY " + ColCategoriaCuenta.POSICION + ", " + ColCategoriaCuenta.NOMBRE_CUENTA;
        String[] whereArgs = {nombreCategoria};
        Cursor cursor = db.rawQuery(select, whereArgs);
        List<CategoriaCuenta> resultado = new ArrayList<>();
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String nombreCuenta = cursor.getString(cursor.getColumnIndex(ColCategoriaCuenta.NOMBRE_CUENTA.toString()));
                Integer posicion = cursor.getInt(cursor.getColumnIndex(ColCategoriaCuenta.POSICION.toString()));
                resultado.add(new CategoriaCuenta(nombreCategoria, nombreCuenta, posicion));
            } while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        return resultado;
    }

    public List<CategoriaCuenta> seleccionarPorCuenta(@NonNull String nombreCuenta) {
        final String snNombreCategoria = "NOM_CAT";
        final String snPosicion = "POS";
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        //SELECT CATEGORIA_CUENTA.NOMBRE_CATEGORIA AS NOM_CAT, CATEGORIA_CUENTA.POSICION AS POS FROM CATEGORIA_CUENTA, CATEGORIA
        //  WHERE CATEGORIA_CUENTA.NOMBRE_CATEGORIA = CATEGORIA.NOMBRE AND CATEGORIA_CUENTA.NOMBRE_CUENTA = ?
        //  ORDER BY CATEGORIA.POSICION, CATEGORIA_CUENTA.NOMBRE_CATEGORIA
        String select = String.format("SELECT %s.%s AS %s, %s.%s AS %s FROM %s, %s WHERE %s.%s = %s.%s AND %s.%s = ? ORDER BY %s.%s, %s.%s",
                Tabla.CATEGORA_CUENTA, ColCategoriaCuenta.NOMBRE_CATEGORIA, snNombreCategoria,
                Tabla.CATEGORA_CUENTA, ColCategoriaCuenta.POSICION, snPosicion,
                Tabla.CATEGORA_CUENTA, Tabla.CATEGORIA,
                Tabla.CATEGORA_CUENTA, ColCategoriaCuenta.NOMBRE_CATEGORIA, Tabla.CATEGORIA, ColCategoria.NOMBRE,
                Tabla.CATEGORA_CUENTA, ColCategoriaCuenta.NOMBRE_CUENTA,
                Tabla.CATEGORIA, ColCategoria.POSICION,
                Tabla.CATEGORA_CUENTA, ColCategoriaCuenta.NOMBRE_CATEGORIA);
        String[] whereArgs = {nombreCuenta};
        Cursor cursor = db.rawQuery(select, whereArgs);
        List<CategoriaCuenta> resultado = new ArrayList<>();
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String nombreCategoria = cursor.getString(cursor.getColumnIndex(snNombreCategoria));
                Integer posicion = cursor.getInt(cursor.getColumnIndex(snPosicion));
                resultado.add(new CategoriaCuenta(nombreCategoria, nombreCuenta, posicion));
            } while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        return resultado;
    }

    public CategoriaCuenta seleccionarUna(@NonNull String nombreCategoria, @NonNull String nombreCuenta) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String select = "SELECT * FROM " + Tabla.CATEGORA_CUENTA + " WHERE " + ColCategoriaCuenta.NOMBRE_CATEGORIA + " = ? AND " + ColCategoriaCuenta.NOMBRE_CUENTA + " = ?";
        String[] whereArgs = {nombreCategoria, nombreCuenta};
        Cursor cursor = db.rawQuery(select, whereArgs);
        CategoriaCuenta resultado = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            Integer posicion = cursor.getInt(cursor.getColumnIndex(ColCategoriaCuenta.POSICION.toString()));
            resultado = new CategoriaCuenta(nombreCategoria, nombreCuenta, posicion);
        }
        cursor.close();
        //db.close();
        return resultado;
    }

    private int sigPosDisponible(String nombreCategoria) {
        int salida = 1;
        String SB_MAXIMO = "MAXIMO";
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String select = String.format("SELECT MAX(%s) AS %s FROM %s WHERE %s = ?",
                ColCategoriaCuenta.POSICION, SB_MAXIMO,
                Tabla.CATEGORA_CUENTA,
                ColCategoriaCuenta.NOMBRE_CATEGORIA);
        String[] whereArgs = {nombreCategoria};
        Cursor cursor = db.rawQuery(select, whereArgs);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            salida = cursor.getInt(cursor.getColumnIndex(SB_MAXIMO)) + 1;
        }
        cursor.close();
        //db.close();
        return salida;
    }

    public long insertarUna(@NonNull CategoriaCuenta categoriaCuenta) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ColCategoriaCuenta.NOMBRE_CATEGORIA.toString(), categoriaCuenta.getNombreCategoria());
        values.put(ColCategoriaCuenta.NOMBRE_CUENTA.toString(), categoriaCuenta.getNombreCuenta());
        values.put(ColCategoriaCuenta.POSICION.toString(), sigPosDisponible(categoriaCuenta.getNombreCategoria()));
        long salida = db.insert(Tabla.CATEGORA_CUENTA.toString(), null, values);
        //db.close();
        return salida;
    }

    public int actualizarUna(@NonNull CategoriaCuenta categoriaCuenta) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ColCategoriaCuenta.POSICION.toString(), categoriaCuenta.getPosicion());
        String where = ColCategoriaCuenta.NOMBRE_CATEGORIA + " = ? AND " + ColCategoriaCuenta.NOMBRE_CUENTA + " = ?";
        String[] whereArgs = {categoriaCuenta.getNombreCategoria(), categoriaCuenta.getNombreCuenta()};
        int salida = db.update(Tabla.CATEGORA_CUENTA.toString(), values, where, whereArgs);
        //db.close();
        return salida;
    }

    public int eliminarUna(@NonNull String nombreCategoria, @NonNull String nombreCuenta) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        int salida;

        //Se obtiene la posición de la categoría/cuenta
        CategoriaCuenta catCuenta = seleccionarUna(nombreCategoria, nombreCuenta);
        Integer pos = null;
        if (catCuenta != null) {
            pos = catCuenta.getPosicion();
        }

        //Se elimina la categoría/cuenta
        String where = ColCategoriaCuenta.NOMBRE_CATEGORIA + " = ? AND " + ColCategoriaCuenta.NOMBRE_CUENTA + " = ?";
        String[] whereArgs = {nombreCategoria, nombreCuenta};
        salida = db.delete(Tabla.CATEGORA_CUENTA.toString(), where, whereArgs);

        if (pos != null) {
            //Se modifican las posiciones del resto de categorias/cuentas
            //Primero se obtiene una lista de las posiciones a modificar, cambiando los valores de posicion
            List<CategoriaCuenta> elementosModificar = new ArrayList<>();
            String select = String.format("SELECT * FROM %s WHERE %s = ? AND %s > ?",
                    Tabla.CATEGORA_CUENTA,
                    ColCategoriaCuenta.NOMBRE_CATEGORIA,
                    ColCategoriaCuenta.POSICION);
            whereArgs = new String[]{nombreCategoria, String.valueOf(pos)};
            Cursor cursor = db.rawQuery(select, whereArgs);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    String nomCuenta = cursor.getString(cursor.getColumnIndex(ColCategoriaCuenta.NOMBRE_CUENTA.toString()));
                    Integer posCatCuenta = cursor.getInt(cursor.getColumnIndex(ColCategoriaCuenta.POSICION.toString())) - 1;
                    elementosModificar.add(new CategoriaCuenta(nombreCategoria, nomCuenta, posCatCuenta));
                } while (cursor.moveToNext());
            }
            cursor.close();
            //Luego se actualizan las posiciones
            for (CategoriaCuenta categoriaCuenta : elementosModificar) {
                actualizarUna(categoriaCuenta);
            }
        }
        //db.close();
        return salida;
    }

    public void imprimir() {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String select = "SELECT * FROM " + Tabla.CATEGORA_CUENTA;
        Cursor cursor = db.rawQuery(select, null);
        Log.i(TAG, "Imprimiendo los valores de la tabla " + Tabla.CATEGORA_CUENTA + "...");
        Log.i(TAG, ColCategoriaCuenta.NOMBRE_CATEGORIA + " - " + ColCategoriaCuenta.NOMBRE_CUENTA + " - " + ColCategoriaCuenta.POSICION);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String nombreCategoria = cursor.getString(cursor.getColumnIndex(ColCategoriaCuenta.NOMBRE_CATEGORIA.toString()));
                String nombreCuenta = cursor.getString(cursor.getColumnIndex(ColCategoriaCuenta.NOMBRE_CUENTA.toString()));
                int posicion = cursor.getInt(cursor.getColumnIndex(ColCategoriaCuenta.POSICION.toString()));
                Log.i(TAG, nombreCategoria + " - " + nombreCuenta + " - " + posicion);
            } while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
    }

    public static void cargarRespaldo(Context context, NombreBD origen, NombreBD destino) {
        DBOpenHelper dbOHOrigen = DBOpenHelper.getInstance(context, origen);
        DBOpenHelper dbOHDestino = DBOpenHelper.getInstance(context, destino);

        CategoriaCuentaDAO daoOrigen = new CategoriaCuentaDAO(dbOHOrigen);
        CategoriaCuentaDAO daoDestino = new CategoriaCuentaDAO(dbOHDestino);
        for (CategoriaCuenta categoriaCuenta : daoOrigen.seleccionarTodas()) {
            daoDestino.insertarUna(categoriaCuenta);
        }
    }
}
