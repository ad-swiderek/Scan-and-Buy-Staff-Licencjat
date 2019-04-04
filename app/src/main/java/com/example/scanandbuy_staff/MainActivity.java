package com.example.scanandbuy_staff;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.adrian.scanandbuy"; //wiadomosc (kod kreskowy) do pozniejszego przeslania w intencie
    private final Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scan();

        Button button = findViewById(R.id.scanBtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan();
            }
        });
    }

    protected void scan() { //uruchamiam skaner kodow kreskowych
        IntentIntegrator integrator = new IntentIntegrator(activity); //tworze specjalny intent z biblioteki zxing ktory pozwala przejsc do activity ze skanerem
        integrator.setBeepEnabled(false); //wylaczam dzwiek skanera
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES); //skaner jest w trybie skanowania wszystkich mozliwych kodow dostepnych w bibliotece
        integrator.initiateScan(); //inicjuje skanowanie
        integrator.setOrientationLocked(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data); //zapisuje rezultat naszego skanowania
        if (result != null) { //jezeli udalo sie zeskanowac i wczesniej wcisnieto przycisk "pracownik" to przechodze do activity w ktorym dodaje produkty i przekazuje w intencie moj numer z kodu kreskowego
            Intent intent = new Intent(this, AddProductActivity.class);
            String message = result.getContents().toString();
            intent.putExtra(EXTRA_MESSAGE, message);
            startActivity(intent);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


}
