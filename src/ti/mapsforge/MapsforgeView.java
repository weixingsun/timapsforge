package ti.mapsforge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUIView;
import org.appcelerator.titanium.TiBlob;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.util.TiRHelper;
import org.appcelerator.titanium.io.TiBaseFile;
import org.appcelerator.titanium.io.TiFileFactory;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.view.TiDrawableReference;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.Dimension;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.util.LatLongUtils;
import org.mapsforge.core.util.IOUtils;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.cache.FileSystemTileCache;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.FixedPixelCircle;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.overlay.Polygon;
import org.mapsforge.map.layer.overlay.Polyline;
import org.mapsforge.map.util.MapViewProjection;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.android.util.AndroidUtil;

import android.app.Activity;
import android.graphics.drawable.*;
import android.graphics.*;
import android.view.*;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.util.*;
import android.os.Build;

public class MapsforgeView extends TiUIView {
	
	private static final String TAG = "MapsforgeView";
	private static final String KEY_DEBUG = "debug";
	private static final String KEY_SCALEBAR = "scalebar";
	private static final String KEY_CENTER = "centerLatlng";
	private static final String KEY_ZOOMLEVEL = "zoomLevel";
	private static final String KEY_MINZOOM = "minZoom";
	private static final String KEY_MAXZOOM = "maxZoom";

    private static final int TIMEOUT_CONNECT = 5000;
    private static final int TIMEOUT_READ = 10000;
    
	private static boolean sDebug = true;
	private GraphicFactory mGraphicFactory;
    private MapViewProjection mp;
    private MapView mapView;
    private int down_x;
    private int down_y;
	private HashMap<String, Layer> mLayers = new HashMap<String, Layer>();
	private SparseArray<Layer> movableLayers = new SparseArray<Layer>();
	private Activity act;
	private static void debugMsg(String msg) {
		if (sDebug) {
			Log.d(TAG, msg);
		}
	}
    
