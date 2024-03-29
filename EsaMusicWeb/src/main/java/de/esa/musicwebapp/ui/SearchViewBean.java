/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.esa.musicwebapp.ui;

import de.esa.musicwebapp.entity.Track;
import de.esa.musicwebapp.services.SearchManager;
import de.esa.musicwebapp.services.userauth.ChangePWClient;
import de.esa.userauth.domain.UserObject;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

/**
 * ManagedBean to be accessed after successful login.
 * All public methods are bound to the JSF UI 
 * @author Fabian
 */
@ManagedBean(name = "search")
@SessionScoped
public class SearchViewBean implements Serializable {

    /**
     * Model used by the ui datatable.
     */
    private List<Track> trackList;
    /**
     * Currently selected track of the datatable.
     */
    private Track selectedTrack;
    private String lyricInp;
    private String titleInp;
    private String artistInp;
    private String oldPwdInp;
    private String newPwdInp;
    /**
     * Container managed instance  of changePWClient used for password change methods.
     */
    @EJB
    private ChangePWClient changePWClient;
    /**
     * Property of managed bean managed by webcontainer.
     * This property is used to determine the login state of the current user.
     */
    @ManagedProperty(value = "#{login.currentUser}")
    private UserObject currentUser;

    public void setCurrentUser(UserObject currentUser) {
        this.currentUser = currentUser;
    }
    
    /**
     * Method to change the password of the current user.
     * This method validates the old and new password.
     * If the old password is correct and the new one is different the password will be set in the database.
     * The database access is deligated to the changePWClent.
     * Result notification messages are displayed on the UI
     * @return view name to redirect to
     */
    public String changePW() {
        boolean result = false;
        if (currentUser == null) {
            displayFailure("CurrentUser is NULL.");
        }else if (this.newPwdInp == null || this.newPwdInp.isEmpty()) {
            displayFailure("New password is NULL or empty.");
        }else if (!this.currentUser.getPassword().equals(this.oldPwdInp)) {
            displayFailure("Old and new password do not match.");
        } else{
            result = changePWClient.sendJMSMessage("changepw:" + currentUser.getName() + ":" + this.newPwdInp);
        }
        if (result) {
            displayInfo("Password successfully changed.");
        } else {
            displayFailure("Password change failed.");
        }
        return "";
    }

    /**
     * Helper method to display failure messages on the messages target.
     * @param message 
     */
    private void displayFailure(String message) {
        FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failure", message));
    }

    /**
     * Helper method to display information messages on the messages target. 
     * @param message 
     */
    private void displayInfo(String message) {
        FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", message));
    }

    /**
     * Method to delete the current logged in user using the JMS client.
     * @return login as redirection target
     */
    public String deleteUser() {
        boolean result = changePWClient.sendJMSMessage("deleteuser:" + currentUser.getName());
        if (result) {
        } else {
            displayFailure("Unable to delete the user named: " + currentUser.getName());
            return "login";
        }
        return "login";
    }

    /**
     * Standard constructor called on view access.
     */
    public SearchViewBean() {
        // trackList = SearchManager.getInstance().searchTracks("Thriller", "Michael Jackson");
    }

    /**
     * Method to destroy the current HTTP-Session.
     * @return 
     */
    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "login";
    }

    public String getOldPwdInp() {
        return oldPwdInp;
    }

    public void setOldPwdInp(String oldPwdInp) {
        this.oldPwdInp = oldPwdInp;
    }

    public String getNewPwdInp() {
        return newPwdInp;
    }

    public void setNewPwdInp(String newPwdInp) {
        this.newPwdInp = newPwdInp;
    }

    /**
     * Proxy Method to call the business logic.
     */
    public void searchTrack() {
        this.trackList = SearchManager.getInstance().searchTracks(titleInp, artistInp);
    }

    public String getLyricInp() {
        return lyricInp;
    }

    public void setLyricInp(String lyricInp) {
        this.lyricInp = lyricInp;
    }

    public String getTitleInp() {
        return titleInp;
    }

    public void setTitleInp(String titleInp) {
        this.titleInp = titleInp;
    }

    public String getArtistInp() {
        return artistInp;
    }

    public void setArtistInp(String artistInp) {
        this.artistInp = artistInp;
    }

    /**
     * Getter accessed by datatable component.
     *
     * @return current view model
     */
    public List<Track> getTrackList() {
        return trackList;
    }

    /**
     * Getter accessed by datatable selectRow event.
     *
     * @return current selection
     */
    public Track getSelectedTrack() {
        return selectedTrack;
    }

    /**
     * Setter accessed by datatable selectRow event.
     *
     * @param selectedTrack
     */
    public void setSelectedTrack(Track selectedTrack) {
        this.selectedTrack = selectedTrack;
    }
}
