package com.qjj.cn.myredraindemo.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qjj.cn.myredraindemo.R;
import com.qjj.cn.myredraindemo.model.RedPacketBean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * created by QinJiaJun
 * Email qinjiajun_1230@163.com
 * on 2019/10/30
 * Describe:红包雨弹窗View
 */
public class RedRainPopupView extends RelativeLayout implements View.OnClickListener {
    private FrameLayout ll_root;
    private View line;
    private View rl_clock, btn_close;
    private TextView tv_countdown,total_countTv;
    private RelativeLayout red_rain_view;
    private ImageView iv_clock;
    private RedPacketView redPacketsView;
    private Timer timer = new Timer();
    private Context mContext;
    private OnProgressListener mOnProgressListener;

    /**
     * 红包雨 倒计时
     * 单位 秒
     */
    private int countdown = 10;
    /**
     * 倒计时计数
     * 单位 秒
     */
    private int number = 0;
    private long numberTime = 60 * 1000L;
    /**
     * 红包雨持续的时间
     * 60 秒
     */
    private int duration = 60;

    /**
     * 有效红包总个数
     */
    private int totalCount = 0;

    /**
     * 中奖率
     */
    private int probability = 50;
    private String redpacketrainid; //红包雨ID

    private SimpleDateFormat formatter = new SimpleDateFormat("mm:ss.SSS");
    /**
     * 开红包累加金额集合
     */
    Map<String, Double> totalmoneysList = new HashMap<>();
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    numberTime = numberTime - 125;
                    if (numberTime > 0) {
                        tv_countdown.setText(formatter.format(new Date(numberTime)));
                    } else {
                        numberTime = 0;
                        stopTask.cancel();
                    }

                    break;

                case 200:// 开始下红包雨
                    Log.i("RedRain", "RedRainPopupView   mHandler  300 倒计时中  countdown:" + countdown);
                    if (countdown == 3) {
                        iv_clock.setImageResource(R.mipmap.red_clock_3);
                    } else if (countdown == 2) {
                        iv_clock.setImageResource(R.mipmap.red_clock_2);
                    } else if (countdown == 1) {
                        iv_clock.setImageResource(R.mipmap.red_clock_1);
                    } else if (countdown == 0) {
                        rl_clock.setVisibility(View.GONE);
                        red_rain_view.setVisibility(View.VISIBLE);
                        countDownTask.cancel();
                        startRedRain();
                        timer.schedule(startRedRainTask, 0, 1000);
                        timer.schedule(stopTask, 0, 125);
                        return;
                    }
                    countdown--;
                    break;

