package com.inhatc.nfcmanager_source;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class NfcWrite extends AppCompatActivity {

    private NfcAdapter objNFCAdapter;
    private PendingIntent objPendingIntent;
    Intent objIntent;
    EditText objET_NFCMSG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_write);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("NFC Tag Writer");
        objET_NFCMSG = (EditText) findViewById(R.id.edtTagMsg);

        objNFCAdapter = NfcAdapter.getDefaultAdapter(this);
        objIntent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //objPendingIntent = PendingIntent.getActivity(this, 0, objIntent,0);
        objPendingIntent = PendingIntent.getActivity(this, 0, objIntent,PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        //종료 버튼 클릭
        Button btnReader = (Button) findViewById(R.id.btnExit);
        btnReader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if(objNFCAdapter != null) {
            objNFCAdapter.enableForegroundDispatch(this, objPendingIntent, null, null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(objNFCAdapter != null) {
            objNFCAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String strText = objET_NFCMSG.getText().toString();
        NdefMessage objMSG = getNdefMessage(strText);
        fNFC_Writer(objMSG, tagFromIntent);

    }

    private NdefMessage getNdefMessage(String text){
        byte[] textBytes = text.getBytes();
        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(), new byte[] {}, textBytes);
        NdefMessage objMSG = new NdefMessage(textRecord);
        return objMSG;
    }

    private boolean fNFC_Writer(NdefMessage message, Tag tagFromIntent) {
        try {
            Ndef objNdef = Ndef.get(tagFromIntent);
            if (objNdef != null) {
                objNdef.connect();
                objNdef.writeNdefMessage(message);
                objNdef.close();
                Toast.makeText(this, "Write NFC Tag !", Toast.LENGTH_LONG).show();
                return true;
            }
            return false;

        } catch (Exception e) {
            System.out.println(e);
            Toast.makeText(this, "Fail to write NFC tag!", Toast.LENGTH_LONG).show();
            return false;
        }

    }
}