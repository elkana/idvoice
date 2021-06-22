import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:idvoice/util/idvoice_util.dart';
import 'package:permission_handler/permission_handler.dart';

import 'enroll/screen_enroll_list.dart';

class ScreenIdentify extends StatefulWidget {
  final String title;

  const ScreenIdentify({Key? key, required this.title}) : super(key: key);

  @override
  _ScreenIdentifyState createState() => _ScreenIdentifyState();
}

class _ScreenIdentifyState extends State<ScreenIdentify> {
  bool _loading = false;
  String? _liveResult;

  MethodChannel _myChannel = const MethodChannel('flutter.my.channel');

  @override
  void initState() {
    super.initState();

    // ternyata ga perlu, pake invokemethod sudh bisa wait for activity result callback
    // _myChannel.setMethodCallHandler((call) async {
    //   print('call.method -> ${call.method}');

    //   switch (call.method) {
    //     case 'idvoice.audio.live.result':
    //       dynamic ss = call.arguments;

    //       print('idvoice.audio.live.result -> $ss');
    //       // dynamic ss = jsonDecode(call.arguments);

    //       // if (ss != null) {
    //       //   TrnEnroll _e = TrnEnroll.fromJson(ss);

    //       setState(() {
    //         _liveResult = '$ss';
    //       });
    //     // }
    //   }
    //   return new Future.value("");
    // });

    // ac = AnimationController(
    //   duration: Duration(seconds: 3),
    // );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        // Here we take the value from the MyHomePage object that was created by
        // the App.build method, and use it to set our appbar title.
        title: Text(widget.title),
        actions: [
          ElevatedButton.icon(
            icon: Text('Enroll'),
            onPressed: () {
              ScreenEnrollList.go(context);
            },
            label: Icon(Icons.add),
          ),
          IconButton(
              icon: Icon(Icons.settings),
              onPressed: () {
                if (this.mounted)
                  setState(() {
                    _loading = false;
                  });

                // ScreenSettings.go(context);
              })
        ],
      ),
      body: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            CircleAvatar(
              radius: 100,
              backgroundImage: AssetImage('images/logo_sp.png'),
            ),
            Divider(),
            _liveResult == null
                ? SizedBox()
                : Text(
                    _liveResult!,
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
            SizedBox(
              height: 10,
            ),
            _loading
                ? CircularProgressIndicator()
                : ElevatedButton.icon(
                    label: Padding(
                      padding: const EdgeInsets.all(18.0),
                      child: Text(
                        'Identify',
                        style: TextStyle(letterSpacing: 1),
                      ),
                    ),
                    onPressed: () async {
                      List list = await IDVoiceUtil.getAllEnroll(_myChannel);

                      if (list.isEmpty) {
                        setState(() {
                          _liveResult = 'No Data, Please Enroll first';
                        });
                        return;
                      }

                      if (!(await Permission.microphone.request().isGranted))
                        return;

                      if (!(await Permission.storage.request().isGranted))
                        return;

                      setState(() {
                        _loading = true;
                        _liveResult = null;
                      });

                      try {
                        var ss = await IDVoiceUtil.liveAudio(_myChannel);

                        print('liveAudio result -> $ss');

                        //       // if (ss != null) {
                        //       //   TrnEnroll _e = TrnEnroll.fromJson(ss);

                        setState(() {
                          _liveResult = '$ss';
                        });
                      } finally {
                        if (this.mounted)
                          setState(() {
                            _loading = false;
                          });
                      }
                    },
                    icon: Icon(Icons.record_voice_over),
                  ),
            SizedBox(
              height: 20,
            ),
          ],
        ),
      ),
    );
  }
}
