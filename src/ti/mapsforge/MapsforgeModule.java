package ti.mapsforge;

import org.appcelerator.kroll.*;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.*;
import org.appcelerator.titanium.util.*;

import com.graphhopper.*;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.util.*;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.*;
import org.mapsforge.core.model.LatLong;

import android.os.AsyncTask;
import android.app.Activity;
import java.util.*;
import org.json.*;

@Kroll.module(name="Mapsforge", id="ti.mapsforge")
public class MapsforgeModule extends KrollModule {
	private static final String TAG = "MapsforgeModule";
	private GraphHopper hopper;
	private InstructionList instructionList;
	public MapsforgeModule(){
		super();
	}
	@Kroll.method
	public void load(final String path) {
		//EncodingManager manager = new EncodingManager("car");//CAR,BIKE,FOOT
		//.setEncodingManager(manager); //.setCHEnable(false).setStoreOnFlush(false).load(file);
		//GraphStorage graph=new GraphBuilder(manager).create();
		new Thread(){
			@Override
			public void run(){
				hopper = new GraphHopper().forMobile()
				.setCHEnable(false);
				//.setWayPointMaxDistance(5).setEnableInstructions(true).setCHWeighting("");
				//set following two params in config.properties when ./graphhopper.sh import *.pbf
				//prepare.chWeighting=no 
				//graph.flagEncoders=car|turnCosts=true,bike,foot
				hopper.setEncodingManager(new EncodingManager("car|turnCosts=true,bike,foot"));
				hopper.load(android.os.Environment.getExternalStorageDirectory()+"/"+path);
			}
		}.start();
	}
	public GHResponse getRoute(KrollDict args){
		String[] from = args.getStringArray("from");
		String[] to   = args.getStringArray("to");
		//String weighting = args.containsKeyAndNotNull("weighting")?args.getString("weighting"):"";
		String vehicle   = args.containsKeyAndNotNull("vehicle")?args.getString("vehicle"):"car";
		String algorithm = args.containsKeyAndNotNull("algorithm")?args.getString("algorithm"):AlgorithmOptions.DIJKSTRA_BI;
		if(from==null||to==null) return null;
		Log.i(TAG, "GH:"+vehicle+",");
		GHRequest req = new GHRequest(Double.parseDouble(from[0]), Double.parseDouble(from[1]), Double.parseDouble(to[0]), Double.parseDouble(to[1]))
			.setAlgorithm(algorithm)	//"dijkstrabi/dijkstra/dijkstraOneToMany/astar/astarbi"
			//.setWeighting(weighting)	//"fastest/shortest"
			.setVehicle(vehicle)	//"car/bike/foot"
			.setLocale(Locale.US);	//"Locale.US"
		//req.getHints().put("instructions", "false");
		return hopper.route(req);
	}
	public String getErrMsg(List<Throwable> errors){
		String ret="";
		for(Throwable err :errors){
			ret+=err.getMessage()+", ";
		}
		return ret;
	}
	@Kroll.method
	public void getRouteAsyncCallback(final KrollDict args, final KrollFunction callback){
		new Thread(){
			@Override
			public void run(){
				KrollDict data = new KrollDict();
				GHResponse resp = getRoute(args);
				if(resp.hasErrors()){
				    data.put("error",1);
					data.put("errmsg",getErrMsg(resp.getErrors()));
					return;
				}else{
				    data.put("error",0);
				}
				data.put("distance",resp.getDistance());	//meter
				data.put("time",	resp.getTime());	//millis
				PointList pl = resp.getPoints();
				TiConvert.putInKrollDict(data, "pts", pl.toGeoJson().toArray());
				//print("global_points",data.get("pts"));
				instructionList = resp.getInstructions();
				data.put("nodes",getNodes(instructionList));
				callback.call(getKrollObject(),data);
			}
		}.start();
	}
	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app){
		// put module init code that needs to run when the application is created
	}
	@Override
	public void onResume(Activity activity) {
		super.onResume(activity);
	}
	@Override
	public void onDestroy(Activity activity) {
		super.onDestroy(activity);
		hopper=null;
	}
	private KrollDict[] getNodes(InstructionList il){
		List<KrollDict> list = new ArrayList<KrollDict>();
		for(int i=0;i<il.getSize();i++){
			KrollDict map = new KrollDict();
			Instruction in = il.get(i);
			int sign = in.getSign();
			String name = in.getName();
			double dist = in.getDistance();
			long time = in.getTime();
			PointList pl = in.getPoints();
			//print("getNodes().PointList=",pl);
			TiConvert.putInKrollDict(map, "pts", pl.toGeoJson().toArray());
    		//print("InstructionList",map.get("pts"));
			map.put("sign", sign);
			map.put("name", name);
			map.put("dist", dist);
			map.put("time", time);
			//annotation 
			//kd.put("extra", in.getExtraInfoJSON());
			list.add(map);
		}
		KrollDict[] temp = new KrollDict[list.size()];
		return list.toArray(temp);
	}
	private void print(String prefix,Object obj){
		if(obj instanceof KrollDict){
			KrollDict kd = (KrollDict)obj;
			Log.i(TAG, prefix+"  "+kd.toString());
			/*
			for (Set name: kd.keySet()){
				String key =name.toString();
				String value = kd.get(name).toString();  
				System.out.println();  
				Log.i(TAG, prefix+" k:v= "+key + ":" + value);
			}*/
		}else{
			Log.i(TAG, prefix+"  "+obj.toString());
		}
	}
	@Kroll.method
	public boolean isInStep(KrollDict args){
		String  point = args.getString("point");
		String points = args.getString("points");
		int range = args.getInt("range");
		//point=[-43.52412333,172.58391931] 
		//points=[[172.584333,-43.523472],[172.584716,-43.523578],[172.585172,-43.523802],[172.585273,-43.523852],[172.585415,-43.523923]]
		LatLong p1 = null;
		List<LatLong> pList = new ArrayList<LatLong>();
		try{
			JSONArray jsonP = new JSONArray(point);
			JSONArray jsonPoints = new JSONArray(points);
			double lat1 = jsonP.getDouble(0);
			double lng1 = jsonP.getDouble(1);
			p1 = new LatLong(lat1,lng1);
			//JSONArray jsonPoints = jsonPoints.getJSONArray("dataArray");
			for (int i = 0 ; i < jsonPoints.length(); i++) {
				JSONArray obj = jsonPoints.getJSONArray(i);
				double lng = obj.getDouble(0);
				double lat = obj.getDouble(1);
				pList.add(new LatLong(lat,lng));
			}
		}catch(Exception e){
			Log.e(TAG, e.getMessage()+Arrays.toString(e.getStackTrace()));
		}
		//Log.i("PolyUtil", "p1("+p1+")"+"poly.size:"+pList.size());
		return PolyUtil.isLocationOnPath(p1, pList, false,range);
	}
	public Instruction find( double lat, double lon, double maxDistance ){
		if(instructionList==null) return null;
		return null;
	}

}