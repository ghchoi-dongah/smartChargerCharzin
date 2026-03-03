package com.dongah.smartcharger.pages;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MemberCardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemberCardFragment extends Fragment {

    private static final Logger logger = LoggerFactory.getLogger(MemberCardFragment.class);

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    int cnt = 0;
    ImageView imgMemberCardTagging;
    AnimationDrawable animationDrawable;
    Handler countHandler;
    Runnable countRunnable;

    public MemberCardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MemberCardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MemberCardFragment newInstance(String param1, String param2) {
        MemberCardFragment fragment = new MemberCardFragment();
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
        View view = inflater.inflate(R.layout.fragment_member_card, container, false);
        imgMemberCardTagging = view.findViewById(R.id.imgMemberCardTagging);
        imgMemberCardTagging.setBackgroundResource(R.drawable.membercardtagging);
        animationDrawable = (AnimationDrawable) imgMemberCardTagging.getBackground();

        //rfCard ready
        ((MainActivity) MainActivity.mContext).getRfCardReaderReceive().rfCardReadRequest();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {

            animationDrawable.start();

            //count
            ((MainActivity) MainActivity.mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    countHandler = new Handler();
                    countRunnable = new Runnable() {
                        @Override
                        public void run() {
                            cnt++;
                            if (Objects.equals(cnt, 20)) {
                                ((MainActivity) MainActivity.mContext).getClassUiProcess().onHome();
                            } else {
                                countHandler.postDelayed(countRunnable, 1000);
                            }
                        }
                    };
                    countHandler.postDelayed(countRunnable, 1000);
                }
            });

        } catch (Exception e) {
            logger.error(" MemberCardFragment error : {}", e.getMessage());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        animationDrawable.stop();
        ((AnimationDrawable) imgMemberCardTagging.getBackground()).stop();
        imgMemberCardTagging.setBackground(null);
        countHandler.removeCallbacks(countRunnable);
        countHandler.removeCallbacksAndMessages(null);
        countHandler.removeMessages(0);
    }

}