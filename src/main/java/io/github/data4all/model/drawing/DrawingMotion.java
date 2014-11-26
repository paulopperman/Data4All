package io.github.data4all.model.drawing;

import java.util.ArrayList;
import java.util.List;

import android.graphics.PointF;

/**
 * The DrawingMotion stores the path of a motion and provides methods to
 * determine the behavior to the motion<br/>
 * <br/>
 * It is used by the painting component to store the user input<br/>
 * Also its used by the MotionInterpreters to interpret the user input
 * 
 * An activity can easily implement the usage of DrawingMotion by overwriting
 * the onTouchEvent-method as following:
 * 
 * <pre>
 *  {@code
 * public boolean onTouchEvent(MotionEvent event) {
 *     int action = event.getAction();
 *     if (action == MotionEvent.ACTION_DOWN) {
 *         currentMotion = new DrawingMotion();
 *         currentMotion.addPoint(event.getX(), event.getY());
 *         return true;
 *     } else if (action == MotionEvent.ACTION_MOVE) {
 *         currentMotion.addPoint(event.getX(), event.getY());
 *         return true;
 *     } else if (action == MotionEvent.ACTION_UP) {
 *         // Do something with the finished motion
 *         return true;
 *     }
 *     return false;
 * }
 * </pre>
 * 
 * @author tbrose
 */
public class DrawingMotion {
	/**
	 * The default tolerance for a Point
	 */
	public static final float POINT_TOLERANCE = 5f;

	/**
	 * List of all added Points
	 */
	private List<PointF> points = new ArrayList<PointF>();

	/**
	 * Adds a Point to the DrawingMotion
	 * 
	 * @param x
	 *            the x value of the point
	 * @param y
	 *            the y value of the point
	 */
	public void addPoint(float x, float y) {
		points.add(new PointF(x, y));
	}

	/**
	 * Calculates if this DrawingMotion is a Path <br/>
	 * A DrawingMotion with zero entries is not a Path <br/>
	 * A DrawingMotion with more entries is a Path if it is not a point
	 * 
	 * @return true - if the motion has a path-size over zero and is not a point <br/>
	 *         false otherwise
	 * 
	 * @see DrawingMotion#isPoint()
	 * @see DrawingMotion#POINT_TOLERANCE
	 */
	public boolean isPath() {
		return !(getPathSize() == 0) && !isPoint();
	}

	/**
	 * Calculates if this DrawingMotion is a Point <br/>
	 * A DrawingMotion with zero entries is not a Point <br/>
	 * A DrawingMotion with more entries is a Point, if all the Points describes
	 * a spot on the screen with at least {@link DrawingMotion#POINT_TOLERANCE
	 * POINT_TOLERANCE} difference from the start-point of the motion
	 * 
	 * @return true - if all Points in the motion are on the given tolerance
	 *         spot around the starting point <br/>
	 *         false otherwise
	 * 
	 * @see DrawingMotion#POINT_TOLERANCE
	 */
	public boolean isPoint() {
		if (getPathSize() == 0) {
			return false;
		}
		for (PointF p : points) {
			if (delta(getStart(), p) > POINT_TOLERANCE) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the first point of this DrawingMotion if there is at least one
	 * point in this motion
	 * 
	 * @return the first point of the motion or null if there is no point in the
	 *         motion
	 */
	public PointF getStart() {
		return points.isEmpty() ? null : points.get(0);
	}

	/**
	 * Returns the last point of this DrawingMotion if there is at least one
	 * point in this motion
	 * 
	 * @return the last point of the motion or null if there is no point in the
	 *         motion
	 */
	public PointF getEnd() {
		return points.isEmpty() ? null : points.get(points.size() - 1);
	}

	/**
	 * Returns the number of points in this DrawingMotion
	 * 
	 * @return the number of points
	 */
	public int getPathSize() {
		return points.size();
	}

	/**
	 * Returns a copy of the points in this DrawingMotion
	 * 
	 * @return a copy of the points in this DrawingMotion
	 */
	public List<PointF> getPoints() {
		List<PointF> result = new ArrayList<PointF>(points.size());
		for (PointF p : points) {
			result.add(new PointF(p.x, p.y));
		}
		return result;
	}

	/**
	 * Returns a copy of the point at the given index
	 * 
	 * @return a copy of the point at the given index
	 */
	public PointF getPoint(int index) {
		if (index < 0 || index >= points.size()) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: "
					+ points.size());
		} else {
			PointF f = points.get(index);
			// f will never be null at this point
			return new PointF(f.x, f.y);
		}
	}

	/**
	 * Calculates the euclidean distance between point a and point b
	 * 
	 * @param a
	 *            the first point
	 * @param b
	 *            the second point
	 * @return the euclidean distance between point a and point b
	 */
	private static float delta(PointF a, PointF b) {
		return (float) Math.sqrt(Math.pow(a.x - b.x, 2)
				+ Math.pow(a.y - b.y, 2));
	}
}
