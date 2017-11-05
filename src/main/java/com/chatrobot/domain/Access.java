package com.chatrobot.domain;

import java.util.Date;

/**
 * 店铺当日访问记录模型
 * Created by hackyo on 2017/5/19.
 */
public class Access {

    private int id;
    private int userId;
    private Date noteDate;
    private int amount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getNoteDate() {
        return noteDate;
    }

    public void setNoteDate(Date noteDate) {
        this.noteDate = noteDate;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

}
