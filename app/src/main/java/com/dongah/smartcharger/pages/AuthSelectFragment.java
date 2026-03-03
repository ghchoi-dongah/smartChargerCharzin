package com.dongah.smartcharger.pages;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
import com.dongah.smartcharger.basefunction.PaymentType;
import com.dongah.smartcharger.basefunction.UiSeq;
import com.dongah.smartcharger.utils.SharedModel;
import com.dongah.smartcharger.websocket.socket.SocketReceiveMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AuthSelectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AuthSelectFragment extends Fragment implements View.OnClickListener {

    private static final Logger logger = LoggerFactory.getLogger(AuthSelectFragment.class);

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    FrameLayout frameMember, frameNoMember;
    View imageViewMemberCheck, imageViewNoMemberCheck;

    TextView textViewMemberUnitInput, textViewNoMemberUnitInput;
    ChargerConfiguration chargerConfiguration;
    ChargingCurrentData chargingCurrentData;
    Handler uiCheckHandler;
    SocketReceiveMessage socketReceiveMessage;


    public AuthSelectFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AuthSelectFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AuthSelectFragment newInstance(String param1, String param2) {
        AuthSelectFragment fragment = new AuthSelectFragment();
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_auth_select, container, false);
        chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
        chargingCurrentData = ((MainActivity) MainActivity.mContext).getClassUiProcess().getChargingCurrentData();
        socketReceiveMessage = ((MainActivity) MainActivity.mContext).getSocketReceiveMessage();
        textViewMemberUnitInput = view.findViewById(R.id.textViewMemberUnitInput);
        textViewNoMemberUnitInput = view.findViewById(R.id.textViewNoMemberUnitInput);
        imageViewMemberCheck = view.findViewById(R.id.imageViewMemberCheck);
        imageViewNoMemberCheck = view.findViewById(R.id.imageViewNoMemberCheck);
        textViewMemberUnitInput.setText(String.format("   :  %s 원", GlobalVariables.getMemberPriceUnit()));
        textViewNoMemberUnitInput.setText(String.format("   :  %s 원", GlobalVariables.getNonMemberPriceUnit()));

        frameMember = view.findViewById(R.id.frameMember);
        frameMember.setOnClickListener(this);
        frameNoMember = view.findViewById(R.id.frameNoMember);
        frameNoMember.setOnClickListener(this);

        if (Objects.equals(chargerConfiguration.getSelectPayment(), "1")) {
            frameNoMember.setVisibility(View.INVISIBLE);
        } else if (Objects.equals(chargerConfiguration.getSelectPayment(), "2")) {
            frameMember.setVisibility(View.INVISIBLE);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            uiCheckHandler = new Handler();
            uiCheckHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((MainActivity) MainActivity.mContext).getClassUiProcess().onHome();
                }
            }, 60000);

        } catch (Exception e) {
            logger.error(" AuthSelectFragment error : {}", e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        try {
            int getId = v.getId();
            if (Objects.equals(getId, R.id.frameMember)) {
                imageViewMemberCheck.setBackgroundResource(R.drawable.checked);
                chargingCurrentData.setPaymentType(PaymentType.MEMBER);
                ((MainActivity) MainActivity.mContext).getClassUiProcess().setUiSeq(UiSeq.MEMBER_CARD);
                ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(UiSeq.MEMBER_CARD, "MEMBER_CARD", null);
            } else if (Objects.equals(getId, R.id.frameNoMember)) {
                imageViewNoMemberCheck.setBackgroundResource(R.drawable.checked);
                chargingCurrentData.setPaymentType(PaymentType.CREDIT);
                ((MainActivity) MainActivity.mContext).getClassUiProcess().setUiSeq(UiSeq.CREDIT_CARD);
                ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(UiSeq.CREDIT_CARD, "CREDIT_CARD", null);
            }

        } catch (Exception e) {
            logger.error(" AuthSelectFragment onClick error : {}", e.getMessage());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        uiCheckHandler.removeCallbacksAndMessages(null);
        uiCheckHandler.removeMessages(0);
        // back image
        String[] requestStrings = new String[1];
        SharedModel sharedModel = new ViewModelProvider(requireActivity()).get(SharedModel.class);
        requestStrings[0] = String.valueOf(0);
        sharedModel.setMutableLiveData(requestStrings);
    }

}