	private static void infoMsg(String msg) {
        Log.i(TAG, msg);
	}
    //or implement GestureDetector.OnGestureListener,GestureDetector.OnDoubleTapListener
	final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
        @Override
        public void onLongPress(MotionEvent e) {
            LatLong ll = mp.fromPixels(e.getX(), e.getY());
			Point p = new Point((int)e.getX(), (int)e.getY());
            sendLongClick(ll, p);
        }
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            LatLong ll = mp.fromPixels(e.getX(), e.getY());
			Point p = new Point((int)e.getX(), (int)e.getY());
            sendClick(ll, p);
            return true;
        }
        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            super.onSingleTapUp(event);
            return true;
        }
        @Override
        public boolean onDown(MotionEvent event) {
            //super.onDown(event);
            return true;
        }
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			// Scrolling uses math based on the viewport (as opposed to math using pixels).
			// Pixel offset is the offset in screen pixels, while viewport offset is the offset within the current viewport. 
			//float viewportOffsetX = distanceX * mCurrentViewport.width() / mContentRect.width();
			//float viewportOffsetY = -distanceY * mCurrentViewport.height() / mContentRect.height();
			// Updates the viewport, refreshes the display. 
			//setViewportBottomLeft(mCurrentViewport.left+viewportOffsetX, mCurrentViewport.bottom+viewportOffsetY);
			//e1 	The first down motion event that started the scrolling.
			//e2 	The move motion event that triggered the current onScroll.
			//distanceX 	The distance along the X axis that has been scrolled since the last call to onScroll. This is NOT the distance between e1 and e2.
			//distanceY 	The distance along the Y axis that has been scrolled since the last call to onScroll. This is NOT the distance between e1 and e2.
			//zoomToSpan();//many times to zoomOut map
			return true;
		}
    });
	/*final ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));
            invalidate();
            return true;
        }
    }*/
	public MapsforgeView(TiViewProxy proxy) {
		super(proxy);
        this.act = proxy.getActivity();
        this.gestureDetector.setIsLongpressEnabled(true);
        //this.tiapp = TiApplication.getInstance();
		AndroidGraphicFactory.createInstance(proxy.getActivity().getApplication());
		mapView = new MapView(proxy.getActivity()){
			@Override
			public boolean onTouchEvent(MotionEvent event) {
                super.onTouchEvent(event);
				return gestureDetector.onTouchEvent(event);
			}
		};
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapView.getMapScaleBar().setVisible(true);
        mapView.getMapZoomControls().setZoomLevelMin((byte) 4);
        mapView.getMapZoomControls().setZoomLevelMax((byte) 20);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        this.mp=new MapViewProjection(mapView);
		this.mGraphicFactory = AndroidGraphicFactory.INSTANCE;
		setNativeView(mapView);
	}
	
	public void zoomToSpan() {
		infoMsg("zoomToSpan()");
		BoundingBox bb = this.mapView.getBoundingBox();
		Dimension dimension = this.mapView.getModel().mapViewDimension.getDimension();
		this.mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(
						bb.getCenterPoint(),
						LatLongUtils.zoomForBounds(dimension, bb, this.mapView.getModel().displayModel.getTileSize()))
		);
	}
	public void repaint(){
		//mapView.invalidate();
        //mapView.getOverlayController().redrawOverlays();
		//mapView.postInvalidate();
		mapView.getModel().mapViewPosition.setZoomLevelMin((byte) 00);
	}
	//public Point toPixels(LatLong in) {
	public org.mapsforge.core.model.Point toPixels(LatLong in){
		return mp.toPixels(in);
	}
    private void sendClick(LatLong ll,Point p){
        KrollDict data = new KrollDict();
        data.put("lat",ll.latitude);
        data.put("lng",ll.longitude);
        data.put("x",p.x);
        data.put("y",p.y);
        TiApplication.getInstance().fireAppEvent("clicked", data);
    }
    private void sendLongClick(LatLong ll,Point p){
        KrollDict data = new KrollDict(); 
        data.put("lat",ll.latitude);
        data.put("lng",ll.longitude);
        data.put("x",p.x);
        data.put("y",p.y);
        TiApplication.getInstance().fireAppEvent("longclicked", data);
    }
    
	public void destroy(){
		((MapView) getNativeView()).destroy();
		mLayers = null;
		mGraphicFactory = null;
	}
	
	@Override
	public void processProperties(KrollDict props) {
		super.processProperties(props);
				
		if (props.containsKey(KEY_DEBUG)) {
			//sDebug = props.getBoolean(KEY_DEBUG);
		}
		debugMsg("processProperties " + props);
		
		MapView mapView = (MapView) getNativeView();
		if (props.containsKey(KEY_SCALEBAR)) {
			mapView.getMapScaleBar().setVisible(props.getBoolean(KEY_SCALEBAR));
			debugMsg("scalebar set to " + (props.getBoolean(KEY_SCALEBAR) ? "visible" : "hidden"));
		}
		
		if (props.containsKey(KEY_MINZOOM)) {
			int zoom = props.getInt(KEY_MINZOOM);
			mapView.getModel().mapViewPosition.setZoomLevelMin((byte) zoom);
			debugMsg("Min zoom level for map view set to " + Integer.toString(zoom));
		}
		
		if (props.containsKey(KEY_MAXZOOM)) {
			int zoom = props.getInt(KEY_MAXZOOM);
			mapView.getModel().mapViewPosition.setZoomLevelMax((byte) zoom);
			debugMsg("Max zoom level for map view set to " + Integer.toString(zoom));
		}
		
		if (props.containsKey(KEY_CENTER)) {
			Object[] coords = (Object[]) props.get(KEY_CENTER);
			double lat = TiConvert.toDouble(coords[0]);
			double lon = TiConvert.toDouble(coords[1]);
			setCenter(lat, lon);
		}
		
		if (props.containsKey(KEY_ZOOMLEVEL)) {
			int zoomlevel = props.getInt(KEY_ZOOMLEVEL);
			setZoomLevel(zoomlevel);
		}
	}
	
	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue,
			KrollProxy proxy) {
		super.propertyChanged(key, oldValue, newValue, proxy);
	}
	
	public Layer getLayer(int id){
		return movableLayers.get(id);
	}
	/**
	 * Adds a tile layer using a FileSystemTileCache to the map view
	 * @param activity	Context of the map view.
	 * @param name	Identifier for the layer.
	 * @param url	URL to the tile source. Must have {z},{x} and {y} place holders.
	 * @param subdomains	Sub domains, if any, for the tile source.
	 * @param parallelrequests	Number of parallel requests the tile source can handle.
	 * @param maxzoom	Highest zoom level for the tile source.
	 * @param minzoom	Lowest zoom level for the tile source.
	 */
	public void addLayer(Activity activity, String name, String url, String[] subdomains,
			int parallelrequests, byte maxzoom, byte minzoom){
		MapView mapView = (MapView) getNativeView();
		GenericTileSource tileSource = new GenericTileSource(url, subdomains, parallelrequests, maxzoom, minzoom);
		TileDownloadLayer downloadLayer = new TileDownloadLayer(createTileCache(activity, name), mapView.getModel().mapViewPosition, tileSource, mGraphicFactory);
		mapView.getLayerManager().getLayers().add(downloadLayer);
		mLayers.put(name, downloadLayer);
		debugMsg("Added layer " + name + " with url " + url);
	}	
	public void addLayer(Activity activity, String name, String filePath){
		MapView mapView = (MapView) getNativeView();
        MapFile mapDataStore = new MapFile(getMapFile(filePath));
        TileCache tileCache = AndroidUtil.createTileCache(activity, name, mapView.getModel().displayModel.getTileSize(), 1f, 
                mapView.getModel().frameBufferModel.getOverdrawFactor());
        TileRendererLayer tileRendererLayer = new TileRendererLayer(tileCache, mapDataStore,
                mapView.getModel().mapViewPosition, false, true, AndroidGraphicFactory.INSTANCE);
        tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
        mapView.getLayerManager().getLayers().add(tileRendererLayer);
        /////
		//GenericTileSource tileSource = new GenericTileSource(url, subdomains, parallelrequests, maxzoom, minzoom);
		mLayers.put(name, tileRendererLayer);
		debugMsg("Added layer " + name + " with file " + filePath);
	}
    private File getMapFile(String path) {
        //String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(android.os.Environment.getExternalStorageDirectory(), path);
        return file;
    }
	/**
	 * Removes a specific layer from the map view.
	 * @param name	Layer identifier as specified in addLayer().
	 */
	public boolean removeLayer(String name) {
		Layer l = mLayers.get(name);
		if (l != null) {
			MapView mapView = (MapView) getNativeView();
			mapView.getLayerManager().getLayers().remove(l);
			mLayers.remove(name);
			return true;
		} else {
			Log.e(TAG, "Layer with name " + name + " could not be found!");
			return false;
		}
	}
    
    /**
     * Starts all tile layers.
     */
    public void startLayers() {
    	Iterator<Entry<String, Layer>> it = mLayers.entrySet().iterator();
    	while (it.hasNext()) {
    		Entry<String, Layer> pairs = (Entry<String, Layer>) it.next();
    		if (pairs.getValue() instanceof TileDownloadLayer) {
    			((TileDownloadLayer) pairs.getValue()).start();
				debugMsg("Started layer " + pairs.getKey());
    		}
    	}
    }
    
    /**
     * Starts a specific tile layer using its identifier.
     * @param name
     */
    public void startLayer(String name) {
    	Iterator<Entry<String, Layer>> it = mLayers.entrySet().iterator();
    	while (it.hasNext()) {
    		Entry<String, Layer> pairs = (Entry<String, Layer>) it.next();
    		if (pairs.getKey().equals(name)) {
    			if (pairs.getValue() instanceof TileDownloadLayer) {
    				((TileDownloadLayer) pairs.getValue()).start();
    				debugMsg("Started layer " + pairs.getKey());
    				return;
    			}
    		}
    	}
    	Log.e(TAG, "Could not find any layer named " + name + " to start!");
    }
    
    /**
     * Sets the center of the map view.
     * @param lat
     * @param lon
     */
    public void setCenter(double lat, double lon) {
		MapView mapView = (MapView) getNativeView();
    	mapView.getModel().mapViewPosition.setCenter(new LatLong(lat, lon));
		//debugMsg("Center for map view set to " + Double.toString(lat) + " " + Double.toString(lon));
    }
    /**
     * Move the center slowly to the map center.
     * @param lat
     * @param lon
     */
    public void animateTo(double lat, double lon) {
		MapView mapView = (MapView) getNativeView();
		mapView.getModel().mapViewPosition.animateTo(new LatLong(lat, lon));
    }
    /**
     * Sets the zoom level of the map view.
     * @param zoomlevel
     */
    public void setZoomLevel(int zoomlevel) {
		MapView mapView = (MapView) getNativeView();
    	mapView.getModel().mapViewPosition.setZoomLevel((byte) zoomlevel);
		debugMsg("Zoom level for map view set to " + Integer.toString(zoomlevel));
    }
    public int getZoomLevel() {
		MapView mapView = (MapView) getNativeView();
    	return mapView.getModel().mapViewPosition.getZoomLevel();
    }
    /**
     * Draws a polyline on the map view.
     * @param coordinates
     * @param color
     * @param strokeWidth
     * @return identifier for the object.
     */
    public int createPolyline(List<LatLong> coordinates, Color color, float strokeWidth) {
		Paint paintStroke = mGraphicFactory.createPaint();
		paintStroke.setStyle(Style.STROKE);
		paintStroke.setColor(color);
		paintStroke.setStrokeWidth(strokeWidth);

		Polyline pl = new Polyline(paintStroke,mGraphicFactory);
		pl.getLatLongs().addAll(coordinates);
		MapView mapView = (MapView) getNativeView();
		mapView.getLayerManager().getLayers().add(pl);
		mLayers.put(Integer.toString(pl.hashCode()), pl);
		mapView.getLayerManager().redrawLayers();
		
		return pl.hashCode();
    }
    
    /**
     * Draws a polygon on the map view.
     * @param coordinates
     * @param fillColor
     * @param strokeColor
     * @param strokeWidth
     * @return identifier for the object.
     */
    public int createPolygon(List<LatLong> coordinates, Color fillColor, Color strokeColor, float strokeWidth) {
    	Paint paintFill = mGraphicFactory.createPaint();
    	paintFill.setStyle(Style.FILL);
    	paintFill.setColor(fillColor);
    	
    	Paint paintStroke = mGraphicFactory.createPaint();
    	paintStroke.setStyle(Style.STROKE);
    	paintStroke.setColor(strokeColor);
    	paintStroke.setStrokeWidth(strokeWidth);
    	
    	Polygon pg = new Polygon(paintFill, paintStroke, mGraphicFactory);
    	pg.getLatLongs().addAll(coordinates);
		MapView mapView = (MapView) getNativeView();
    	mapView.getLayerManager().getLayers().add(pg);
    	mLayers.put(Integer.toString(pg.hashCode()), pg);
		mapView.getLayerManager().redrawLayers();

    	return pg.hashCode();
    }
    public boolean isInteger(String s) {
		try { 
			Integer.parseInt(s); 
		} catch(NumberFormatException e) { 
			return false; 
		} catch(NullPointerException e) {
			return false;
		}
		// only got here if we didn't return false
		return true;
	}
    /**
     * Puts a marker on the map view.
     * @param pos
     * @param iconPath	Must be a URL or file system path on the device(i.e '/sdcard/marker.png').
     * @param horizontalOffset
     * @param verticalOffset
     * @param iconWidth
     * @param iconHeight
     * @return identifier for the object.
     */
    public int createMarker(LatLong pos, String iconPath, int horizontalOffset, int verticalOffset, int iconWidth, int iconHeight) {
    	debugMsg("pos: "            +pos.toString()+", " +
    			"iconPath: "        +iconPath+", " +
    			"horizontalOffset: "+Integer.toString(horizontalOffset)+", " +
				"verticalOffset: "  +Integer.toString(verticalOffset)+", " +
				"iconWidth: "       +Integer.toString(iconWidth)+", " +
				"iconHeight: "      +Integer.toString(iconHeight));
    	Bitmap icon = null;
		if(isInteger(iconPath)){
			int iconResId = 0;
			try { 
				iconResId=Integer.parseInt(iconPath);
			} catch(Exception e) {}
			icon = AndroidGraphicFactory.convertToBitmap(act.getResources().getDrawable(iconResId));
		}else if(iconPath.startsWith("R")){
			String resourcePath = iconPath.substring(1);
			int id=0;
			try {
				id = TiRHelper.getResource(resourcePath);
			}catch(Exception e){
				Log.e(TAG, "Unable to create bitmap. No marker drawn.");
			}
			Log.i(TAG, "imageId="+id);
			icon = AndroidGraphicFactory.convertToBitmap(act.getResources().getDrawable(id));//BitmapFactory.decodeResource(null, id);
		}else{
			InputStream is = createInputStream(iconPath);
			if (is == null) {
				Log.e(TAG, "Unable to retrieve marker image from "+iconPath+". No marker drawn.");
				return -1;
			}
			try {
				icon = mGraphicFactory.createResourceBitmap(is, 0);
			} catch (IOException e) {
				Log.e(TAG, "Unable to create bitmap. No marker drawn.");
				Log.e(TAG, e.getMessage());
				return -1;
			} finally {
				IOUtils.closeQuietly(is);
			}
		}
		
		if ((iconWidth > 0 && iconHeight > 0) && (iconWidth != icon.getWidth() || iconHeight != icon.getHeight())) {
			icon = resizeBitmap(icon, iconWidth, iconHeight);
		}
		
		Marker m = new Marker(pos, icon, horizontalOffset, verticalOffset);
		MapView mapView = (MapView) getNativeView();
		mapView.getLayerManager().getLayers().add(m);
    	mLayers.put(Integer.toString(m.hashCode()), m);
		mapView.getLayerManager().redrawLayers();
		movableLayers.append(m.hashCode(),m);
    	return m.hashCode();
    }
    /*
    private Marker createMarker(LatLong pos, int res) {
       Drawable drawable = this.act.getResources().getDrawable(res);
        Bitmap bm = AndroidGraphicFactory.convertToBitmap(drawable);
		Marker m = new Marker(pos, bm, 0, 0);
		MapView mapView = (MapView) getNativeView();
		mapView.getLayerManager().getLayers().add(m);
    	mLayers.put(Integer.toString(m.hashCode()), m);
		mapView.getLayerManager().redrawLayers();
       return m; //new Marker(geoPoint, drawableToBitmap(drawable));
    }*/
    /**
     * Draws a circle on the map view.
     * @param latLong
     * @param radius	Note: The radius is in meters!
     * @param fillColor
     * @param strokeColor
     * @param strokeWidth
     * @return identifier for the object.
     */
    public int createCircle(LatLong latLong, int radius, Color fillColor, Color strokeColor, float strokeWidth) {
    	Paint paintFill = mGraphicFactory.createPaint();
    	paintFill.setColor(fillColor);
    	paintFill.setStyle(Style.FILL);
    	
    	Paint paintStroke = mGraphicFactory.createPaint();
    	paintStroke.setColor(strokeColor);
    	paintStroke.setStrokeWidth(strokeWidth);
    	paintStroke.setStyle(Style.STROKE);
    	
    	Circle c = new Circle(latLong, radius, paintFill, paintStroke);
		MapView mapView = (MapView) getNativeView();
    	mapView.getLayerManager().getLayers().add(c);
    	mLayers.put(Integer.toString(c.hashCode()), c);
		mapView.getLayerManager().redrawLayers();
		movableLayers.append(c.hashCode(),c);
    	return c.hashCode();
    }
    public int createRealCircle(LatLong latLong, int radius, int fillColor) {
    	Paint paintFill = mGraphicFactory.createPaint();
    	paintFill.setColor(fillColor);
    	paintFill.setStyle(Style.FILL);    	
    	Circle c = new Circle(latLong, radius, paintFill, null);
		MapView mapView = (MapView) getNativeView();
    	mapView.getLayerManager().getLayers().add(c);
    	mLayers.put(Integer.toString(c.hashCode()), c);
		mapView.getLayerManager().redrawLayers();
		movableLayers.append(c.hashCode(),c);
    	return c.hashCode();
    }
	
    public int createCircle(LatLong latLong, int radius, int fillColor) {
    	Paint paintFill = mGraphicFactory.createPaint();
    	paintFill.setColor(fillColor);
    	paintFill.setStyle(Style.FILL);    	
    	FixedPixelCircle c = new FixedPixelCircle(latLong, radius, paintFill, null,true);
		MapView mapView = (MapView) getNativeView();
    	mapView.getLayerManager().getLayers().add(c);
    	mLayers.put(Integer.toString(c.hashCode()), c);
		mapView.getLayerManager().redrawLayers();
		movableLayers.append(c.hashCode(),c);
    	return c.hashCode();
    }
    private TileCache createTileCache(Activity activity, String name) {
        String cacheDirectoryName = activity.getExternalCacheDir().getAbsolutePath() + File.separator + name;
        File cacheDirectory = new File(cacheDirectoryName);
        if (!cacheDirectory.exists()) {
                cacheDirectory.mkdir();
        }
        return new FileSystemTileCache(1024, cacheDirectory, mGraphicFactory);
    }
    
    private static URLConnection getURLConnection(URL url) throws IOException {
        URLConnection urlConnection = url.openConnection();
        urlConnection.setConnectTimeout(TIMEOUT_CONNECT);
        urlConnection.setReadTimeout(TIMEOUT_READ);
        return urlConnection;
    }
    
    private static InputStream createInputStream(String iconPath) {
    	InputStream is = null;
     	if (iconPath.startsWith("www")) {
    		iconPath = "http://" + iconPath;
    	}
    	if (iconPath.startsWith("http")) {
    		URL url = null;
			try {
				url = new URL(iconPath);
    			URLConnection conn = getURLConnection(url);
    			is = conn.getInputStream();
			} catch (MalformedURLException e1) {
				Log.e(TAG, "The URL is malformed.");
				Log.e(TAG, e1.getMessage());
				is = null;
			} catch (IOException e) {
    			Log.e(TAG, "Could not load file from " + url.toString());
    			Log.e(TAG, e.getMessage());
    			is = null;
    		}
    	} else {
    		//Should be a file system path
    		File f = new File(iconPath);
    		try {
    			is = new FileInputStream(f);
    		} catch (FileNotFoundException e) {
    			Log.e(TAG, "File not found.");
    			Log.e(TAG, e.getMessage());
    			is = null;
    		}
    	}
    	return is;
    }
    
    /*public InputStream getISfromResource(String imagePath){
        TiDrawableReference r = TiDrawableReference.fromUrl();
        return r.getInputStream();
    }
    
    public TiBlob loadImageFromApplication(String imageName) {
        infoMsg("image="+imageName);
		TiBlob result = null;
		try {
			// Load the image from the application assets
			String url = imageName;
			TiBaseFile file = TiFileFactory.createTitaniumFile( new String[] { url }, false);
			Bitmap bitmap = TiUIHelper.createBitmap(file.getInputStream());
			// The bitmap must be converted to a TiBlob before returning
			result = TiBlob.blobFromImage(bitmap);
		} catch (IOException e) {
			Log.e(TAG, " EXCEPTION - IO");
		}
		Log.d(TAG, " " + result);
		return result;
	}*/
    private static Bitmap resizeBitmap(Bitmap bitmap, int width, int height) {
		/*
		 * TODO Is this really the easiest way to resize a bitmap
		 *  when using mapsforge?
		 */
		debugMsg(String.format("Resizing bitmap from %dx%d to %dx%d", bitmap.getWidth(), bitmap.getHeight(), width, height));
		android.graphics.Bitmap aBitmap = AndroidGraphicFactory.getBitmap(bitmap);
		android.graphics.Bitmap scaled = android.graphics.Bitmap.createScaledBitmap(aBitmap, width, height, false);
		Drawable d = new BitmapDrawable(scaled);
		bitmap = AndroidGraphicFactory.convertToBitmap(d);
		//Bitmaps are evil, null references to ensure they get GC'd
		d = null;
		scaled = null;
		aBitmap = null;
		return bitmap;
    }
    public static android.graphics.Bitmap drawableToBitmap (Drawable drawable) {
        android.graphics.Bitmap bitmap = null;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = android.graphics.Bitmap.createBitmap(1, 1, android.graphics.Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = android.graphics.Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), android.graphics.Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
    
}