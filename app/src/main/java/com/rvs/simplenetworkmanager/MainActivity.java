// ****************

// Производство: Соловьев Роман, 10.12.2021
// На разработку ушел 21 день с учетом плавного изучения работы Android Studio,
// специальных классов и методов.

// Сделано на заказ: проект по предмету "Компьютерные сети".

// *****************

package com.rvs.simplenetworkmanager;

//Импорт библиотек.

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "main"; //Тег для определения ошибки в терминале Android Studio.
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 100;
    // Случайное уникальное число для запроса прав. Используется для идентификатора доступа.
    private Button buttonActivate; // Главная кнопка на экране.
    private TextView connectionText, ipText, macText, frequencyText, ssidText, linkSpdText, bssidText;
    // Текстовые не изменяемые в ходе программы поля на экране.
    private TextView connectionTextValue, ipTextValue, macTextValue, frequencyTextValue, ssidTextValue,
    linkSpdTextValue, bssidTextValue; // Изменяемые текстовые поля на экране. Содержат сначала значение "null".
    // Используются для вывода полученных значений.
    private boolean isEnabledButton = true; // Идентификатор состояния кнопки.
    private boolean isOnWorkingMode = false;
    // True означает, что кнопка включена, т.е. при нажатии приложение запустится.
    // False означает, что кнопка находится во втором состоянии, т.е. при нажатии данные о сети
    // уничтожатся.

    //private String api_url = "http://api.icndb.com/jokes/random";

    //Главный метод приложения. Запускает MainActivity - главный экран.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Нужные функции
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Для того, чтобы использовать элемент экрана, его нужно "найти". Для этого заполняются
        // приватные поля класса. В дальнейшем планируется ввод подсказок при нажатии на неизменяемые
        // текстовые поля, поэтому тут они тоже есть.

        buttonActivate = findViewById(R.id.btnActivate);
        connectionText = findViewById(R.id.isConnectedText);
        ipText = findViewById(R.id.ipText);
        macText = findViewById(R.id.MACText);
        frequencyText = findViewById(R.id.frequencyText);
        ssidText = findViewById(R.id.ssidText);
        linkSpdText = findViewById(R.id.linkSpdText);
        bssidText = findViewById(R.id.bssidText);
        connectionTextValue = findViewById(R.id.connectionTextValue);
        ipTextValue = findViewById(R.id.ipTextValue);
        macTextValue = findViewById(R.id.macTextValue);
        frequencyTextValue = findViewById(R.id.frequencyTextValue);
        ssidTextValue = findViewById(R.id.ssidTextValue);
        linkSpdTextValue = findViewById(R.id.linkSpdTextValue);
        bssidTextValue = findViewById(R.id.bssidTextValue);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint("SetTextI18n") // Нотация для перевода текста.
    //Метод, срабатывающий при нажатии на кнопку.
    public void onClick (View v) {
        if (isEnabledButton && !isOnWorkingMode) {
            buttonActivate.setText("Clear");
            isOnWorkingMode = true;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isOnWorkingMode) {
                        WifiManager manager = (WifiManager) getApplicationContext().
                                getSystemService(Context.WIFI_SERVICE);
                        ConnectivityManager connManager = (ConnectivityManager)
                                getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo mWifi = connManager.
                                getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                        if (!manager.isWifiEnabled()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    connectionTextValue.setText("Wifi disabled");
                                    clearValues();
                                    Toast.makeText(getApplicationContext(), "Please restart the app.",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                            isEnabledButton = false;
                            return;
                        }
                        if (mWifi.isConnected()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    connectionTextValue.setText("Active");
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    connectionTextValue.setText("None connected");
                                    clearValues();
                                    Toast.makeText(getApplicationContext(), "Please restart the app",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                            isEnabledButton = false;
                            return;
                        }
                        isEnabledButton = false;
                        // Присваивание всем полям нужного значения.
                        String IP = null;
                        try {
                            IP = getIP();
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }

                        String finalIP = IP;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ipTextValue.setText(finalIP);
                                String MAC = getMACAddress("wlan0");
                                macTextValue.setText(MAC);
                                int frequency = getFrequency();
                                frequencyTextValue.setText(String.valueOf(frequency));
                                String ssid = getSSID();
                                ssidTextValue.setText(ssid);
                                int linkSpd = getLinkSpeed();
                                linkSpdTextValue.setText(String.valueOf(linkSpd).concat(" Mbps"));
                                String bssid = getRouterMac();
                                bssidTextValue.setText(bssid);
                            }
                        });


                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }).start();

        } else {
            buttonActivate.setText("Know about your network");
            isEnabledButton = true;
            isOnWorkingMode = false;
            connectionTextValue.setText("null");
            clearValues();
        }
    }

    @SuppressLint("SetTextI18n")
    private void clearValues() {
        ipTextValue.setText("null");
        macTextValue.setText("null");
        frequencyTextValue.setText("null");
        ssidTextValue.setText("null");
        linkSpdTextValue.setText("null");
        bssidTextValue.setText("null");
    }

    public String getIP () throws UnknownHostException {
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiinfo = manager.getConnectionInfo();
        byte[] myIPAddress = BigInteger.valueOf(wifiinfo.getIpAddress()).toByteArray();
        for (int i = 0; i < myIPAddress.length / 2; i++) {
            byte temp;
            temp = myIPAddress[i];
            myIPAddress[i] = myIPAddress[myIPAddress.length - i - 1];
            myIPAddress[myIPAddress.length - i - 1] = temp;
        }
        InetAddress myInetIP = InetAddress.getByAddress(myIPAddress);
        return myInetIP.getHostAddress();
    }



    @Deprecated
    public String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface anInterface : interfaces) {
                if (interfaceName != null) {
                    if (!anInterface.getName().equalsIgnoreCase(interfaceName)) continue;
                }
                byte[] mac = anInterface.getHardwareAddress();
                if (mac==null) return "null";
                StringBuilder buf = new StringBuilder();
                for (byte b : mac) buf.append(String.format("%02X:", b));
                if (buf.length()>0) buf.deleteCharAt(buf.length()-1);
                return buf.toString();
            }
        } catch (Exception ignored) { }
        return "null";
    }

    // Метод для возвращения частоты сети.
    public int getFrequency() {
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        return info.getFrequency();
    }

    //Метод для возвращения SSID.
    public String getSSID() {
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();

        //Здесь нужно разрешение на использование геоданных. Такова специфика работы служб WiFi.
        if ( Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.
                    permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_PERMISSIONS); // Запрашиваем эти права с этим уникальным кодом.
                Log.i(LOG_TAG, "User location NOT ENABLED, waiting for permission");
                Toast.makeText(this, "Please enable location service.", Toast.LENGTH_SHORT)
                        .show();
            }
        }
        // SSID выдается с кавычками. Тут они не нужны.
        return info.getSSID().replace("\"", "");
    }

    // Метод для возвращения скорости сети.
    public int getLinkSpeed() {
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        return info.getLinkSpeed();
    }

    //Незаконченный метод определения типа защиты сети.

    public String getRouterMac() {
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        return info.getBSSID();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public String getSecurityType() {
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        int res = info.getCurrentSecurityType();
        switch (res) {
            case WifiInfo.SECURITY_TYPE_OPEN:
                return "Open Network";
            case WifiInfo.SECURITY_TYPE_EAP:
                return "EAP";
            case WifiInfo.SECURITY_TYPE_OWE:
                return "OWE";
            case WifiInfo.SECURITY_TYPE_EAP_WPA3_ENTERPRISE:
                return "WPA3_Enterprise";
            case WifiInfo.SECURITY_TYPE_EAP_WPA3_ENTERPRISE_192_BIT:
                return "WPA3_ENTERPRISE_192_BIT";
            case WifiInfo.SECURITY_TYPE_WEP:
                return "WEP";
            case WifiInfo.SECURITY_TYPE_WAPI_PSK:
                return "WAPI_PSK";
            case WifiInfo.SECURITY_TYPE_WAPI_CERT:
                return "WAPI_CERT";
            case WifiInfo.SECURITY_TYPE_PSK:
                return "PSK";
            case WifiInfo.SECURITY_TYPE_PASSPOINT_R1_R2:
                return "R1-R2 Passpoint";
            case WifiInfo.SECURITY_TYPE_PASSPOINT_R3:
                return "R3 Passpoint";
            case WifiInfo.SECURITY_TYPE_SAE:
                return "SAE";
            default:
                return "null";
            case WifiInfo.SECURITY_TYPE_UNKNOWN:
                return "unknown";
        }
    }


    public void onClickChuck(View v) {
        startActivity(new Intent(this, NorrisActivity.class));
    }
}