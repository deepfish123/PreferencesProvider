package com.zlm.libs.preferences;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Set;

/**
 * @Description: ContentProvider
 * @author: zhangliangming
 * @date: 2018-04-29 16:39
 **/
public abstract class PreferencesProvider extends ContentProvider {

    private UriMatcher mUriMatcher;

    /**
     * 表列名
     */
    public static String COLUMNNAME = "SPCOLUMNNAME";
    /**
     * authorities key
     */
    public static String AUTHORITIES_KEY = "authorities_key";
    /**
     * authorities_spname
     */
    public static String AUTHORITIES_SPNAME = "authorities_spname";
    /**
     * string
     */
    private String mStringPath = "string/*/*/";
    public static final int STRING_CONTENT_URI_CODE = 100;
    /**
     * int
     */
    private String mIntegerPath = "integer/*/*/";
    public static final int INTEGER_CONTENT_URI_CODE = 101;
    /**
     * long
     */
    private String mLongPath = "long/*/*/";
    public static final int LONG_CONTENT_URI_CODE = 102;
    /**
     * float
     */
    private String mFloatPath = "float/*/*/";
    public static final int FLOAT_CONTENT_URI_CODE = 104;
    /**
     * boolean
     */
    private String mBooleanPath = "boolean/*/*/";
    public static final int BOOLEAN_CONTENT_URI_CODE = 105;

    /**
     *
     */
    private String mDeletePath = "delete/*/*/";
    public static final int DELETE_CONTENT_URI_CODE = 106;

    /**
     *
     */
    private String mPutsPath = "puts";
    public static final int PUTS_CONTENT_URI_CODE = 107;

    public abstract String getAuthorities();


    @Override
    public boolean onCreate() {

        String authorities = getAuthorities();
        //保存authorities
        PreferencesUtils.putString(getContext(), AUTHORITIES_SPNAME, AUTHORITIES_KEY, authorities);

        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(authorities, mStringPath, STRING_CONTENT_URI_CODE);
        mUriMatcher.addURI(authorities, mStringPath + "*/", STRING_CONTENT_URI_CODE);

        mUriMatcher.addURI(authorities, mIntegerPath, INTEGER_CONTENT_URI_CODE);
        mUriMatcher.addURI(authorities, mIntegerPath + "*/", INTEGER_CONTENT_URI_CODE);

        mUriMatcher.addURI(authorities, mLongPath, LONG_CONTENT_URI_CODE);
        mUriMatcher.addURI(authorities, mLongPath + "*/", LONG_CONTENT_URI_CODE);

        mUriMatcher.addURI(authorities, mFloatPath, FLOAT_CONTENT_URI_CODE);
        mUriMatcher.addURI(authorities, mFloatPath + "*/", FLOAT_CONTENT_URI_CODE);

        mUriMatcher.addURI(authorities, mBooleanPath, BOOLEAN_CONTENT_URI_CODE);
        mUriMatcher.addURI(authorities, mBooleanPath + "*/", BOOLEAN_CONTENT_URI_CODE);

        mUriMatcher.addURI(authorities, mDeletePath, DELETE_CONTENT_URI_CODE);

        mUriMatcher.addURI(authorities, mPutsPath, PUTS_CONTENT_URI_CODE);

        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Model model = getModel(uri);
        if (model == null) return null;
        int code = mUriMatcher.match(uri);
        Cursor cursor = buildCursor(getContext(), model, code);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Model model = getModel(uri);
        if (model == null) return null;
        int code = mUriMatcher.match(uri);
        if (code == STRING_CONTENT_URI_CODE || code == INTEGER_CONTENT_URI_CODE || code == LONG_CONTENT_URI_CODE
                || code == FLOAT_CONTENT_URI_CODE || code == BOOLEAN_CONTENT_URI_CODE || code == PUTS_CONTENT_URI_CODE) {
            insert(getContext(), values, model);
        }
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Model model = getModel(uri);
        if (model == null) return -1;
        int code = mUriMatcher.match(uri);
        if (code == STRING_CONTENT_URI_CODE || code == INTEGER_CONTENT_URI_CODE || code == LONG_CONTENT_URI_CODE
                || code == FLOAT_CONTENT_URI_CODE || code == BOOLEAN_CONTENT_URI_CODE) {
            delete(getContext(), model);
        }

        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Model model = getModel(uri);
        if (model == null) return -1;
        int code = mUriMatcher.match(uri);
        if (code == STRING_CONTENT_URI_CODE || code == INTEGER_CONTENT_URI_CODE || code == LONG_CONTENT_URI_CODE
                || code == FLOAT_CONTENT_URI_CODE || code == BOOLEAN_CONTENT_URI_CODE) {
            insert(getContext(), values, model);
        }
        return 0;
    }

