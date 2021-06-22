import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:idvoice/model/trn_enroll.dart';
import 'package:idvoice/util/idvoice_util.dart';

import 'screen_add_group.dart';

class ScreenEnrollList extends StatefulWidget {
  @override
  _ScreenEnrollState createState() => _ScreenEnrollState();

  static void go(BuildContext context) {
    Navigator.push(
        context,
        MaterialPageRoute(
            builder: (context) => ScreenEnrollList(
                // contractNo: contractNo,
                )));
  }
}

class _ScreenEnrollState extends State<ScreenEnrollList> {
  MethodChannel _myChannel = MethodChannel('flutter.my.channel');

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('People'),
        actions: [
          ElevatedButton.icon(
            onPressed: () {
              ScreenAddGroup.go(context, (val) {
                if (val && this.mounted) setState(() {});
              });
            },
            icon: Text('Add Person'),
            label: Icon(Icons.add),
          ),
          IconButton(
              icon: Icon(Icons.delete_sweep_outlined),
              onPressed: () async {
                await IDVoiceUtil.clearAll(_myChannel);

                if (this.mounted) setState(() {});
              }),
        ],
      ),
      body: FutureBuilder<List<TrnEnroll>>(
          future: IDVoiceUtil.getAllEnroll(_myChannel),
          builder: (context, snapshot) {
            if (snapshot.connectionState == ConnectionState.waiting)
              return CircularProgressIndicator();

            if (!snapshot.hasData) return Text('No Data. Add Enroll first.');

            return ListView.separated(
              separatorBuilder: (context, index) => SizedBox(),
              itemCount: snapshot.data!.length,
              itemBuilder: (context, index) => Dismissible(
                key: Key(snapshot.data![index].uid!),
                child: Padding(
                    padding: EdgeInsets.all(8.0),
                    child: Card(
                      child: Padding(
                        padding: const EdgeInsets.all(8.0),
                        child: ListTile(
                          title: Text(
                              snapshot.data![index].fullName!.toUpperCase()),
                          subtitle: Text(
                              snapshot.data![index].groupName!.toUpperCase()),
                          leading: CircleAvatar(
                            radius: 30,
                            child: Icon(Icons.mic),
                          ),
                          // leading: SizedBox(
                          //   width: 80,
                          //   height: 100,
                          //   child:
                          //       Image.file(new File(snapshot.data[index].fileName)),
                          // ),
                        ),
                      ),
                    )
                    // Center(child: Text("Index $index")),
                    ),
                // onDismissed: (direction) async {
                //   bool ret =
                //       await TruefaceUtil.delete(snapshot.data[index].uid);

                //   if (ret && this.mounted) setState(() {});
                // },
              ),
            );
          }),
    );
  }
}
