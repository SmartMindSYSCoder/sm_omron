import 'package:flutter_test/flutter_test.dart';
import 'package:sm_omron/sm_omron.dart';

import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockSmOmronPlatform
    with MockPlatformInterfaceMixin
     {


}

void main() {


  test('getPlatformVersion', () async {
    SMOmron smOmronPlugin = SMOmron();
    MockSmOmronPlatform fakePlatform = MockSmOmronPlatform();
   // SmOmronPlatform.instance = fakePlatform;

  });
}
