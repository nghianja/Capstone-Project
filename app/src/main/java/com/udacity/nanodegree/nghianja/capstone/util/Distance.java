package com.udacity.nanodegree.nghianja.capstone.util;

/**
 * Utility class of static methods for calculating distance between points.
 *
 * References:
 * [1] https://www.codingame.com/training/easy/defibrillators/solution?id=4522809
 * [2] http://stackoverflow.com/questions/27928/calculate-distance-between-two-latitude-longitude-points-haversine-formula
 */

public class Distance {

    public static final double EARTH_RAD = 6371;

    public static double getDistance(double latA, double lonA, double latB, double lonB) {
        double rLatA = deg2rad(latA);
        double rLonA = deg2rad(lonA);
        double rLatB = deg2rad(latB);
        double rLonB = deg2rad(lonB);

        double x = (rLonB - rLonA) * Math.cos((rLatA + rLatB) / 2);
        double y = (rLatB - rLatA);
        double d = Math.sqrt(x * x + y * y) * EARTH_RAD;
        return d;
    }

    private static double deg2rad(double deg) {
        return deg * (Math.PI/180);
    }

}
