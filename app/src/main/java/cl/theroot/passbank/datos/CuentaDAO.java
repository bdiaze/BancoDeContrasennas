package cl.theroot.passbank.datos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cl.theroot.passbank.datos.nombres.ColCuenta;
import cl.theroot.passbank.datos.nombres.NombreBD;
import cl.theroot.passbank.datos.nombres.Tabla;
import cl.theroot.passbank.dominio.CategoriaCuenta;
import cl.theroot.passbank.dominio.Cuenta;

public class CuentaDAO extends DAO {
    private static final String TAG = "BdC-CuentaDAO";

    public CuentaDAO(@NonNull Context context) {
        dbOpenHelper = DBOpenHelper.getInstance(context, NombreBD.BANCO_CONTRASENNAS);
    }

    private CuentaDAO(@NonNull DBOpenHelper dbOpenHelper) {
        this.dbOpenHelper = dbOpenHelper;
    }

    public List<Cuenta> seleccionarTodas() {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String select = "SELECT * FROM " + Tabla.CUENTA + " ORDER BY " + ColCuenta.NOMBRE;
        Cursor cursor = db.rawQuery(select, null);
        List<Cuenta> resultado = new ArrayList<>();
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String nombre = cursor.getString(cursor.getColumnIndex(ColCuenta.NOMBRE.toString()));
                String descripcion = cursor.getString(cursor.getColumnIndex(ColCuenta.DESCRIPCION.toString()));
                Integer validez = cursor.getInt(cursor.getColumnIndex(ColCuenta.VALIDEZ.toString()));
                Integer vencInf = cursor.getInt(cursor.getColumnIndex(ColCuenta.VENCIMIENTO_INFORMADO.toString()));
                resultado.add(new Cuenta(nombre, descripcion, validez, vencInf));
            } while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
        return resultado;
    }

    public List<Cuenta> buscarCuentas(@NonNull String textoBuscado) {
        String textoOriginal = textoBuscado.trim().toLowerCase()
                .replace('á', 'a')
                .replace('é', 'e')
                .replace('í', 'i')
                .replace('ó', 'o')
                .replace('ú', 'u');

        textoBuscado = textoOriginal.replaceAll(" +", " ");

        String[] elemBuscar = textoBuscado.split(" ");
        List<Cuenta> salida = new ArrayList<>();
        if (elemBuscar.length > 0) {
            SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
            StringBuilder select = new StringBuilder();
            List<String> whereArgs = new ArrayList<>();

            select.append("SELECT ").append(ColCuenta.NOMBRE).append(", ");
            select.append(ColCuenta.DESCRIPCION).append(", ");
            select.append(ColCuenta.VALIDEZ).append(", ");
            select.append(ColCuenta.VENCIMIENTO_INFORMADO).append(", ");
            select.append("MIN(POSICION) AS MIN_POSICION FROM (");

            select.append("SELECT *, 10 AS POSICION FROM ").append(Tabla.CUENTA).append(" WHERE ");
            select.append("REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(");
            select.append(ColCuenta.NOMBRE);
            select.append("), 'á', 'a'), 'é', 'e'), 'í', 'i'), 'ó', 'o'), 'ú', 'u') ");
            select.append("LIKE ? ");
            whereArgs.add("%" + textoOriginal + "%");

            select.append("UNION ALL SELECT *, 20 AS POSICION FROM ").append(Tabla.CUENTA).append(" WHERE ");
            select.append("REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(");
            select.append(ColCuenta.DESCRIPCION);
            select.append("), 'á', 'a'), 'é', 'e'), 'í', 'i'), 'ó', 'o'), 'ú', 'u') ");
            select.append("LIKE ? ");
            whereArgs.add("%" + textoOriginal + "%");

            StringBuilder strWhere1 = new StringBuilder();
            List<String> parWhere1 = new ArrayList<>();
            StringBuilder strWhere2 = new StringBuilder();
            List<String> parWhere2 = new ArrayList<>();
            boolean conOr = false;
            for (String elemento : elemBuscar) {
                if (elemento.length() > 0) {
                    if (!conOr) {
                        conOr = true;
                    } else {
                        strWhere1.append(" OR ");
                        strWhere2.append(" OR ");
                    }
                    strWhere1.append("REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(");
                    strWhere1.append(ColCuenta.NOMBRE);
                    strWhere1.append("), 'á', 'a'), 'é', 'e'), 'í', 'i'), 'ó', 'o'), 'ú', 'u')");
                    strWhere1.append(" LIKE ?");
                    parWhere1.add("%" + elemento + "%");

                    strWhere2.append("REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(");
                    strWhere2.append(ColCuenta.DESCRIPCION);
                    strWhere2.append("), 'á', 'a'), 'é', 'e'), 'í', 'i'), 'ó', 'o'), 'ú', 'u')");
                    strWhere2.append(" LIKE ?");
                    parWhere2.add("%" + elemento + "%");
                }
            }

            select.append("UNION ALL SELECT *, 30 AS POSICION FROM ").append(Tabla.CUENTA).append(" WHERE ");
            select.append(strWhere1).append(" ");
            whereArgs.addAll(parWhere1);

            select.append("UNION ALL SELECT *, 40 AS POSICION FROM ").append(Tabla.CUENTA).append(" WHERE ");
            select.append(strWhere2).append(" ");
            whereArgs.addAll(parWhere2);

            select.append(") GROUP BY ").append(ColCuenta.NOMBRE).append(", ");
            select.append(ColCuenta.DESCRIPCION).append(", ");
            select.append(ColCuenta.VALIDEZ).append(", ");
            select.append(ColCuenta.VENCIMIENTO_INFORMADO).append(" ");
            select.append("ORDER BY MIN_POSICION, ").append(ColCuenta.NOMBRE);

            Cursor cursor = db.rawQuery(select.toString(), Arrays.copyOf(whereArgs.toArray(), whereArgs.size(), String[].class));
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    String nombre = cursor.getString(cursor.getColumnIndex(ColCuenta.NOMBRE.toString()));
                    String descripcion = cursor.getString(cursor.getColumnIndex(ColCuenta.DESCRIPCION.toString()));
                    Integer validez = cursor.getInt(cursor.getColumnIndex(ColCuenta.VALIDEZ.toString()));
                    salida.add(new Cuenta(nombre, descripcion, validez));
                } while(cursor.moveToNext());
            }
            cursor.close();
            //db.close();
        }
        return salida;
    }

    public Cuenta seleccionarUna(@NonNull String nombre) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String select = "SELECT * FROM " + Tabla.CUENTA + " WHERE " + ColCuenta.NOMBRE + " = ?";
        String[] whereArgs = {nombre};
        Cursor cursor = db.rawQuery(select, whereArgs);
        Cuenta resultado = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            nombre = cursor.getString(cursor.getColumnIndex(ColCuenta.NOMBRE.toString()));
            String descripcion = cursor.getString(cursor.getColumnIndex(ColCuenta.DESCRIPCION.toString()));
            Integer validez = cursor.getInt(cursor.getColumnIndex(ColCuenta.VALIDEZ.toString()));
            resultado = new Cuenta(nombre, descripcion, validez);
        }
        cursor.close();
        //db.close();
        return resultado;
    }

    public long insertarUna(@NonNull Cuenta cuenta) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ColCuenta.NOMBRE.toString(), cuenta.getNombre());
        if (cuenta.getDescripcion() != null) {
            values.put(ColCuenta.DESCRIPCION.toString(), cuenta.getDescripcion());
        } else {
            values.putNull(ColCuenta.DESCRIPCION.toString());
        }
        values.put(ColCuenta.VALIDEZ.toString(), cuenta.getValidez());
        if (cuenta.getVencInf() != null) {
            values.put(ColCuenta.VENCIMIENTO_INFORMADO.toString(), cuenta.getVencInf());
        }
        return db.insert(Tabla.CUENTA.toString(), null, values);
    }

    public int actualizarUna(@NonNull String nombreAnterior, @NonNull Cuenta cuenta) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ColCuenta.NOMBRE.toString(), cuenta.getNombre());
        if (cuenta.getDescripcion() != null) {
            values.put(ColCuenta.DESCRIPCION.toString(), cuenta.getDescripcion());
        } else {
            values.putNull(ColCuenta.DESCRIPCION.toString());
        }
        values.put(ColCuenta.VALIDEZ.toString(), cuenta.getValidez());
        if (cuenta.getVencInf() != null) {
            values.put(ColCuenta.VENCIMIENTO_INFORMADO.toString(), cuenta.getVencInf());
        }
        String where = ColCuenta.NOMBRE + " = ?";
        String[] whereArgs= {nombreAnterior};
        return db.update(Tabla.CUENTA.toString(), values, where, whereArgs);
    }

    public int eliminarUna(@NonNull String nombre) {
        CategoriaCuentaDAO categoriaCuentaDAO = new CategoriaCuentaDAO(dbOpenHelper);

        //Obtenemos lista de Categorías/AdapCuentas asociadas a la cuenta, y se eliminan
        for (CategoriaCuenta categoriaCuenta : categoriaCuentaDAO.seleccionarPorCuenta(nombre)) {
            categoriaCuentaDAO.eliminarUna(categoriaCuenta.getNombreCategoria(), categoriaCuenta.getNombreCuenta());
        }

        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        String where = ColCuenta.NOMBRE + " = ?";
        String[] whereArgs = {nombre};
        return db.delete(Tabla.CUENTA.toString(), where, whereArgs);
    }

    public void imprimir() {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        String select = "SELECT * FROM " + Tabla.CUENTA;
        Cursor cursor = db.rawQuery(select, null);
        Log.i(TAG, "Imprimiendo los valores de la tabla " + Tabla.CUENTA + "...");
        Log.i(TAG, ColCuenta.NOMBRE + " - " + ColCuenta.DESCRIPCION + " - " + ColCuenta.VALIDEZ);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                String nombre = cursor.getString(cursor.getColumnIndex(ColCuenta.NOMBRE.toString()));
                String descripcion = cursor.getString(cursor.getColumnIndex(ColCuenta.DESCRIPCION.toString()));
                int validez = cursor.getInt(cursor.getColumnIndex(ColCuenta.VALIDEZ.toString()));
                Log.i(TAG, nombre + " - " + descripcion + " - " + validez);
            } while(cursor.moveToNext());
        }
        cursor.close();
        //db.close();
    }

    public static void cargarRespaldo(Context context, NombreBD origen, NombreBD destino) {
        DBOpenHelper dbOHOrigen = DBOpenHelper.getInstance(context, origen);
        DBOpenHelper dbOHDestino = DBOpenHelper.getInstance(context, destino);
        //DBOpenHelper dbOHOrigen = new DBOpenHelper(context, origen);
        //DBOpenHelper dbOHDestino = new DBOpenHelper(context, destino);

        CuentaDAO daoOrigen = new CuentaDAO(dbOHOrigen);
        CuentaDAO daoDestino = new CuentaDAO(dbOHDestino);
        for (Cuenta cuenta : daoOrigen.seleccionarTodas()) {
            daoDestino.insertarUna(cuenta);
        }
    }
}
