import 'dart:io';
import 'package:photo_manager/photo_manager.dart';
import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';
import 'dart:typed_data';
import 'package:path/path.dart';
import 'package:flutter/services.dart';
import 'package:image/image.dart' as img;

import 'Utils.dart';

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
  File _image;
  String _response = "nothing";
  int number = 0; String _path = null;
  Map<dynamic, dynamic> bboxes;
  static const platform = const MethodChannel('hello.channels.test/photos');

  void initState() {
    super.initState();
  }

  Future<void> _getAResponse(List<String> l) async {

    String response;
    print("*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-");
    try {

      bboxes =await platform.invokeMethod('getAResponse', {"paths": l, "number": number});
      print(bboxes);
      print('Response for java part');
      response = 'Response for java part';

    } on PlatformException catch (e) {
      bboxes = Map();
      response = "Failed to get a response level: '${e.message}'.";
    } on Exception catch (e){
      print(e);
      print("errrooooot");
      bboxes = Map();
    }

    setState(() {
      _response = response;
    });
  }

  _fetchNewMedia() async {
    var result = await PhotoManager.requestPermission();
    if (result) {
      // app directory
      Directory appDocDir = await getApplicationDocumentsDirectory();
      String appDocPath = appDocDir.path;
      print(appDocPath);

      //load the album list

      List<AssetPathEntity> albums = await PhotoManager.getAssetPathList();
      print(albums);
      List<AssetEntity> albumMedia = await albums[0].getAssetListPaged(0, 20);
      print(albumMedia);
      
      AssetEntity media; int i = 20; File tempFile, newImage;
      List<String> listPaths = [];
      for (media in albumMedia) {
        if (media.type == AssetType.image) {
          tempFile = await media.originFile;

          /*var fileName = basename(tempFile.path);
          print(fileName);*/

          //newImage = await tempFile.copy('$appDocPath/$fileName');
          listPaths.add(tempFile.path);
          print(tempFile.path);

          i -= 1;
          if (i == 0) break;
        }
      }

      print( "the list of paths to pass : ----------------------------------------------");
      print(listPaths);

      _path = listPaths[number];
      /*print("the content of the app directory: **********************************");
      var dir = Directory('$appDocPath');
      print(dir.listSync(recursive: true));*/

      _getAResponse(listPaths);

      //_image = await albumMedia[number].originFile;

      setState(() {
        print("\n\n\n");
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
              /*Container(
                height: 224,
                decoration: BoxDecoration(
                    image: DecorationImage(
                  image: _image != null
                      ? Image.file(_image).image
                      : AssetImage(
                          'assets/images/robot.jpg'), // <-- Expecting ImageProvider
                )),
              ),*/
              (_path!= null && bboxes != null) ? ImageWidget(path:_path, color: img.getColor(255, 0, 0), bounding_boxes : bboxes ) : Image.asset('assets/images/robot.jpg'),
              const SizedBox(
                height: 20,
              ),
              TextField(
                onSubmitted: (value) {
                  number = int.parse(value);
                },
                keyboardType: TextInputType.number ,
                decoration: InputDecoration(
                  border: OutlineInputBorder(),
                  labelText: 'number of the image',
                ),
              ),
              const SizedBox(
                height: 20,
              ),
              RaisedButton(
                onPressed: _fetchNewMedia,
                child:
                    const Text('get an image', style: TextStyle(fontSize: 20)),
              ),
              SizedBox(
                height: 20,
              ),
              Text(_response),
            ],
          ),
        ));
  }
}

class ImageWidget extends StatelessWidget {
  String path;
  int x, y, w, h, color; 
  Map<dynamic, dynamic> bounding_boxes;

  ImageWidget({ this.path, this.bounding_boxes, this.color});
  
  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      builder: (context, snap) {
        if (snap.connectionState == ConnectionState.none || snap.connectionState == ConnectionState.waiting ||
            snap.data == null || snap.hasData == null) {
          print('project snapshot data is: ${snap.data}');
          return Container(
            color: Colors.black,
            height: 50,
            width: 50,
          );
        } else {
          print("snappppppppp    " + snap.data.toString());
          return snap.data;
        }
      },
      future: preprocessing(),
    );
  }

  Future<Widget> preprocessing() async {
    /*var result = await PhotoManager.requestPermission();
    print(result);*/
    print("dakhla");
    //print(this.path);
    Directory appDocDir = await getApplicationDocumentsDirectory();
    String appDocPath = appDocDir.path;
    print(appDocPath);
    var fileName = basename(this.path);
    img.Image photo ; String imagePath = "${appDocPath}/${fileName}photo5.jpg";
    try {
      print("begin");
      photo = img.decodeImage(File(this.path).readAsBytesSync());
      print("${photo.height} width ${photo.width}");
      //photo = img.gaussianBlur(photo, 20);
     // photo = img.copyResize(photo, width: 300, height: 300);
      photo = Utils.resizeImage(photo);
      print("${photo.height} width ${photo.width}");
      print(bounding_boxes);
      for ( List box in bounding_boxes.values){
        photo = Utils.drawRectangle(photo, box, img.getColor(255, 0, 0));
      }
      /*photo = img.drawLine(photo, 0, 0, 300, 240, img.getColor(255, 0, 0),
          thickness: 5);*/
      print("end1");
      
      File(imagePath).writeAsBytesSync(img.encodeJpg(photo));
      print("end2");
    } on Exception catch (e) {
      print(e.toString());
    }
    return  Image.file(File("${imagePath}"));
  }
}
