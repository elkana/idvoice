package com.ppu.idvoice;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * berfungsi sebagai table tampungan juga. maksimal 3 rows setelah itu harus dimerge menjadi 1 row saja.
 */
public class TrnEnroll extends RealmObject implements Serializable {
    @PrimaryKey
    @SerializedName("uid")
    private String uid;

    @SerializedName("createdDate")
    private Long createdDate;

    @SerializedName("fileName")
    private String fileName;

    @SerializedName("groupName")
    private String groupName;

    @SerializedName("fullName")
    private String fullName;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String toString() {
        return "TrnEnroll{" + "uid='" + uid + '\'' + ", createdDate=" + createdDate + ", fileName='" + fileName + '\''
                + ", groupName='" + groupName + '\'' + ", fullName='" + fullName + '\'' + '}';
    }
}
