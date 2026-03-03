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

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.R;
import com.dongah.smartcharger.basefunction.ChargingCurrentData;
import com.dongah.smartcharger.basefunction.ClassUiProcess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChargingFinishFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChargingFinishFragment extends Fragment implements View.OnClickListener {

    private static final Logger logger = LoggerFactory.getLogger(ChargingFinishFragment.class);

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    Button btnStopConfirm;

    TextView  txtSoc, txtAmountOfCharge, txtChargePay, txtChargeTime;
    MainActivity activity;
    ClassUiProcess classUiProcess;
    ChargingCurrentData chargingCurrentData;
    DecimalFormat payFormatter = new DecimalFormat("#,###,##0");
    DecimalFormat unitPriceFormatter = new DecimalFormat("#,###,##0.0");
    DecimalFormat powerFormatter = new DecimalFormat("#,###,##0.00");
    Handler uiCheckHandler, paymentHandler;

    public ChargingFinishFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChargingFinishFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChargingFinishFragment newInstance(String param1, String param2) {
        ChargingFinishFragment fragment = new ChargingFinishFragment();
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
        View view = inflater.inflate(R.layout.fragment_charging_finish, container, false);

        txtSoc = view.findViewById(R.id.txtSoc);
        txtAmountOfCharge = view.findViewById(R.id.txtAmountOfCharge);
        txtChargePay = view.findViewById(R.id.txtChargePay);
        txtChargeTime = view.findViewById(R.id.txtChargeTime);
        btnStopConfirm = view.findViewById(R.id.btnStopConfirm);
        btnStopConfirm.setOnClickListener(this);
        activity = ((MainActivity) MainActivity.mContext);
        classUiProcess = activity.getClassUiProcess();
        chargingCurrentData = classUiProcess.getChargingCurrentData();
        classUiProcess.onStopData();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {

            //unplug check 후 초기 화면
            uiCheckHandler = new Handler();
            uiCheckHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!((MainActivity) MainActivity.mContext).getControlBoard().getRxData().isCsPilot()) {
                        btnStopConfirm.performClick();
                    }
                    uiCheckHandler.postDelayed(this, 60000);
                }
            }, 60000);

            //charging finish info
            ((MainActivity) MainActivity.mContext).runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @SuppressLint("SetTextI18n")
                @Override
                public void run() {
                    txtSoc.setText(chargingCurrentData.getSoc() == 0 ? "케이블 정리해 주세요." :  chargingCurrentData.getSoc() + " %");
                    txtAmountOfCharge.setText(powerFormatter.format(chargingCurrentData.getPowerMeterUse() * 0.01) + " kWh");
                    txtChargePay.setText(payFormatter.format(chargingCurrentData.getPowerMeterUsePay()) + " 원") ;
                    txtChargeTime.setText(chargingCurrentData.getChargingUseTime());
                }
            });
        } catch (Exception e) {
            logger.error("onViewCreated : {}", e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        int getId = v.getId();
        if (Objects.deepEquals(getId, R.id.btnStopConfirm)) {
            ((MainActivity) MainActivity.mContext).getClassUiProcess().onHome();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (uiCheckHandler != null) {
            uiCheckHandler.removeCallbacksAndMessages(null);
            uiCheckHandler.removeMessages(0);
            uiCheckHandler = null;
        }
    }
}