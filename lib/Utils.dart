import 'package:image/image.dart' as img;
import 'dart:io';
import 'dart:math';

class Utils {
  static img.Image resizeImage (img.Image original){

    int new_height, new_width;
    int old_height  = original.height;
    int old_width = original.width;
    double aspect_ratio = old_width / old_height;
    print(aspect_ratio);
    img.Image  resized;
        if (max(old_height, old_width) >420){
            if (old_height >= old_width){
                new_height = 420;
                new_width = (new_height *aspect_ratio).floor();
            }else{
                new_width = 420;
                new_height =(new_width/aspect_ratio).floor();
            }
            print(new_height.toString() + " "+ new_width.toString());
            resized = img.copyResize(original, width: new_width, height: new_height,);
            return resized;
        }else{
            return original;
        }
  }
  static img.Image drawRectangle (img.Image original, List box, int color ,{int thick :1}){

    var x0 = box[0];
    var x1 = box[2];
    var y0 = box[1];
    var y1 = box[3];
    img.drawLine(original, x0, y0, x1, y0, color, thickness: thick);
    img.drawLine(original, x1, y0, x1, y1, color, thickness: thick);
    img.drawLine(original, x0, y1, x1, y1, color, thickness: thick);
    img.drawLine(original, x0, y0, x0, y1, color, thickness: thick);

    return original;
  }
}