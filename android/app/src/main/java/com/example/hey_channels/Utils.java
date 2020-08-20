package com.example.hey_channels;


import android.content.Context;
import android.util.Log;
//import sun.jvm.hotspot.utilities.BitMap;

import java.util.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

public class Utils {

    public static String assetFilePath(Context context, String assetName) {
        File file = new File(context.getFilesDir(), assetName);

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e("pytorchandroid", "Error process asset " + assetName + " to file path");
        }
        return null;
    }

    public static double[] convertFloatsToDoubles(float[] input)
    {
        if (input == null)
        {
            return null; // Or throw an exception - your choice
        }
        double[] output = new double[input.length];
        for (int i = 0; i < input.length; i++)
        {
            output[i] = input[i];
        }
        return output;
    }

    public static Bitmap resizeImage (Bitmap original){
        int new_height, new_width;
        int old_height  = original.getHeight();
        int old_width = original.getWidth();
        float aspect_ratio = (float)old_width / old_height;
        Bitmap resized;
        if (Math.max(old_height, old_width) >420){
            if (old_height >= old_width){
                new_height = 420;
                new_width = (int)(new_height *aspect_ratio);
            }else{
                new_width = 420;
                new_height = (int)(new_width/aspect_ratio);
            }
            resized = Bitmap.createScaledBitmap(original, new_width, new_height , false);
            return resized;
        }else{
            return original;
        }
    }

    public static ArrayList<ArrayList<Integer>> sliding_windows (int height, int width){

        int [][] dims = {{240, 240}, {120, 120}, {80, 80}, {120, 60},{60, 120}, /*{300, 300}, {180, 360}, {180, 90}*/};
        int steps = 14;
        int [] xs = new int[steps];
        for (int i =0; i < steps; i++){
            xs[i]  = (i+1) * 26 ;
        }
        int [] ys = Arrays.copyOf(xs, xs.length);
        

        int [][] ctr = new int[xs.length*ys.length][2];
        int index = 0;
        for (int i = 0; i< xs.length; i++){
            for (int j = 0; j< ys.length; j++){

                ctr[index][1] = xs[i] -13;
                ctr[index][0] = ys[j] - 13;
                index = index+1;

            }

        }
        int [][] boxes = new int[steps*steps*9][4];
        for (int ind = 0; ind < boxes.length; ind++){
                boxes [ind][0] = -1;
                boxes [ind][1] = -1;
                boxes [ind][2] = -1;
                boxes [ind][3] = -1;

        }
        //System.out.println("making : \n");
        index = 0; int x,y,w,h;
        for (int i =0; i<ctr.length; i++){
            y = ctr[i][0];
            x = ctr[i][1];
            for (int j=0; j < dims.length; j++){

                h = dims[j][0];
                w = dims[j][1];
                boxes [index][0] = x - w /2;
                boxes [index][1] = y - h /2;
                boxes [index][2] = x + w /2;
                boxes [index][3] = y + h /2;
                index = index+1;
            }
        }
        //System.out.println("filtring: \n");
        ArrayList<ArrayList<Integer>> output = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> element ;
        //System.out.println(boxes.length);
        for (int ind =0; ind<boxes.length; ind++){
            if ((boxes[ind][0] >= 0) && (boxes [ind][1] >= 0)  && (boxes [ind][2] <= width) && (boxes [ind][3] <= height)){
                element = new ArrayList<Integer>(4);
                element.add( boxes [ind][0]);
                element.add( boxes [ind][1]);
                element.add( boxes [ind][2]);
                element.add( boxes [ind][3]);
                ArrayList<Integer> copy = new ArrayList<Integer>(element);
                output.add(copy);
            }
        }
        System.out.println("printing bounding boxes : \n");
        for (int ind=0; ind<output.size(); ind++){
            element = output.get(ind);
            //System.out.println(element.get(0).toString()+"  "+element.get(1).toString()+"  "+element.get(2).toString()+"  "+element.get(3).toString());
            System.out.println("******* : "+(element.get(2)-element.get(0))+"  "+(element.get(3)-element.get(1)));
        }
        System.out.println("We generated :" + output.size()+ "  bounding boxes");
        return output;

    }

    public static TreeMap<Integer, ArrayList<Integer>> non_max_supression (TreeMap<Integer, ArrayList<Integer>> proposals, double threshold)
    {

        if ( proposals == null || proposals.size() == 0){
            return null;
        }
        int xx1, xx2, yy1, yy2, w, h, areaBox1, areaBox2, inter; double IOU; 
        ArrayList<Integer> keys_to_remove , tmp;
        TreeMap<Integer, ArrayList<Integer>> boxes = new TreeMap<>();
        //System.out.println("Innnnnnnnnnnnnnnnnnnnnn :");
        while (proposals.size() != 0){
            Map.Entry<Integer, ArrayList<Integer>> entry = proposals.firstEntry();
            int confidence = entry.getKey(); 
            //System.out.println(proposals);
            //System.out.println(entry);
            ArrayList<Integer> value = entry.getValue(); 
            areaBox1 = (value.get(2)-value.get(0)) * (value.get(3)-value.get(1));
            boxes.put(confidence, value);
            tmp = (ArrayList<Integer> )proposals.remove(confidence);


            keys_to_remove = new ArrayList<Integer>();

            for (Map.Entry<Integer, ArrayList<Integer>> element : proposals.entrySet()) {
                Integer conf = element.getKey();
                ArrayList<Integer> coords = element.getValue(); 
                xx1 = Math.max(value.get(0), coords.get(0));
                yy1 = Math.max(value.get(1), coords.get(1));
                xx2 = Math.min(value.get(2), coords.get(2));
                yy2 = Math.min(value.get(3), coords.get(3));

                w = Math.max(0, xx2 - xx1 );
                h = Math.max(0, yy2 - yy1 );
                inter = w*h;
                //System.out.println(element);
                //System.out.println("intersection  "+inter);
                areaBox2 = (coords.get(2)-coords.get(0)) * (coords.get(3)-coords.get(1));
                //System.out.println("areas : 1 : "+areaBox1+ " 2 :"+areaBox2);
                IOU = ((double)inter) /((double)areaBox1 + areaBox2 - inter);
                //System.out.println(IOU);
                if (IOU > threshold){
                    //System.out.println("herrrrrrrreeeeeeeeee");
                    keys_to_remove.add(conf);
                }

            }
            for (int key : keys_to_remove){
                tmp = (ArrayList<Integer> )proposals.remove(key);
            }
        }
        

        return boxes;
        
        


    }

}