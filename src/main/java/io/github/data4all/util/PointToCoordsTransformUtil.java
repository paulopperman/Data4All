package io.github.data4all.util;

/**
 * This Class uses the Orientation of the Phone, the Pixel 
 * and the Camera-Parameters to calculate the Distance between 
 * the Phone and the Object in a 2D System.
 * 
 * @author burghardt
 * @version 0.1
 *
 */

import io.github.data4all.logger.Log;
import io.github.data4all.model.DeviceOrientation;
import io.github.data4all.model.data.Node;
import io.github.data4all.model.data.TransformationParamBean;
import io.github.data4all.model.drawing.Point;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;

public class PointToCoordsTransformUtil {
    static String TAG = "PointToWorldCoords";
    private int osmID = -1;
    private static int osmVersion = 1;
    private double height = 0;
    TransformationParamBean tps;
    DeviceOrientation deviceOrientation;

    public PointToCoordsTransformUtil() {
    }

    /**
     * Constructor, which some Data
     * 
     * @param tps
     * @param deviceOrientation
     */
    public PointToCoordsTransformUtil(TransformationParamBean tps,
            DeviceOrientation deviceOrientation) {
        this.tps = tps;
        this.deviceOrientation = deviceOrientation;
    }

    /**
     * Opens transform with saved informations
     * 
     * @param points
     * @return List of Nodes
     */
    public List<Node> transform(List<Point> points) {
        return transform(tps, deviceOrientation, points);
    }

    /**
     * transforms a List of Points in a List of GPS-coordinates
     * 
     * @param tps
     * @param deviceOrientation
     * @return List of Nodes
     */
    public List<Node> transform(TransformationParamBean tps,
            DeviceOrientation deviceOrientation, List<Point> points) {

        List<Node> nodes = new ArrayList<Node>();
        this.height = tps.getHeight();
        for (Point point : points) {
          //  Point point = new Point((tps.getPhotoHeight() - iter.getY() + 1),
            //        (tps.getPhotoWidth() - iter.getX() + 1));
            Log.d(TAG, "Point X:" + point.getX() + " Y: " + point.getY());
            Log.d(TAG,
                    "TPS-DATA pic height;width height" + tps.getPhotoHeight()
                            + " " + tps.getPhotoWidth() + " " + tps.getHeight());
            // first calculates local coordinates in meter
            double[] coord = calculateCoordFromPoint(tps, deviceOrientation,
                    point);
            Log.d(TAG, "Calculated local Coords:" + coord[0] + "  " + coord[1]);
            // transforms local coordinates in global GPS-coordinates
            Node node = calculateGPSPoint(tps.getLocation(), coord);

            Log.d(TAG,
                    "Calculated Lat / Lon:" + node.getLat() + "  "
                            + node.getLon());
            nodes.add(node);
        }
        return nodes;
    }

    /**
     * opens calculate4thPoint with saved information
     * 
     * @param points
     * @return
     */
    public Point calculate4thPoint(List<Point> points) {
        return calculate4thPoint(tps, deviceOrientation, points);
    }

    /**
     * calculates a 4th Point (for houses etc.) with 3 given Points
     * 
     * @param points
     * @return the 4th Point
     */
    public Point calculate4thPoint(TransformationParamBean tps,
            DeviceOrientation deviceOrientation, List<Point> points) {
        if (points.size() != 3) {
            return null;
        }
        List<double[]> coords = new ArrayList<double[]>();
        this.height = tps.getHeight();
        for (Point point : points) {
            coords.add(calculateCoordFromPoint(tps, deviceOrientation, point));
        }
        double[] coord = add4Point(coords);
        return calculatePointFromCoords(tps, deviceOrientation, coord);
    }

