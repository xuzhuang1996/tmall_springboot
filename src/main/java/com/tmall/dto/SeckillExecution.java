package com.tmall.dto;

import com.tmall.enums.SeckillStatEnum;

//秒杀结果
public class SeckillExecution {
    SeckillStatEnum stateKill;//写这种DTO，首先是写是否成功，然后根据需求，再添加其他前台需要的数据
    boolean successSec;
    int pid;

    public SeckillExecution(SeckillStatEnum successKill,int id,boolean SuccessSec){
        successSec=SuccessSec;
        stateKill = successKill;
        pid = id;
    }

    public SeckillExecution(SeckillStatEnum StateKill,boolean SuccessSec){
        stateKill = StateKill;
        successSec=SuccessSec;
    }


}
