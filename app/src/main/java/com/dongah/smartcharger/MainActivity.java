package com.dongah.smartcharger;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dongah.smartcharger.basefunction.ChargerConfiguration;
import com.dongah.smartcharger.basefunction.ChargingCurrentData;
import com.dongah.smartcharger.basefunction.ClassUiProcess;
import com.dongah.smartcharger.basefunction.ConfigurationKeyRead;
import com.dongah.smartcharger.basefunction.DumpDataSend;
import com.dongah.smartcharger.basefunction.FocusChangeEnabled;
import com.dongah.smartcharger.basefunction.FragmentChange;
import com.dongah.smartcharger.basefunction.FragmentCurrent;
import com.dongah.smartcharger.basefunction.GlobalVariables;
import com.dongah.smartcharger.basefunction.UiSeq;
import com.dongah.smartcharger.controlboard.ControlBoard;
import com.dongah.smartcharger.rfcard.RfCardReaderReceive;
import com.dongah.smartcharger.websocket.ocpp.core.Reason;
import com.dongah.smartcharger.websocket.socket.SocketReceiveMessage;
import com.dongah.smartcharger.websocket.socket.SocketState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {


    public static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    @SuppressLint("StaticFieldLeak")
    public static Context mContext;

    TextView textViewTime, textViewVersionValue;
    ImageView imgNetwork;
    Handler handler = new Handler();
    Runnable runnable;

    FragmentChange fragmentChange;
    ChargerConfiguration chargerConfiguration;
    ClassUiProcess classUiProcess;
    SocketReceiveMessage socketReceiveMessage;
    ControlBoard controlBoard;
    RfCardReaderReceive rfCardReaderReceive;
    ConfigurationKeyRead configurationKeyRead;
    DumpDataSend dumpDataSend;
    /**
     * current fragment Exception check
     */
    FragmentCurrent fragmentCurrent;

    public FragmentChange getFragmentChange() {
        return fragmentChange;
    }
    public ChargerConfiguration getChargerConfiguration() {
        return chargerConfiguration;
    }
    public ClassUiProcess getClassUiProcess() {
        return classUiProcess;
    }
    public SocketReceiveMessage getSocketReceiveMessage() {
        return socketReceiveMessage;
    }

    public ControlBoard getControlBoard() {
        return controlBoard;
    }
    public RfCardReaderReceive getRfCardReaderReceive() {
        return rfCardReaderReceive;
    }

    public ConfigurationKeyRead getConfigurationKeyRead() {
        return configurationKeyRead;
    }

    public DumpDataSend getDumpDataSend() {
        return dumpDataSend;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        /* 슬립 모드 방지*/
        super.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideNavigationBar();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        /* 세로 고정 */
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        mContext = this;
        fragmentCurrent = new FragmentCurrent();
        imgNetwork = findViewById(R.id.imgNetwork);
        textViewTime = findViewById(R.id.textViewTime);
        textViewVersionValue = findViewById(R.id.textViewVersionValue);
        textViewVersionValue.setText("Ver-" + GlobalVariables.VERSION + " | ");

        // 0. ConfigurationKey read */
        configurationKeyRead = new ConfigurationKeyRead();
        configurationKeyRead.onRead();
        // 1. charger configuration, ConfigurationKey read */
        chargerConfiguration = new ChargerConfiguration();
        chargerConfiguration.onLoadConfiguration();

        // 2. fragment change management */
        fragmentChange = new FragmentChange();
        fragmentChange.onFragmentChange(UiSeq.INIT, "INIT", "");
        // 3. Control board
        controlBoard = new ControlBoard(GlobalVariables.maxChannel, chargerConfiguration.getControlCom());
        // 4. rf card reade : MID = terminal ID */
        rfCardReaderReceive = new RfCardReaderReceive(chargerConfiguration.getRfCom());

        //5. ChargerOperate read
        File file = new File(GlobalVariables.getRootPath() + File.separator + "ChargerOperate");
        if (file.exists()) {
            FileReader fileReader;
            try {
                fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line;
                int count = 0;
                while ((line = bufferedReader.readLine()) != null) {
                    GlobalVariables.ChargerOperation[count] = Objects.equals(line, "true");
                    count++;
                }
            } catch (IOException e) {
                logger.error("ChargerOperate read error : {}", e.getMessage());
            }
        } else {
            for (int i = 0; i < GlobalVariables.maxPlugCount; i++) {
                GlobalVariables.ChargerOperation[i] = true;
            }
        }
//        //dongahelecomm url
//        String baseUrl = (GlobalVariables.getSecurityProfile().equals("2") ? "wss://" : "ws://") +  chargerConfiguration.getServerConnectingString() + ":" + chargerConfiguration.getServerPort() +
//                "/v2/" + chargerConfiguration.getChargerId();

        //6. web socket
        String baseUrl = chargerConfiguration.getServerConnectingString() + ":" + chargerConfiguration.getServerPort() + "/"
                + chargerConfiguration.getChargerId() ;
        //configuration key == null 인지 확인
        socketReceiveMessage = new SocketReceiveMessage(Objects.equals(GlobalVariables.getServerURL(), "") ? baseUrl : GlobalVariables.getServerURL());

        // 7. classUiProcess */
        classUiProcess = new ClassUiProcess(1);

        // 8. DumpDataSend
        dumpDataSend = new DumpDataSend();
    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        try {
            // channel argument check
            if (fragmentCurrent.getCurrentFragment() instanceof FocusChangeEnabled) {
                ((FocusChangeEnabled) fragmentCurrent.getCurrentFragment()).onWindowFocusChanged(hasFocus);
            }
            if (hasFocus) {
                hideNavigationBar();
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     * ui version update
     */
    public void onRebooting() {
        try {
            boolean result = false;
            ChargingCurrentData chargingCurrentData;
            chargingCurrentData = getClassUiProcess().getChargingCurrentData();
            result = chargingCurrentData.isReBoot() && (getClassUiProcess().getUiSeq() == UiSeq.INIT);

            if (result) {
                getClassUiProcess().setUiSeq(UiSeq.REBOOTING);
                getClassUiProcess().getChargingCurrentData().setStopReason(Reason.Reboot);
            }

        } catch (Exception e) {
            logger.error(" version reboot : {}", e.getMessage());
        }
    }

    public void onRebooting(String type) {
        try {
            ((MainActivity) MainActivity.mContext).getSocketReceiveMessage().getSocket().disconnect();
            if (Objects.equals(type, "Soft")) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.dongah.autoupdates", "com.dongah.autoupdates.MainActivity"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent); // 새 앱 실행
                    overridePendingTransition(0, 0);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        ActivityCompat.finishAffinity(MainActivity.this); // 모든 액티비티 종료
                        System.exit(0);
                    }, 100); // 200ms 딜레이
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }

            } else {
                PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                powerManager.reboot("reboot");
            }
        } catch (Exception e) {
            logger.error("onRebooting : {}", e.getMessage());
        }
    }

    public void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        runnable = new Runnable() {
            @Override
            public void run() {
                updateTime();
                // 1초마다 실행
                handler.postDelayed(this, 1000);
                try {
                    if (socketReceiveMessage.getSocket().getState() != null) {
                        imgNetwork.setBackgroundResource(socketReceiveMessage.getSocket().getState() == SocketState.OPEN ?
                                R.drawable.network : R.drawable.nonetwork);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        };
        runnable.run();
    }

    private void updateTime() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String currentTime = sdf.format(new Date());
            textViewTime.setText(currentTime);
        } catch (Exception e){
            logger.error(e.getMessage());
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        classUiProcess.stopEventLoop();
    }

}