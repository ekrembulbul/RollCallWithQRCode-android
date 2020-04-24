package com.example.rollcall.Data;

public class User {
    public String sId;
    public String degree;
    public String name;
    public String surname;
    public String email;

    public User() {
    }

    public User(User user) {
        this.sId = user.sId;
        this.degree = user.degree;
        this.name = user.name;
        this.surname = user.surname;
        this.email = user.email;
    }

    public User(String sId, String degree, String name, String surname, String email) {
        this.sId = sId;
        this.degree = degree;
        this.name = name;
        this.surname = surname;
        this.email = email;
    }
}
