package com.dongah.smartcharger.pages;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.R;
import com.dongah.smartcharger.basefunction.ChargerConfiguration;
import com.dongah.smartcharger.basefunction.ChargingCurrentData;
import com.dongah.smartcharger.utils.SharedModel;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link QrFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QrFragment extends Fragment {

    private static final Logger logger = LoggerFactory.getLogger(QrFragment.class);

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Handler uiCheckHandler;
    ImageView imgQrCode;

    public QrFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment QrFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static QrFragment newInstance(String param1, String param2) {
        QrFragment fragment = new QrFragment();
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
        View view = inflater.inflate(R.layout.fragment_qr, container, false);
        imgQrCode = view.findViewById(R.id.imgQrCode);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
//            MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.mContext, R.raw.qr);
//            mediaPlayer.start();

            ChargerConfiguration chargerConfiguration = ((MainActivity) MainActivity.mContext).getChargerConfiguration();
            ChargingCurrentData chargingCurrentData = ((MainActivity) MainActivity.mContext).getClassUiProcess().getChargingCurrentData();
            chargingCurrentData.setConnectorId(1);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap("/" + chargerConfiguration.getChargerId()
                            + "/" + chargingCurrentData.getConnectorId(),
                    BarcodeFormat.QR_CODE, 600, 600);
            imgQrCode.setImageBitmap(bitmap);

            uiCheckHandler = new Handler();
            uiCheckHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((MainActivity) MainActivity.mContext).getClassUiProcess().onHome();
                }
            }, 15000);


        } catch (Exception e) {
            logger.error("QR_CODE onViewCreated : {}", e.getMessage());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // header title message change
        SharedModel sharedModel = new ViewModelProvider(requireActivity()).get(SharedModel.class);
        String[] requestStrings = new String[1];
        requestStrings[0] = "0";
        sharedModel.setMutableLiveData(requestStrings);
    }
}