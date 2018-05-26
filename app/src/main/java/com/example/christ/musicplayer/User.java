package com.example.christ.musicplayer;

import java.util.Calendar;

public class User {
    public static final String TABLE = "Users";
    public static final String KEY_Name = "username";
    public static final String KEY_Password = "password";
    public static final String KEY_ID = "id";
    public static final String KEY_Gender = "gender";
    public static final String KEY_Age = "age";
    public static final String KEY_Score ="score";
    public static final String KEY_Date = "date";

    private int id;
    private String username;
    private int age;
    private int gender;
    private String password;
    private int score;
    private String date;

    public User(){}

    public User(String name, String password, int age, int sexual){
        setUsername(name);
        setAge(age);
        setPassword(password);
        setGender(sexual);
        setScore(0);
        Calendar calendar = Calendar.getInstance();
        setDate(String.valueOf(calendar.get(Calendar.YEAR))+"."+String.valueOf(calendar.get(Calendar.MONTH)+1)
                +"."+String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
    }

    public void setId(int id){ this.id = id; }
    public void setUsername(String username){ this.username = username; }
    public void setAge(int age){ this.age = age; }
    public void setGender(int sexual){ this.gender = sexual; }
    public void setPassword(String password){ this.password = password; }
    public void setScore(int score){ this.score = score; }
    public void setDate(String day) { date = day; }

    public int getId(){ return this.id; }
    public String getUsername(){ return this.username; }
    public String getPassword() { return password; }
    public int getAge(){ return  this.age; }
    public int getScore(){ return  this.score; }
    public int getGender(){ return  this.gender; }
    public String getDate(){ return this.date; }
}
