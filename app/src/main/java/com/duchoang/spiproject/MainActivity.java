package com.duchoang.spiproject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import android.util.Log;
import com.google.android.things.pio.PeripheralManager;
import com.galarzaa.androidthings.Rc522;
import com.google.android.things.pio.SpiDevice;

public class MainActivity extends Activity {
    private static final String TAG = "RFID@DucHoang";

    // LED RGB indication
    private Gpio mLedGpioGreen;
    private Gpio mLedGpioRed;
    private Gpio mLedGpioBlue;

    // MFRC522
    private Rc522 mRc522;

    // Handler
    private Handler mHandlerCheckRFID = new Handler();
    private Handler mHandlerLed = new Handler();

    // Present flag
    private boolean presentFlag = false;

    // Membership
    private String[] memberList = {"1610755", "1613346", "1513249", "1611830"};
    private int ledState = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            SpiDevice spiDevice = PeripheralManager.getInstance().openSpiDevice("SPI0.0");
            Gpio resetPin = PeripheralManager.getInstance().openGpio("BCM25");

            // mRc522
            mRc522 = new Rc522(spiDevice, resetPin);

            // Name ports
            mLedGpioRed = PeripheralManager.getInstance().openGpio("BCM2");
            mLedGpioGreen = PeripheralManager.getInstance().openGpio("BCM3");
            mLedGpioBlue = PeripheralManager.getInstance().openGpio("BCM4");

            // Define them as outputs
            mLedGpioRed.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mLedGpioGreen.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mLedGpioBlue.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

            // Assign initial states
            mLedGpioRed.setActiveType(Gpio.ACTIVE_LOW);
            mLedGpioGreen.setActiveType(Gpio.ACTIVE_LOW);
            mLedGpioBlue.setActiveType(Gpio.ACTIVE_LOW);
        } catch(Exception e) {
            Log.w(TAG, "Error on opening GPIO ports.");
        }
        mHandlerLed.post(mLedRunnable);
        mHandlerCheckRFID.post(mRc522Runnable);

    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        if (mLedGpioBlue != null && mLedGpioGreen != null && mLedGpioRed != null) {
            try {
                mLedGpioGreen.close();
                mLedGpioRed.close();
                mLedGpioBlue.close();
            } catch (Exception e) {
                Log.w(TAG, "Error on closing ports.");
            } finally {
                mLedGpioBlue = null;
                mLedGpioRed = null;
                mLedGpioGreen = null;
            }
        }
    }

    // Check for any tags if present
    private Runnable mRc522Runnable = new Runnable() {
        @Override
        public void run() {
//            writeToRFID("Quang", Rc522.getBlockAddress(15, 0));
//            writeToRFID("24/08/1998", Rc522.getBlockAddress(15, 1));
//            writeToRFID("1610000", Rc522.getBlockAddress(15, 2));
//            Log.i(TAG, "Done!!!");


            // Name
            String name = readFromRFID(Rc522.getBlockAddress(15, 0));
            Log.i(TAG, name);

            // DOB
            String DOB = readFromRFID(Rc522.getBlockAddress(15, 1));
            Log.i(TAG, DOB);

            // ID
            String ID = readFromRFID(Rc522.getBlockAddress(15, 2));
            Log.i(TAG, ID);



            if (presentFlag) {
                for (int i = 0; i < 4; i++) {
                    if (ID.equals(memberList[i])) {
                        ledState = 0;
                        presentFlag = false;
                        break;
                    }
                    ledState = 4;
                    case1Counter = 0;
                }
                presentFlag = false;
            }
            mHandlerCheckRFID.postDelayed(this, 500);
        }
    };


    // State:
    // 0: member present
    // 1: not a member present
    // 2: default state
    // 3: just for blinking purpose
    private int case1Counter = 0;
    private Runnable mLedRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                switch (ledState) {
                    case 0: {
                        mLedGpioRed.setValue(false);
                        mLedGpioGreen.setValue(true);
                        mLedGpioBlue.setValue(false);
                        ledState = 2;
                        break;
                    }
                    case 1: {
                        if (case1Counter <= 5) {
                            mLedGpioRed.setValue(true);
                            mLedGpioGreen.setValue(false);
                            mLedGpioBlue.setValue(false);
                            case1Counter++;
                        }
                        else {
                            ledState = 2;
                            case1Counter = 0;
                            break;
                        }
                        ledState = 3;
                        break;
                    }
                    case 2: {
                        mLedGpioRed.setValue(false);
                        mLedGpioGreen.setValue(false);
                        mLedGpioBlue.setValue(true);
                        break;
                    }
                    case 3: {
                        mLedGpioRed.setValue(false);
                        mLedGpioGreen.setValue(false);
                        mLedGpioBlue.setValue(false);
                        ledState = 1;
                        break;
                    }
                    case 4: {
                        mLedGpioRed.setValue(true);
                        mLedGpioGreen.setValue(false);
                        mLedGpioBlue.setValue(false);
                        ledState = 2;
                        break;
                    }

                }
            } catch (Exception e) {
                Log.w(TAG, "Error when displaying LEDs!");
            }
            mHandlerLed.postDelayed(this, 100);
        }
    };

