package com.h4413.recyclyon.Activities.Deposit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.vision.barcode.Barcode;
import com.h4413.recyclyon.Activities.HomeActivity;
import com.h4413.recyclyon.Model.User;
import com.h4413.recyclyon.R;
import com.h4413.recyclyon.Services.UserServices;
import com.h4413.recyclyon.Utilities.HttpClient;
import com.h4413.recyclyon.Utilities.IntentExtraKeys;
import com.h4413.recyclyon.Utilities.Routes;

import org.json.JSONException;
import org.json.JSONObject;


public class DepositInProgressActivity extends AppCompatActivity {

    private final static int REQUEST_CODE_END_DEPOSIT = 1;
    private final static int REQUEST_CODE_ERROR = 2;

    private Barcode mQRCode;

    private Button mFinishButton;

    private ImageView mImage;

    private String idDepot;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit_in_progress);
        configureToolbar();

        Intent intent = getIntent();
        mQRCode = intent.getParcelableExtra("QRCode");


        mFinishButton = (Button) findViewById(R.id.deposit_progress_button);

        mUser = UserServices.getCurrentUserFromSharedPreferences(this);
        JSONObject body = new JSONObject();
        try {
            body.put("idUtilisateur", mUser._id);
            body.put("idAssoc", mUser.idAssoc);
            body.put("idCapteur", mQRCode.displayValue);
        } catch (JSONException e) { e.printStackTrace(); }

        HttpClient.POST(Routes.BeginDeposit, null, body.toString(), DepositInProgressActivity.this, new HttpClient.OnResponseCallback() {
            @Override
            public void onJSONResponse(int statusCode, JSONObject response) {
                if(statusCode == 200) {
                    try {
                        idDepot = response.get("_id").toString();
                    } catch (JSONException e) {e.printStackTrace();}
                } else {
                    Intent intent = new Intent(DepositInProgressActivity.this, DepositRejectionActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_ERROR);
                }
            }
        });

        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject body = new JSONObject();
                try {
                    body.put("montant", Integer.parseInt("0"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                HttpClient.POST(Routes.FinishDeposit, mQRCode.displayValue, body.toString(), DepositInProgressActivity.this, new HttpClient.OnResponseCallback() {
                    @Override
                    public void onJSONResponse(int statusCode, JSONObject response) {
                        if(statusCode != 200)
                        {
                            Intent intent = new Intent(DepositInProgressActivity.this, HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            return;
                        }
                        String idAssoc = "";
                        String montant = "";
                        try {
                            idAssoc = response.get("idAssoc").toString();
                            montant = response.get("montant").toString();
                        } catch (JSONException e) {e.printStackTrace();}
                        Intent intent = new Intent(DepositInProgressActivity.this, DepositEndActivity.class);
                        intent.putExtra(IntentExtraKeys.DEPOT_MONTANT_KEY, montant);
                        intent.putExtra(IntentExtraKeys.ID_ASSOC_KEY, idAssoc);
                        startActivityForResult(intent, REQUEST_CODE_END_DEPOSIT);
                        finish();
                    }
                });
            }
        });

        mImage = (ImageView) findViewById(R.id.deposit_in_progress_image);

        float centerX = this.getResources().getDisplayMetrics().widthPixels/2;
        float centerY = this.getResources().getDisplayMetrics().heightPixels/2;
        RotateAnimation anim = new RotateAnimation(355f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(1900);

        mImage.startAnimation(anim);
    }

    private void configureToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_deposit_in_progress);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(resultCode);
        finish();
    }

    @Override
    public void onBackPressed() {return;}
}
