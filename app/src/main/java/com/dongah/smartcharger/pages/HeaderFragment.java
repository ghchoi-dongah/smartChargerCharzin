package com.dongah.smartcharger.pages;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.R;
import com.dongah.smartcharger.basefunction.UiSeq;
import com.dongah.smartcharger.utils.SharedModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HeaderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HeaderFragment extends Fragment implements View.OnClickListener {

    public static final Logger logger = LoggerFactory.getLogger(HeaderFragment.class);

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    TextView textViewChargerId;
    int clickedCnt = 0;
    ImageButton btnHome, btnLogo;
    SharedModel sharedModel;
    View view;

    public HeaderFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HeaderFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HeaderFragment newInstance(String param1, String param2) {
        HeaderFragment fragment = new HeaderFragment();
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

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_header, container, false);
        MainActivity activity = ((MainActivity) MainActivity.mContext);
        textViewChargerId = view.findViewById(R.id.textViewChargerId);
        textViewChargerId.setText("ID-" + activity.getChargerConfiguration().getChargerId());
        btnHome = view.findViewById(R.id.btnHome);
        btnHome.setOnClickListener(this);
        btnLogo = view.findViewById(R.id.btnLogo);
        btnLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickedCnt > 8) {
                    try {
                        UiSeq uiSeq = ((MainActivity) MainActivity.mContext).getClassUiProcess().getUiSeq();
                        if (Objects.equals(uiSeq, UiSeq.INIT) | Objects.equals(uiSeq, UiSeq.FAULT)) {
                            ((MainActivity) MainActivity.mContext).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((MainActivity) MainActivity.mContext).getClassUiProcess().setUiSeq(UiSeq.ADMIN_PASS);
                                    ((MainActivity) MainActivity.mContext).getFragmentChange().onFragmentChange(UiSeq.ADMIN_PASS,"ADMIN_PASS",null);
                                }
                            });

                        }
                        clickedCnt = 0;
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }
                clickedCnt++;
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            sharedModel = new ViewModelProvider(requireActivity()).get(SharedModel.class);
            sharedModel.getLiveData().observe(getViewLifecycleOwner(), new Observer<String[]>() {
                @Override
                public void onChanged(String[] strings) {
                    UiSeq uiSeq = ((MainActivity) MainActivity.mContext).getClassUiProcess().getUiSeq();
                    switch (uiSeq) {
                        case INIT:
                        case MEMBER_CARD:
                        case MEMBER_CARD_WAIT:
                        case CREDIT_CARD_WAIT:
                        case CHARGING:
                        case PLUG_CHECK:
                        case CONNECT_CHECK:
                        case FAULT:
                        case REBOOTING:
                            btnHome.setVisibility(View.INVISIBLE);
                            break;
                        default:
                            btnHome.setVisibility(View.VISIBLE);
                            break;
                    }
                }
            });
        } catch (Exception e) {
            logger.error("HeaderFragment onViewCreated : {}", e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        int getId = v.getId();
        if (Objects.equals(getId, R.id.btnHome))  {
            // initialize process
            ((MainActivity) MainActivity.mContext).getClassUiProcess().setUiSeq(UiSeq.INIT);
            ((MainActivity) MainActivity.mContext).getClassUiProcess().onHome();
        }
    }

    @Override
    public void onDetach() {
        try {
            super.onDetach();
            view.getAnimation().cancel();
            view.clearAnimation();
        } catch (Exception e) {
            logger.error(" HeaderFragment onDetach error : {}" , e.getMessage());
        }
    }
}