import 'dart:convert';
import 'dart:io';

import 'package:flutter/services.dart';
import 'package:idvoice/model/trn_enroll.dart';

class IDVoiceUtil {
  // static const platform = const MethodChannel("flutter.my.channel");

  static Future<String> takeVoice(
      MethodChannel platform, String groupName /*, int counter*/) async {
    // if (file == null) return false;

    dynamic ret = await platform.invokeMethod('idvoice.enroll.voice', {
      // 'file': file.path,
      'group': groupName,
      // 'counter': counter,
    });

    print('enrollAudio terima $ret');

    return ret;
  }

  static Future<String> mergeVoices(
      MethodChannel platform, String groupName /*, int counter*/) async {
    // if (file == null) return false;

    dynamic ret = await platform.invokeMethod('idvoice.merge.voice', {
      // 'file': file.path,
      'group': groupName,
      // 'counter': counter,
    });

    print('enrollAudio terima $ret');

    return ret;
  }

  static Future<String> liveAudio(MethodChannel mc) async {
    dynamic ret = await mc.invokeMethod('idvoice.audio.live', {
      // 'file': file.path,
      // 'score.minimum': '$scoreSimilarity',
    });

    return ret;
  }

  // due to android startactivityforresult, need to pass custom methodchannel
  // static Future<String> livePhoto(
  //     MethodChannel mc, double scoreSimilarity) async {
  //   dynamic ret = await mc.invokeMethod('truface.camera.live', {
  //     // 'file': file.path,
  //     'score.minimum': '$scoreSimilarity',
  //   });

  //   return ret;
  // }

  static Future<List<TrnEnroll>> getAllEnroll(MethodChannel platform) async {
    dynamic result = await platform.invokeMethod("idvoice.group");
    // dynamic result = await platform.invokeMethod("truface.group.face", {
    //   'group': 'elkana',
    // });

    print('getAllEnroll terima $result');

    List<TrnEnroll> rows = [];

    List<dynamic> ss = jsonDecode(result);
    for (int i = 0; i < ss.length; i++) {
      TrnEnroll value = TrnEnroll.fromJson(ss[i]);

      rows.add(value);
    }

    return rows;
  }

  // static Future<bool> delete(String uid) async {
  //   dynamic ret = await platform.invokeMethod('truface.delete.uid', {
  //     'uid': uid,
  //   });

  //   print('delete terima $ret');

  //   return true;
  // }

  static Future<bool> clearAll(MethodChannel platform) async {
    dynamic ret = await platform.invokeMethod('idvoice.clear');

    print('clearAll terima $ret');

    return true;
  }
}
