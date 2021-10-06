package customphonedialer.abror96.customphonedialer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
//import androidx.appcompat.app.AppCompatActivity;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import customphonedialer.abror96.customphonedialer.R;
import kotlin.collections.ArraysKt;

import static android.Manifest.permission.CALL_PHONE;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
import static android.telecom.TelecomManager.ACTION_CHANGE_DEFAULT_DIALER;
import static android.telecom.TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME;

public class DialerActivity extends AppCompatActivity {
    TextView callinfo;

    @BindView(R.id.phoneNumberInput)
    EditText phoneNumberInput;

    public static int REQUEST_PERMISSION = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialer);

        //リスナー設定
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        telephonyManager.listen(mListener, PhoneStateListener.LISTEN_CALL_STATE);



        ButterKnife.bind(this);

        if (getIntent() != null && getIntent().getData() != null)
            phoneNumberInput.setText(getIntent().getData().getSchemeSpecificPart());

    }

    PhoneStateListener mListener = new PhoneStateListener(){
        @Override
        public void onCallStateChanged(int state, String callNumber) {
            //Log.d(TAG, ":" + state+"-PhoneNumber:"+callNumber);
            switch(state){
                case TelephonyManager.CALL_STATE_IDLE:      //待ち受け（終了時）
                    Toast.makeText(DialerActivity.this, "通話終了\nCALL_STATE_IDLE", Toast.LENGTH_LONG).show();
                    break;
                case TelephonyManager.CALL_STATE_RINGING:   //着信*
                    if(callNumber==null){
                        callNumber="";
                    }
                    Toast.makeText(DialerActivity.this, "着信中\nCALL_STATE_RINGING: " + callNumber, Toast.LENGTH_SHORT).show();
                    callinfo.setText("着信："+callNumber);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:   //通話
                    Toast.makeText(DialerActivity.this, "通話中\nCALL_STATE_OFFHOOK", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };






    @Override
    protected void onStart() {
        super.onStart();
        offerReplacingDefaultDialer();

        phoneNumberInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                makeCall();
                return true;
            }
        });
    }

    private void makeCall() {
        if (android.support.v4.content.PermissionChecker.checkSelfPermission(this, CALL_PHONE) == PERMISSION_GRANTED) {
            Uri uri = Uri.parse("tel:"+phoneNumberInput.getText().toString().trim());
            startActivity(new Intent(Intent.ACTION_CALL, uri));
        }
    }

    private void offerReplacingDefaultDialer() {
        TelecomManager telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);

        if (!getPackageName().equals(telecomManager.getDefaultDialerPackage())) {
            Intent intent = new Intent(ACTION_CHANGE_DEFAULT_DIALER)
                    .putExtra(EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION && ArraysKt.contains(grantResults, PERMISSION_GRANTED)) {
            makeCall();
        }
    }
}
