package com.qjj.cn.myredraindemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.qjj.cn.myredraindemo.model.RedRainActivityResponse;
import com.qjj.cn.myredraindemo.service.RedRainService;
import com.qjj.cn.myredraindemo.util.StatusBarUtil;
import com.qjj.cn.myredraindemo.widget.RedRainPopupView;

import java.util.HashMap;
import java.util.Map;


/**
 * created by QinJiaJun
 * Email qinjiajun_1230@163.com
 * on 2019/10/25
 * Describe: 红包雨活动界面
 */
public class RedRainActivity extends Activity {
    private RedRainPopupView redRainPopupView;
    private int type = 0;
    /**
     * 红包雨ID
     */
    private String redpacketrainid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setStatusBar(this, true, true, R.color.transparent);
        setContentView(R.layout.activity_red_rain);
        initView();
        initData();
    }


    private void initView() {
        redRainPopupView = findViewById(R.id.redRainPopupView);
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundleExtra = intent.getBundleExtra("data");
            if (bundleExtra != null) {
                RedRainActivityResponse.ResultEntity data = bundleExtra.getParcelable(RedRainService.REDRAINACTIVITYRESPONSE_KEY);
                this.type = bundleExtra.getInt("type", 4);
                Log.i("RedRain", "RedRainActivity initView   intent  type: " + type + "  data:" + data.toString());
                getDataToUI(data);
            } else {
                this.type = intent.getIntExtra("type", 0);
                if (type == 2) {
                    int duration = intent.getIntExtra(RedRainService.DURATION_KEY, 0);
                    int countdown = intent.getIntExtra(RedRainService.COUNTDOWN_KEY, 0);
                    int percent = intent.getIntExtra(RedRainService.PERCENT_KEY, 50);
                    this.redpacketrainid = intent.getStringExtra(RedRainService.REDPACKETRAINID_KEY);
                    String types = intent.getStringExtra(RedRainService.TYPES_KEY);
                    redRainPopupView.setCountDown(countdown);
                    redRainPopupView.setDuration(duration);
                    redRainPopupView.setRedpacketrainid(redpacketrainid);
                    redRainPopupView.setTypes(types);
                    redRainPopupView.setProbability(percent);
                } else {
                    RedRainActivityResponse.ResultEntity data = intent.getParcelableExtra(RedRainService.REDRAINACTIVITYRESPONSE_KEY);
                    Log.i("RedRain", "RedRainActivity    intent  data: " + data.toString());
                    getDataToUI(data);
                }
            }
        }

        redRainPopupView.setOnProgressListener(new RedRainPopupView.OnProgressListener() {
            @Override
            public void onStopRedRain(int number) {
                dismiss();
            }

            @Override
            public void onEnd(boolean isAdd) {
                Log.i("RedRain", "StartRedRain   RedRainActivity    onEnd  getSession(): " + redRainPopupView.getSession() + "  getRedpacketrainid(): " + redRainPopupView.getRedpacketrainid() + "   redpacketrainid: " + redpacketrainid );
                //红包雨结算界面  由于一些原因 暂时不公开
                dismiss();
            }
        });

        redRainPopupView.run();
        Log.i("RedRain", "StartRedRain  RedRainActivity    intent   redpacketrainid: " + redpacketrainid);
    }

    private void initData() {
        getRedPacketData();
    }

    private void getDataToUI(RedRainActivityResponse.ResultEntity data) {
        if (data != null) {
            this.redpacketrainid = data.getRedPacketRainId();
            redRainPopupView.setTypes(data.getTypes());
            redRainPopupView.setRedpacketrainid(data.getRedPacketRainId());
            redRainPopupView.setProbability(data.getPercent());
            if (type == 0 || type == 4) {
                redRainPopupView.setCountDown(Integer.parseInt(data.getCountdown()));
                redRainPopupView.setDuration(Integer.parseInt(data.getDuration()));
            } else if (type == 1) {
                Long serverTime = Long.parseLong(data.getServerTime()); //服务器当前时间，时间戳
                Long beginTime = Long.parseLong(data.getBeginTime());//开始下雨时间 ，时间戳
                if (serverTime < beginTime) {
                    long time = beginTime - serverTime;
                }
            }
        }
    }

    /**
     * 获取当前场次红包雨 获得的具体金额数
     * 如果在红包雨下落期间（活动期间） 用户手动关闭当前界面
     * 再次打开后 应当再次显示关闭前同场红包雨获得金额数 以及展示列表
     */
    private void getRedPacketData() {
        Map<String, String> stringMap = new HashMap<>();
        stringMap.put("redPacketRainId", redpacketrainid);
        //请求服务器
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
