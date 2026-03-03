package com.dongah.smartcharger.pages;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.R;
import com.dongah.smartcharger.basefunction.ChargerConfiguration;
import com.dongah.smartcharger.basefunction.ChargingCurrentData;
import com.dongah.smartcharger.basefunction.ClassUiProcess;
import com.dongah.smartcharger.basefunction.GlobalVariables;
import com.dongah.smartcharger.basefunction.UiSeq;
import com.dongah.smartcharger.websocket.ocpp.core.ChargePointStatus;
import com.dongah.smartcharger.websocket.socket.handler.handlersend.AuthorizeReq;
import com.dongah.smartcharger.websocket.socket.handler.handlersend.StatusNotificationReq;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MemberCardWaitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemberCardWaitFragment extends Fragment {

    private static final Logger logger = LoggerFactory.getLogger(MemberCardWaitFragment.class);

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    int cnt = 0;
    ClassUiProcess classUiProcess;
    ChargingCurrentData chargingCurrentData;
    ChargerConfiguration chargerConfiguration;
    ImageView imageViewLoading;
    AnimationDrawable animationDrawable;
    TextView txtCount;
    Handler countHandler;
    Runnable countRunnable;


    public MemberCardWaitFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MemberCardWaitFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MemberCardWaitFragment newInstance(String param1, String param2) {
        MemberCardWaitFragment fragment = new MemberCardWaitFragment();
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
        View view = inflater.inflate(R.layout.fragment_member_card_wait, container, false);
        txtCount = view.findViewById(R.id.txtCount);
        imageViewLoading = view.findViewById(R.id.imageViewLoading);
        imageViewLoading.setBackgroundResource(R.drawable.ani_loading);
        animationDrawable = (AnimationDrawable) imageViewLoading.getBackground();

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            animationDrawable.start();
            chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
            classUiProcess = ((MainActivity) MainActivity.mContext).getClassUiProcess();
            chargingCurrentData = classUiProcess.getChargingCurrentData();

            ((MainActivity) MainActivity.mContext).runOnUiThread(() -> {
                countHandler = new Handler();
                countRunnable = new Runnable() {
                    @Override
                    public void run() {
                        cnt++;
                        txtCount.setText(String.valueOf(cnt));
                        if (cnt == 20) classUiProcess.onHome();
                        countHandler.postDelayed(this, 1000);
                    }
                };
                countHandler.postDelayed(countRunnable, 1000);
            });

            MainActivity activity = ((MainActivity) MainActivity.mContext);
            if (GlobalVariables.isLocalPreAuthorize()) {
                boolean tagExists = isIdTagExists(chargingCurrentData.getIdTag());
                if (tagExists) {
                    chargingCurrentData.setAuthorizeResult(true);
                    chargingCurrentData.setPowerUnitPrice(GlobalVariables.getMemberPriceUnit());
                    if (!Objects.equals(chargingCurrentData.getChargePointStatus(), ChargePointStatus.Preparing)) {
                        chargingCurrentData.setChargePointStatus(ChargePointStatus.Preparing);
                        StatusNotificationReq statusNotificationReq = new StatusNotificationReq();
                        statusNotificationReq.sendStatusNotification(chargingCurrentData.getConnectorId());
                    }
                    activity.getClassUiProcess().setUiSeq(UiSeq.PLUG_CHECK);
                    activity.getFragmentChange().onFragmentChange(UiSeq.PLUG_CHECK, "PLUG_CHECK", null);
                } else {
                    Toast.makeText(activity, "충전 불가!!! . 등록된 ID가 아닙니다.", Toast.LENGTH_SHORT).show();
                    activity.getClassUiProcess().onHome();
                }
            } else {
                if (activity.getSocketReceiveMessage() != null) {
                    AuthorizeReq authorizeReq = new AuthorizeReq(chargingCurrentData.getConnectorId());
                    authorizeReq.sendAuthorize(chargingCurrentData.getIdTag());
                } else {
                    Toast.makeText(activity, "서버와 통신 DISCONNECT!!! 인증 실패. ", Toast.LENGTH_SHORT).show();
                    ((MainActivity) MainActivity.mContext).getClassUiProcess().onHome();
                }
            }
        } catch (Exception e) {
            logger.error(" MemberCardWaitFragment error : {}", e.getMessage());
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean isIdTagExists(String targetIdTag) {
        try {
            File file = new File(
                    GlobalVariables.getRootPath()
                            + File.separator
                            + "localList.dongah"
            );

            if (!file.exists()) return false;

            String jsonStr = new String(Files.readAllBytes(file.toPath()));
            JSONArray array = new JSONArray(jsonStr);

            // 현재 UTC 시간
            Instant now = Instant.now();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                String savedIdTag = obj.optString("idTag", "");
                if (!targetIdTag.equals(savedIdTag)) continue;

                JSONObject idTagInfo = obj.optJSONObject("idTagInfo");
                if (idTagInfo == null) continue;

                String status = idTagInfo.optString("status", "");
                String expiryDateStr = idTagInfo.optString("expiryDate", "");
                if (expiryDateStr.isEmpty()) continue;

                Instant expiryTime = Instant.parse(expiryDateStr);
                // idTag는 같지만 조건 불충족(만료 or status 불일치)

                return "Accepted".equals(status) && expiryTime.isAfter(now);
            }

        } catch (Exception e) {
            logger.error(" isIdTagExists error : {}", e.getMessage());
        }
        return false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            if (animationDrawable != null) {
                animationDrawable.stop();
            }

            if (imageViewLoading != null) {
                Drawable bg = imageViewLoading.getBackground();
                if (bg instanceof AnimationDrawable) {
                    ((AnimationDrawable) bg).stop();
                }
                imageViewLoading.setBackground(null);
            }

            if (countHandler != null) {
                countHandler.removeCallbacksAndMessages(null);
                countHandler = null;
            }
            countRunnable = null;
        } catch (Exception e) {
            logger.error("MemberCardWaitFragment onDetach : {} ", e.getMessage());
        }
    }
}