                case 300://  红包雨倒计时
                    Log.i("RedRain", "RedRainPopupView   mHandler  300 红包雨进行中  duration:" + duration);
                    if (0 == duration) {
                        stopRedRain();
                    }
                    duration--;
                    break;
            }
        }
    };

    /**
     * 倒计时
     */
    TimerTask countDownTask = new TimerTask() {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(200);
        }
    };

    /**
     * 开始红包
     */
    TimerTask startRedRainTask = new TimerTask() {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(300);
        }
    };

    /**
     * 开始红包
     */
    TimerTask stopTask = new TimerTask() {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(100);
        }
    };



    public RedRainPopupView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public RedRainPopupView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public void initView(Context context) {
        mContext = context;
        View mainView = LayoutInflater.from(context).inflate(R.layout.popupwindow_red_rain_layout, this, true);
        ll_root = mainView.findViewById(R.id.ll_root);
        line = mainView.findViewById(R.id.line);
        rl_clock = mainView.findViewById(R.id.rl_clock);
        btn_close = mainView.findViewById(R.id.btn_close);
        red_rain_view = mainView.findViewById(R.id.red_rain_view);
        iv_clock = mainView.findViewById(R.id.iv_clock);
        redPacketsView = mainView.findViewById(R.id.red_packets_view);
        tv_countdown = mainView.findViewById(R.id.tv_countdown);
        total_countTv = mainView.findViewById(R.id.total_count);
        btn_close.setOnClickListener(this);
        redPacketsView.setCount(20);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_close) {
            if (mOnProgressListener != null) {
                mOnProgressListener.onStopRedRain(this.duration);
            }
        }
    }

    /**
     * 设置当前倒计时  单位秒
     *
     * @param time 如果  time=0  则已经开始
     */
    public void setCountDown(int time) {
        this.countdown = time;
    }

    /**
     * 设置下雨持续多久时长 单位秒
     *
     * @param time 如果  time=0  则已经结束
     */
    public void setDuration(int time) {
        this.duration = time;
        this.numberTime = time * 1000L;
    }

    public String getRedpacketrainid() {
        return redpacketrainid;
    }

    public void setRedpacketrainid(String redpacketrainid) {
        this.redpacketrainid = redpacketrainid;
    }

    /**
     * 设置下雨已进行多少秒 单位秒
     *
     * @param number
     */
    public void setNumberTime(int number) {
        this.number = number;
    }

    public int getCountdown() {
        return this.countdown;
    }


    public int getDuration() {
        return this.duration;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    /**
     * 开始下红包雨
     */
    public void run() {
        if (countdown > 0) {
            timer.schedule(countDownTask, 0, 1000);
            rl_clock.setVisibility(View.VISIBLE);
            red_rain_view.setVisibility(View.GONE);
        } else {
            rl_clock.setVisibility(View.GONE);
            red_rain_view.setVisibility(View.VISIBLE);
            if (duration > 0) {
                timer.schedule(startRedRainTask, 0, 1000);
                timer.schedule(stopTask, 0, 125);
                startRedRain();
            }
        }
    }

    /**
     * 开始下红包雨
     */
    public void startRedRain() {
        redPacketsView.startRain();
        redPacketsView.setOnRedPacketClickListener(new RedPacketView.OnRedPacketClickListener() {
            @Override
            public void onRedPacketClickListener(RedPacketBean redPacket) {
                //请求服务器 调用开红包接口
                boolean isWinning = redPacket.isRealRedPacket(probability);
                if (isWinning) {
                    if (totalCount == 4) {
                        redPacket.setType(RedPacketBean.TYPE_BOOM_BT);
                        setWinning(redPacket);
                    } else {
                        redPacketRainOpen(redPacket);
                        totalCount += 1;
                        total_countTv.setText(totalCount+"");
                    }
                } else {
                    redPacket.setType(RedPacketBean.TYPE_BOOM_BT);
                    setWinning(redPacket);
                }
            }
        });
    }

    /**
     * 停止下红包雨
     */
    public void stopRedRain() {
        totalCount = 0;//金额清零
        startRedRainTask.cancel();
        stopTask.cancel();
        timer.cancel();
        duration = 0;
        if (mOnProgressListener != null) {
            if (totalmoneysList.size() > 0) {
                mOnProgressListener.onEnd(true);
            } else {
                mOnProgressListener.onEnd(false);
            }

        }
        redPacketsView.stopRainNow();
    }

    /**
     * 窗口监听
     */
    public interface OnProgressListener {
        void onStopRedRain(int time);

        void onEnd(boolean isAdd);

    }

    public void setOnProgressListener(OnProgressListener listener) {
        this.mOnProgressListener = listener;
    }


    public int getProbability() {
        return probability;
    }

    public void setProbability(int probability) {
        this.probability = probability;
    }

    /**
     * 调用中奖打开红包接口
     *
     * @param redPacket
     */
    private void redPacketRainOpen(final RedPacketBean redPacket) {

        RedPacketBean redPacketBean = new RedPacketBean();
        redPacketBean.setSymbol("1");
        redPacketBean.setType(RedPacketBean.TYPE_VALID_BT);
        redPacketBean.x = redPacket.x;
        redPacketBean.y = redPacket.y;
        setWinning(redPacketBean);

        Log.i("RedRainMoney", " Post  " + redPacketBean.getSymbol()+" Winning= true"+" totalCount= "+totalCount );
    }

    /**
     * 设置中奖
     */
    private synchronized void setWinning(final RedPacketBean redPacket) {
        String text = "";
        final TextView tv = new TextView(redPacketsView.getContext());
        if (redPacket.getType() == RedPacketBean.TYPE_BOOM_BT) {
            text = "";
        } else {
            text = "+" + redPacket.getSymbol();
        }
        tv.setText(text);
        tv.setTextColor(Color.parseColor("#FF4917"));
        tv.setTextSize(30);
        tv.getPaint().setFakeBoldText(true);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins((int) redPacket.x, (int) redPacket.y, 0, 0);
        tv.setLayoutParams(lp);
        red_rain_view.addView(tv);
        final float y = line.getY();
        PropertyValuesHolder scaleYProper = PropertyValuesHolder.ofFloat("translationY", 0, -1300);
        PropertyValuesHolder scaleXHolder = PropertyValuesHolder.ofFloat("scaleX", 1, 1.1f);
        PropertyValuesHolder scaleYHolder = PropertyValuesHolder.ofFloat("scaleY", 1, 1.1f);
        final ValueAnimator animator = ObjectAnimator.ofPropertyValuesHolder(tv, scaleYProper, scaleXHolder, scaleYHolder);
        animator.setDuration(1400);
        animator.start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (tv.getY() > 0 && tv.getY() <= y) {
                    tv.setVisibility(View.GONE);
                    animator.cancel();
                }

            }
        });

        Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (tv != null) {
                    tv.setVisibility(View.GONE);
                    if (red_rain_view != null) {
                        red_rain_view.removeView(tv);
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };
        animator.addListener(mAnimatorListener);
    }

    public void onDestroy() {
        if (timer != null) {
            timer.cancel();
        }
        if (countDownTask != null) {
            countDownTask.cancel();
        }

        if (startRedRainTask != null) {
            startRedRainTask.cancel();
        }
        if (stopTask != null) {
            stopTask.cancel();
        }
        timer = null;
        countDownTask = null;
        startRedRainTask = null;

        mContext = null;

        mHandler.removeMessages(100);
        mHandler.removeMessages(200);
        mHandler.removeMessages(300);
        mHandler.removeMessages(400);
        totalmoneysList.clear();
        totalmoneysList = null;
        mHandler = null;
    }

}
