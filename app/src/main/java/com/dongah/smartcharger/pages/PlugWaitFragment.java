package com.dongah.smartcharger.pages;

import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.R;
import com.dongah.smartcharger.basefunction.ChargerConfiguration;
import com.dongah.smartcharger.basefunction.ChargingCurrentData;
import com.dongah.smartcharger.basefunction.GlobalVariables;
import com.dongah.smartcharger.utils.SharedModel;
import com.dongah.smartcharger.websocket.ocpp.core.ChargePointErrorCode;
import com.dongah.smartcharger.websocket.ocpp.core.ChargePointStatus;
import com.dongah.smartcharger.websocket.socket.handler.handlersend.StatusNotificationReq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlugWaitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlugWaitFragment extends Fragment {

    private static final Logger logger = LoggerFactory.getLogger(PlugWaitFragment.class);

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    int cnt = 0;
    boolean imgChange = false;
    TextView txtPlugWaitMessage;
    ImageView imgInlet, imgPlugConnector;
    AnimationDrawable aniInlet, aniPlug;
    Handler countHandler;
    Runnable countRunnable;

    ChargerConfiguration chargerConfiguration;
    ChargingCurrentData chargingCurrentData;

    public PlugWaitFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlugWaitFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlugWaitFragment newInstance(String param1, String param2) {
        PlugWaitFragment fragment = new PlugWaitFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_plug_wait, container, false);
        imgChange = false;

        txtPlugWaitMessage = view.findViewById(R.id.txtPlugWaitMessage);
        imgInlet = view.findViewById(R.id.imgInlet);
        imgPlugConnector = view.findViewById(R.id.imgPlugConnector);
        imgPlugConnector.setBackgroundResource(R.drawable.plugconnecting);
        aniPlug = (AnimationDrawable) imgPlugConnector.getBackground();

        imgInlet.setBackgroundResource(R.drawable.plugbackground);
        aniInlet = (AnimationDrawable) imgInlet.getBackground();

        chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
        chargingCurrentData = ((MainActivity) MainActivity.mContext).getClassUiProcess().getChargingCurrentData();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
//            MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.mContext, R.raw.plugwait);
//            mediaPlayer.start();

            cnt = 0;
            aniPlug.start();

            ((MainActivity) MainActivity.mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    countHandler = new Handler();
                    countRunnable = new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void run() {
                            cnt++;
                            if (cnt >= GlobalVariables.getConnectionTimeOut()) {
                                countHandler.removeCallbacks(countRunnable);
                                countHandler.removeCallbacksAndMessages(null);
                                countHandler.removeMessages(0);
                                try {
                                    // 충전기 종료
                                    ((MainActivity) MainActivity.mContext).getControlBoard().getTxData().setMainMC(false);
                                    ((MainActivity) MainActivity.mContext).getControlBoard().getTxData().setPwmDuty((short) 100);
                                    //선 결제에 의한 무카드 취소 (4:무카드 취소)(5:부분 취소)
//                                if (chargingCurrentData.isPrePaymentResult()) {
//                                    ((MainActivity) getActivity()).getTls3800().onTLS3800Request(mChannel, TLS3800.CMD_TX_PAYCANCEL, 4);
//                                }

                                    //preparing
                                    if (Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Preparing) &&
                                            Objects.equals(chargerConfiguration.getAuthMode(), "0") &&
                                            !((MainActivity) MainActivity.mContext).getControlBoard().getRxData().isCsPilot()) {
                                        chargingCurrentData.setChargePointStatus(ChargePointStatus.Available);
                                        chargingCurrentData.setChargePointErrorCode(ChargePointErrorCode.NoError);
                                        StatusNotificationReq statusNotificationReq = new StatusNotificationReq();
                                        statusNotificationReq.sendStatusNotification(chargingCurrentData.getConnectorId());
                                    }
                                    ((MainActivity) MainActivity.mContext).getClassUiProcess().onHome();
                                } catch (Exception e) {
                                    logger.error(" PlugWaitFragment timeout : {}", e.getMessage());
                                    ((MainActivity) MainActivity.mContext).getClassUiProcess().onHome();
                                }
                            } else {
                                countHandler.postDelayed(countRunnable, 1000);
                            }

                            //connecting wait
                            if (!imgChange &&
                                    ((MainActivity) MainActivity.mContext).getControlBoard().getRxData().isCsPilot()) {
                                imgChange = true;
                                cnt = 0;
                                if (aniPlug.isRunning()) {
                                    aniPlug.stop();
                                    imgPlugConnector.setBackgroundResource(R.drawable.plugwait12);
                                    aniInlet.start();
                                }
                                txtPlugWaitMessage.setText(R.string.plugStartMessage);
                            }
                        }
                    };
                    countHandler.postDelayed(countRunnable, 1000);
                }
            });
        } catch (Exception e) {
            logger.error("PlugWaitFragment onViewCreated : {}", e.getMessage());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            SharedModel sharedModel = new ViewModelProvider(requireActivity()).get(SharedModel.class);
            String[] requestStrings = new String[1];
            requestStrings[0] = "0";
            sharedModel.setMutableLiveData(requestStrings);

            countHandler.removeCallbacks(countRunnable);
            countHandler.removeCallbacksAndMessages(null);
            countHandler.removeMessages(0);
            if (aniPlug != null) {
                aniPlug.stop();
                imgPlugConnector.setBackground(null);
            }
            if (aniInlet != null) {
                aniInlet.stop();
                imgInlet.setBackground(null);
            }

        } catch (Exception e) {
            logger.error("PlugWaitFragment onDetach : {}", e.getMessage());
        }
    }

}