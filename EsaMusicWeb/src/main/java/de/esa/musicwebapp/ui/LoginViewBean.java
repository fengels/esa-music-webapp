/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.esa.musicwebapp.ui;

import de.esa.musicwebapp.service.userauth.ChangePWClient;
import de.esa.userauth.domain.IllegalUsernameException;
import de.esa.userauth.domain.UserAuthRemote;
import de.esa.userauth.domain.UserObject;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

/**
 *
 * @author nto
 */
@ManagedBean
@SessionScoped
public class LoginViewBean implements Serializable {

    /*@EJB
    private UserAuthClient userAuthClient;*/
    
    @EJB(beanInterface=UserAuthRemote.class)
    private UserAuthRemote userAuth;
    @EJB
    private ChangePWClient changePWClient;
    
    private String usernameInp;
    private String passwordInp;

    public LoginViewBean(){
        
    }
    
    public String changePW() {
        boolean result = changePWClient.sendJMSMessage("changepw:" + this.usernameInp + ":" + this.passwordInp);
           if(result){
           }else{
                FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failure", "Changing the password failed."));
           }
           return "/search";
    }

    public String deleteUser() {
        boolean result = changePWClient.sendJMSMessage("deleteuser:" + this.usernameInp);
        if(result){
        }else{
            FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failure", "Unable to delete the user named: " + this.usernameInp));
        }
        return "/search";
    }

    public String login() {
         UserObject result =null;
        try{
       result = userAuth.login(this.usernameInp, this.passwordInp);
        }catch(IllegalUsernameException ex){
            
        }
        if (result == null) {
            FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login failure", "Login failed username or password are unknown."));
        }
        return "/login";
    }

    public String register() {
        UserObject result=null;
        try {
        result = userAuth.register(this.usernameInp, this.passwordInp);
            
        } catch (IllegalUsernameException e) {
        }
        if (result == null) {
            FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registration failure", "Username is already in use."));
        }
        return "/login";
    }

    public String getUsernameInp() {
        return usernameInp;
    }

    public void setUsernameInp(String usernameInp) {
        this.usernameInp = usernameInp;
    }

    public String getPasswordInp() {
        return passwordInp;
    }

    public void setPasswordInp(String passwordInp) {
        this.passwordInp = passwordInp;
    }

   
}