package com.example.socialmediaapp.Model;

public class ModelUser {
    String email;
    String uid;
    String name;
    String phone;
    String image;
    String cover;

    public ModelUser() {

    }

    public ModelUser(String email, String uid, String name, String phone, String image, String cover) {
        this.email = email;
        this.uid = uid;
        this.name = name;
        this.phone = phone;
        this.image = image;
        this.cover = cover;
    }

    public String getEmail() {
        return email;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getImage() {
        return image;
    }

    public String getCover() {
        return cover;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }
}