    /**
     * calculates local coordinates for a Point with the orientation of the
     * phone, the pixel and information of the camera
     * 
     * @param tps
     * @param deviceOrientation
     * @param point
     * @return coordinates in a local system
     */
    public double[] calculateCoordFromPoint(TransformationParamBean tps,
            DeviceOrientation deviceOrientation, Point point) {
        this.height = tps.getHeight();
        double azimuth = -deviceOrientation.getAzimuth();
        double pixelpitch = calculateAngleFromPixel(point.getX(),
                tps.getPhotoWidth(), tps.getCameraMaxPitchAngle());
        double pixelroll = -calculateAngleFromPixel(point.getY(),
                tps.getPhotoHeight(), tps.getCameraMaxRotationAngle());
        double pitch = deviceOrientation.getPitch();
        double roll = deviceOrientation.getRoll();      
        
        

        double[] vector = new double[3];
        // Without any rotation
        vector[2] = -1;
        vector[1] = Math.tan(pixelpitch);
        vector[0] = Math.tan(pixelroll);
        //rotate around X-Achse with pitch
        double[] vector2 = new double[3];
        vector2[0] = vector[0]; 
        vector2[1] = vector[1] * Math.cos(pitch) 
                - vector[2] * Math.sin(pitch);
        vector2[2] = vector[1] * Math.sin(pitch) 
                + vector[2] * Math.cos(pitch);
        //rotate around line through origin with pitch angle
        double[] finalVector = new double[3];
        finalVector[0] = vector2[0] * Math.cos(roll) 
                - vector2[1] * Math.sin(pitch) * Math.sin(roll) 
                + vector2[2] * Math.cos(pitch) * Math.sin(roll);       
        finalVector[1] = vector2[0] * Math.sin(roll) * Math.sin(pitch) 
                + vector2[1] * ( Math.cos(pitch) * Math.cos(pitch) * (1- Math.cos(roll)) + Math.cos(roll))
                + vector2[2] * Math.cos(pitch) * Math.sin(pitch) * (1- Math.cos(roll));
        finalVector[2] = - vector2[0] * Math.cos(pitch) * Math.sin(roll) 
                + vector2[1] * Math.sin(pitch) * Math.cos(pitch) * (1- Math.cos(roll)) 
                + vector2[2] * (Math.sin(pitch) * Math.sin(pitch) * (1-Math.cos(roll)) + Math.cos(roll));
        
        if(finalVector[2] >= 0){
            double[] fail = {0.0,0.0,-1};
            return fail;
        }
        
        double tempXX = finalVector[0] * (height / -finalVector[2]);
        double tempYY = finalVector[1] * (height / -finalVector[2]);
        double[] coord = new double[3];
        // Rotate Vector with Azimuth (Z is fix))
        coord[0] = ((tempXX * Math.cos(azimuth)) - (tempYY * Math.sin(azimuth)));
        coord[1] = ((tempXX * Math.sin(azimuth)) + (tempYY * Math.cos(azimuth)));
        coord[2] = 0;
        return coord;
    }

    /*
     * Calculates a Vector with the given Orientation. The Coordinate-System: y
     * = North , x = West , z = Earth-Center
     * 
     * @param orientation
     * 
     * @return
     * 
     * public double[] calculateCoordfromOrientation(double[] orientation){ /
     * //calculate fix Z with Pitch double z = Math.cos(orientation[1]);
     * 
     * //calculate temp.Y with Pitch double y = Math.sin(-orientation[1]);
     * double x; // if the pitch is Zero, it wouldn't be possible to calculate
     * the X-Variable if (orientation[1] != 0.0){ //calculate a temporary X with
     * fix Z and Roll x = (Math.tan(orientation[2]) * z); } else{ //calculate
     * new Z and X, Y is 0 x = Math.sin(orientation[2]); z =
     * Math.cos(orientation[2]); }
     * 
     * // Rotate Vector with Azimuth (Z is fix)) double[] vector = new
     * double[3]; vector[0] = ((x * Math.cos(orientation[0])) - (y *
     * Math.sin(orientation[0]))); vector[1] = ((x * Math.sin(orientation[0])) +
     * (y * Math.cos(orientation[0]))); vector[2] = z;
     * 
     * return vector; //calculate local coords without azimuth double z =
     * Math.cos(orientation[1]); double y = Math.sin(-orientation[1]); double
     * tempY = y * (height / z); double x = Math.sin(orientation[2]); z =
     * Math.cos(orientation[2]); double tempX = x * (height / z); double[] coord
     * = new double[3]; // Rotate Vector with Azimuth (Z is fix)) coord[0] =
     * ((tempX * Math.cos(orientation[0])) - (tempY *
     * Math.sin(orientation[0]))); coord[1] = ((tempX *
     * Math.sin(orientation[0])) + (tempY * Math.cos(orientation[0]))); coord[2]
     * = 0; return coord;
     * 
     * }
     */
    /*
     * @param orientation
     * 
     * @return coords in m in a System with (0,0) = Phoneposition; x = West/East
     * Axis and y = Norh/South Axis
     * 
     * public double[] calculate2dPoint(double[] vector){ double[] coord = new
     * double[3]; double z = height / vector[2]; coord[0] = vector[0] * z;
     * coord[1] = vector[1] * z; coord[2] = 0; Log.d(TAG,"Calculated X = " +
     * coord[0] + " and Y = " + coord[1]); return coord; }
     */

    /**
     * Calculates the fourth point in dependence of the first three points of
     * the given list
     * 
     * @param areaPoints
     *            A list with exact three points
     */
    private static double[] add4Point(List<double[]> coords) {
        double[] a = coords.get(0);
        double[] b = coords.get(1);
        double[] c = coords.get(2);
        double[] coord = new double[2];
        coord[0] = a[0] + (c[0] - b[0]);
        coord[1] = a[1] + (c[1] - b[1]);
        return coord;
    }

