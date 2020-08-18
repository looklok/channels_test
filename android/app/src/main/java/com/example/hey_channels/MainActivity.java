package com.example.hey_channels;

import androidx.annotation.NonNull;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugins.GeneratedPluginRegistrant;
import io.flutter.plugin.common.MethodChannel;
import java.util.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

public class MainActivity extends FlutterActivity {

  private static final String CHANNEL = "hello.channels.test/photos";
  private Classifier classifier;
  @Override
  public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
    GeneratedPluginRegistrant.registerWith(flutterEngine);
    super.configureFlutterEngine(flutterEngine);
    new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
        .setMethodCallHandler(
          (call, result) -> {
            if (call.method.equals("getAResponse")) {
              System.out.println("watch out it's java time");
              ArrayList<Object> paths = call.argument("paths");
              System.out.println(paths); 
              String res = getAResponse(paths);

              if (res != null) {
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

  private String getAResponse (ArrayList<Object> paths){
    
    
    classifier = new Classifier(Utils.assetFilePath(this,"resnet18.pt"));
    String path = (String) paths.get(0);
    System.out.println(path);
    Bitmap imageBitmap;
    try{
          imageBitmap = BitmapFactory.decodeFile(path);

    }  catch(Exception e){
          
        System.out.println(e);
        return null;
    }

    String pred = classifier.predict(imageBitmap);
    
    return pred;
  }
}
