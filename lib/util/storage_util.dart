import 'package:shared_preferences/shared_preferences.dart';

extension Ex on double {
  double toPrecision(int n) => double.parse(toStringAsFixed(n));
}

class StorageUtil {
  static const String keyScoreSimilarity = 'IDVOICE.SIMILARITY.SCORE';

  static Future<SharedPreferences> _prefs = SharedPreferences.getInstance();

  static Future<bool> setString(String name, String value) async {
    final SharedPreferences prefs = await _prefs;

    return prefs.setString(name, value);
  }

  static Future<String> getValueAsString(String name) async {
    final SharedPreferences prefs = await _prefs;

    return prefs.getString(name) ?? '';
  }

  static Future<double?> getScoreSimilarity() async {
    String _s = await getValueAsString(keyScoreSimilarity);

    return _s == '' ? 0.7 : double.tryParse(_s);
  }

  static Future setScoreSimilarity(double score) async {
    double d1 = score.toPrecision(1);
    await setString(keyScoreSimilarity, '$d1');
  }
}
