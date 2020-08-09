package com.example.dangeroussituationv3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

    ArrayAdapter<String> adapter;
    EditText editText;
    Button envoi;
    ArrayList<String> itemList;
    String phoneNumber = "numberPhone";
    int numberLine = -1;
    String keyNumberLine = "numberLine";
    int LIMIT_SIZE = 10, MIN_SIZE = 0, MAX_SIZE = 20;
    private FusedLocationProviderClient client;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);

        client = LocationServices.getFusedLocationProviderClient(this);
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);

        //preferences.edit().clear().apply();

        String[] items = {};
        int j = 0;
        String initValue = "+33637334475", Number;


        itemList = new ArrayList<String>(Arrays.asList(items));
        adapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.txtview, itemList);

        numberLine = preferences.getInt(keyNumberLine, 0);

        Number = preferences.getString(phoneNumber + j, "");
        if (Number.length() == 0 || numberLine == 0)
        {
            preferences.edit().putString(phoneNumber + j, initValue).apply();
            itemList.add(0, initValue);

            adapter.notifyDataSetChanged();

            preferences.edit().putInt(keyNumberLine, 1).apply();
            numberLine = 1;
        }
        else {
            for (j = 0; j < LIMIT_SIZE; j++)
            {
                Number = preferences.getString(phoneNumber + j, "");
                if (Number.length() != 0)
                {
                    itemList.add(j, Number);
                    adapter.notifyDataSetChanged();
                }
            }
        }

        ListView listV = (ListView) findViewById(R.id.list);
        listV.setAdapter(adapter);
        listV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Show input box
                showInputBox(itemList.get(position), position);
            }
        });

        listV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                int i = 0;
                String prefAct, prefNext;
                SharedPreferences preferences = getPreferences(MODE_PRIVATE);
                numberLine = preferences.getInt(keyNumberLine, 0);
                if (numberLine > 0)
                {
                    numberLine--;
                    preferences.edit().putInt(keyNumberLine, numberLine).apply();

                    for (i = position; i < (LIMIT_SIZE - 1); i++)
                    {
                        prefNext = preferences.getString(phoneNumber + (i + 1), "");
                        if (prefNext.length() != 0)
                            preferences.edit().putString(phoneNumber + i, prefNext).apply();
                        else
                            preferences.edit().putString(phoneNumber + i, "").apply();
                        preferences.edit().putString(phoneNumber + (i + 1), "").apply();
                    }
                    preferences.edit().putString(phoneNumber + "9", "").apply();

                    itemList.remove(position);
                    adapter.notifyDataSetChanged();

                    Toast.makeText(getApplicationContext(), "Suppression de l'élément " + (position + 1), Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });

        editText = (EditText) findViewById(R.id.txtInput);

        Button btAdd = (Button) findViewById(R.id.btAdd);
        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newItem = editText.getText().toString();
                String i;
                SharedPreferences preferences = getPreferences(MODE_PRIVATE);
                numberLine = preferences.getInt(keyNumberLine, 0);

                if (numberLine < LIMIT_SIZE) {
                    if (newItem.length() == MIN_SIZE)
                        Toast.makeText(getApplicationContext(), "IMPOSSIBLE!!! L'emplacement doit être non vide...", Toast.LENGTH_SHORT).show();
                    else if (newItem.length() > MAX_SIZE)
                        Toast.makeText(getApplicationContext(), "IMPOSSIBLE!!! L'emplacement est trop petit", Toast.LENGTH_SHORT).show();
                    else {
                        // add new item to arraylist
                        itemList.add(newItem);

                        //i = String.valueOf(numberLine);
                        preferences.edit().putString(phoneNumber + numberLine, newItem).apply();
                        numberLine++;
                        preferences.edit().putInt(keyNumberLine, numberLine).apply();

                        // notify listview of data changed
                        adapter.notifyDataSetChanged();
                        if (numberLine == LIMIT_SIZE)
                            Toast.makeText(getApplicationContext(), "Nombre de contacts MAX atteind", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                    Toast.makeText(getApplicationContext(), "Nombre de contacts MAX atteind", Toast.LENGTH_SHORT).show();
            }
        });


        client = LocationServices.getFusedLocationProviderClient(this);

        envoi = (Button) findViewById(R.id.btnEnvoi);
        envoi.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    return;

                client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        String chaine = location.toString();
                        if (chaine.length() != 0)
                        {
                            for ( int i = 0; i < itemList.size(); i++)
                            {
                                SmsManager.getDefault().sendTextMessage(itemList.get(i), null, "Bonjour, il y a une situation dangereuse: " + location.toString(), null, null);
                            }
                        }
                        else
                            Toast.makeText(getApplicationContext(), "Location a NULL", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void showInputBox(String oldItem, final int index) {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setTitle("Input Box");
        dialog.setContentView(R.layout.input_box);

        TextView txtMessage = (TextView) dialog.findViewById(R.id.txtmessage);
        txtMessage.setText("Update item");
        txtMessage.setTextColor(Color.parseColor("#ff2222"));

        final EditText editText = (EditText) dialog.findViewById(R.id.txtinput);
        editText.setText(oldItem);

        Button bt = (Button) dialog.findViewById(R.id.btdone);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPhoneNumber = editText.getText().toString();

                if (newPhoneNumber.length() == MIN_SIZE)
                    Toast.makeText(getApplicationContext(), "IMPOSSIBLE!!! L'emplacement doit être non vide...", Toast.LENGTH_SHORT).show();
                else if (newPhoneNumber.length() > MAX_SIZE)
                    Toast.makeText(getApplicationContext(), "IMPOSSIBLE!!! L'emplacement est trop petit", Toast.LENGTH_SHORT).show();
                else {
                    SharedPreferences preferences = getPreferences(MODE_PRIVATE);
                    preferences.edit().putString(phoneNumber + index, newPhoneNumber).apply();

                    itemList.set(index, newPhoneNumber);
                    adapter.notifyDataSetChanged();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}

