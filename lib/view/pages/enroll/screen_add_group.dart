import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:idvoice/model/trn_enroll.dart';
import 'package:idvoice/util/idvoice_util.dart';
import 'package:permission_handler/permission_handler.dart';

class ScreenAddGroup extends StatefulWidget {
  @override
  _ScreenAddGroupState createState() => _ScreenAddGroupState();

  static void go(BuildContext context, Function(bool) onExit) {
    Navigator.push(
        context,
        MaterialPageRoute(
            builder: (context) => ScreenAddGroup(
                // contractNo: contractNo,
                ))).then((value) => onExit(value == null ? false : value));
  }
}

class _ScreenAddGroupState extends State<ScreenAddGroup> {
  bool _loading = false;
  String? _groupName;
  File? _selectedFile;
  TextEditingController _controller = new TextEditingController();
  final _formKey = GlobalKey<FormState>();
  MethodChannel _myChannel = MethodChannel('flutter.my.channel');
  int lastCounter = 0;
  String? _result;
  // File? activeAudio;

  @override
  void initState() {
    super.initState();

    _myChannel.setMethodCallHandler((call) async {
      switch (call.method) {
        case 'idvoice.add.result':
          dynamic ss = jsonDecode(call.arguments);

          if (ss != null) {
            TrnEnroll _e = TrnEnroll.fromJson(ss);
            lastCounter += 1;

            // kalau udah 3 minta di merge
            if (lastCounter == 3) {
              String ret =
                  await IDVoiceUtil.mergeVoices(_myChannel, _groupName!);

              print('Setelah merge $_groupName jadi $ret');

              _result = 'Completed !';

              // should close
              Navigator.of(context).pop(true);
            } else
              _result = 'Voice ${_e.groupName} $lastCounter/3'.toUpperCase();

            setState(() {
              // activeAudio = new File(_e.fileName!);
            });
          }
      }
      return new Future.value("");
    });
    // ac = AnimationController(
    //   duration: Duration(seconds: 3),
    // );
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future _submit() async {
    final form = _formKey.currentState;

    if (!form!.validate()) return;

    if (_selectedFile == null) {
      print('No Photo selected');
      return;
    }

    form.save();

    setState(() {
      _loading = true;
    });

    try {
      // bool ret = await TruefaceUtil.enrollPhoto(_selectedFile, _groupName);

      // if (ret) Navigator.pop(context, ret);
    } finally {
      if (this.mounted)
        setState(() {
          _loading = false;
        });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Enroll'),
        actions: [
          // ElevatedButton.icon(
          //   onPressed: _loading ? null : () => _submit(),
          //   icon: Text('Register'),
          //   label: Icon(Icons.save_alt_rounded),
          // ),
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(8.0),
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              new TextFormField(
                maxLength: 30,
                readOnly: lastCounter > 0,
                controller: _controller,
                decoration: InputDecoration(
                    icon: Icon(Icons.person_add),
                    labelText: "Your Name",
                    hintText: "John Doe"),
                validator: (val) => val!.length < 3 ? 'Name too short' : null,
                onSaved: (val) {
                  _groupName = val;
                },
              ),
              SizedBox(height: 10),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: [
                  Opacity(
                    opacity: lastCounter > 0 ? 1.0 : 0.3,
                    child: FloatingActionButton(
                      heroTag: 'Voice1',
                      onPressed: null,
                      child: Icon(
                        Icons.check_circle_outlined,
                        size: 40,
                      ),
                    ),
                  ),
                  Opacity(
                    opacity: lastCounter > 1 ? 1.0 : 0.3,
                    child: FloatingActionButton(
                      heroTag: 'Voice2',
                      onPressed: null,
                      child: Icon(
                        Icons.check_circle_outlined,
                        size: 40,
                      ),
                    ),
                  ),
                  Opacity(
                    opacity: lastCounter > 2 ? 1.0 : 0.3,
                    child: FloatingActionButton(
                      heroTag: 'Voice3',
                      onPressed: null,
                      child: Icon(
                        Icons.check_circle_outlined,
                        size: 40,
                      ),
                    ),
                  ),
                ],
              ),
              SizedBox(
                height: 20,
              ),
              Text('Record 3 voices at minimum\n${_result ?? ''}'),
              Expanded(
                child: Center(
                  child: InkWell(
                    child: Icon(
                      Icons.mic,
                      size: 80,
                    ),
                    onTap: lastCounter >= 3
                        ? null
                        : () async {
                            if (!(await Permission.microphone
                                .request()
                                .isGranted)) return;

                            if (!(await Permission.storage.request().isGranted))
                              return;

                            final form = _formKey.currentState;

                            if (!form!.validate()) return;

                            form.save();

                            if (_groupName == null || _groupName!.length < 1) {
                              print('No Name defined');
                              return;
                            }

                            await IDVoiceUtil.takeVoice(
                                _myChannel, _groupName!);

                            // Either the permission was already granted before or the user just granted it.
                          },
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
