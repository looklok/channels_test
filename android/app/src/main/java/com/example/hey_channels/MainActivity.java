package com.example.hey_channels;

import androidx.annotation.NonNull;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugins.GeneratedPluginRegistrant;
//import sun.jvm.hotspot.utilities.BitMap;
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
              int number = call.argument("number");
              System.out.println(number); 
              HashMap<Integer, ArrayList<Integer>> res = getAResponse(paths, number);

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

  private HashMap<Integer, ArrayList<Integer>> getAResponse (ArrayList<Object> paths, int number){
    
    
    classifier = new Classifier(Utils.assetFilePath(this,"resnet18.pt"));
    String path = (String) paths.get(number);
    System.out.println(path);
    Bitmap imageBitmap, resizedBitmap;
    try{
          imageBitmap = BitmapFactory.decodeFile(path);

    }  catch(Exception e){
          
        System.out.println(e);
        return null;
    }

    resizedBitmap = Utils.resizeImage(imageBitmap);
    int height = resizedBitmap.getHeight();
    int width = resizedBitmap.getWidth();
    System.out.println(height);
    System.out.println(width+"\n");
    ArrayList<ArrayList<Integer>> boxes = Utils.sliding_windows(height, width);
    Object[] pred; long start, end, elapsedTime;
    Bitmap croppedImageBitmap; double probabilty;
    TreeMap<Integer, ArrayList<Integer>> bounding_boxes = new TreeMap<>(Collections.reverseOrder());
    start = System.nanoTime();
    for (ArrayList<Integer> box : boxes ){
      try {
          croppedImageBitmap = Bitmap.createBitmap(resizedBitmap, box.get(0), box.get(1),  box.get(2)-box.get(0), box.get(3)-box.get(1) );
          //start = System.nanoTime();
        
          pred = classifier.predict(croppedImageBitmap);

          
          probabilty = (double)pred[1] ;
          System.out.println(probabilty);
          if (probabilty > 0.6) {
            System.out.println(box);
            System.out.println(Constants.IMAGENET_CLASSES[(int)pred[0]]);
            bounding_boxes.put((int)(probabilty*1000), new ArrayList<>(box));
          }
      } catch (Exception e) {
          System.out.println(e);
      }
      
    }
    end = System.nanoTime();
    elapsedTime = end - start;
    System.out.println((int) (elapsedTime / 1000000) );
    System.out.println("bounding boxes before non max suppression : "+bounding_boxes.size());
    TreeMap<Integer, ArrayList<Integer>> res = Utils.non_max_supression(bounding_boxes, 0.35);

    /*TreeMap<Integer, ArrayList<Integer>> test = new TreeMap<>(Collections.reverseOrder());
    ArrayList<Integer> element = new ArrayList<>();
    element.add(0); element.add(0); element.add(60); element.add(60);
    ArrayList<Integer> element1 = new ArrayList<>();
    element1.add(70); element1.add(70); element1.add(90); element1.add(90);
    ArrayList<Integer> element2 = new ArrayList<>();
    element2.add(0); element2.add(0); element2.add(80); element2.add(80);
    test.put(70, new ArrayList<>(element));
    //test.put(60, new ArrayList<>(element1));
    //test.put(90, new ArrayList<>(element2));*/

    if(res != null && res.size() > 0){
      for (Map.Entry<Integer, ArrayList<Integer>> en : res.entrySet()) {
        System.out.println("predicted bounding boxes : "+en.getKey()+" : "+ en.getValue().get(0)+"  "+en.getValue().get(1)+"  "+en.getValue().get(2)+"  "+en.getValue().get(3));
      }
    }else{
      return null;
    }
    

    pred = classifier.predict(imageBitmap);
    
    HashMap<Integer, ArrayList<Integer>> hashMap = new HashMap<Integer, ArrayList<Integer>>();
    hashMap.putAll(res);

    return hashMap;
  }
}