//    public void readFromRFID(byte[] readBuffer, byte block) {
//        while (true){
//            boolean success = mRc522.request();
//            if(!success){
//                continue;
//            }
//            success = mRc522.antiCollisionDetect();
//            if(!success){
//                continue;
//            }
//            byte[] uid = mRc522.getUid();
//            mRc522.selectTag(uid);
//            break;
//        }
//
//        // Factory Key A:
//        byte[] key = {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
//        boolean result = mRc522.authenticateCard(Rc522.AUTH_A, block, key);
//        if (!result) {
//            return;
//        }
//        result = mRc522.readBlock(block, readBuffer);
//        if (!result) {
//            return;
//        }
//
//        mRc522.stopCrypto();
//    }

    public void dumpInfo() {
        while (true){
            boolean success = mRc522.request();
            if(!success){
                continue;
            }
            success = mRc522.antiCollisionDetect();
            if(!success){
                continue;
            }
            byte[] uid = mRc522.getUid();
            mRc522.selectTag(uid);
            break;
        }

        String temp = mRc522.dumpMifare1k();
        Log.i(TAG, temp);
    }

    public String readFromRFID(byte block) {
        byte[] readBuffer = new byte[16];
        while (true){
            boolean success = mRc522.request();
            if(!success){
                continue;
            }
            success = mRc522.antiCollisionDetect();
            if(!success){

                continue;
            }
            byte[] uid = mRc522.getUid();
            mRc522.selectTag(uid);
            break;
        }

        presentFlag = true;

        // Factory Key A:
        byte[] key = {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
        boolean result = mRc522.authenticateCard(Rc522.AUTH_A, block, key);
        if (!result) {
            return "";
        }
        result = mRc522.readBlock(block, readBuffer);
        if (!result) {
            return "";
        }
        mRc522.stopCrypto();
        String tempString = new String(readBuffer);
        String returnString = "";
        for (int i = 0; i < 16; i++) {
            if (tempString.charAt(i) == ' ') {
                returnString = tempString.substring(0, i);
                break;
            }
        }
        return returnString;
    }

    public void writeToRFID(String str, byte block) {
        String tempString = str;
        for (int i = str.length(); i < 16; i++) {
            tempString = tempString + " ";
        }
        byte[] writeBuffer = tempString.getBytes();

        while(true){
            boolean success = mRc522.request();
            if(!success){
                continue;
            }
            success = mRc522.antiCollisionDetect();
            if(!success){
                continue;
            }
            byte[] uid = mRc522.getUid();
            mRc522.selectTag(uid);
            break;
        }

        // Factory Key A:
        byte[] key = {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
        boolean result = mRc522.authenticateCard(Rc522.AUTH_A, block, key);
        if (!result) {
            return;
        }
        result = mRc522.writeBlock(block, writeBuffer);
        if (!result) {
            return;
        }
        mRc522.stopCrypto();
    }
}
