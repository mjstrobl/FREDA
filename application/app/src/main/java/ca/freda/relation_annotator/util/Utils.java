package ca.freda.relation_annotator.util;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;

public class Utils {

    public static boolean checkStringForUppercase(String str) {
        for(int i=0;i < str.length();i++) {
            char ch = str.charAt(i);
            if (Character.isUpperCase(ch)) {
                return true;
            }
        }
        return false;
    }

    public static Set jsonArrayToSet(JSONArray array) throws JSONException {
        Set<Object> result = new HashSet<>();

        for (int i = 0; i < array.length(); i++) {
            result.add(array.getInt(i));
        }

        return result;
    }
}
