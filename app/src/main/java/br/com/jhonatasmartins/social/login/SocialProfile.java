package br.com.jhonatasmartins.social.login;

/**
 * Created by jhonatas on 6/18/15.
 */
public class SocialProfile {

    String name;
    String email;
    String image;
    String cover;
    Social network;

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getCover() {
        return cover;
    }

    public String getEmail() {
        return email;
    }

    public Social getNetwork() {
        return network;
    }
}
