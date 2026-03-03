package com.dongah.smartcharger.basefunction;

import android.os.Bundle;

import androidx.fragment.app.FragmentTransaction;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.R;
import com.dongah.smartcharger.pages.AdminPasswordFragment;
import com.dongah.smartcharger.pages.AuthSelectFragment;
import com.dongah.smartcharger.pages.ChargingFinishFragment;
import com.dongah.smartcharger.pages.ChargingFragment;
import com.dongah.smartcharger.pages.ConfigSettingFragment;
import com.dongah.smartcharger.pages.ControlDebugFragment;
import com.dongah.smartcharger.pages.CreditCardFragment;
import com.dongah.smartcharger.pages.CreditCardWaitFragment;
import com.dongah.smartcharger.pages.EnvironmentFragment;
import com.dongah.smartcharger.pages.FaultFragment;
import com.dongah.smartcharger.pages.InitFragment;
import com.dongah.smartcharger.pages.MemberCardFragment;
import com.dongah.smartcharger.pages.MemberCardWaitFragment;
import com.dongah.smartcharger.pages.MessageYesNoFragment;
import com.dongah.smartcharger.pages.PlugWaitFragment;
import com.dongah.smartcharger.pages.QrFragment;
import com.dongah.smartcharger.pages.SocFragment;
import com.dongah.smartcharger.pages.WebSocketDebugFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FragmentChange {

    public static final Logger logger = LoggerFactory.getLogger(FragmentChange.class);

    public FragmentChange() {
    }


    public void onFragmentChange(UiSeq uiSeq, String sendText, String type) {
        Bundle bundle = new Bundle();
//        ((MainActivity) MainActivity.mContext).setFragmentSeq(uiSeq);
        FragmentTransaction transaction = ((MainActivity) MainActivity.mContext).getSupportFragmentManager().beginTransaction();
        switch (uiSeq) {
            case INIT:
                try {
                    InitFragment initFragment = new InitFragment();
                    transaction.replace(R.id.frameBody, initFragment, sendText);
                    initFragment.setArguments(bundle);
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange-initFragment error :  {}", e.getMessage());
                }
                break;
            case AUTH_SELECT:
                try {
                    AuthSelectFragment authSelectFragment = new AuthSelectFragment();
                    transaction.replace(R.id.frameBody, authSelectFragment, sendText);
                    authSelectFragment.setArguments(bundle);
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange-authSelectFragment error :  {}", e.getMessage());
                }
                break;
            case SOC:
                try {
                    SocFragment socFragment = new SocFragment();
                    transaction.replace(R.id.frameBody, socFragment, sendText);
                    socFragment.setArguments(bundle);
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange-socFragment error :  {}", e.getMessage());
                }
                break;
            case MEMBER_CARD:
                try {
                    MemberCardFragment memberCardFragment = new MemberCardFragment();
                    transaction.replace(R.id.frameBody, memberCardFragment, sendText);
                    memberCardFragment.setArguments(bundle);
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange-memberCardFragment error :  {}", e.getMessage());
                }
                break;
            case MEMBER_CARD_WAIT:
                try {
                    MemberCardWaitFragment memberCardWaitFragment = new MemberCardWaitFragment();
                    transaction.replace(R.id.frameBody, memberCardWaitFragment, sendText);
                    memberCardWaitFragment.setArguments(bundle);
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange-memberCardWaitFragment error :  {}", e.getMessage());
                }
                break;
            case CREDIT_CARD:
                try {
                    CreditCardFragment creditCardFragment = new CreditCardFragment();
                    transaction.replace(R.id.frameBody, creditCardFragment, sendText);
                    creditCardFragment.setArguments(bundle);
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange-creditCardFragment error :  {}", e.getMessage());
                }
                break;
            case CREDIT_CARD_WAIT:
                try {
                    CreditCardWaitFragment creditCardWaitFragment = new CreditCardWaitFragment();
                    transaction.replace(R.id.frameBody, creditCardWaitFragment, sendText);
                    creditCardWaitFragment.setArguments(bundle);
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange-creditCardWaitFragment error :  {}", e.getMessage());
                }
                break;
            case PLUG_CHECK:
            case CONNECT_CHECK:
                try {
                    PlugWaitFragment plugWaitFragment = new PlugWaitFragment();
                    transaction.replace(R.id.frameBody, plugWaitFragment, sendText);
                    plugWaitFragment.setArguments(bundle);
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange-plugWaitFragment error :  {}", e.getMessage());
                }
                break;
            case CHARGING:
                try {
                    ChargingFragment chargingFragment = new ChargingFragment();
                    transaction.replace(R.id.frameBody, chargingFragment, sendText);
                    chargingFragment.setArguments(bundle);
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange-chargingFragment error :  {}", e.getMessage());
                }
                break;
            case CHARGING_STOP_MESSAGE:
                try {
                    MessageYesNoFragment messageYesNoFragment = new MessageYesNoFragment();
                    transaction.replace(R.id.frameBody, messageYesNoFragment, "CHARGING_STOP_MESSAGE");
                    messageYesNoFragment.setArguments(bundle);
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : messageYesNoFragment {}", e.getMessage());
                }
                break;
            case FINISH:
                try {
                    ChargingFinishFragment chargingFinishFragment = new ChargingFinishFragment();
                    transaction.replace(R.id.frameBody, chargingFinishFragment, "FINISH");
                    chargingFinishFragment.setArguments(bundle);
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : FINISH {}", e.getMessage());
                }
                break;
            case QR_CODE:
                try {
                    QrFragment qrFragment = new QrFragment();
                    transaction.replace(R.id.frameBody, qrFragment, "QR_CODE");
                    qrFragment.setArguments(bundle);
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : QR CODE {}", e.getMessage());
                }
                break;
            case FAULT:
                try {
                    FaultFragment faultFragment = new FaultFragment();
                    transaction.replace(R.id.frameBody, faultFragment, "FINISH");
                    bundle.putString("param2", "FAULT_MESSAGE");
                    faultFragment.setArguments(bundle);
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : faultFragment {}", e.getMessage());
                }
                break;
            case ADMIN_PASS:
                try {
                    AdminPasswordFragment adminPasswordFragment = new AdminPasswordFragment();
                    transaction.replace(R.id.frameBody, adminPasswordFragment, "ADMIN");
                    adminPasswordFragment.setArguments(bundle);
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : ADMIN_PASS {}", e.getMessage());
                }
                break;
            case ENVIRONMENT:
                try {
                    EnvironmentFragment environmentFragment = new EnvironmentFragment();
                    transaction.replace(R.id.frameBody, environmentFragment, "environment");
                    environmentFragment.setArguments(bundle);
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : environmentFragment {}", e.getMessage());
                }
                break;
            case CONFIG_SETTING:
                try {
                    ConfigSettingFragment configSettingFragment = new ConfigSettingFragment();
                    transaction.replace(R.id.frameBody, configSettingFragment, "configSetting");
                    configSettingFragment.setArguments(bundle);
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : configSetting {}", e.getMessage());
                }
                break;
            case WEB_SOCKET:
                try {
                    WebSocketDebugFragment webSocketDebugFragment = new WebSocketDebugFragment();
                    transaction.replace(R.id.frameBody, webSocketDebugFragment, "WEBSOCKET");
                    webSocketDebugFragment.setArguments(bundle);
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : webSocketDebugFragment {}", e.getMessage());
                }
                break;
            case CONTROL_BOARD_DEBUGGING:
                try {
                    ControlDebugFragment controlDebugFragment = new ControlDebugFragment();
                    transaction.replace(R.id.frameBody, controlDebugFragment, "CONTROL");
                    controlDebugFragment.setArguments(bundle);
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : controlDebugFragment {}", e.getMessage());
                }
                break;
            case REBOOTING:
                try {
                    FaultFragment faultFragment = new FaultFragment();
                    transaction.replace(R.id.frameBody, faultFragment, "REBOOTING");
                    bundle.putString("param2", "REBOOTING");
                    bundle.putString("param3", type);
                    faultFragment.setArguments(bundle);
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("onFragmentChange error : REBOOTING {}", e.getMessage());
                }
                break;
        }

    }



}
