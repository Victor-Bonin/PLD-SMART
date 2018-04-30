package com.h4413.recyclyon.Activities;

import android.bluetooth.BluetoothClass;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.h4413.recyclyon.Activities.Connection.InscriptionActivity;
import com.h4413.recyclyon.Listeners.NavigationItemSelectedListener;
import com.h4413.recyclyon.Model.User;
import com.h4413.recyclyon.R;
import com.h4413.recyclyon.Services.UserServices;
import com.h4413.recyclyon.Utilities.HttpClient;
import com.h4413.recyclyon.Utilities.NavbarInitializer;
import com.h4413.recyclyon.Utilities.Routes;
import com.h4413.recyclyon.Utilities.SharedPreferencesKeys;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    private Spinner mSexInput;
    private EditText mAdressInput;
    private EditText mDateNaissanceInput;

    private Button mChangeButton;
    private Button mCancelButton;
    private Button mSubmitButton;
    private ImageButton associationChangeButton;

    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        NavbarInitializer.initNavigationMenu(this, R.id.nav_account, R.string.title_activity_account);

        mUser = new User();

        mSubmitButton = (Button) findViewById(R.id.profile_activity_submit_btn);
        mCancelButton = (Button)findViewById(R.id.profile_activity_cancel_btn);
        mSexInput = (Spinner) findViewById(R.id.profile_activity_gender_input);
        mAdressInput = (EditText) findViewById(R.id.profile_activity_adress_input);
        mDateNaissanceInput = (EditText) findViewById(R.id.profile_activity_date_input);
        mChangeButton = (Button) findViewById(R.id.profile_activity_modification_btn);
        associationChangeButton=(ImageButton) findViewById(R.id.profile_activity_btn_change_association);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.inscriptionSexeChoices, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSexInput.setAdapter(adapter);


        mUser = UserServices.getCurrentUserFromSharedPreferences(this);
        mAdressInput.setText(mUser.adresse);
        SimpleDateFormat simpleDate =  new SimpleDateFormat("dd/MM/yyyy");
        String strDt = simpleDate.format(mUser.dateNaissance);
        mDateNaissanceInput.setText(strDt);
        mSexInput.setSelection(mUser.sexe+1);
        
        //Verouille les champs sans le click sur modifier
        mSexInput.setEnabled(false);
        mAdressInput.setEnabled(false);
        mDateNaissanceInput.setEnabled(false);

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disableInputs();
            }
        });

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUser.adresse = mAdressInput.getText().toString();
                Date date = null;
                if(!mDateNaissanceInput.getText().toString().equals("")) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    date = new Date();
                    try {
                        date = sdf.parse(mDateNaissanceInput.getText().toString());
                    } catch (ParseException e) {
                        Toast.makeText(getApplicationContext(), "Format de date incorrect : dd/mm/yyyy", Toast.LENGTH_LONG).show();
                    }
                }
                mUser.dateNaissance = date;
                mUser.sexe = mSexInput.getSelectedItemPosition()-1;
                JSONObject obj = new JSONObject();
                try {
                    obj.put("mail", mUser.mail);
                    obj.put("motDePasse", mUser.motDePasse);
                    obj.put("nom", mUser.nom);
                    obj.put("adresse", mUser.adresse);
                    obj.put("dateNaissance", mUser.dateNaissance);
                    obj.put("sexe", mUser.sexe);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                HttpClient.PUT(Routes.Users, mUser._id, obj.toString(), ProfileActivity.this, new HttpClient.OnResponseCallback() {
                    @Override
                    public void onJSONResponse(int statusCode, JSONObject response) {
                        if(statusCode == 200) {
                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            sharedPref.edit().putString(SharedPreferencesKeys.USER_KEY, response.toString()).apply();
                        } else {
                            Toast.makeText(getApplicationContext(), "Erreur dans le changement de votre profil", Toast.LENGTH_LONG).show();
                        }
                        disableInputs();
                    }
                });
            }
        });

        mChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSexInput.setEnabled(true);
                mAdressInput.setEnabled(true);
                mDateNaissanceInput.setEnabled(true);

                findViewById(R.id.profile_activity_association_layout).setVisibility(View.GONE);
                mChangeButton.setVisibility(View.GONE);
                findViewById(R.id.profile_activity_modification_layout).setVisibility(View.VISIBLE);
            }
        });


    }

    private void disableInputs() {
        mSexInput.setEnabled(false);
        mAdressInput.setEnabled(false);
        mDateNaissanceInput.setEnabled(false);

        findViewById(R.id.profile_activity_association_layout).setVisibility(View.VISIBLE);
        mChangeButton.setVisibility(View.VISIBLE);
        findViewById(R.id.profile_activity_modification_layout).setVisibility(View.GONE);

        mAdressInput.setText(mUser.adresse);
        SimpleDateFormat simpleDate =  new SimpleDateFormat("dd/MM/yyyy");
        String strDt = simpleDate.format(mUser.dateNaissance);
        mDateNaissanceInput.setText(strDt);
        mSexInput.setSelection(mUser.sexe+1);
    }
}