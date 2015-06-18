package br.com.jhonatasmartins.social.login;

/**
 * Created by jhonatas on 6/18/15.
 */
public abstract class SimpleAuthListener implements Auth.OnAuthListener{

    @Override
    public void onLoginCancel() {}

    @Override
    public void onLoginError(String message) {}

    @Override
    public void onLoginSuccess(SocialProfile profile) {}

    @Override
    public void onRevoke() {}
}