    /**
     * Calculates the Angle altered by the given Pixel
     * 
     * @param pixel
     * @param width
     * @param maxAngle
     * @param oldAngle
     * @return altered Angle
     */
    public double calculateAngleFromPixel(double pixel, double width,
            double maxAngle) {

        // Log.d(TAG, "Calculate Angle, OldAngle: " + oldAngle + " maxANgle: " +
        // maxAngle);
        
        if ((pixel - (width / 2)) == 0) {
            return 0;
        }
        double percent = pixel / width;
        percent = percent - 0.5;
        double z = Math.sin(maxAngle/2);
        double angle = Math.asin(z * percent);
        return angle;
        
    }

    /**
     * calculates
     * 
     * @param tps
     * @param deviceOrientation
     * @param coord
     * @return calculated Point
     */
    public Point calculatePointFromCoords(TransformationParamBean tps,
            DeviceOrientation deviceOrientation, double[] coord) {
        if (coord[2] == -1) {
            return null;
        }
        // rotates the vector with azimuth
        double x = ((coord[0] * Math.cos(deviceOrientation.getAzimuth())) - (coord[1] * Math
                .sin(deviceOrientation.getAzimuth())));
        double y = ((coord[0] * Math.sin(deviceOrientation.getAzimuth())) + (coord[1] * Math
                .cos(deviceOrientation.getAzimuth())));
        double h = tps.getHeight();
        double p = (x*x*y*y)/2;
        double root = Math.sqrt(p*p - (x*x*y*y)+h*h);
        double xx1 = Math.sqrt(p + root);
        double xx2 = p - root;
        double pitch = -Math.asin(y / xx1)+ deviceOrientation.getPitch(); 
        double rotation = -Math.asin(x / xx1) + deviceOrientation.getRoll();
   

        // calculates the multiplier of the Pixel
        double x2 = (pitch + (tps.getCameraMaxPitchAngle() / 2))
                / tps.getCameraMaxPitchAngle();
        double y2 = -((rotation - (tps.getCameraMaxRotationAngle() / 2)) / tps
                .getCameraMaxRotationAngle());

        // multiply with the Width and Height of the Photo
        float xx = (float) ((int) (x2 * tps.getPhotoWidth()));
        float yy = (float) ((int) (y2 * tps.getPhotoHeight()));
        Point point = new Point(xx, yy);
        return point;
    }

    /**
     * calculates GPS-Point from Coordinates in a local System and the given
     * Location
     * 
     * @param location
     * @param point
     * @return A Node with a GPS Point
     */
    public Node calculateGPSPoint(Location location, double[] coord) {
        double radius = 6371004.0;
        double lat = Math.toRadians(location.getLatitude());
        double lon = Math.toRadians(location.getLongitude());

        // calculate the Length of the current Latitude with the earth Radius
        double lonLength = radius * Math.cos(lat);
        lonLength = lonLength * 2 * Math.PI;
        // add to the current Latitude the distance of the coord
        double lon2 = lon + Math.toRadians((coord[0] * 360) / lonLength);
        /*
         * if (lat2 < (-Math.PI/2)){ lat2 += Math.PI; } if (lat2 > (Math.PI/2)){
         * lat2 -= Math.PI; }
         */

        // calculate the Length of the current Longitude with the earth Radius
        double latLength = radius * 2 * Math.PI;
        // add to the current Longitude the distance of the coord
        double lat2 = lat + Math.toRadians((coord[1] * 360) / latLength);
        /*
         * if (lon2 > (Math.PI/4)){ lon2 = (Math.PI/2) - lon2; } if (lon2 <
         * (-Math.PI/4)){ lon2 = -(Math.PI/2) + lon2; }
         */
        lat2 = Math.toDegrees(lat2);
        lon2 = Math.toDegrees(lon2);

        Node node = new Node(osmID, osmVersion, lat2, lon2);
        osmID--;
        return node;
    }

    /**
     * Calculates the given GPS-Point in a local System
     * 
     * @param location
     * @param node
     * @return coord[]
     */
    public double[] calculateCoordFromGPS(Location location, Node node) {
        double radius = 6371004.0;
        double lat = Math.toRadians(node.getLat() - location.getLatitude());
        double lon = Math.toRadians(node.getLon() - location.getLongitude());
        double localLat = Math.toRadians(location.getLatitude());

        // calculate the Length of the current Latitude with the earth Radius
        double latLength = radius * Math.cos(localLat);

        double[] coord = new double[3];
        coord[0] = latLength * lat;

        double lonLength = radius;
        coord[1] = lonLength * lon;
        coord[2] = 0;

        return coord;
    }

}
