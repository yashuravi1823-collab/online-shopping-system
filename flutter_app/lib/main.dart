import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

// For Android emulator use: http://10.0.2.2:8080/api
// For a physical phone use your laptop's Wi-Fi IPv4 address, e.g.
// http://192.168.1.10:8080/api
const String API_BASE = 'http://10.0.2.2:8080/api';

void main() => runApp(const ShopApp());

class ShopApp extends StatelessWidget {
  const ShopApp({super.key});
  @override
  Widget build(BuildContext context) => MaterialApp(
    debugShowCheckedModeBanner: false,
    title: 'ShopEasy',
    theme: ThemeData(colorSchemeSeed: Colors.deepPurple, useMaterial3: true),
    home: const LoginPage(),
  );
}

class LoginPage extends StatefulWidget {
  const LoginPage({super.key});
  @override State<LoginPage> createState() => _LoginPageState();
}
class _LoginPageState extends State<LoginPage> {
  final email=TextEditingController(), password=TextEditingController();
  bool loading=false;
  Future<void> login() async {
    setState(()=>loading=true);
    final r=await http.post(Uri.parse('$API_BASE/login'),body:{
      'email':email.text,'password':password.text});
    setState(()=>loading=false);
    final d=jsonDecode(r.body);
    if(!mounted)return;
    if(d['success']==true) {
      Navigator.pushReplacement(context,MaterialPageRoute(builder:(_)=>HomePage(user:d)));
    } else ScaffoldMessenger.of(context).showSnackBar(SnackBar(content:Text(d['message']??'Login failed')));
  }
  @override Widget build(BuildContext context)=>Scaffold(
    appBar:AppBar(title:const Text('ShopEasy Login')),
    body:Padding(padding:const EdgeInsets.all(20),child:Column(
      mainAxisAlignment:MainAxisAlignment.center,
      children:[
        TextField(controller:email,decoration:const InputDecoration(labelText:'Email')),
        TextField(controller:password,obscureText:true,decoration:const InputDecoration(labelText:'Password')),
        const SizedBox(height:15),
        FilledButton(onPressed:loading?null:login,child:Text(loading?'Please wait':'Login')),
        const SizedBox(height:8),
        const Text('Register on the web version for this starter project.')
      ])));
}

class HomePage extends StatefulWidget {
  final Map<String,dynamic> user;
  const HomePage({super.key,required this.user});
  @override State<HomePage> createState()=>_HomePageState();
}
class _HomePageState extends State<HomePage>{
  List products=[]; List cart=[]; List orders=[];
  @override void initState(){super.initState();load();}
  Future<void> load() async{
    final p=await http.get(Uri.parse('$API_BASE/products'));
    final o=await http.get(Uri.parse('$API_BASE/orders?userId=${widget.user['userId']}'));
    setState((){products=jsonDecode(p.body);orders=jsonDecode(o.body);});
  }
  Future<void> add(dynamic p) async {
    final i=cart.indexWhere((x)=>x['id']==p['id']);
    if(i>=0) cart[i]['qty']++; else cart.add({...p,'qty':1});
    setState((){});
  }
  Future<void> checkout() async{
    if(cart.isEmpty){msg('Cart is empty');return;}
    String? mode=await showDialog<String>(context:context,builder:(c)=>SimpleDialog(
      title:const Text('Select payment mode'),
      children:['COD','UPI','CARD'].map((x)=>SimpleDialogOption(
        onPressed:()=>Navigator.pop(c,x),child:Text(x))).toList()));
    if(mode==null)return;
    final items=cart.map((x)=>'${x['id']}:${x['qty']}').join(',');
    final r=await http.post(Uri.parse('$API_BASE/order/create'),body:{
      'userId':'${widget.user['userId']}','paymentMode':mode,'items':items});
    final d=jsonDecode(r.body);
    msg(d['success']==true?'Order #${d['orderId']} placed. Delivery by ${d['expectedDelivery']}':d['message']??'Error');
    if(d['success']==true){cart.clear();load();}
  }
  Future<void> cancel(int id) async{
    final r=await http.post(Uri.parse('$API_BASE/order/cancel'),body:{
      'orderId':'$id','userId':'${widget.user['userId']}'});
    final d=jsonDecode(r.body);msg(d['message']??'');
    load();
  }
  void msg(String s)=>ScaffoldMessenger.of(context).showSnackBar(SnackBar(content:Text(s)));
  @override Widget build(BuildContext context)=>Scaffold(
    appBar:AppBar(title:Text('ShopEasy - ${widget.user['name']}')),
    body:ListView(
      padding:const EdgeInsets.all(12),
      children:[
        const Text('Products',style:TextStyle(fontSize:22,fontWeight:FontWeight.bold)),
        ...products.map((p)=>Card(child:ListTile(
          title:Text(p['name']),subtitle:Text('₹${p['price']}\\n${p['description']}'),
          trailing:FilledButton(onPressed:()=>add(p),child:const Text('Add'))))),
        const Divider(),
        Text('Cart: ${cart.length} product(s)',style:const TextStyle(fontSize:20,fontWeight:FontWeight.bold)),
        ...cart.map((x)=>Text('${x['name']} × ${x['qty']}')),
        FilledButton(onPressed:checkout,child:const Text('Checkout')),
        const Divider(),
        const Text('My Orders',style:TextStyle(fontSize:22,fontWeight:FontWeight.bold)),
        ...orders.map((o)=>Card(child:ListTile(
          title:Text('Order #${o['id']} — ${o['status']}'),
          subtitle:Text('₹${o['totalAmount']} | ${o['paymentMode']}\\nDelivery: ${o['expectedDelivery']}'),
          trailing:['PLACED','SHIPPED','OUT_FOR_DELIVERY'].contains(o['status'])
            ?TextButton(onPressed:()=>cancel(o['id']),child:const Text('Cancel')):null))),
      ]));
}
