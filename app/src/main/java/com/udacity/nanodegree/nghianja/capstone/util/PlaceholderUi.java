package com.udacity.nanodegree.nghianja.capstone.util;

import android.content.Context;

/**
 * Utility class of static methods for material design layout metrics for placeholders.
 *
 * References:
 * [1] https://medium.com/@lucasurbas/placeholder-ui-launch-screen-d85c35552119#.wts5r1jbr
 */
public class PlaceholderUi {

    /**
     * https://github.com/lurbas/PlaceholderUI/blob/master/app/src/main/java/com/lucasurbas/placeholderui/Utils.java
     */
    public static int dpToPx(int dp, Context context){
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
}
