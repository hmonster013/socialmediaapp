package com.example.socialmediaapp.Model;

import java.io.Serializable;

public class ModelPost implements Serializable {
    String uid, uName, uEmail, uDp;
    String pId, pImage, pTime, pTitle, pDescr;
    int pComments;
    int pLike;
    public ModelPost(){

    }

    public ModelPost(String uid, String uName, String uEmail, String uDp, String pId, String pImage, String pTime, String pTitle, String pDescr, int pComments, int pLike) {
        this.uid = uid;
        this.uName = uName;
        this.uEmail = uEmail;
        this.uDp = uDp;
        this.pId = pId;
        this.pImage = pImage;
        this.pTime = pTime;
        this.pTitle = pTitle;
        this.pDescr = pDescr;
        this.pComments = pComments;
        this.pLike = pLike;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuDp() {
        return uDp;
    }

    public void setuDp(String uDp) {
        this.uDp = uDp;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getpImage() {
        return pImage;
    }

    public void setpImage(String pImage) {
        this.pImage = pImage;
    }

    public String getpTime() {
        return pTime;
    }

    public void setpTime(String pTime) {
        this.pTime = pTime;
    }

    public String getpTitle() {
        return pTitle;
    }

    public void setpTitle(String pTitle) {
        this.pTitle = pTitle;
    }

    public String getpDescr() {
        return pDescr;
    }

    public void setpDescr(String pDescr) {
        this.pDescr = pDescr;
    }

    public int getpComments() {
        return pComments;
    }

    public void setpComments(int pComments) {
        this.pComments = pComments;
    }

    public int getpLike() {
        return pLike;
    }

    public void setpLike(int pLike) {
        this.pLike = pLike;
    }
}
