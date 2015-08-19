package br.com.jhonatasmartins.social.login;

/**
 * Created by jhonatas on 6/18/15.
 */
public class SocialProfile {

    public static final String FACEBOOK = "facebook";
    public static final String TWITTER = "twitter";
    public static final String GOOGLE = "google";

    String name;
    String email;
    String image;
    String cover;
    String network;

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

    public String getNetwork() {
        return network;
    }
}
