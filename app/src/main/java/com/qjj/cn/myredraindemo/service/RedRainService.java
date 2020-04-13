package com.qjj.cn.myredraindemo.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;

import com.qjj.cn.myredraindemo.App;
import com.qjj.cn.myredraindemo.RedRainActivity;
import com.qjj.cn.myredraindemo.model.RedRainActivityResponse;
import com.qjj.cn.myredraindemo.util.DateUtils;

import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * created by QinJiaJun
 * Email qinjiajun_1230@163.com
 * on 2019/10/25
 * Describe: 红包雨活动服务
 */

public class RedRainService extends Service {
    private static final String TAG = "RedRain";
    public static final String SHOWTYPE_KEY = "SHOWTYPE_KEY";
    public static final int TYPE_START_REDRAIN = 100;
    public static final String DURATION_KEY = "DURATION_KEY";
    public static final String COUNTDOWN_KEY = "COUNTDOWN_KEY";
    public static final String REDPACKETRAINID_KEY = "redPacketRainId";
    public static final String PERCENT_KEY = "PERCENT_KEY";
    public static final String REDRAINACTIVITYRESPONSE_KEY = "REDRAINACTIVITYRESPONSE_KEY";

    private RedRainActivityResponse.ResultEntity data;
    private AlarmManager mAlarmManager;
    private Timer mTimer;
    /**
     * 倒计时任务
     */
    private TimerTask countDownTask;

    /**
     * 红包雨持续的时间
     * 15 秒
     */
    private int duration = 10;
    /**
     * 红包雨 倒计时
     * 单位 秒
     */
    private int countdown = 10;
    /**
     * 红包雨 包含红包大类，1现金，2通证
     * 单位 秒
     */
    private String types = "1,2";
    /**
     * 中奖率
     */
    private int percent = 100;

