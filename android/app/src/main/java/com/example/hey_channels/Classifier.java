package com.example.hey_channels;

import android.graphics.Bitmap;
import org.pytorch.Tensor;
import org.pytorch.Module;
import org.pytorch.IValue;
import org.pytorch.torchvision.TensorImageUtils;

import java.util.*;


public class Classifier {

    Module model;
    float[] mean = {0.485f, 0.456f, 0.406f};
    float[] std = {0.229f, 0.224f, 0.225f};

    public Classifier(String modelPath){

        model = Module.load(modelPath);

    }

    public void setMeanAndStd(float[] mean, float[] std){

        this.mean = mean;
        this.std = std;
    }

    public Tensor preprocess(Bitmap bitmap, int size){

        bitmap = Bitmap.createScaledBitmap(bitmap,size,size,false);
        return TensorImageUtils.bitmapToFloat32Tensor(bitmap,this.mean,this.std);

    }

    public int argMax(float[] inputs){

        int maxIndex = -1;
        float maxvalue = 0.0f;

        for (int i = 0; i < inputs.length; i++){

            if(inputs[i] > maxvalue) {

                maxIndex = i;
                maxvalue = inputs[i];
            }

        }


        return maxIndex;
    }

    public Object [] predict(Bitmap bitmap){
        long start, end, elapsedTime;
        
        Tensor tensor = preprocess(bitmap,224);
        IValue inputs = IValue.from(tensor);
        
        start = System.nanoTime();
        Tensor outputs = model.forward(inputs).toTensor();
        end = System.nanoTime();
        float[] scores = outputs.getDataAsFloatArray();
        int classIndex = argMax(scores);
        
        elapsedTime = end - start;
        
        double[] scoresD = Utils.convertFloatsToDoubles(scores);
        double prob = softmax(scoresD[classIndex], scoresD );
        
        System.out.println("infrence time for this pass is : "+ ((int) (elapsedTime / 1000000)) +" ms");
        //System.out.println("the probability of this class is :"+ prob);
        Object [] result = {classIndex, prob};

        return result;

    }

    private double softmax(double input, double[] neuronValues) {
        double total = Arrays.stream(neuronValues).map(Math::exp).sum();
        return Math.exp(input) / total;
    }

}
