package com.example.hey_channels;

import androidx.annotation.NonNull;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugins.GeneratedPluginRegistrant;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {

  private static final String CHANNEL = "hello.channels.test/photos";

  @Override
  public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
    GeneratedPluginRegistrant.registerWith(flutterEngine);
    super.configureFlutterEngine(flutterEngine);
    new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
        .setMethodCallHandler(
          (call, result) -> {
            if (call.method.equals("getAResponse")) {
              System.out.println("watch out it's java time");
              System.out.println(call.arguments.getClass().getName()); 
              boolean res = getAResponse();

              if (res) {
                result.success(res);
              } else {
                result.error("UNAVAILABLE", "A problem with your method sir", null);
              }
            } else {
              result.notImplemented();
            }
          }
        ); 


  }

  private boolean getAResponse (){
    return true;
  }
}
