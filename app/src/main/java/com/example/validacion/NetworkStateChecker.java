package com.example.validacion;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;



public class NetworkStateChecker extends BroadcastReceiver {

    //objeto auxiliar de contexto y base de datos
    private Context context;
    private DatabaseHelper db;


    @SuppressLint("Range")
    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;

        db = new DatabaseHelper(context);

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        //si hay una red
        if (activeNetwork != null) {
            //si está conectado a wifi o plan de datos móviles
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {

                //obteniendo todos los nombres no sincronizados
                Cursor cursor = db.getUnsyncedNames();
                if (cursor.moveToFirst()) {
                    do {
                       // loginAdmin();
                        //llamar al método para guardar el nombre no sincronizado en MySQL
                        Toast.makeText(context.getApplicationContext(), "enviando los datos", Toast.LENGTH_SHORT).show();
                        saveName(
                                cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID)),
                                cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME))
                        );
                    } while (cursor.moveToNext());
                }
            }
        }else {
            Toast.makeText(context.getApplicationContext(), "Red desactivado", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * método que toma dos argumentos
     * nombre que se va a guardar e id del nombre de SQLite
     * si el nombre se envía correctamente
     * Actualizaremos el estado sincronizado en SQLite
    * */
    private void saveName(final int id, final String name) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, MainActivity.URL_SAVE_NAME,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                //actualizando el estado en sqlite
                                db.updateNameStatus(id, MainActivity.NAME_SYNCED_WITH_SERVER);
                             //   Toast.makeText(context.getApplicationContext(), "Enviando en servidor", Toast.LENGTH_SHORT).show();
                                //enviando la transmisión para actualizar la lista
                                context.sendBroadcast(new Intent(MainActivity.DATA_SAVED_BROADCAST));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                return params;
            }
        };

        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }

    public void loginAdmin() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context.getApplicationContext());
       // View mView = getLayoutInflater().inflate(R.layout.dialog_agregar, null);
        View mView = LayoutInflater.from(context).inflate(R.layout.dialog_agregar, null);
      //  LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final EditText mEmail = (EditText) mView.findViewById(R.id.etEmail);

        Button mLogin = (Button) mView.findViewById(R.id.btnLogin);





        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();



    }



}
