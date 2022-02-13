package com.miguel.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.*;
import org.w3c.dom.Text;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    private RequestQueue queue;
    private EditText monto;
    private Spinner pais;
    private Button convert;
    private TextView fecha,resultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        queue= Volley.newRequestQueue(this);
        pais=(Spinner)findViewById(R.id.spinner_pais);
        convert=(Button)findViewById(R.id.btnConvertir);
        monto=(EditText) findViewById(R.id.txt_monto);
        fecha=(TextView)findViewById(R.id.txt_id);
        resultado=(TextView)findViewById(R.id.textView5);
        connetioInternet();

        String [] opciones={"Dólar","Euro","Yen japonés","Libra esterlina","Dólar Canadiense"};
        ArrayAdapter add=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, opciones);
        pais.setAdapter(add);
        //obtenerDatos();
        convert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    connetioInternet();
                    String validarMonto=monto.getText().toString();
                    String id_serie="";
                    if (validarMonto.equals("")){
                        monto.setError("Campo obligatorio");
                    }else {
                        final Double amount=Double.parseDouble(monto.getText().toString());
                        if (pais.getSelectedItem().toString()=="Dólar"){
                            id_serie="SF43718";
                        }else if (pais.getSelectedItem().toString()=="Euro"){
                            id_serie="SF46410";
                        }else if (pais.getSelectedItem().toString()=="Yen japonés"){
                            id_serie="SF46406";
                        }else if (pais.getSelectedItem().toString()=="Libra esterlina"){
                            id_serie="SF46407";
                        }else if (pais.getSelectedItem().toString()=="Dólar Canadiense"){
                            id_serie="SF60632";
                        }
                        String url_page="https://www.banxico.org.mx/SieAPIRest/service/v1/series/";
                        String idSerie=id_serie;
                        String root="/datos/oportuno?token=d77d948bec6538cedc0f6a2b2ef38f06be50b9ef71604b02a46e1fbf13593c44";
                        String url=url_page+idSerie+root;
                        /*
                        {
            "bmx":{
            "series":[
            {
            "idSerie":"SF43718",
            "titulo":"Tipo de cambio Pesos por dólar E.U.A. Tipo de cambio para solventar obligaciones denominadas en moneda extranjera Fecha de determinación (FIX)",
            "datos":[
               {
                  "fecha":"09/02/2022",
                  "dato":"20.5008"
               }
            ]
            }
            ]
            }
            }
                         */
                        JsonObjectRequest request=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONObject bmx=response.getJSONObject("bmx");
                                    JSONArray series=bmx.getJSONArray("series");
                                    for (int i=0; i<series.length(); i++){
                                        JSONObject jObject=series.getJSONObject(i);
                                        String api_titulo=jObject.getString("titulo");

                                        JSONArray datos=jObject.getJSONArray("datos");
                                        for (int a=0;a<datos.length();a++){
                                            JSONObject jObjectDatos=datos.getJSONObject(i);
                                            String api_fecha=jObjectDatos.getString("fecha");
                                            fecha.setText(api_fecha);
                                            String api_dato=jObjectDatos.getString("dato");
                                            Double obt_dato=Double.parseDouble(api_dato);
                                            Double res=amount*obt_dato;
                                            //Imprimir un Double con dos decimales
                                            DecimalFormat precision= new DecimalFormat("0.00");
                                            resultado.setText(precision.format(res));
                                        }

                                    }

                                } catch (JSONException e) {
                                    Log.e("error",e.getMessage());
                                }

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                            }
                        });
                        queue.add(request);
                    }
                }catch (NumberFormatException e){
                    Toast.makeText(MainActivity.this, "Ingrese monto", Toast.LENGTH_LONG).show();
                }catch(ArithmeticException e){
                    Toast.makeText(MainActivity.this, "error en operacion", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
    private  void connetioInternet(){
        //Validacion de conexion a ineternet
        ConnectivityManager connectivityManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo= connectivityManager.getActiveNetworkInfo();
        if (networkInfo !=null && networkInfo.isConnected()==true){
            //Toast.makeText(MainActivity.this, "Conexion exitosa a internet", Toast.LENGTH_LONG).show();
        }else {
            new AlertDialog.Builder(this)
                    .setTitle("Sin Internet")
                    .setMessage("Vereficar la señal en tu área.")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

}
