package br.com.jhonatasmartins.social;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;

import com.squareup.picasso.Transformation;

/**
 * Created by jhonatas on 6/17/15.
 */
public class RoundedTransformation implements Transformation {

    private Resources resources;

    public RoundedTransformation(Resources res){
        resources = res;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        Bitmap bitmap = getRoundedBitmap(source);

        source.recycle();

        return bitmap;
    }

    @Override
    public String key() {
        /* key is unique and always is the same */
        return "profile";
    }

    private Bitmap getRoundedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Paint paintBorder = new Paint();
        paintBorder.setAntiAlias(true);
        paintBorder.setColor(resources.getColor(android.R.color.white));

        //assuming that picture is square
        int size = bitmap.getWidth();

        BitmapShader shader = new BitmapShader(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false),
                Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);

        int xy = size / 2;
        int radius = size ;

        canvas.drawCircle(xy, xy, (radius / 2) - 4.0f, paintBorder);
        canvas.drawCircle(xy, xy, (radius / 2) - 4.0f, paint);

        return output;
    }
}
