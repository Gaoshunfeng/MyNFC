package com.example.vivi.mynfc;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private NfcAdapter mNfcAdapter;
    private TextView id,readcontext,dateggeshi;
   private Button read,xie;
    private static final String TAG = "LoyaltyCardReader";
    private static final byte[] SELECT_OK_SW = {(byte) 0x90, (byte) 0x00};
    private static final String SAMPLE_LOYALTY_CARD_AID = "F222222222";
    // ISO-DEP command HEADER for selecting an AID.
    // Format: [Class | Instruction | Parameter 1 | Parameter 2]
    private static final String SELECT_APDU_HEADER = "00A40400";
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private Context mContext;
    private String[][] mTechLists;
    private int BlockData;
    String info = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext=this;

        checkNFCFunction();//检查手机nfc功能是否开启

        init();

        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        mFilters = new IntentFilter[] { ndef, };
        mTechLists = new String[][] { new String[] { NfcA.class.getName() } };

    }
    /////////////////////////////////////
    @Override
    protected void onNewIntent(final Intent intent) {
        // TODO 自动生成的方法存根
        super.onNewIntent(intent);

        String intentActionStr = intent.getAction();// 获取到本次启动的action
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intentActionStr)// NDEF类型
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intentActionStr)// 其他类型
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intentActionStr)) {// 未知类型

            // 在intent中读取Tag id
            final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] ss=tag.getTechList();
            String s=null;
            for(int i=0;i<ss.length;i++)
            {
                s+=ss[i];s+=" ";
            }
            byte[] bytesId = tag.getId();// 获取id数组

            info = ByteArrayChange.ByteArrayToHexString(bytesId) + "\n";
            id.setText("标签UID:  " + "\n" + info);
            dateggeshi.setText("标签格式为："+"\n"+s);
            read.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   readcontext.setText("读取的内容为："+readTag(tag));
                }
            });
            xie.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    writeTag(tag);
                }
            });

        }
    }
///////////////////////////////////
    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);//禁止消息推送
        }
    }
/////////////////////////////////
    @Override
    protected void onResume() {
        // TODO 自动生成的方法存根
        super.onResume();
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent,
                    null, null);//当将发现的标签分发到应用程序时，这将优先考虑前台活动。
        }
    }
//////////////////////////////////////////
    private Dialog SetDialogWidth(Dialog dialog) {
        // TODO 自动生成的方法存根
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        if (screenWidth > screenHeight) {
            params.width = (int) (((float) screenHeight) * 0.875);

        } else {
            params.width = (int) (((float) screenWidth) * 0.875);
        }
        dialog.getWindow().setAttributes(params);

        return dialog;
    }


///////////////////////////////////////
    public int StringToInt(String s) {//将string转为int类型
        if (!(TextUtils.isEmpty(s)) || s.length() > 0) {
            BlockData = Integer.parseInt(s);
        } else {
            Toast.makeText(MainActivity.this, "Block输入有误", Toast.LENGTH_SHORT).show();
        }
        System.out.println(BlockData);
        return BlockData;
    }
/////////////////////////////////////////
    private void init() {
        // TODO 自动生成的方法存根
        id= (TextView) findViewById(R.id.id);
        read= (Button) findViewById(R.id.read);
        readcontext= (TextView) findViewById(R.id.readcontext);
        dateggeshi= (TextView) findViewById(R.id.geshi);
        xie= (Button) findViewById(R.id.xie);

    }
    /////////////////////////////////////
