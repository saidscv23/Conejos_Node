package com.example.conejos;

import androidx.appcompat.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    //Aquí se declaran las variables para los elementos de la interfaz de usuari
    private EditText etPeriodo, etParejas, etCrias, etMortalidad;
    private Button btnCalcular;
    private TextView tvResultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Asegúrate de que el layout sea el correcto

        //El método onCreate es donde se inicializa la actividad.
        //setContentView establece el layout que se usará para esta actividad.
        //El código obtiene referencias a los elementos de la UI utilizando findViewById.

        etPeriodo = findViewById(R.id.id_periodo);
        etParejas = findViewById(R.id.id_nparejas);
        etCrias = findViewById(R.id.id_ncrias);
        etMortalidad = findViewById(R.id.id_mort);
        btnCalcular = findViewById(R.id.id_calcular);
        tvResultado = findViewById(R.id.id_resultado);




        // Configurar el botón para realizar la solicitud HTTP
        btnCalcular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String periodo = etPeriodo.getText().toString().trim();
                String parejas = etParejas.getText().toString().trim();
                String crias = etCrias.getText().toString().trim();
                String mortalidad = etMortalidad.getText().toString().trim();

                // Ejecutar la tarea en segundo plano para obtener los datos del servidor
                new ConsultarConejosTask().execute(
                        String.format(
                                "http://10.10.33.180:3000/conejos?p=%s&nPar=%s&nCri=%s&tMor=%s",
                                periodo,
                                parejas,
                                crias,
                                mortalidad
                        )
                );
            }
        });
    }

    //ConsultarConejosTask extiende AsyncTask, lo que permite realizar tareas en segundo plano sin bloquear la interfaz de usuario.
    //El método doInBackground realiza la solicitud HTTP usando OkHttpClient. Si hay un error, se captura y se devuelve como una cadena con el mensaje de error.
    //El método onPostExecute se ejecuta cuando doInBackground termina. Si hay un error, se muestra en tvResultado.
    //Si no hay errores, onPostExecute procesa la respuesta como un JSONArray y construye una cadena formateada para mostrarla en tvResultado.

    private class ConsultarConejosTask extends AsyncTask<String, Void, String> {
        private final OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(String... urls) {
            try {
                Request request = new Request.Builder()
                        .url(urls[0])
                        .build();

                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String resultado) {
            super.onPostExecute(resultado);

            if (resultado.startsWith("Error:")) {
                tvResultado.setText(resultado);
                return;
            }

            try {
                JSONArray jsonArray = new JSONArray(resultado);
                StringBuilder formattedResult = new StringBuilder();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    int periodo = jsonObject.getInt("periodo");
                    double pAnual = jsonObject.getDouble("pAnual");
                    double pMorir = jsonObject.getDouble("pMorir");
                    double pRestante = jsonObject.getDouble("pRestante");
                    double nParejas = jsonObject.getDouble("nParejas");
                    double nCrias = jsonObject.getDouble("nCrias");

                    formattedResult.append(
                            String.format(
                                    "Periodo: %d\npAnual = %.2f\npMorir = %.2f\npRestante = %.2f\nnParejas = %.2f\nnCrias = %.2f\n\n",
                                    periodo,
                                    pAnual,
                                    pMorir,
                                    pRestante,
                                    nParejas,
                                    nCrias
                            )
                    );
                }

                tvResultado.setText(formattedResult.toString());

            } catch (JSONException e) {
                tvResultado.setText("Error al procesar la respuesta");
            }
        }
    }
}
