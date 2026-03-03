package com.dongah.smartcharger.pages;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.R;
import com.dongah.smartcharger.basefunction.ChargerConfiguration;
import com.dongah.smartcharger.basefunction.ChargingCurrentData;
import com.dongah.smartcharger.basefunction.GlobalVariables;
import com.dongah.smartcharger.basefunction.UiSeq;
import com.dongah.smartcharger.utils.SharedModel;
import com.dongah.smartcharger.websocket.ocpp.core.ChargePointStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InitFragment extends Fragment implements View.OnClickListener {

    private static final Logger logger = LoggerFactory.getLogger(InitFragment.class);


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Animation animBlink;
    View viewCircle;
    TextView txtInitMessage, textViewMemberUnitInput;
    ConstraintLayout initLayout;
    ChargerConfiguration chargerConfiguration;
    SharedModel sharedModel;
    String[] requestStrings = new String[1];

    public InitFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InitFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InitFragment newInstance(String param1, String param2) {
        InitFragment fragment = new InitFragment();
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

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_init, container, false);
        textViewMemberUnitInput = view.findViewById(R.id.textViewMemberUnitInput);
        textViewMemberUnitInput.setText("회원 단가 : " + (int)GlobalVariables.getMemberPriceUnit() + " 원" );

        animBlink = AnimationUtils.loadAnimation(getActivity(), R.anim.blink);
        viewCircle = view.findViewById(R.id.viewCircle);
        viewCircle.setOnClickListener(this);
        txtInitMessage = view.findViewById(R.id.txtInitMessage);
        txtInitMessage.startAnimation(animBlink);

        initLayout = view.findViewById(R.id.initLayout);
        initLayout.setOnClickListener(this);
        ((MainActivity) MainActivity.mContext).getClassUiProcess().setUiSeq(UiSeq.INIT);
        txtInitMessage = view.findViewById(R.id.txtInitMessage);

        txtInitMessage.setOnClickListener(this);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) MainActivity.mContext).getClassUiProcess().getChargingCurrentData().setConnectorId(1);
        // home image
        sharedModel = new ViewModelProvider(requireActivity()).get(SharedModel.class);
        requestStrings[0] = String.valueOf(0);
        sharedModel.setMutableLiveData(requestStrings);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            requestStrings[0] = String.valueOf(0);
            sharedModel.setMutableLiveData(requestStrings);
            animBlink.cancel();
            animBlink = null;
        } catch (Exception e) {
            logger.error(" Init Fragment onDetach error : {}" , e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        try {
            MainActivity activity = ((MainActivity) MainActivity.mContext);
            chargerConfiguration = activity.getChargerConfiguration();
            ChargingCurrentData chargingCurrentData = activity.getClassUiProcess().getChargingCurrentData();
            if (chargingCurrentData.getReservedStatus() != ChargePointStatus.Reserved) {
                chargingCurrentData.onCurrentDataClear();
            }
            if (Objects.equals(chargerConfiguration.getAuthMode(), "0")) {
                // 0:credit + member   1:member*/
                switch (Integer.parseInt(chargerConfiguration.getSelectPayment())) {
                    case 0:
                        activity.getClassUiProcess().setUiSeq(UiSeq.AUTH_SELECT);
                        activity.getFragmentChange().onFragmentChange(UiSeq.AUTH_SELECT, "AUTH_SELECT", null);
                        break;
                    case 1:
                        activity.getClassUiProcess().setUiSeq(UiSeq.MEMBER_CARD);
                        activity.getFragmentChange().onFragmentChange(UiSeq.MEMBER_CARD, "MEMBER_CARD", null);
                        break;
                    case 2:
                        activity.getClassUiProcess().setUiSeq(UiSeq.CREDIT_CARD);
                        activity.getFragmentChange().onFragmentChange(UiSeq.CREDIT_CARD, "CREDIT_CARD", null);
                        break;
                }
            } else if (Objects.equals(chargerConfiguration.getAuthMode(), "4")) {
                // local 회원 인증용
                double testPrice = Double.parseDouble(((MainActivity) MainActivity.mContext).getChargerConfiguration().getTestPrice());
                ((MainActivity) MainActivity.mContext).getClassUiProcess().getChargingCurrentData().setPowerUnitPrice(testPrice);
                ((MainActivity) MainActivity.mContext).getClassUiProcess().setUiSeq(UiSeq.AUTH_SELECT);
                ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(UiSeq.AUTH_SELECT, "AUTH_SELECT", null);
            } else {
                double testPrice = Double.parseDouble(((MainActivity) MainActivity.mContext).getChargerConfiguration().getTestPrice());
                ((MainActivity) MainActivity.mContext).getClassUiProcess().getChargingCurrentData().setPowerUnitPrice(testPrice);
                ((MainActivity) MainActivity.mContext).getClassUiProcess().setUiSeq(UiSeq.PLUG_CHECK);
                ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(UiSeq.PLUG_CHECK, "PLUG_CHECK", null);
            }

        } catch (Exception e) {
            logger.error(" init onClick error : {}", e.getMessage());
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) MainActivity.mContext).hideNavigationBar();
    }

}