    /**
     * 删除
     *
     * @param context
     * @param model
     */
    private void delete(Context context, Model model) {
        SharedPreferences.Editor editor = PreferencesUtils.getEditor(context, model.getSpName());
        editor.remove(model.getKey());
        editor.apply();
    }

    /**
     * 插入数据
     *
     * @param context
     * @param values
     * @param model
     */
    private void insert(Context context, ContentValues values, Model model) {
        SharedPreferences.Editor editor = PreferencesUtils.getEditor(context, model.getSpName());
        Set<String> keys = values.keySet();
        for (String key : keys) {
            Object value = values.get(key);
            if (value instanceof Integer) {
                editor.putInt(key, Integer.parseInt(value + ""));
            } else if (value instanceof Long) {
                editor.putLong(key, Long.parseLong(value + ""));
            } else if (value instanceof Float) {
                editor.putFloat(key, Float.parseFloat(value + ""));
            } else if (value instanceof Boolean) {
                editor.putBoolean(key, Boolean.valueOf(value + ""));
            } else {
                editor.putString(key, (value == null ? "" : value) + "");
            }
        }
        editor.apply();
    }

    /**
     * 从sp中获取数据
     *
     * @return
     */
    private Cursor buildCursor(Context context, Model model, int code) {
        Object value = null;
        Object defValue = model.getDefValue();
        switch (code) {
            case STRING_CONTENT_URI_CODE:

                if (defValue == null) {
                    value = PreferencesUtils.getString(context, model.getSpName(), model.getKey());
                } else {
                    value = PreferencesUtils.getString(context, model.getSpName(), model.getKey(), String.valueOf(defValue));
                }

                break;
            case INTEGER_CONTENT_URI_CODE:
                if (defValue == null) {
                    value = PreferencesUtils.getInt(context, model.getSpName(), model.getKey());
                } else {
                    if (!TextUtils.isDigitsOnly(defValue + "")) {
                        defValue = -1;
                    }
                    value = PreferencesUtils.getInt(context, model.getSpName(), model.getKey(), Integer.parseInt(defValue + ""));
                }

                break;
            case LONG_CONTENT_URI_CODE:

                if (defValue == null) {
                    value = PreferencesUtils.getLong(context, model.getSpName(), model.getKey());
                } else {
                    if (!TextUtils.isDigitsOnly(defValue + "")) {
                        defValue = -1;
                    }
                    value = PreferencesUtils.getLong(context, model.getSpName(), model.getKey(), Long.parseLong(defValue + ""));
                }

                break;
            case FLOAT_CONTENT_URI_CODE:

                if (defValue == null) {
                    value = PreferencesUtils.getFloat(context, model.getSpName(), model.getKey());
                } else {

                    value = PreferencesUtils.getFloat(context, model.getSpName(), model.getKey(), Float.parseFloat(defValue + ""));
                }

                break;
            case BOOLEAN_CONTENT_URI_CODE:

                if (defValue == null) {
                    value = PreferencesUtils.getBoolean(context, model.getSpName(), model.getKey()) + "";
                } else {
                    value = PreferencesUtils.getBoolean(context, model.getSpName(), model.getKey(), Boolean.valueOf(defValue + "")) + "";
                }

                break;
            default:
                break;
        }
        if (value == null) return null;
        //
        String[] columnNames = {COLUMNNAME};
        MatrixCursor cursor = new MatrixCursor(columnNames);
        Object[] values = {value};
        cursor.addRow(values);
        return cursor;
    }

    /**
     * 从uri中获取spname和key
     *
     * @param uri
     * @return
     */
    private Model getModel(Uri uri) {
        try {

            Model model = new Model();
            model.setSpName(uri.getPathSegments().get(1));
            if (uri.getPathSegments().size() > 2) {
                model.setKey(uri.getPathSegments().get(2));
            }
            if (uri.getPathSegments().size() > 3) {
                model.setDefValue(uri.getPathSegments().get(3));
            }

            return model;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     */
    private class Model {
        private String spName;
        private String key;
        private Object defValue;

        public String getSpName() {
            return spName;
        }

        public void setSpName(String spName) {
            this.spName = spName;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Object getDefValue() {
            return defValue;
        }

        public void setDefValue(Object defValue) {
            this.defValue = defValue;
        }
    }
}
