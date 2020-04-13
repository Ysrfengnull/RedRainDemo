package com.qjj.cn.myredraindemo.widget;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
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
import com.qjj.cn.myredraindemo.model.GetRedPacketRainOpenResponse;
import com.qjj.cn.myredraindemo.model.RedPacketBean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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

    private View rl_clock, btn_close;
    private TextView tv_countdown;
    private FrameLayout ll_root;
    private RelativeLayout red_rain_view;
    private ImageView iv_clock;
    private RedPacketView redPacketsView;
    private Timer timer = new Timer();
    private Context mContext;
    private OnProgressListener mOnProgressListener;

    /**
     * 红包雨 包含红包大类，1现金，2通证
     * 单位 秒
     */
    private String types = "1,2";
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
     * 场次  第几场红包雨
     * 5
     */
    private int session = 1;

    private int totalmoney = 0;

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
    private int index = 0;
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


                case 400:
                    TextView mTextView = (TextView) msg.obj;
                    if (mTextView != null) {
                        mTextView.setVisibility(View.GONE);
                        if (red_rain_view != null) {
                            red_rain_view.removeView(mTextView);
                        }
                    }
                    Bundle data = msg.getData();
                    String moneyType = data.getString("moneyType");
                    String contract = data.getString("contract");
                    int redPackeType = data.getInt("redPackeType", 5);
                    String money = data.getString("money");
                    Log.i("RedRainMoney", " 添加金币  " + contract + " , " + moneyType + " , " + money);
                    Log.i("TotalMoneys", " 获得金币  " + contract + " , " + moneyType + " , " + money);
                    double parseDouble = Double.parseDouble(money);
                    if (!totalmoneysList.containsKey(contract)) {
                        setTotalMoneys(contract, moneyType, parseDouble, redPackeType);
                    } else {
                        Double aDouble = totalmoneysList.get(contract) + parseDouble;
                        Iterator<Map.Entry<String, Double>> it = totalmoneysList.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry<String, Double> tab = it.next();
                            String key = tab.getKey();
                            if (key.equals(contract)) {
                                it.remove();
                                break;
                            }
                        }
                        setTotalMoneys(contract, moneyType, aDouble, redPackeType);
                    }
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
        rl_clock = mainView.findViewById(R.id.rl_clock);
        btn_close = mainView.findViewById(R.id.btn_close);
        red_rain_view = mainView.findViewById(R.id.red_rain_view);
        iv_clock = mainView.findViewById(R.id.iv_clock);
        redPacketsView = mainView.findViewById(R.id.red_packets_view);
        redPacketsView.setCount(50);
        tv_countdown = mainView.findViewById(R.id.tv_countdown);
        btn_close.setOnClickListener(this);
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

    public int getSession() {
        return session;
    }

    public void setSession(int session) {
        this.session = session;
    }

    public String getTypes() {
        if (redPacketsView != null) {
            return redPacketsView.getTypes();
        }
        return this.types;
    }

    public void setTypes(String types) {
        this.types = types;
        if (redPacketsView != null) {
            redPacketsView.setTypes(this.types);
        }
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
                    redPacketRainOpen(redPacket);
                } else {
                    setWinning(redPacket, false);
                }
            }
        });
    }

    /**
     * 停止下红包雨
     */
    public void stopRedRain() {
        totalmoney = 0;//金额清零
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


    private void setTotalMoneys(String contract, String moneyType, double money, int redPackeType) {
        synchronized (this) {
            Log.i("TotalMoneys", " 累加金币  " + contract + " , " + moneyType + " , " + money);
            totalmoneysList.put(contract, money);
 //更新个数
        }

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
        Map<String, String> map = new HashMap<>();
        if (redPacket.getType() == RedPacketBean.TYPE_BOOM_GOLD) {
            map.put("type", "1"); //1现金，2通证
        } else {
            map.put("type", "2");
        }
        map.put("redPacketRainId", redpacketrainid);//红包雨ID
        map.put("times", String.valueOf(session));//	第几场雨

        /**
         * 此处为接口返回数据
         */
        GetRedPacketRainOpenResponse rainOpenResponse = new GetRedPacketRainOpenResponse();
        rainOpenResponse.setCode(1);
        rainOpenResponse.setStatus(1);
        rainOpenResponse.setMsg("获得红包");
        rainOpenResponse.setData(new GetRedPacketRainOpenResponse.ResultEntity());
        int num = (int) (Math.random() * 5 + 1);
        switch (num) {
            case 1:
                rainOpenResponse.getData().setSymbol("AAA");
                rainOpenResponse.getData().setContract("AAA");
                rainOpenResponse.getData().setMoney("0.1235");
                break;
            case 2:
                rainOpenResponse.getData().setSymbol("BBB");
                rainOpenResponse.getData().setContract("BBB");
                rainOpenResponse.getData().setMoney("0.2012");
                break;
            case 3:
                rainOpenResponse.getData().setSymbol("CCC");
                rainOpenResponse.getData().setContract("CCC");
                rainOpenResponse.getData().setMoney("0.1013");
                break;
            case 4:
                rainOpenResponse.getData().setSymbol("DDD");
                rainOpenResponse.getData().setContract("DDD");
                rainOpenResponse.getData().setMoney("0.1132");
                break;
            case 5:
                rainOpenResponse.getData().setSymbol("EEE");
                rainOpenResponse.getData().setContract("EEE");
                rainOpenResponse.getData().setMoney("0.1354");
                break;
        }

        if (rainOpenResponse.getStatus() == 1) {
            GetRedPacketRainOpenResponse.ResultEntity data = rainOpenResponse.getData();
            Log.i("RedRainMoney", " Post  " + data.getContract() + " , " + data.getSymbol() + " , " + data.getMoney());
            RedPacketBean redPacketBean = new RedPacketBean();
            redPacketBean.setContract(data.getContract());
            redPacketBean.setMoney(data.getMoney());
            redPacketBean.setSymbol(data.getSymbol());
            redPacketBean.setType(redPacket.getType());
            redPacketBean.x = redPacket.x;
            redPacketBean.y = redPacket.y;
            setWinning(redPacketBean, true);
        } else {
            RedPacketBean redPacketBean = new RedPacketBean();
            redPacketBean.setType(redPacket.getType());
            redPacketBean.x = redPacket.x;
            redPacketBean.y = redPacket.y;
            setWinning(redPacketBean, false);
        }
    }

    /**
     * 设置中奖
     */
    private synchronized void setWinning(final RedPacketBean redPacket, final boolean isWinning) {
        String text = "";
        String money = "0";
        String types = "";
        final TextView tv = new TextView(redPacketsView.getContext());

        if (isWinning) {
            money = redPacket.getMoney();
            types = redPacket.getSymbol();
            if (redPacket.getType() == RedPacketBean.TYPE_BOOM_BT) {
                text = String.format("+%1$s%2$s", money, types);
            } else {
                text = "+" + money;
            }
            if (text.contains(types)) {
                // 字体颜色
                int index = text.indexOf(types);
                ForegroundColorSpan buleSpan = new ForegroundColorSpan(Color.parseColor("#FFFFFF"));
                SpannableStringBuilder style = new SpannableStringBuilder(text);
                style.setSpan(buleSpan, index, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                tv.setText(style);
            } else {
                tv.setText(text);
            }
        } else {
            tv.setText(mContext.getString(R.string.happy_and_prosperous));
        }

        tv.setTextColor(Color.parseColor("#FFBD00"));
        tv.setTextSize(20);
        tv.getPaint().setFakeBoldText(true);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins((int) redPacket.x, (int) redPacket.y, 0, 0);

        tv.setLayoutParams(lp);
        red_rain_view.addView(tv);

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
