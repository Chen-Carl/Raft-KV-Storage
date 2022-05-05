package com.zoecarl;  
  
import java.util.ArrayList;  
import java.util.List;  
  
import com.alibaba.fastjson.JSON;  
  
class User {  
    private String name;  
    private int age;  
  
    public String getName() {  
        return name;  
    }  
  
    public void setName(String name) {  
        this.name = name;  
    }  
  
    public int getAge() {  
        return age;
    }  
  
    public void setAge(int age) {  
        this.age = age;  
    }  
  
    @Override  
    public String toString() {  
        return "User [name=" + name + ", age=" + age + "]";  
    }  
};  

public class testRawJSON {  
    public static void main(String[] args) {  
        User guestUser = new User();  
        guestUser.setName("guest");  
        guestUser.setAge(28);  

        System.out.println(guestUser);
        byte[] userBytes = JSON.toJSONBytes(guestUser);
        User res = JSON.parseObject(userBytes, User.class);
        System.out.println(res);
    }  
}