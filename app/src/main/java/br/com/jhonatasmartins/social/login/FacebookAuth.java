package br.com.jhonatasmartins.social.login;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import br.com.jhonatasmartins.social.R;

/**
 * Created by jhonatas on 6/18/15.
 */
public class FacebookAuth extends Auth implements FacebookCallback<LoginResult> {

    public final String LOG_TAG = getClass().getName();
    final String IMAGE_SCHEME = "image";
    final int MAX_SIZE_ATTACHMENT = 12 * 1024 * 1024;

    LoginManager facebookLoginManager;
    CallbackManager facebookCallbackManager;
    Activity hostActivity;

    public FacebookAuth(Activity activity, OnAuthListener onAuthListener){
        FacebookSdk.sdkInitialize(activity.getApplicationContext());

        hostActivity = activity;
        facebookCallbackManager = CallbackManager.Factory.create();
        facebookLoginManager = LoginManager.getInstance();
        facebookLoginManager.registerCallback(facebookCallbackManager, this);

        setOnAuthListener(onAuthListener);
    }

    @Override
    public void login() {
        facebookLoginManager.logInWithReadPermissions(hostActivity,
                Arrays.asList("public_profile", "email"));
    }

    @Override
    public void logout() {
        facebookLoginManager.logOut();
    }

    @Override
    public void revoke() {
        AccessToken token = AccessToken.getCurrentAccessToken();

        if (token != null) {
            String user = token.getUserId();

            //url to revoke login DELETE {user_id}/permissions
            String graphPath = user + "/permissions";

            final GraphRequest requestLogout = new GraphRequest(token, graphPath, null, HttpMethod.DELETE, new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse graphResponse) {
                    //call logout to clear token info and profile
                    logout();

                    onAuthListener.onRevoke();
                }
            });

            requestLogout.executeAsync();
        }else{
            onAuthListener.onLoginError("Token is null");
        }
    }

    @Override
    public void share(String content, Uri imageOrVideo) {

        if(imageOrVideo != null) {
            ContentResolver contentResolver = hostActivity.getContentResolver();
            String mimeType = contentResolver.getType(imageOrVideo);

            File file = uriToFile(imageOrVideo);
            if(file.getUsableSpace() > MAX_SIZE_ATTACHMENT){
                Toast.makeText(hostActivity,
                        R.string.attachment_much_long,
                        Toast.LENGTH_LONG).show();
                return;
            }

            if(mimeType.contains(IMAGE_SCHEME)){
                shareImage(imageOrVideo);
            }else{
                shareVideo(content, imageOrVideo);
            }
        }

    }

    @Override
    public void onSuccess(LoginResult loginResult) {
        GraphRequest meRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                String cover= "", name = "", email = "";

                try {
                    name = jsonObject.getString("name");
                    email = jsonObject.getString("email");
                    cover = jsonObject.getJSONObject("cover").getString("source");
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }

                requestProfilePhoto(name, email, cover);
            }
        });

        Bundle params = new Bundle();
        params.putString("fields", "name, email, cover");

        meRequest.setParameters(params);
        meRequest.executeAsync();
    }

    @Override
    public void onCancel() {
        onAuthListener.onLoginCancel();
    }

    @Override
    public void onError(FacebookException e) {
        Log.e(LOG_TAG, e.getMessage());
        onAuthListener.onLoginError(e.getMessage());
    }

    public CallbackManager getFacebookCallbackManager() {
        return facebookCallbackManager;
    }

    private void requestProfilePhoto(final String name, final String email, final String cover){
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        final SocialProfile profile = new SocialProfile();
        profile.name = name;
        profile.email = email;
        profile.cover = cover;
        profile.network = SocialProfile.FACEBOOK;

        //url to get profile photo /{user-id}/picture?redirect=false
        String path = accessToken.getUserId() + "/picture";

        GraphRequest photoRequest = new GraphRequest(accessToken, path, null, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {

                try {
                    profile.image = graphResponse.getJSONObject().getJSONObject("data").getString("url");
                }catch (JSONException e){
                    Log.e(LOG_TAG, e.getMessage());
                }

                onAuthListener.onLoginSuccess(profile);
            }
        });


        Bundle parameters = new Bundle();
        parameters.putBoolean("redirect", false);

        photoRequest.setParameters(parameters);
        photoRequest.executeAsync();
    }

    private void shareImage(Uri imageOrVideo){
        try {
            //convert to image
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(hostActivity.getContentResolver(),
                    imageOrVideo);

            SharePhoto sharePhoto = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();

            SharePhotoContent photoContent = new SharePhotoContent.Builder()
                    .addPhoto(sharePhoto)
                    .build();

            if(ShareDialog.canShow(SharePhotoContent.class)){
                ShareDialog.show(hostActivity, photoContent);
            }else{
                Toast.makeText(hostActivity,
                        R.string.need_facebook_app,
                        Toast.LENGTH_LONG).show();
            }
        }catch (IOException e){
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private void shareVideo(String content, Uri imageOrVideo){
        ShareVideo shareVideo = new ShareVideo.Builder()
                .setLocalUrl(imageOrVideo)
                .build();

        ShareVideoContent videoContent = new ShareVideoContent.Builder()
                .setVideo(shareVideo)
                .setContentDescription(content)
                .build();

        if(ShareDialog.canShow(ShareVideoContent.class)){
            ShareDialog.show(hostActivity, videoContent);
        }else{
            Toast.makeText(hostActivity,
                    R.string.need_facebook_app,
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * query for uri passed as param to get real path and
     * create a new file and return it
     * @param uri {@link Uri} uri from file
     * @return new {@link File} real file path
     */
    private File uriToFile(Uri uri){
        String filePath;
        ContentResolver contentResolver = hostActivity.getContentResolver();

        Cursor cursor = contentResolver.query(uri, new String[]{MediaStore.MediaColumns.DATA},
                null, null, null);

        cursor.moveToFirst();
        filePath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
        cursor.close();

        return new File(filePath);
    }
}
