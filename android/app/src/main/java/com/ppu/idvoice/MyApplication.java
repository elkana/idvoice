package com.ppu.idvoice;

import io.flutter.app.FlutterApplication;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MyApplication extends FlutterApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(config);
    }


}
