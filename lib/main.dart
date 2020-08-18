import 'dart:io';
import 'package:photo_manager/photo_manager.dart';
import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';
import 'dart:typed_data';
import 'package:path/path.dart';
import 'package:flutter/services.dart';
import 'package:image/image.dart' as img;


void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Flutter Demo',
      theme: ThemeData(
         primarySwatch: Colors.indigo,
      ),
      home: MyHomePage(title: 'Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);
  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {

  File _image ;
  String _response = "nothing";
  static const platform = const MethodChannel('hello.channels.test/photos');

  void initState(){
    super.initState();
    
  }


  Future<void> _getAResponse(List<String> l ) async {   //as simple as it can be
    String response;
    print("*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-");
    try {
      final String result = await platform.invokeMethod('getAResponse',  {"paths" : l});
      response = 'Response for java part = $result';
    } on PlatformException catch (e) {
      response = "Failed to get a response level: '${e.message}'.";
    } 

    setState(() {
      _response = response;
    });
  }



  _fetchNewMedia() async {

    var result = await PhotoManager.requestPermission();
    if (result) {
      // success
      Directory appDocDir = await getApplicationDocumentsDirectory();
      String appDocPath = appDocDir.path;
      print(appDocPath);

      //load the album list
      
      List<AssetPathEntity> albums = await PhotoManager.getAssetPathList();
      print(albums);
      print("---------------------------------------------------");
      print("---------------------------------------------------");
      List<AssetEntity> albumMedia = await albums[0].getAssetListPaged(0, 20);
      print(albumMedia);
      print("---------------------------------------------------");
      print("---------------------------------------------------");
      AssetEntity media; int i=20; File tempFile, newImage;
      List<String> listPaths = [];
      for (media in albumMedia){

        if (media.type == AssetType.image){
          tempFile = await media.originFile;
          print(tempFile.path);
          var fileName = basename(tempFile.path);
          print(fileName);

          //newImage = await tempFile.copy('$appDocPath/$fileName');
          listPaths.add(tempFile.path);
          print(tempFile.path);

          i-=1; 
          if(i==0) break;

        }        

      }

      print("the list of paths to pass : ----------------------------------------------");
      print(listPaths);

      /*print("the content of the app directory: **********************************");
      var dir = Directory('$appDocPath');
      print(dir.listSync(recursive: true));*/
      
      _getAResponse(listPaths);
      
      setState(() {
        print("\n\n\n");
        _image = tempFile;
      });
    } else {
      // fail
    }
  }
  
  @override
  Widget build(BuildContext context) {
    
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
           Container(
              height: 200,
              decoration: BoxDecoration(
                image: DecorationImage(
                  image: _image != null ? Image.file(_image).image : AssetImage('assets/images/robot.jpg'),  // <-- Expecting ImageProvider
                )
              ),
            ),
            const SizedBox(height: 20,),
            RaisedButton(
              onPressed: _fetchNewMedia ,
              child: const Text(
                'get an image',
                style: TextStyle(fontSize: 20)
              ),
            
            ),
            SizedBox(height: 20,),
            Text(_response),
           // ImageWidget(x: 0,y: 0, w: 200, h: 200, path:"/storage/emulated/0/DCIM/Camera/20200806_193622.jpg", color: img.getColor(255, 0, 0),),
          ],
        ),
        
        )
    );
  }
}
class ImageWidget extends StatelessWidget {
  String path;
  int x,y,w,h, color; 
  ImageWidget({this.x, this.y, this.h, this.w, this.path, this.color });
  @override
  Widget build(BuildContext context) {
    
    
    return FutureBuilder(builder: (context, snap){
        if (snap.connectionState == ConnectionState.none || snap.connectionState == ConnectionState.waiting || snap.data == null ||
          snap.hasData == null) {
        print('project snapshot data is: ${snap.data}');
        return Container(color: Colors.black, height: 50, width: 50,);
        }else{
            print("snappppppppp"+snap.data.toString());
            return Image.file(File("${snap.data}/photo.jpg"));
        }
        
    },
    future: preprocessingGetInfo(),
    );
    
    
    
  }

  Future<String> preprocessingGetInfo() async {
    var result = await PhotoManager.requestPermission();
    print(result);
    print("dakhla");
    print(this.path);
    Directory appDocDir = await getApplicationDocumentsDirectory();
    String appDocPath = appDocDir.path;
    print(appDocPath);
    try {
      print("begin");    
      img.Image photo = img.decodeJpg(File(this.path).readAsBytesSync());
      print("${photo.height} width ${photo.width}");
      //photo = img.gaussianBlur(photo, 20);
      photo= img.copyResize(photo, width: 300, height: 300);
      print("${photo.height} width ${photo.width}");
      photo= img.drawLine(photo, 0, 0, 300, 240, img.getColor(255, 0, 0), thickness: 15);
      photo= img.drawRect(photo, this.x, this.y , this.x+this.w, this.y+this.h , this.color);
      print("end1");
      File("${appDocPath}/photo.jpg").writeAsBytesSync(img.encodeJpg(photo));
      print("end2");
    } on Exception catch (e) {
          print(e.toString());
    }
    return appDocPath;
  }


}

