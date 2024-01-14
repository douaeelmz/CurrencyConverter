package ma.fstt.convertisseurdevise;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;



import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    TextView convertFromDropdownTextView, convertToDropdownTextView, conversionText;
    EditText amountToConvert;
    ArrayList<String> arrayList;
    Dialog fromDialog, toDialog;
    Button conversionButton;
    String convertFromValue, convertToValue, conversionValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        convertFromDropdownTextView = findViewById(R.id.convert_from_dropdown_menu);
        convertToDropdownTextView = findViewById(R.id.convert_to_dropdown_menu);
        conversionButton = findViewById(R.id.conversionButton);
        conversionText = findViewById(R.id.conversionText);
        amountToConvert = findViewById(R.id.amountToConvertValueEdit);

        fetchCurrencyCodes();


        convertFromDropdownTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fromDialog = new Dialog(MainActivity.this);
                fromDialog.setContentView(R.layout.from);
                fromDialog.getWindow().setLayout(650, 800);
                fromDialog.show();

                EditText editText = fromDialog.findViewById(R.id.edit_text);
                ListView listView = fromDialog.findViewById(R.id.list_view);


                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, arrayList);
                listView.setAdapter(adapter);


                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        adapter.getFilter().filter(charSequence);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {


                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                          convertFromDropdownTextView.setText(adapter.getItem(i));
                          fromDialog.dismiss();
                          convertFromValue = adapter.getItem(i);
                    }
                });
            }
        });

        convertToDropdownTextView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                toDialog = new Dialog(MainActivity.this);
                toDialog.setContentView(R.layout.to);
                toDialog.getWindow().setLayout(650,800);
                toDialog.show();


                EditText editText = toDialog.findViewById(R.id.edit_text);
                ListView listView = toDialog.findViewById(R.id.list_view);


                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, arrayList);
                listView.setAdapter(adapter);

                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        adapter.getFilter().filter(charSequence);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {


                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        convertToDropdownTextView.setText(adapter.getItem(i));
                        fromDialog.dismiss();
                        convertToValue = adapter.getItem(i);
                    }
                });
            }
        });

        conversionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Double amountToConvert = Double.valueOf(MainActivity.this.amountToConvert.getText().toString());
//                Log.d("con:",convertFromValue);
//                Log.d("con1:",convertToValue);
//                Log.d("con2:", String.valueOf(amountToConvert));
                getConversionRate(convertFromValue,convertToValue,amountToConvert);
            }
        });
    }

    private String getConversionRate(String convertFromValue, String convertToValue, Double amountToConvert) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://v6.exchangerate-api.com/v6/2439844c56f3bc1c66a6e5f7/pair/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();



            CurrencyConverterService service = retrofit.create(CurrencyConverterService.class);
            Call<ConversionResponse> call = service.convertCurrency(amountToConvert,convertFromValue, convertToValue);

            call.enqueue(new Callback<ConversionResponse>() {
                @Override
                public void onResponse(Call<ConversionResponse> call, Response<ConversionResponse> response) {
                    if (response.isSuccessful()) {
                        ConversionResponse conversionResponse = response.body();
                        if ("success".equals(conversionResponse.getResult())) {
//                            Log.d("suceess", String.valueOf(amountToConvert));
                            double conversionRate = conversionResponse.getConversionRate();
//                            Log.d("suceess", String.valueOf(conversionRate));
                            double convertedAmount = amountToConvert * conversionRate;

                            conversionText.setText(String.format("%.2f", convertedAmount));
                        } else {
                            Log.d("failure", String.valueOf(amountToConvert));
                        }
                    } else {
                        // API errors
                    }
                }

                @Override
                public void onFailure(Call<ConversionResponse> call, Throwable t) {
                    // network errors
                }
            });



        return conversionValue;
    }

    private void fetchCurrencyCodes() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://openexchangerates.org/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CurrencyConverterService service = retrofit.create(CurrencyConverterService.class);
        Call<Map<String, String>> call = service.getCurrencyCodes("f60d9b142de24c90986cb680a0b027c8");

        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Map<String, String> currencyCodes = response.body();

                    // Récupérer les clés (codes de devises) à partir de la réponse API
                    Set<String> keys = currencyCodes.keySet();

                    // Remplir les spinners avec les codes de devises
                    arrayList = new ArrayList<>(keys);

                } else {
                    //erreur api
                }
            }
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {

            }
        });

    }

}