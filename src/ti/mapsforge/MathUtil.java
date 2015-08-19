package ti.mapsforge;

/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import static java.lang.Math.*;
import android.util.Log;

/**
 * Utility functions that are used my both PolyUtil and SphericalUtil.
 */
public class MathUtil {
    /**
     * The earth's radius, in meters.
     * Mean radius as defined by IUGG.
     */
    public static final double EARTH_RADIUS = 6371009;
	private static final String tag = MathUtil.class.getSimpleName();

    /**
     * Restrict x to the range [low, high].
     */
    public static double clamp(double x, double low, double high) {
        return x < low ? low : (x > high ? high : x);
    }
    
    /**
     * Wraps the given value into the inclusive-exclusive interval between min and max.
     * @param n   The value to wrap.
     * @param min The minimum.
     * @param max The maximum.
     */
    public static double wrap(double n, double min, double max) {
        return (n >= min && n < max) ? n : (mod(n - min, max - min) + min);
    }

    /**
     * Returns the non-negative remainder of x / m.
     * @param x The operand.
     * @param m The modulus.
     */
    static double mod(double x, double m) {
        return ((x % m) + m) % m;
    }

    /**
     * Returns mercator Y corresponding to latitude.
     * See http://en.wikipedia.org/wiki/Mercator_projection .
     */
    public static double mercator(double lat) {
        return log(tan(lat * 0.5 + PI/4));
    }

    /**
     * Returns latitude from mercator Y.
     */
    public static double inverseMercator(double y) {
        return 2 * atan(exp(y)) - PI / 2;
    }
    
    /**
     * Returns haversine(angle-in-radians).
     * hav(x) == (1 - cos(x)) / 2 == sin(x / 2)^2.
     */
    public static double hav(double x) {
        double sinHalf = sin(x * 0.5);
        return sinHalf * sinHalf;
    }

    /**
     * Computes inverse haversine. Has good numerical stability around 0.
     * arcHav(x) == acos(1 - 2 * x) == 2 * asin(sqrt(x)).
     * The argument must be in [0, 1], and the result is positive.
     */
    static double arcHav(double x) {
        return 2 * asin(sqrt(x));
    }
    
    // Given h==hav(x), returns sin(abs(x)).
    public static double sinFromHav(double h) {
        return 2 * sqrt(h * (1 - h));
    }

    // Returns hav(asin(x)).
    public static double havFromSin(double x) {
        double x2 = x * x;
        return x2 / (1 + sqrt(1 - x2)) * .5;
    }

    // Returns sin(arcHav(x) + arcHav(y)).
    public static double sinSumFromHav(double x, double y) {
        double a = sqrt(x * (1 - x));
        double b = sqrt(y * (1 - y));
        return 2 * (a + b - 2 * (a * y + b * x));
    }

    /**
     * Returns hav() of distance from (lat1, lng1) to (lat2, lng2) on the unit sphere.
     */
    public static double havDistance(double lat1, double lat2, double dLng) {
        return hav(lat1 - lat2) + hav(dLng) * cos(lat1) * cos(lat2);
    }
    
    public static boolean compare(double a, double b){
    	double epsilon = 0.0000000000001;
	    if(abs(a - b) < epsilon){
	    	return true;
	    }
	    return false;
    }

	public static boolean compare(org.mapsforge.core.model.LatLong a,
			org.mapsforge.core.model.LatLong b) {
    	if(compare(a.latitude,b.latitude) && compare(a.longitude,b.longitude)){
	    	return true;
	    }
		return false;
	}
}
