package gdut.bsx.tensorflowtraining.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import java.util.HashMap;

public class FontCache {
    private static final String TAG = "FontCache";

    private static HashMap<String, Typeface> fontCache = new HashMap<>();

    public static Typeface getTypeface(String fontname, Context context) {
        Typeface typeface = fontCache.get(fontname);

        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/"+fontname);
                Log.e(TAG,fontname);
            } catch (Exception e) {
                Log.e(TAG,"failure");
                return null;
            }

            fontCache.put(fontname, typeface);
        }

        return typeface;
    }
}