//    public String readTagIsoDep(Intent intent)
//    {
//        Parcelable p = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//        Tag tag=(Tag)p;
//        IsoDep isodep_ = IsoDep.get(tag);
//        if (isodep_ != null){
//            try {
//                isodep_.connect();
//                byte[] command = BuildSelectApdu(SAMPLE_LOYALTY_CARD_AID);
//                byte[] result = isodep_.transceive(command);
//
//                int resultLength = result.length;
//                byte[] statusWord = { result[resultLength - 2],
//                        result[resultLength - 1] };
//                byte[] payload = Arrays.copyOf(result, resultLength - 2);
//                if (Arrays.equals(SELECT_OK_SW, statusWord)) {
//                    // The remote NFC device will immediately respond with its
//                    // stored account number
//                    String accountNumber = new String(payload, "UTF-8");
//                    Log.i(TAG, "Received: " + accountNumber);
//                    // Inform CardReaderFragment of received account number
//                    return accountNumber;
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }
/////////////////
//public static byte[] BuildSelectApdu(String aid) {
//    // Format: [CLASS | INSTRUCTION | PARAMETER 1 | PARAMETER 2 | LENGTH | DATA]
//    return HexStringToByteArray(SELECT_APDU_HEADER + String.format("%02X", aid.length() / 2) + aid);
//}
/////////////////
//public static byte[] HexStringToByteArray(String s) {
//    int len = s.length();
//    byte[] data = new byte[len / 2];
//    for (int i = 0; i < len; i += 2) {
//        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
//                + Character.digit(s.charAt(i+1), 16));
//    }
//    return data;
//}
    public void writeTag(Tag tag){
        String[] techlist=tag.getTechList();
        MifareUltralight mifareUltralight=MifareUltralight.get(tag);
        try {
            mifareUltralight.connect();
            mifareUltralight.writePage(7, "班级".getBytes(Charset.forName("GB2312")));
            mifareUltralight.writePage(8, "的人".getBytes(Charset.forName("GB2312")));
            mifareUltralight.writePage(5, "我是".getBytes(Charset.forName("GB2312")));
            mifareUltralight.writePage(6, "软件".getBytes(Charset.forName("GB2312")));
            Toast.makeText(this, "写入完成", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                mifareUltralight.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
        ////////////////////////////////////////////
    public String readTag(Tag tag) {//读取数据
        MifareUltralight mifareUltralight=MifareUltralight.get(tag);
        for (String tech : tag.getTechList()) {
            System.out.println(tech);// 显示设备支持技术
        }
        boolean auth = false;
        // 读取TAG

        try {
            mifareUltralight.connect();
           String da=new String();
                for(int i=4;i<16;i=i+4){
                    byte[] data= mifareUltralight.readPages(i);
                    da=da+new String(data,Charset.forName("GB2312"));
                }

            return da;
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                mifareUltralight.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
            return null;
    }
/////////////////////////////
        private void checkNFCFunction() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);//获取适配器

        if (mNfcAdapter == null) {
            // mTextView.setText("NFC apdater is not available");
            Dialog dialog = null;
            AlertDialog.Builder customBuilder = new AlertDialog.Builder(
                    mContext);
            customBuilder
                    .setTitle("很遗憾")
                    .setMessage("没发现NFC设备，请确认您的设备支持NFC功能!")
                    .setIcon(R.drawable.add)
                    .setPositiveButton("是",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            });
            dialog = customBuilder.create();
            dialog.setCancelable(false);// back
            dialog.setCanceledOnTouchOutside(false);
            SetDialogWidth(dialog).show();
            return;
        } else {
            if (!mNfcAdapter.isEnabled()) {
                Dialog dialog = null;
                AlertDialog.Builder customBuilder = new AlertDialog.Builder(
                        mContext);
                customBuilder
                        .setTitle("提示")
                        .setMessage("请确认NFC功能是否开启!")
                        .setIcon(R.drawable.add)
                        .setPositiveButton("现在去开启......",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        dialog.dismiss();
                                        Intent setnfc = new Intent(
                                                Settings.ACTION_NFC_SETTINGS);
                                        startActivity(setnfc);
                                    }
                                });
                dialog = customBuilder.create();
                dialog.setCancelable(false);// back
                dialog.setCanceledOnTouchOutside(false);
                SetDialogWidth(dialog).show();
                return;
            }

        }

    }
}
