import 'package:json_annotation/json_annotation.dart';

part 'trn_enroll.g.dart';

@JsonSerializable()
class TrnEnroll {
  String? uid;
  String? fileName;
  String? groupName;
  String? fullName;
  int? createdDate;

  TrnEnroll(
      {this.uid,
      this.fileName,
      this.groupName,
      this.fullName,
      this.createdDate});

  factory TrnEnroll.fromJson(Map<String, dynamic> data) =>
      _$TrnEnrollFromJson(data);

  Map<String, dynamic> toJson() => _$TrnEnrollToJson(this);
}
