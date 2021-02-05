package ca.freda.relation_annotator.util;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class HierarchicalViewIdViewer {

    public static View debugViewIds(View view, String logtag) {
        Log.v(logtag, "traversing: " + view.getClass().getSimpleName() + ", id: " + view.getId());
        if (view.getParent() != null && (view.getParent() instanceof ViewGroup)) {
            return debugViewIds((View)view.getParent(), logtag);
        }
        else {
            debugChildViewIds(view, logtag, 0);
            return view;
        }
    }

    private static void debugChildViewIds(View view, String logtag, int spaces) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                Log.v(logtag, padString("view: " + child.getClass().getSimpleName() + "(" + child.getId() + ")", spaces));
                debugChildViewIds(child, logtag, spaces + 1);
            }
        }
    }

    private static String padString(String str, int noOfSpaces) {
        if (noOfSpaces <= 0) {
            return str;
        }
        StringBuilder builder = new StringBuilder(str.length() + noOfSpaces);
        for (int i = 0; i < noOfSpaces; i++) {
            builder.append(' ');
        }
        return builder.append(str).toString();
    }

}