package br.com.jhonatasmartins.social.login;

/**
 * Created by jhonatas on 6/18/15.
 */
public abstract class Auth {

    protected OnAuthListener onAuthListener;

    public abstract void login();

    /**
     * implement logout, logout just clear user info from app and doesn`t revoke access
     */
    public abstract void logout();

    /**
     * implement revoke method from facebook or google inside this method,
     * in callback method from API (Facebook or Google) call {@link OnAuthListener#onRevoke()}
     * to clear shared preferences, clear activity stack and something else
     */
    public abstract void revoke();

    /**
     * you must set onAuthListener to receiver callback events
     * when login was successful, error, cancel or revoke
     * @param onAuthListener
     */
    public void setOnAuthListener(OnAuthListener onAuthListener){
        this.onAuthListener = onAuthListener;
    }

    public interface OnAuthListener{
        void onLoginSuccess(SocialProfile profile);
        void onLoginError(String message);
        void onLoginCancel();
        void onRevoke();
    }
}
