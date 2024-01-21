package chaitu.android.ychat.model;


import java.io.Serializable;

public class User implements Serializable {
  public  String name;
   public String mobile;
   public String profilePic;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    String token;


    public User(String name, String mobile, String profilePic,String token) {
        this.name = name;
        this.mobile = mobile;
        this.profilePic = profilePic;
        this.token=token;

    }

    public User() {
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return mobile;
    }

    public void setPhone(String phone) {
        this.mobile = phone;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }
}
