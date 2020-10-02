package com.example.fitrans;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.pax.poslink.CommSetting;
import com.pax.poslink.aidl.BasePOSLinkCallback;
import com.pax.poslink.fullIntegration.AuthorizeCard;
import com.pax.poslink.fullIntegration.CompleteOnlineEMV;
import com.pax.poslink.fullIntegration.InputAccount;
import com.pax.poslink.fullIntegration.RemoveCard;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private CommSetting commset(){
        CommSetting commSetting = new CommSetting();
        commSetting.setType(CommSetting.AIDL);
        commSetting.setTimeOut("-1");
        return commSetting;
    }

    public InputAccount.InputAccountRequest doInputAccount(){
        InputAccount.InputAccountRequest inputreq = new InputAccount.InputAccountRequest();

        inputreq.setEdcType("CREDIT");
        inputreq.setTransType("SALE");
        inputreq.setAmount("100");
        inputreq.setMagneticSwipeEntryFlag("1");
        inputreq.setManualEntryFlag("1");
        inputreq.setExpiryDatePrompt("1");
        inputreq.setCVVPrompt("1");
        inputreq.setZipPrompt("1");
        inputreq.setContactlessEntryFlag("1");
        inputreq.setContactEMVEntryFlag("1");
        inputreq.setFallbackInsertEntryFlag("1");
        inputreq.setFallbackSwipeEntryFlag("1");
        inputreq.setEncryptionFlag("1");
        inputreq.setKeySLot("1");
        inputreq.setTimeOut("300");

        return inputreq;
    }

    public AuthorizeCard.AuthorizeRequest doAuthorizeCard(){
        AuthorizeCard.AuthorizeRequest authreq = new AuthorizeCard.AuthorizeRequest();

        authreq.setAmount("100");
        authreq.setPinEncryptionType("1");
        authreq.setPinAlgorithm("0");
        authreq.setKeySlot("1");
        authreq.setPinBypassFlag("4");
        //authreq.setPinpadType("2");
        authreq.setTimeOut(Integer.parseInt("300"));

        return authreq;
    }

    public CompleteOnlineEMV.CompleteOnlineEMVRequest doCompleteOnlineEMVRequest(){
        CompleteOnlineEMV.CompleteOnlineEMVRequest coemvreq = new CompleteOnlineEMV.CompleteOnlineEMVRequest();

        coemvreq.setOnlineAuthorizationResult("0");
        coemvreq.setResponseCode("00");

        return coemvreq;
    }

    public RemoveCard.RemoveCardRequest doRemoveCardRequest(){
        RemoveCard.RemoveCardRequest remcardreq =  new RemoveCard.RemoveCardRequest();

        remcardreq.setMessage1("Success");
        remcardreq.setMessage2("Please remove card");

        return remcardreq;
    }

    public void processtrans(View view) {
        InputAccount.inputAccountWithEMV(this, doInputAccount(), commset(), new BasePOSLinkCallback<InputAccount.InputAccountResponse>() {
            @Override
            public void onFinish(InputAccount.InputAccountResponse inputAccountResponse) {
                Log.i("inputResult code", inputAccountResponse.getResultCode());
                Log.i("inputResult text", inputAccountResponse.getResultTxt());
                Log.i("Entry mode", inputAccountResponse.getEntryMode());
                Log.i("Track1 data", inputAccountResponse.getTrack1Data());
                Log.i("Track2 data", inputAccountResponse.getTrack2Data());
                Log.i("Track3 data", inputAccountResponse.getTrack3Data());
                Log.i("PAN", inputAccountResponse.getPan());
                Log.i("Masked PAN", inputAccountResponse.getMaskedPAN());
                Log.i("Trans KSN", inputAccountResponse.getKsn());
                Log.i("ClssTransPath", inputAccountResponse.getContactlessTransactionPath());
                Log.i("ClssAuthRes", inputAccountResponse.getAuthorizationResult());
                Log.i("EMV data", inputAccountResponse.getEmvData());

                if(inputAccountResponse.getEntryMode().equals("4")) {
                    AuthorizeCard.authorize(getApplicationContext(), doAuthorizeCard(), commset(), new BasePOSLinkCallback<AuthorizeCard.AuthorizeResponse>() {
                        @Override
                        public void onFinish(AuthorizeCard.AuthorizeResponse authorizeResponse) {
                            Log.i("authResult code", authorizeResponse.getResultCode());
                            Log.i("authResult text", authorizeResponse.getResultTxt());
                            Log.i("AuthRes1", authorizeResponse.getAuthorizationResult());
                            Log.i("EMV data", authorizeResponse.getEmvData());
                            Log.i("PIN KSN", authorizeResponse.getKSN());
                            Log.i("PIN Block", authorizeResponse.getPinBlock());
                            Log.i("PIN bypass status", authorizeResponse.getPinBypassStatus());

                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    final CompleteOnlineEMV.CompleteOnlineEMVResponse res = CompleteOnlineEMV.completeOnlineEMV(getApplicationContext(), doCompleteOnlineEMVRequest(), commset());
                                    Log.i("onlineEMVResult code", res.getResultCode());
                                    Log.i("onlineEMVResult text", res.getResultTxt());
                                    Log.i("AuthRes2", res.getAuthorizationResult());
                                    Log.i("EMV data", res.getEmvData());

                                    RemoveCard.removeCard(getApplicationContext(), doRemoveCardRequest(), commset(), new BasePOSLinkCallback<RemoveCard.RemoveCardResponse>() {
                                        @Override
                                        public void onFinish(RemoveCard.RemoveCardResponse removeCardResponse) {
                                            Log.i("removeResult code", removeCardResponse.getResultCode());
                                            Log.i("removeResult text", removeCardResponse.getResultTxt());
                                        }
                                    }, null);
                                }
                            });
                            thread.start();
                        }
                    }, null);
                }
            }
        }, null);
    }
}