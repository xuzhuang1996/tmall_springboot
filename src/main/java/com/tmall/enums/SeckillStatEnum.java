package com.tmall.enums;

public enum SeckillStatEnum {

    //定义一组状态号以及对应的描述
    Success(1,"秒杀成功"),
    END(0, "秒杀结束"),
    REPEAT_KILL(-1, "重复秒杀"),
    INNER_ERROR(-2, "系统异常"),
    DATA_REWRITE(-3, "数据篡改");

    //定义状态号以及描述
    private int state;
    private String stateInfo;

    //初始化这些类，构造函数必须跟定义的Success等里面的参数匹配
    SeckillStatEnum(int state, String stateInfo) {
        this.state = state;
        this.stateInfo = stateInfo;
    }

    //取值
    public static SeckillStatEnum stateOf(int index){
        for (SeckillStatEnum state : values()) {
            if (state.getState() == index) {
                return state;
            }
        }
        return null;
    }

    public int getState() {
        return state;
    }

    public String getStateInfo() {
        return stateInfo;
    }
}
