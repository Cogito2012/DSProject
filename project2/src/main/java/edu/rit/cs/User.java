package edu.rit.cs;

import java.io.Serializable;

public class User implements Serializable {

    public String name;
    public String password;
    public String role; // subsriber or publisher

    public int roleID;
    
    public User(String name, String password, String role){
        this.name = name;
        this.password = password;
        this.role = role;
        this.roleID = -1;
    }

    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                '}';
    }

    public int getRoleID() {
        return roleID;
    }

    public void setRoleID(int roleID) {
        this.roleID = roleID;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
