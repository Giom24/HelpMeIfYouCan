package de.helpmeifyoucan.helpmeifyoucan.models.dtos.request;
import de.helpmeifyoucan.helpmeifyoucan.validation.Annotations.ValidPassword;

public class Credentials {


    private String email;
    
    @ValidPassword
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}