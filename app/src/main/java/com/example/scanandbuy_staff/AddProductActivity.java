package com.example.scanandbuy_staff;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.scanandbuy_staff.databinding.ActivityAddProductBinding;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Tag;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class AddProductActivity extends AppCompatActivity {

    private ActivityAddProductBinding binding;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private DatabaseReference databaseProducts = database.getReference("products");
    private StorageReference storageReference = storage.getReferenceFromUrl("gs://scanandbuy-53a52.appspot.com");
    private StorageReference productsImagesJpg;
    private StorageReference productsImages;
    private String message;
    private ProductClass productClass = new ProductClass();
    private static final String TAG = "AddProductActivity";
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static boolean isCameraUsed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_product);
        Intent intent = getIntent();
        message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE); //odebranie wiadomości z zeskanowanym kodem kreskowy,
        TextView textView = findViewById(R.id.barcodeTextView);
        textView.setText(message); //wyświetlenie kodu w textView

        storageReference = FirebaseStorage.getInstance().getReference(); //utworzenie referencji do "storage" w którym przechowywane będą zdjęcia
        productsImagesJpg = storageReference.child(message + ".jpg");
        productsImages = storageReference.child("productsImages/" + message + ".jpg");
        productsImages.getName().equals(productsImagesJpg.getName());
        productsImages.getPath().equals(productsImagesJpg.getPath());

        readFromFirebase(); //odczyt danych z bazy na wypadek gdyby produkt już się w niej znajdował

        binding.saveBtn.setOnClickListener(new View.OnClickListener() { //tworzymy wydarzenie ktore po kliknieciu w button wywola metode zapisujaca dane do bazy
            @Override
            public void onClick(View v) { //zapisanie danych do bazy po naciśnięciu przycisku
                saveToFirebase();
                if (binding.imageView.getDrawable() != null) //zapisanie zdjęcia do "storage" jeżeli zostało wybrane/zrobione
                    saveToStorage();
            }
        });

        binding.addNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //zeskanowanie kolejnego produktu
                finish();
                Intent intentMain = new Intent(AddProductActivity.this,
                        MainActivity.class);
                startActivity(intentMain);
            }
        });

        binding.editNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //edycja nazwy produktu poprzez wyświetlenie okna z możliwością wpisania tekstu
                final AlertDialog popUpWindow = new AlertDialog.Builder
                        (AddProductActivity.this).create();
                final EditText editText = new EditText(AddProductActivity.this);
                popUpWindow.setTitle("Edytuj nazwę");
                popUpWindow.setMessage("Wprowadz nową nazwę: ");
                popUpWindow.setView(editText);

                popUpWindow.setButton(popUpWindow.BUTTON_NEGATIVE, "Anuluj",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                popUpWindow.dismiss();
                            }
                        });

                popUpWindow.setButton(popUpWindow.BUTTON_POSITIVE, "Zapisz",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                binding.nameTextView.setText(editText.getText());
                            }
                        });
                popUpWindow.show();
            }
        });

        binding.editPriceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //edycja ceny poprzez wyświetlenie okna z możliwością jej wpisania
                final AlertDialog popUpWindow = new AlertDialog.Builder
                        (AddProductActivity.this).create();
                final EditText editText = new EditText(AddProductActivity.this);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL);
                popUpWindow.setTitle("Edytuj cenę");
                popUpWindow.setMessage("Wprowadz nową cenę: ");
                popUpWindow.setView(editText);

                popUpWindow.setButton(popUpWindow.BUTTON_NEGATIVE, "Anuluj",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                popUpWindow.dismiss();
                            }
                        });

                popUpWindow.setButton(popUpWindow.BUTTON_POSITIVE, "Zapisz",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                binding.priceTextView.setText(editText.getText());
                            }
                        });
                popUpWindow.show();
            }
        });

        binding.addQuantityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //zmiana ilości sztuk produktu poprzez dodanie odpowiedniej liczby do już istniejącej (lub nie) oraz wyświetlenie okna w którym można wpisać dodawaną ilość
                final AlertDialog popUpWindow = new AlertDialog.Builder
                        (AddProductActivity.this).create();
                final EditText editText = new EditText(AddProductActivity.this);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_SIGNED);
                popUpWindow.setTitle("Dodaj");
                popUpWindow.setMessage("Ile sztuk dodać?");
                popUpWindow.setView(editText);

                popUpWindow.setButton(popUpWindow.BUTTON_NEGATIVE, "Anuluj",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                popUpWindow.dismiss();
                            }
                        });

                popUpWindow.setButton(popUpWindow.BUTTON_POSITIVE, "Zapisz",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (binding.quantityTextView.getText() != "") {
                                    int sum = Integer.parseInt(binding.quantityTextView.getText()
                                            .toString()) + Integer.parseInt(editText.getText().toString());
                                    binding.quantityTextView.setText(String.valueOf(sum));
                                } else {
                                    binding.quantityTextView.setText(editText.getText());
                                }
                            }
                        });
                popUpWindow.show();
            }
        });

        binding.addPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //dodanie zdjęcia ze zdjęć istniejących na urządzeniu
                Intent addPictureIntent = new Intent(Intent.ACTION_PICK);
                addPictureIntent.setType("image/*");
                startActivityForResult(addPictureIntent, RESULT_LOAD_IMAGE);
            }
        });

        binding.takePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //uruchomienie aparatu w celu zrobienia zdjęcia produktu
                isCameraUsed = true;
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });
    }

    private void saveToFirebase() { //przypisanie informacji zebranych z poszczególnych TextView do instancji klasy ProductClass a następnie zapisanie obiektu do bazy danych w chmurze
        if (binding.nameTextView.getText() != "" && binding.priceTextView.getText() != ""
                && binding.quantityTextView.getText() != "") { //sprawdzenie czy wszystkie wymagane pola zostały wypełnione
            ProductClass productObject = new ProductClass(binding.barcodeTextView.getText()
                    .toString(), binding.nameTextView.getText().toString(),
                    binding.priceTextView.getText().toString(),
                    binding.quantityTextView.getText().toString());
            databaseProducts.child(binding.barcodeTextView.getText().toString())
                    .setValue(productObject, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null)
                                showToastMessage("Dodano pomyslnie.");
                            else
                                showToastMessage("Error " + databaseError);
                        }
                    });
        } else {
            showToastMessage("Wypełnij wszystkie pola!");
        }
    }

    private void saveToStorage() { //konwersja zdjęcia oraz zapisanie go do "storage"
        ImageView imageToSend = binding.imageView;
        imageToSend.setDrawingCacheEnabled(true);
        imageToSend.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imageToSend.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = productsImagesJpg.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToastMessage("Podczas dodawania obrazu wystąpił błąd");
            }
        });
    }

    private void readFromFirebase() { //odczyt danych z bazy na podstawie kodu kreskowego oraz przypisanie ich do textView w oknie aplikacji oraz odczyt zdjęcia
        Query query = databaseProducts.orderByChild("barcode").equalTo(message);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    productClass = ds.getValue(ProductClass.class);
                }
                if (productClass.getQuantity() != null) {
                    try {
                        binding.nameTextView.setText(productClass.getProductName());
                        binding.priceTextView.setText(productClass.getPrice());
                        binding.quantityTextView.setText(productClass.getQuantity());
                    } catch (NumberFormatException e) {
                        showToastMessage("Błąd");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Error", databaseError.toException());
            }
        });
        storageReference.child(message + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(AddProductActivity.this).load(uri).into(binding.imageView);
            }
        });
    }

    private void showToastMessage(String message) { // wyświetlenie wiadomości w postaci toast'a
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //odebranie rezultatu w postaci wybranego lub zrobionego zdjęcia
        if (isCameraUsed) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap image = (Bitmap) extras.get("data");
                binding.imageView.setImageBitmap(image);
            } else {
                showToastMessage("Nie zrobiono zdjęcia");
            }
            isCameraUsed = false;
        } else {
            if (resultCode == RESULT_OK) {
                try {
                    Uri imageUri = data.getData();
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    binding.imageView.setImageBitmap(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    showToastMessage("Wystąpił błąd");
                }
            } else {
                showToastMessage("Nie wybrano zdjecia");
            }
        }
    }
}
