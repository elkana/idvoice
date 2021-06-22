package com.ppu.idvoice;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;

public class DataUtil {

    public static TrnEnroll saveToDB(Context context, File file, String groupName, String fullName) {
        Realm r = Realm.getDefaultInstance();
        try {
            TrnEnroll trn = new TrnEnroll();

            // format: [collId][millisinceepoch]
            // trnCollPos.setUid(collId + new Date().getTime());
            trn.setUid(java.util.UUID.randomUUID().toString());

            // gimana caranya ?
            trn.setUid(java.util.UUID.randomUUID().toString());
            trn.setFileName(file.getPath());
            trn.setCreatedDate(new Date().getTime());
            trn.setGroupName(groupName);
            trn.setFullName(fullName);

            r.beginTransaction();

            r.copyToRealmOrUpdate(trn);

            r.commitTransaction();

            return trn;
            // Log.i("truface", ">>>" + trn.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            r.close();
        }

        return null;
    }

    public static List<TrnEnroll> getEnrollList(Context context, String groupName) {
        Realm r = Realm.getDefaultInstance();
        try {
            if (groupName == null || groupName.trim().length() < 1)
                return r.copyFromRealm(r.where(TrnEnroll.class).findAll());

            return r.copyFromRealm(r.where(TrnEnroll.class).equalTo("groupName", groupName).findAll());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            r.close();
        }

        return new ArrayList<>();
    }

    public static boolean deleteFromDB(Context context, String uid) {
        Realm r = Realm.getDefaultInstance();
        try {
            // delete its file
            TrnEnroll data = r.where(TrnEnroll.class).equalTo("uid", uid).findFirst();

            File f = new File(data.getFileName());

            if (f.exists())
                f.delete();

            r.beginTransaction();
            data.deleteFromRealm();
            // r.where(TrnEnroll.class).equalTo("uid", uid).findAll().deleteAllFromRealm();
            r.commitTransaction();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            r.close();
        }

        return false;
    }

    public static List<TrnEnroll> getAllEnroll(Context context) {
        Realm r = Realm.getDefaultInstance();
        try {
            return r.copyFromRealm(r.where(TrnEnroll.class).findAll());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            r.close();
        }

        return new ArrayList<>();
    }

    public static void clearAllEnroll(Context context) {
        Realm r = Realm.getDefaultInstance();
        try {
            r.beginTransaction();
            r.where(TrnEnroll.class).findAll().deleteAllFromRealm();
            r.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();

        } finally {
            r.close();
        }
    }

    public static TrnEnroll getEnroll(Context context, String uid) {
        Realm r = Realm.getDefaultInstance();
        try {
            TrnEnroll _e = r.where(TrnEnroll.class).equalTo("uid", uid).findFirst();

            if (_e != null)
                return r.copyFromRealm(_e);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            r.close();
        }

        return null;
    }

}
