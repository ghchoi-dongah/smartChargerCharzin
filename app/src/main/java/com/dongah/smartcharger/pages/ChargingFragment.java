package com.dongah.smartcharger.pages;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.R;
import com.dongah.smartcharger.basefunction.ChargerConfiguration;
import com.dongah.smartcharger.basefunction.ClassUiProcess;
import com.dongah.smartcharger.basefunction.PaymentType;
import com.dongah.smartcharger.basefunction.UiSeq;
import com.dongah.smartcharger.utils.SharedModel;
import com.dongah.smartcharger.websocket.ocpp.utilities.ZonedDateTimeConvert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChargingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChargingFragment extends Fragment implements View.OnClickListener {

    private static final Logger logger = LoggerFactory.getLogger(ChargingFragment.class);

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    TextView txtChargePay, txtChargeTime, txtAmountOfCharge, txtPowerUnitPrice;
    Button  btnChargingStop;
    TextView txtSoc;

    Handler uiUpdateHandler;
    double powerUnitPrice = 0f;
    SharedModel sharedModel;
    String[] requestStrings = new String[1];

    ClassUiProcess classUiProcess;
    Date startTime = null, useTime = null;
    DecimalFormat payFormatter = new DecimalFormat("#,###,##0");
    DecimalFormat powerFormatter = new DecimalFormat("#,###,##0.00");
    ZonedDateTimeConvert zonedDateTimeConvert = new ZonedDateTimeConvert();


    public ChargingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChargingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChargingFragment newInstance(String param1, String param2) {
        ChargingFragment fragment = new ChargingFragment();
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
        View view = inflater.inflate(R.layout.fragment_charging, container, false);
        txtSoc = view.findViewById(R.id.txtSoc);
        txtPowerUnitPrice = view.findViewById(R.id.txtPowerUnitPrice);
        txtChargePay = view.findViewById(R.id.txtChargePay);
        txtChargeTime = view.findViewById(R.id.txtChargeTime);
        txtAmountOfCharge = view.findViewById(R.id.txtAmountOfCharge);
        btnChargingStop = view.findViewById(R.id.btnChargingStop);
        btnChargingStop.setOnClickListener(this);
        return view;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedModel = new ViewModelProvider(requireActivity()).get(SharedModel.class);
        try {
            try {
                classUiProcess = ((MainActivity) MainActivity.mContext).getClassUiProcess();
                startTime = zonedDateTimeConvert.doStringDateToDate(classUiProcess.getChargingCurrentData().getChargingStartTime());
                powerUnitPrice = classUiProcess.getChargingCurrentData().getPowerUnitPrice();
                txtPowerUnitPrice.setText(powerUnitPrice + " 원");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            onCharging();
        } catch (Exception e) {
            logger.error("ChargingFragment onViewCreated : {}", e.getMessage());
        }

    }

    @Override
    public void onClick(View v) {
        int getId = v.getId();
        if  (Objects.equals(getId, R.id.btnChargingStop)) {
            try {
                ChargerConfiguration chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
                if (!Objects.equals(chargerConfiguration.getAuthMode(), "0")) {
                    ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(UiSeq.CHARGING_STOP_MESSAGE, "CHARGING_STOP_MESSAGE", null);
                } else {
                    //서버 인증 모드인 경우
                    PaymentType paymentType = classUiProcess.getChargingCurrentData().getPaymentType();
                    ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(UiSeq.CHARGING_STOP_MESSAGE, "CHARGING_STOP_MESSAGE", null);

                }
            } catch (Exception e) {
                logger.error("Charging onClick error : {}", e.getMessage());
            }
        }
    }


    private void onCharging() {
        uiUpdateHandler = new Handler();
        uiUpdateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((MainActivity) MainActivity.mContext).runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @SuppressLint({"DefaultLocale", "SetTextI18n"})
                    @Override
                    public void run() {
                        try {
                            long diffTime = 0;
                            classUiProcess = ((MainActivity) MainActivity.mContext).getClassUiProcess();
                            useTime = zonedDateTimeConvert.doStringDateToDate(zonedDateTimeConvert.getStringCurrentTimeZone());
                            if (useTime != null) {
                                diffTime = (useTime.getTime() - startTime.getTime()) / 1000;
                                int hour = (int) diffTime / 3600;
                                int minute = (int) (diffTime % 3600) / 60;
                                int second = (int) diffTime % 60;
                                classUiProcess.getChargingCurrentData().setChargingTime((int) diffTime);
                                txtChargeTime.setText(String.format("%02d", hour) + ":" + String.format("%02d", minute) + ":" + String.format("%02d", second));
                                classUiProcess.getChargingCurrentData().setChargingUseTime(txtChargeTime.getText().toString());
                                txtChargePay.setText(payFormatter.format((long) classUiProcess.getChargingCurrentData().getPowerMeterUsePay()) + " 원");
                                txtAmountOfCharge.setText(powerFormatter.format(classUiProcess.getChargingCurrentData().getPowerMeterUse() * 0.01) + " kWh");
                                if (classUiProcess.getChargingCurrentData().getSoc() == 0) {
                                    txtSoc.setVisibility(View.INVISIBLE);
                                } else {
                                    txtSoc.setVisibility(View.VISIBLE);
                                    txtSoc.setText(classUiProcess.getChargingCurrentData().getSoc() + "%");
                                }
//                                // 충전 남은 시간 : PLC 에서 지원 안함
//                                int rHour = classUiProcess.getChargingCurrentData().getRemaintime() / 3600;
//                                int rMinute = (classUiProcess.getChargingCurrentData().getRemaintime() % 3600) / 60;
//                                int rSecond = classUiProcess.getChargingCurrentData().getRemaintime() % 60;
//                                txtRemainTime.setText(String.format("%02d", rHour) + ":" + String.format("%02d", rMinute) + ":" + String.format("%02d", rSecond));
                            }

                        } catch (Exception e) {
                            logger.error("ChargingFragment onCharging : {}", e.getMessage());
                        }
                    }
                });
                uiUpdateHandler.postDelayed(this, 1000);
            }
        }, 50);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        uiUpdateHandler.removeCallbacksAndMessages(null);
        uiUpdateHandler.removeMessages(0);
        if (uiUpdateHandler != null) uiUpdateHandler = null;
        requestStrings[0] = "0";
        sharedModel.setMutableLiveData(requestStrings);

    }
}