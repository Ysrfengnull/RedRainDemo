package com.qjj.cn.myredraindemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.qjj.cn.myredraindemo.model.RedRainActivityResponse;
import com.qjj.cn.myredraindemo.service.RedRainService;
import com.qjj.cn.myredraindemo.util.StatusBarUtil;
import com.qjj.cn.myredraindemo.widget.RedRainPopupView;


/**
 * created by QinJiaJun
 * Email qinjiajun_1230@163.com
 * on 2019/10/25
 * Describe: 红包雨活动界面
 */
public class RedRainActivity extends Activity {
    private RedRainPopupView redRainPopupView;

    /**
     * 红包雨ID
     */
    private String redpacketrainid;

    public static void startRedRainActivity(Context context,RedRainActivityResponse.ResultEntity data) {
        Intent intent = new Intent(context, RedRainActivity.class);
        intent.putExtra(RedRainService.REDRAINACTIVITYRESPONSE_KEY, data);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    public static void startBundleRedRainActivity(Context context, RedRainActivityResponse.ResultEntity data ){
        Intent intent = new Intent(context , RedRainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(RedRainService.REDRAINACTIVITYRESPONSE_KEY, data);
        intent.putExtra("data", bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void setStartRedRainActivity(Context context,String redpacketrainid, int duration, int countdown, int percent) {
        Intent intent = new Intent(context, RedRainActivity.class);
        intent.putExtra(RedRainService.DURATION_KEY, duration);
        intent.putExtra(RedRainService.COUNTDOWN_KEY, countdown);
        intent.putExtra(RedRainService.REDPACKETRAINID_KEY, redpacketrainid);
        intent.putExtra(RedRainService.PERCENT_KEY, percent);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setStatusBar(this, true, true, R.color.transparent);
        setContentView(R.layout.activity_red_rain);
        initView();
    }


    private void initView() {
        redRainPopupView = findViewById(R.id.redRainPopupView);
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundleExtra = intent.getBundleExtra("data");
            if (bundleExtra != null) {
                RedRainActivityResponse.ResultEntity data = bundleExtra.getParcelable(RedRainService.REDRAINACTIVITYRESPONSE_KEY);
                Log.i("RedRain", "RedRainActivity initView   intent  type: " + "  data:" + data.toString());
                getDataToUI(data);
            } else {
                dismiss();
            }
        }

        redRainPopupView.setOnProgressListener(new RedRainPopupView.OnProgressListener() {
            @Override
            public void onStopRedRain(int number) {
                Log.i("RedRain", "StartRedRain   RedRainActivity    onEnd  getTotalCount(): " + redRainPopupView.getTotalCount() + "  getRedpacketrainid(): " + redRainPopupView.getRedpacketrainid() + "   redpacketrainid: " + redpacketrainid );
                dismiss();
            }

            @Override
            public void onEnd(boolean isAdd) {
                Log.i("RedRain", "StartRedRain   RedRainActivity    onEnd  getTotalCount(): " + redRainPopupView.getTotalCount() + "  getRedpacketrainid(): " + redRainPopupView.getRedpacketrainid() + "   redpacketrainid: " + redpacketrainid );
                //红包雨结算界面  由于一些原因 暂时不公开
                dismiss();
            }
        });

        redRainPopupView.run();
        Log.i("RedRain", "StartRedRain  RedRainActivity    intent   redpacketrainid: " + redpacketrainid);
    }

    private void getDataToUI(RedRainActivityResponse.ResultEntity data) {
        if (data != null) {
            this.redpacketrainid = data.getRedPacketRainId();
            redRainPopupView.setRedpacketrainid(data.getRedPacketRainId());
            redRainPopupView.setProbability(data.getPercent());
            redRainPopupView.setCountDown(Integer.parseInt(data.getCountdown()));
            redRainPopupView.setDuration(Integer.parseInt(data.getDuration()));
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void dismiss() {
        overridePendingTransition(R.anim.anim_rain_in, R.anim.anim_rain_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (redRainPopupView != null) {
            redRainPopupView.onDestroy();
        }
    }
}
