package com.qjj.cn.myredraindemo.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.qjj.cn.myredraindemo.App;
import com.qjj.cn.myredraindemo.R;
import com.qjj.cn.myredraindemo.RedRainActivity;
import com.qjj.cn.myredraindemo.model.RedRainActivityResponse;
import com.qjj.cn.myredraindemo.util.DateUtils;

import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;

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
    public static final int TYPE_LANGUAGE = 105;

    public static final String DURATION_KEY = "DURATION_KEY";
    public static final String COUNTDOWN_KEY = "COUNTDOWN_KEY";
    public static final String TYPES_KEY = "TYPES_KEY";
    public static final String REDPACKETRAINID_KEY = "redPacketRainId";
    public static final String PERCENT_KEY = "PERCENT_KEY";
    public static final String REDRAINACTIVITYRESPONSE_KEY = "REDRAINACTIVITYRESPONSE_KEY";

    private RedRainActivityResponse.ResultEntity data;


    private WindowManager.LayoutParams mLayoutParams;
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
    private int duration = 60;
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
                Intent intent = new Intent(RedRainService.this, RedRainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(RedRainService.REDRAINACTIVITYRESPONSE_KEY, data);
                bundle.putInt("type", 4);
                intent.putExtra("data", bundle);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Log.i(TAG, "RedRainService    runnableStartRain  runnableStartRain ");
                mHandler.removeCallbacks(runnableRain);
            } else {
                Log.i(TAG, "RedRainService    runnableStartRain  postDelayed  1000");
                mHandler.postDelayed(runnableRain, 1000);
            }

        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "RedRainService   onCreate   ");

        //设置WindowManger布局参数以及相关属性
        mLayoutParams = new WindowManager.LayoutParams();
        Log.i(TAG, "RedRainService   onDestroy --  " );
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
        }else if (type == TYPE_LANGUAGE) {
            Log.i(TAG, "RedRainService   onStartCommand  TYPE_LANGUAGE  mFloatingView INVISIBLE ");
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showFloatingWindow();
                    mHandler.post(runnableRain);
                }
            }, 1000);
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
        rainOpenResponse.getData().setBeginTime("1573091440");
        rainOpenResponse.getData().setServerTime("1573091429");
        rainOpenResponse.getData().setCountdown("3");
        rainOpenResponse.getData().setForecast("10");
        rainOpenResponse.getData().setDuration("10");
        rainOpenResponse.getData().setPercent(80);
        rainOpenResponse.getData().setRedPacketRainId("561651651561");
        rainOpenResponse.getData().setTypes("1,2");
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
            //开始倒计时时间 ，时间戳
            int countdown = Integer.parseInt(data.getCountdown());
            //服务器当前时间，时间戳
            Long serverTime = Long.parseLong(data.getServerTime());
            //开始下雨时间 ，时间戳
            Long beginTime = Long.parseLong(data.getBeginTime());
            //每场开始时间
            Long startTime = (beginTime );
            try {
                Log.i(TAG, "RedRainService   startRedPacketRainAlarm  " +
                        "serverTime: " + data.getServerTime() + "  " + DateUtils.longToString(serverTime * 1000L, "yyyy-MM-dd HH:mm:ss") +
                        "   beginTime: " + data.getBeginTime() + "  " + DateUtils.longToString(beginTime * 1000L, "yyyy-MM-dd HH:mm:ss")

                );
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //每场开始倒计时任务时间
            Long countdownTime = startTime - countdown;
            //每次红包结束时间
            Long endTime = startTime + duration;
            try {
                Log.i(TAG, "StartRedRain  RedRainService   startRedPacketRainAlarm "+
                        "serverTime: " + data.getServerTime() + "  " + DateUtils.longToString(serverTime * 1000L, "yyyy-MM-dd HH:mm:ss") +
                        "   startTime: " + startTime + "  " + DateUtils.longToString(startTime * 1000L, "yyyy-MM-dd HH:mm:ss") +
                        "   countdownTime: " + countdownTime + "  " + DateUtils.longToString(countdownTime * 1000L, "yyyy-MM-dd HH:mm:ss") +
                        "   endTime: " + endTime + "  " + DateUtils.longToString(endTime * 1000L, "yyyy-MM-dd HH:mm:ss"));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (serverTime < startTime) {
                Log.i(TAG, "RedRainService  serverTime < startTime   getStartTimeToTask ");
                App.getAppContext().setRedRain(true);
                getStartTimeToTask(serverTime, startTime, data);
                return;
            } else if (serverTime == startTime) {
                App.getAppContext().setRedRain(true);
                Log.i(TAG, "RedRainService  serverTime == startTime   setStartRedRainActivity ");
                setStartRedRainActivity(duration, 0, data.getTypes(), 2);
                return;
            } else {
                Log.i(TAG, "RedRainService   getStartTimeToTask   红包雨活动时间已超出  活动结束了");
                App.getAppContext().setRedRain(false);
                onDestroy();
            }
        }
    }

    private void showFloatingWindow() {
        //Android 8  对悬浮窗 进行了 改变
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        //View以外的区域可以响应点击和触摸事件
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //设置位图格式 默认是不透明的
        mLayoutParams.format = PixelFormat.TRANSPARENT;
        //初始化位置
        mLayoutParams.x = 10;
        mLayoutParams.y = 100;
    }


    private class FloatingOnTouchListener implements View.OnTouchListener {
        //开始触控的坐标，移动时的坐标（相对于屏幕左上角的坐标）
        private int mTouchStartX, mTouchStartY, mTouchCurrentX, mTouchCurrentY;
        //开始时的坐标和结束时的坐标（相对于自身控件的坐标）
        private int mStartX, mStartY, mStopX, mStopY;
        //判断悬浮窗口是否移动，这里做个标记，防止移动后松手触发了点击事件
        private boolean isMove;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            int action = event.getAction();

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    isMove = false;
                    mTouchStartX = (int) event.getRawX();
                    mTouchStartY = (int) event.getRawY();
                    mStartX = (int) event.getX();
                    mStartY = (int) event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    mTouchCurrentX = (int) event.getRawX();
                    mTouchCurrentY = (int) event.getRawY();
                    mLayoutParams.x += mTouchCurrentX - mTouchStartX;
                    mLayoutParams.y += mTouchCurrentY - mTouchStartY;
                    mTouchStartX = mTouchCurrentX;
                    mTouchStartY = mTouchCurrentY;
                    break;
                case MotionEvent.ACTION_UP:
                    mStopX = (int) event.getX();
                    mStopY = (int) event.getY();
                    if (Math.abs(mStartX - mStopX) >= 1 || Math.abs(mStartY - mStopY) >= 1) {
                        isMove = true;
                    }
                    break;
            }
            //如果是移动事件不触发OnClick事件，防止移动的时候一放手形成点击事件
            return isMove;
        }
    }

    /**
     * 在显示悬浮窗得情况下 切换语言 重走生命周期
     *
     * @param countdown
     * @param duration
     * @param redpacketrainid
     * @param types
     */
    private void setVisibleFloatingTask(int countdown, int duration, String redpacketrainid,  String types) {
        this.redpacketrainid = redpacketrainid;

        this.countdown = countdown;
        this.duration = duration;
        this.types = types;
        Log.i(TAG, "RedRainService   setVisibleFloatingTask   开启悬浮窗任务");
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (countDownTask != null) {
            countDownTask.cancel();
            mTimer = null;
        }
        mHandler.removeMessages(200);

        if (mTimer == null) {
            mTimer = new Timer();
        }
        countDownTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(200);
            }
        };

        mTimer.schedule(countDownTask, 0, 1000);
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
        try {
            Log.i(TAG, "RedRainService   getStartTimeToTask  serverTime: " + serverTime
                    + "  " + DateUtils.longToString(serverTime * 1000L, "yyyy-MM-dd HH:mm:ss") + "   beginTime: " + startTime + "  " + DateUtils.longToString(startTime * 1000L, "yyyy-MM-dd HH:mm:ss"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //下雨持续时长，单位秒
        int duration = Integer.parseInt(data.getDuration());
        //开始倒计时时间 ，时间戳
        int countdown = Integer.parseInt(data.getCountdown());

        if (serverTime <= startTime) {
            long countdownTime = startTime - countdown;
            // 小于倒计时时间 开始计时任务
            if (serverTime < countdownTime) {
                long anHour = ((countdownTime - serverTime)) * 1000L; // 这是毫秒数
                Log.i(TAG, "RedRainService   getStartTimeToTask   红包雨活动 即将到来 倒计时中 " + anHour + "毫秒");

                //方法二
                mHandler.postDelayed(runnableStartRain, anHour);

            } else if (serverTime == countdownTime) {
                Log.i(TAG, "RedRainService   getStartTimeToTask   红包雨活动 即将到来 倒计时中 " + "正好10秒");
                startRedRainActivity(data, 0, 1);
            } else {
                if (serverTime < startTime) {
                    setStartRedRainActivity(duration, (int) (startTime - serverTime), data.getTypes(), 2);
                    Log.i(TAG, "RedRainService   getStartTimeToTask   红包雨活动 即将到来 倒计时秒： " + (int) (startTime - serverTime));
                } else {
                    Log.i(TAG, "RedRainService   getStartTimeToTask   红包雨活动 进行中 ");
                    setStartRedRainActivity((int) ((startTime + duration) - serverTime), 0, data.getTypes(), 2);
                }
            }
            this.redpacketrainid = data.getRedPacketRainId();
        } else {
            Log.i(TAG, "RedRainService   getStartTimeToTask   红包雨 活动时间结束了");
        }

    }

    private void startRedRainActivity(RedRainActivityResponse.ResultEntity data, int type, int session) {
        Intent i = new Intent(this, RedRainActivity.class);
        i.putExtra(RedRainService.REDRAINACTIVITYRESPONSE_KEY, data);
        i.putExtra("type", type);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private void setStartRedRainActivity(int duration, int countdown, String types, int type) {
        this.redpacketrainid = redpacketrainid;
        this.countdown = countdown;
        this.duration = duration;
        this.types = types;
        Intent intent = new Intent(RedRainService.this, RedRainActivity.class);
        intent.putExtra("type", type);
        intent.putExtra(RedRainService.DURATION_KEY, duration);
        intent.putExtra(RedRainService.COUNTDOWN_KEY, countdown);
        intent.putExtra(RedRainService.TYPES_KEY, types);
        intent.putExtra(RedRainService.REDPACKETRAINID_KEY, redpacketrainid);
        intent.putExtra(RedRainService.PERCENT_KEY, percent);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

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
