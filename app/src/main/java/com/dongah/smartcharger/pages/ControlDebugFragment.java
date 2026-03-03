package com.dongah.smartcharger.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.R;
import com.dongah.smartcharger.basefunction.UiSeq;
import com.dongah.smartcharger.controlboard.ControlBoard;
import com.dongah.smartcharger.controlboard.ControlBoardListener;
import com.dongah.smartcharger.controlboard.ControlBoardUtil;
import com.dongah.smartcharger.controlboard.ListViewDspAdapter;
import com.dongah.smartcharger.controlboard.RxData;
import com.dongah.smartcharger.controlboard.TxData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ControlDebugFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ControlDebugFragment extends Fragment implements View.OnClickListener, ControlBoardListener {

    private static final Logger logger = LoggerFactory.getLogger(ControlDebugFragment.class);

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    DecimalFormat decimalFormat;
    Button btnClose;
    RadioGroup rgSelectChannel;
    ListView listRx, listTx;
    ListViewDspAdapter listViewRxAdapter, listViewTxAdapter;
    ControlBoard controlBoard;
    ControlBoardUtil controlBoardUtil;
    int currCh = 0;

    Map<Integer, String> statusMap = new HashMap<>();


    public ControlDebugFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ControlDebugFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ControlDebugFragment newInstance(String param1, String param2) {
        ControlDebugFragment fragment = new ControlDebugFragment();
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

        View view = inflater.inflate(R.layout.fragment_control_debug, container, false);
        //** integer format */
        decimalFormat = new DecimalFormat("#,###,##0.0#");
        controlBoardUtil = new ControlBoardUtil();
        btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(this);

        //** Rx data */
        listRx = view.findViewById(R.id.listRx);
        listViewRxAdapter = new ListViewDspAdapter();
        listViewRxAdapter.notifyDataSetChanged();
        listRx.setAdapter(listViewRxAdapter);

        //* Tx data */
        listTx = view.findViewById(R.id.listTx);
        listViewTxAdapter = new ListViewDspAdapter();
        listViewTxAdapter.notifyDataSetChanged();
        listTx.setAdapter(listViewTxAdapter);

        controlBoard = ((MainActivity) MainActivity.mContext).getControlBoard();
        controlBoard.setDspControlListener(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        statusMap.put(0, "STATE_A 12V");
        statusMap.put(1, "STATE_B 9V");
        statusMap.put(2, "STATE_C 6V");
        statusMap.put(3, "STATE_D 3V");
        statusMap.put(4, "STATE_E 0V");
        statusMap.put(5, "STATE_E -12V");
    }

    @Override
    public void onClick(View v) {
        try {
            //** fragment change */
            ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(UiSeq.ENVIRONMENT, "ENVIRONMENT", null);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        controlBoard.setDspControlListenerStop();
    }

    @Override
    public void onControlBoardReceive(RxData rxData) {
        ((MainActivity) MainActivity.mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    listViewRxAdapter.clearItem();
                    listViewRxAdapter.addItem("Rx", "RunCount",String.valueOf(rxData.getCsRunCount()));
                    listViewRxAdapter.addItem("RX","CP STATE", statusMap.get((int) rxData.getCsCPStatus()));
                    listViewRxAdapter.addItem("Rx", "PWM Duty", String.valueOf(rxData.getCsPwmDuty()));
                    listViewRxAdapter.addItem("Rx", "F/W Ver",String.valueOf(rxData.getCsFirmwareVersion()));
                    listViewRxAdapter.addItem("RX","csSequence", rxData.getCsSequenceStatus() == 0 ? "대기" : rxData.getCsSequenceStatus() == 1 ? "충전중" : "종료");
                    listViewRxAdapter.addItem("RX","csStart", String.valueOf(rxData.isCsStart()));
                    listViewRxAdapter.addItem("RX","csStop", String.valueOf(rxData.isCsStop()));
                    listViewRxAdapter.addItem("RX","FAULT", String.valueOf(rxData.isCsFault()));
                    listViewRxAdapter.addItem("RX","csEmergency", String.valueOf(rxData.isCsEmergency()));
                    listViewRxAdapter.addItem("RX","csMcStatus", String.valueOf(rxData.isCsMcStatus()));
                    listViewRxAdapter.addItem("RX","csPilot", String.valueOf(rxData.isCsPilot()));
                    listViewRxAdapter.addItem("RX","csOVR", String.valueOf(rxData.isCsOVR()));
                    listViewRxAdapter.addItem("RX","csUVR", String.valueOf(rxData.isCsUVR()));
                    listViewRxAdapter.addItem("RX","csOCR", String.valueOf(rxData.isCsOCR()));
                    listViewRxAdapter.addItem("RX","전력량 kWh", decimalFormat.format(rxData.getActiveEnergy()));
                    listViewRxAdapter.addItem("RX","전력 kW", decimalFormat.format(rxData.getActivePower())) ;
                    listViewRxAdapter.addItem("RX","AC 전압 V", decimalFormat.format(rxData.getVoltage())) ;
                    listViewRxAdapter.addItem("RX","AC 전류 A", decimalFormat.format(rxData.getCurrent())) ;
                    listViewRxAdapter.addItem("RX","주파수 Hz", decimalFormat.format(rxData.getFrequency())) ;
                    listViewRxAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    listViewRxAdapter.clearItem();
                    logger.error(" Rx onControlBoardReceive error : {}", e.getMessage() );
                }
            }
        });

    }

    @Override
    public void onControlBoardSend(TxData txData) {
        ((MainActivity) MainActivity.mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    listViewTxAdapter.clearItem();
                    listViewTxAdapter.addItem("TX","RunCount", String.valueOf(txData.getRunCount()));
                    listViewTxAdapter.addItem("TX","IsBoardRest", String.valueOf(txData.isBoardRest()));
                    listViewTxAdapter.addItem("TX","IsMainMC", String.valueOf(txData.isMainMC()));
                    listViewTxAdapter.addItem("TX","IsCPRelay", String.valueOf(txData.isCPRelay()));
                    listViewTxAdapter.addItem("TX","PWM Duty", String.valueOf(txData.getPwmDuty()));
                    listViewTxAdapter.addItem("TX","Ui Seq", txData.getUiSequence() == 1 ? "대기" : txData.getUiSequence() == 2 ? "충전중" : "종료");
                    listViewTxAdapter.addItem("TX","powerMeterHigh", String.valueOf(txData.getHighPowerMeter()));
                    listViewTxAdapter.addItem("TX","powerMeterLow", String.valueOf(txData.getLowPowerMeter()));
                    listViewTxAdapter.addItem("TX","out voltage", String.valueOf(txData.getOutVoltage()));
                    listViewTxAdapter.addItem("TX","out current", String.valueOf(txData.getOutCurrent()));
                    listViewTxAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    listViewTxAdapter.clearItem();
                    logger.error(" Tx onControlBoardReceive error : {}", e.getMessage() );
                }
            }
        });
    }
}