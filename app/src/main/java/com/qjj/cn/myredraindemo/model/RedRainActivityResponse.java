package com.qjj.cn.myredraindemo.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * created by QinJiaJun
 * Email qinjiajun_1230@163.com
 * on 2019/11/2
 * Describe: 红包雨预报
 */
public class RedRainActivityResponse implements Parcelable {


    private int status;
    private int code;
    private String msg;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    private ResultEntity data;

    public ResultEntity getData() {
        return data;
    }

    public void setData(ResultEntity data) {
        this.data = data;
    }

    public static class ResultEntity implements Parcelable {
        /**
         * serverTime : 1573798955
         * beginTime : 1573797600
         * forecast : 600
         * countdown : 10
         * duration : 60
         * times : 5
         * interval : 360
         * percent : 50
         */

        private String redPacketRainId;//红包雨ID
        private String subject;     // 红包雨标题
        private String serverTime;  // 服务器当前时间，时间戳
        private String beginTime;   // 开始下雨时间 ，时间戳
        private String countdown;   //  倒计时，单位秒
        private String duration;    // 下雨持续时长，单位秒
        private int percent;     //中奖率

        public String getRedPacketRainId() {
            return redPacketRainId;
        }

        public void setRedPacketRainId(String redPacketRainId) {
            this.redPacketRainId = redPacketRainId;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getServerTime() {
            return serverTime;
        }

        public void setServerTime(String serverTime) {
            this.serverTime = serverTime;
        }

        public String getBeginTime() {
            return beginTime;
        }

        public void setBeginTime(String beginTime) {
            this.beginTime = beginTime;
        }

        public String getCountdown() {
            return countdown;
        }

        public void setCountdown(String countdown) {
            this.countdown = countdown;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }



        public int getPercent() {
            return percent;
        }

        public void setPercent(int percent) {
            this.percent = percent;
        }

        @Override
        public String toString() {
            return "ResultEntity{" +
                    "redPacketRainId='" + redPacketRainId + '\'' +
                    ", subject='" + subject + '\'' +
                    ", serverTime='" + serverTime + '\'' +
                    ", beginTime='" + beginTime + '\'' +
                    ", countdown='" + countdown + '\'' +
                    ", duration='" + duration + '\'' +
                    ", percent='" + percent + '\'' +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.redPacketRainId);
            dest.writeString(this.subject);
            dest.writeString(this.serverTime);
            dest.writeString(this.beginTime);
            dest.writeString(this.countdown);
            dest.writeString(this.duration);
            dest.writeInt(this.percent);
        }

        public ResultEntity() {
        }

        protected ResultEntity(Parcel in) {
            this.redPacketRainId = in.readString();
            this.subject = in.readString();
            this.serverTime = in.readString();
            this.beginTime = in.readString();
            this.countdown = in.readString();
            this.duration = in.readString();
            this.percent = in.readInt();
        }

        public static final Parcelable.Creator<ResultEntity> CREATOR = new Parcelable.Creator<ResultEntity>() {
            @Override
            public ResultEntity createFromParcel(Parcel source) {
                return new ResultEntity(source);
            }

            @Override
            public ResultEntity[] newArray(int size) {
                return new ResultEntity[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.data, flags);
    }

    public RedRainActivityResponse() {
    }

    protected RedRainActivityResponse(Parcel in) {
        this.data = in.readParcelable(ResultEntity.class.getClassLoader());
    }

    public static final Parcelable.Creator<RedRainActivityResponse> CREATOR = new Parcelable.Creator<RedRainActivityResponse>() {
        @Override
        public RedRainActivityResponse createFromParcel(Parcel source) {
            return new RedRainActivityResponse(source);
        }

        @Override
        public RedRainActivityResponse[] newArray(int size) {
            return new RedRainActivityResponse[size];
        }
    };
}