    private String redpacketrainid; //红包雨ID
    /**
     * 定时任务Intent
     */
    private PendingIntent pi;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 200:
                    Log.i(TAG, "RedRainService   mHandler  countdown: " + countdown + "     duration: " + duration);
                    if (countdown > 0) {
                        countdown--;
                    } else {
                        if (0 == duration) {
                            cancelTimeAccount();
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
//                                    getRedPacketRainInfo();
                                    mHandler.post(runnableRain);
                                }
                            }, 2000);
                        }
                        duration--;
                    }
                    break;
            }
        }
    };

    /**
     * 显示奖励界面
     */
    private Runnable runnablePop = new Runnable() {
        @Override
        public void run() {
            if (App.getAppContext().isActivity()) {
                Log.i(TAG, "RedRainService    runnablePop  StartRedRainAwardPopActivity ");
                mHandler.removeCallbacks(runnablePop);
            } else {
                Log.i(TAG, "RedRainService    runnablePop  postDelayed  1000");
                mHandler.postDelayed(this, 1000);
            }

        }
    };

    /**
     * 请求最新红包雨信息
     */
    private Runnable runnableRain = new Runnable() {
        @Override
        public void run() {
            if (App.getAppContext().isActivity()) {
                getRedPacketRainInfo();
                Log.i(TAG, "RedRainService    runnableRain  getRedPacketRainInfo ");
                mHandler.removeCallbacks(runnableRain);
            } else {
                Log.i(TAG, "RedRainService    runnableRain  postDelayed  1000");
                mHandler.postDelayed(this, 1000);
            }

        }
    };


    /**
     * 打开红包雨界面
     */
    private Runnable runnableStartRain = new Runnable() {
        @Override
        public void run() {
            if (App.getAppContext().isActivity()) {
                RedRainActivity.startBundleRedRainActivity(RedRainService.this,data);
                Log.i(TAG, "RedRainService    runnableStartRain  runnableStartRain ");
                mHandler.removeCallbacks(runnableRain);
            } else {
                Log.i(TAG, "RedRainService    runnableStartRain  postDelayed  1000");
                //后台就不开启了
//                mHandler.postDelayed(runnableRain, 1000);
            }

        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "RedRainService   onStartCommand   flags: " + flags);
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }

        int type = intent.getIntExtra(SHOWTYPE_KEY, TYPE_START_REDRAIN);

        Log.i(TAG, "RedRainService    onStartCommand  type: " + type);
        if (type == TYPE_START_REDRAIN) {
            cancelTimeAccount();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    getRedPacketRainInfo();
                    mHandler.post(runnableRain);
                }
            }, 100);
        }
        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * 调用接口
     * 获取红包雨信息
     */
    private void getRedPacketRainInfo() {
        RedRainActivityResponse rainOpenResponse = new RedRainActivityResponse();
        rainOpenResponse.setCode(1);
        rainOpenResponse.setStatus(1);
        rainOpenResponse.setData(new RedRainActivityResponse.ResultEntity());
        rainOpenResponse.getData().setBeginTime("1573091430");
        rainOpenResponse.getData().setServerTime("1573091429");
        rainOpenResponse.getData().setCountdown("3");
        rainOpenResponse.getData().setDuration("10");
        rainOpenResponse.getData().setPercent(80);
        rainOpenResponse.getData().setRedPacketRainId("561651651561");
        if (rainOpenResponse.getStatus() == 1) {
            RedRainActivityResponse.ResultEntity data = rainOpenResponse.getData();
            redpacketrainid = data.getRedPacketRainId();
            Log.i(TAG, "RedRainService      getRedPacketRainInfo  rainOpenResponse.getData(): " + data.toString());
            startRedPacketRainAlarm(data);
        } else {
            App.getAppContext().setRedRain(false);
            onDestroy();
        }

    }

    /**
     * 开始定时任务
     * 时间戳位10位  精确到秒
     *
     * @param data
     */
    private void startRedPacketRainAlarm(RedRainActivityResponse.ResultEntity data) {
        if (data != null) {
            this.data = data;
            this.percent = data.getPercent();
            if (mAlarmManager == null) {
                mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            }
            //下雨持续时长，单位秒
            int duration = Integer.parseInt(data.getDuration());
            //服务器当前时间，时间戳
            Long serverTime = Long.parseLong(data.getServerTime());
            //每场开始时间 开始下雨时间 ，时间戳
            Long beginTime = Long.parseLong(data.getBeginTime());
            //每次红包结束时间
            Long endTime = beginTime + duration;
            try {
                Log.i(TAG, "StartRedRain  RedRainService   startRedPacketRainAlarm "+
                        "serverTime: " + data.getServerTime() + "  " + DateUtils.longToString(serverTime * 1000L, "yyyy-MM-dd HH:mm:ss") +
                        "   endTime: " + endTime + "  " + DateUtils.longToString(endTime * 1000L, "yyyy-MM-dd HH:mm:ss"));
                Log.i(TAG,"serverTime == beginTime?"+(serverTime == beginTime ));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (serverTime < beginTime) {
                Log.i(TAG, "RedRainService  serverTime < startTime   getStartTimeToTask ");
                App.getAppContext().setRedRain(true);
                getStartTimeToTask(serverTime, beginTime, data);
                return;
            } else if (serverTime.longValue() == beginTime.longValue()) {
                App.getAppContext().setRedRain(true);
                Log.i(TAG, "RedRainService  serverTime == startTime   setStartRedRainActivity ");
                this.duration = duration;
                RedRainActivity.setStartRedRainActivity(RedRainService.this,redpacketrainid,duration, 0,  percent);
                return;
            } else {
                Log.i(TAG, "RedRainService   getStartTimeToTask   红包雨活动时间已超出  活动结束了");
                App.getAppContext().setRedRain(false);
                onDestroy();
            }
        }
    }

    /**
     * 根据服务器时间 进行分配任务
     *
     * @param serverTime 服务器当前时间  12:30:00
     * @param startTime  开始时间
     * @param data
     */
    private void getStartTimeToTask(Long serverTime, Long startTime, RedRainActivityResponse.ResultEntity data) {
        Log.i(TAG, "StartRedRain  RedRainService   getStartTimeToTask  " +"红包雨");
        Log.i(TAG, "RedRainService   getStartTimeToTask  RedRainActivityResponse data: " + data.toString());
        this.data = data;
        this.percent = data.getPercent();
        this.redpacketrainid = data.getRedPacketRainId();
        try {
            Log.i(TAG, "RedRainService   getStartTimeToTask  serverTime: " + serverTime
                    + "  " + DateUtils.longToString(serverTime * 1000L, "yyyy-MM-dd HH:mm:ss") + "   beginTime: " + startTime + "  " + DateUtils.longToString(startTime * 1000L, "yyyy-MM-dd HH:mm:ss"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (serverTime < startTime) {
            // 小于倒计时时间 开始计时任务
            long anHour = ((startTime - serverTime)) * 1000L; // 这是毫秒数
            Log.i(TAG, "RedRainService   getStartTimeToTask   红包雨活动 即将到来 倒计时中 " + anHour + "毫秒");
            //方法二
            mHandler.postDelayed(runnableStartRain, anHour);
        } else if (serverTime.longValue() == startTime.longValue()) {
            Log.i(TAG, "RedRainService   getStartTimeToTask   红包雨活动 即将到来 倒计时中 " + "正好3秒");
            RedRainActivity.startRedRainActivity(RedRainService.this,data);
        } else {
            Log.i(TAG, "RedRainService   getStartTimeToTask   红包雨 活动时间结束了");
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelTimeAccount();
        mHandler.removeCallbacks(runnablePop);
        mHandler.removeCallbacks(runnableRain);
        Log.i(TAG, "RedRainService   onDestroy -- ");
    }

    //取消计时
    public void cancelTimeAccount() {
        if (countDownTask != null) {
            countDownTask.cancel();
        }
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = null;
        countDownTask = null;
        if (pi != null && mAlarmManager != null) {
            mAlarmManager.cancel(pi);
            pi = null;
        }
    }


}
