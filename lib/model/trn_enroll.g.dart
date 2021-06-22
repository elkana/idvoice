// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trn_enroll.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TrnEnroll _$TrnEnrollFromJson(Map<String, dynamic> json) {
  return TrnEnroll(
    uid: json['uid'] as String,
    fileName: json['fileName'] as String,
    groupName: json['groupName'] as String,
    fullName: json['fullName'] as String,
    createdDate: json['createdDate'] as int,
  );
}

Map<String, dynamic> _$TrnEnrollToJson(TrnEnroll instance) => <String, dynamic>{
      'uid': instance.uid,
      'fileName': instance.fileName,
      'groupName': instance.groupName,
      'fullName': instance.fullName,
      'createdDate': instance.createdDate,
    };
