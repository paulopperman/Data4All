/*
 * Copyright (c) 2014, 2015 Data4All
 * 
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 * 
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.data4all.view;

import io.github.data4all.R;
import io.github.data4all.logger.Log;
import io.github.data4all.model.data.AbstractDataElement;
import io.github.data4all.model.drawing.AreaMotionInterpreter;
import io.github.data4all.model.drawing.BuildingMotionInterpreter;
import io.github.data4all.model.drawing.DrawingMotion;
import io.github.data4all.model.drawing.MotionInterpreter;
import io.github.data4all.model.drawing.Point;
import io.github.data4all.model.drawing.PointMotionInterpreter;
import io.github.data4all.model.drawing.RedoUndo;
import io.github.data4all.model.drawing.RedoUndo.UndoRedoListener;
import io.github.data4all.model.drawing.WayMotionInterpreter;
import io.github.data4all.util.PointToCoordsTransformUtil;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * This TouchView listen to MotionEvents from the user, saves them into
 * DrawingMotions, uses MotionInterpreters to interpret the input and draws the
 * interpreted polygons.<br/>
 * The shown polygon can be modified via deletion, {@link PointMover} or
 * {@link io.github.data4all.model.drawing.RedoUndo UndoRedo} and obtained as a
 * {@link AbstractDataElement}.
 * 
 * 
 * @author tbrose
 *
 * @see MotionEvent
 * @see DrawingMotion
 * @see MotionInterpreter
 * @see UndoRedoListener
 * @see PointToCoordsTransformUtil
 * @see AbstractDataElement
 * @see PointMover
 */
public class TouchView extends View {

    /**
     * All types of interpretation that are provided by this {@link TouchView}.
     * 
     * @author tbrose
     */
    public static enum InterpretationType {
        AREA, POINT, BUILDING, WAY;
    }

    /**
     * Mover to move a point
     */
    private PointMover mover;

    /**
     * Point to know where the moving point started
     */
    private Point startPoint;

    /**
     * Boolean to check if the move motion could be a delete motion
     */
    private boolean isDelete;

    /**
     * Point to set if the current start motion found a point
     */
    private Point lookUpPoint;

    /**
     * The paint to draw the path with.
     */
    private final Paint pathPaint = new Paint();

    /**
     * The paint to draw the area with.
     */
    private Paint areaPaint = new Paint();

    /**
     * The path to draw.
     */
    private final Path path = new Path();
    /**
     * The motion interpreted Polygon.
     */
    private List<Point> polygon = new ArrayList<Point>();
    /**
     * The Polygon with the current pending motion.
     */
    private List<Point> newPolygon = new ArrayList<Point>();
    /**
     * The current motion the user is typing via the screen.
     */
    private DrawingMotion currentMotion;
    /**
     * An object for the calculation of the point transformation.
     */
    private PointToCoordsTransformUtil pointTrans;
    /**
     * The currently used interpreter.
     */
    private MotionInterpreter interpreter;
    /**
     * The current used RedoUndo object.
     */
    private RedoUndo redoUndo;

    /**
     * is the polygon activated
     */
    private Boolean polygonclicked = false;

    private float downX = 0;
    private float downY = 0;

    /**
     * The drawing size of a point (calculating with the density for same result
     * on each device)
     */
    private int pointRadius = (int) (getPointsize() * getResources()
            .getDisplayMetrics().density);

    /**
     * The current used RedoUndo listener.
     */
    private UndoRedoListener undoRedoListener;

    private CaptureAssistView cameraAssistView;
    /**
     * Standard strings for actions
     */
    final static String add = "ADD";
    final static String delete = "DELETE";
    final static String moveFrom = "MOVE_FROM";
    final static String moveTo = "MOVE_TO";

    /**
     * Simple constructor to use when creating a view from code.
     * 
     * @param context
     *            The Context the view is running in, through which it can
     *            access the current theme, resources, etc.
     */
    public TouchView(Context context) {
        super(context);
    }

    /**
     * Constructor that is called when inflating a view from XML. This is called
     * when a view is being constructed from an XML file, supplying attributes
     * that were specified in the XML file. This version uses a default style of
     * 0, so the only attribute values applied are those in the Context's Theme
     * and the given AttributeSet.<br/>
     * <br/>
     * The method onFinishInflate() will be called after all children have been
     * added.
     * 
     * @param context
     *            The Context the view is running in, through which it can
     *            access the current theme, resources, etc.
     * @param attrs
     *            The attributes of the XML tag that is inflating the view.
     */
    public TouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Perform inflation from XML and apply a class-specific base style from a
     * theme attribute. This constructor of View allows subclasses to use their
     * own base style when they are inflating. For example, a Button class's
     * constructor would call this version of the super class constructor and
     * supply {@code R.attr.buttonStyle} for defStyleAttr; this allows the
     * theme's button style to modify all of the base view attributes (in
     * particular its background) as well as the Button class's attributes.
     * 
     * @param context
     *            The Context the view is running in, through which it can
     *            access the current theme, resources, etc.
     * @param attrs
     *            The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr
     *            An attribute in the current theme that contains a reference to
     *            a style resource that supplies default values for the view.
     *            Can be 0 to not look for defaults.
     */
    public TouchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawARGB(0, 0, 0, 0);

        path.reset();
        if (newPolygon != null && newPolygon.size() != 0) {
            path.moveTo(newPolygon.get(0).getX(), newPolygon.get(0).getY());
            // first draw all lines
            int limit = newPolygon.size();
            // Don't draw the last line if it is not an area
            limit -= interpreter.isArea() ? 0 : 1;
            for (int i = 0; i < limit; i++) {
                // The next point in the polygon
                final Point b = newPolygon.get((i + 1) % newPolygon.size());
                final Point a = newPolygon.get(i);
                path.lineTo(a.getX(), a.getY());
                path.lineTo(b.getX(), b.getY());
                canvas.drawLine(a.getX(), a.getY(), b.getX(), b.getY(),
                        pathPaint);
            }
            if (interpreter instanceof AreaMotionInterpreter
                    || interpreter instanceof BuildingMotionInterpreter) {
                canvas.drawPath(path, areaPaint);
            }
            // afterwards draw the points
            for (Point p : newPolygon) {
                canvas.drawCircle(p.getX(), p.getY(), pointRadius, pathPaint);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View#onTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        if (!(cameraAssistView.overHorizont(new Point(event.getX(), event
                .getY())))) {
            switch (action) {
            case MotionEvent.ACTION_DOWN:
                currentMotion = new DrawingMotion();
                this.handleMotion(event, "start");
                break;
            case MotionEvent.ACTION_UP:
                this.handleMotion(event, "end");
                polygon = newPolygon;
                this.undoUseable();
                this.redoUseable();
                this.undoRedoListener.okUseable(hasEnoughNodes());
                break;
            case MotionEvent.ACTION_MOVE:
                this.handleMotion(event, "move");
                postInvalidate();
                break;
            default:
                Log.e(this.getClass().getSimpleName(), "ERROR, no event found!");
            }
        }
        return true;
    }

    /**
     * Handles the given motion:<br/>
     * Add the point to the current motion<br/>
     * Logs the motion<br/>
     * Apple to move and delete points in the motion<br/>
     * Causes the view to redraw itself afterwards
     * 
     * @param event
     *            The touch event
     * @param action
     *            the named action which is in progress
     */
    private void handleMotion(MotionEvent event, String action) {
        if (currentMotion != null) {

            downX = event.getX();
            downY = event.getY();
            if (action.equals("start")) {

                // longClickHandler.postDelayed(mLongPressed, 1000);

                this.lookUpPoint = lookUp(event.getX(), event.getY(), 50);
                if (lookUpPoint != null) {
                    // longClickHandler.removeCallbacks(mLongPressed);
                    // Log.d("", "Long Press cancled because there is a point");

                    this.mover = movePoint(lookUpPoint);
                    startPoint = lookUpPoint;
                    lookUpPoint = null;
                    if (!polygonclicked) {
                        isDelete = true;
                    }
                } else {

                    startPoint = new Point(downX, downY);
                    // if (insidePolygon(startPoint) == false) {
                    // polygonclicked(false);

                    if (!polygonclicked) {
                        if (!insidePolygon(startPoint)) {
                            currentMotion.addPoint(event.getX(), event.getY());
                            newPolygon = interpreter.interprete(polygon,
                                    currentMotion);
                        }
                        // } else {
                        // polygonclicked(true);
                    }

                    // }
                }
            } else if (action.equals("move")) {

                float offsetX = startPoint.getX() - event.getX();
                float offsetY = startPoint.getY() - event.getY();

                Log.d("", "polygonclicked: " + polygonclicked);
                if (polygonclicked) {

                    // if (Math.abs(offsetX) > 15 || Math.abs(offsetY) > 15) {
                    movePolygon(offsetX, offsetY);
                    startPoint = new Point(downX, downY);
                    // }

                } else if (mover != null) {
                    if (Math.abs(offsetX) > 10 && Math.abs(offsetY) > 10
                            && polygonclicked == false) {
                        // handler.removeCallbacks(mLongPressed);
                        // Log.d("", "Long Press cancled MOVE");
                        mover.moveTo(event.getX(), event.getY());
                        isDelete = false;
                        redoUndo.add(new Point(event.getX(), event.getY()),
                                moveTo, mover.getIdx());
                    }
                } else {
                    if (Math.abs(offsetX) > 5 && Math.abs(offsetY) > 5) {
                        currentMotion.addPoint(event.getX(), event.getY());
                        newPolygon = interpreter.interprete(polygon,
                                currentMotion);
                    }
                }
            } else if (action.equals("end")) {
                // longClickHandler.removeCallbacks(mLongPressed);
                if (startPoint.getX() - event.getX() < 5
                        && startPoint.getY() - event.getY() < 5) {
                    polygonclicked(insidePolygon(startPoint));
                }

                // if (polygonclicked) {
                // longClickHandler.removeCallbacks(mLongPressed);
                // polygonclicked = false;
                //
                // areaPaint.setColor(MotionInterpreter.AREA_COLOR);
                // areaPaint.setAlpha(100);
                // Log.d("", "Long Press canceld END");
                if (isDelete) {
                    redoUndo.add(startPoint, delete, mover.getIdx());
                    this.deletePoint(startPoint);
                    mover = null;
                    isDelete = false;
                } else {
                    if (mover != null) {
                        mover = null;
                    } else {
                        redoUndo = new RedoUndo(newPolygon);
                    }
                }
                invalidate();
            } else {
                Log.e(this.getClass().getSimpleName(),
                        "ERROR, No action Found for: " + action);
            }
        }
    }

    /**
     * handles long clicks.
     * 
     * @author konerman
     * 
     */
    final Handler longClickHandler = new Handler();
    Runnable mLongPressed = new Runnable() {
        public void run() {
            Log.i("", "Long press!");
            if (insidePolygon(new Point(downX, downY))) {
                polygonclicked = true;
                Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setAlpha(100);
                areaPaint = paint;

                postInvalidate();
            }
        }
    };

    public void polygonclicked(boolean clicked) {
        if (polygonclicked && clicked) {
            polygonclicked = false;
        } else if (!polygonclicked && clicked) {
            polygonclicked = true;

        }

        if (polygonclicked) {
            Paint paint = new Paint();
            paint.setColor(Color.GREEN);
            paint.setAlpha(100);
            areaPaint = paint;
        } else {
            Paint paint = new Paint();
            paint.setColor(Color.BLUE);
            paint.setAlpha(100);
            areaPaint = paint;
        }
    }

    /**
     * Deletes a Point of the polygon.
     * 
     * @author konerman
     * 
     * @param point
     *            The point to delete
     */
    public void deletePoint(Point point) {
        if (!(interpreter instanceof BuildingMotionInterpreter)) {
            if (polygon.remove(point)) {
                Log.d(this.getClass().getSimpleName(), "Point deleted");
            } else {
                Log.d(this.getClass().getSimpleName(), "Point not found");
            }
            postInvalidate();
        }
    }

    /**
     * Return true if the given point is contained inside the polygon. See:
     * http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
     * 
     * @param test
     *            The point to check
     * @return true if the point is inside the polygon, false otherwise
     *
     */
    private boolean insidePolygon(Point test) {
        int i, j;
        boolean result = false;
        for (i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            if ((polygon.get(i).getY() > test.getY()) != (polygon.get(j).getY() > test
                    .getY())
                    && (test.getX() < (polygon.get(j).getX() - polygon.get(i)
                            .getX())
                            * (test.getY() - polygon.get(i).getY())
                            / (polygon.get(j).getY() - polygon.get(i).getY())
                            + polygon.get(i).getX())) {
                result = !result;
            }
        }
        Log.d(this.getClass().getSimpleName(), "-------inside polygon:"
                + result);
        return result;
    }

    /**
     * moves the entire polygon
     * 
     * @konerman
     * 
     * @param x
     *            the offset of the x-coordinate
     * @param y
     *            the offset of the y-coordinate
     * @return the new Polygon
     *
     */
    public void movePolygon(float x, float y) {
        Log.e(this.getClass().getSimpleName(), "----MOVEpolygon");
        List<Point> poly = new ArrayList<Point>();
        x = -x;
        y = -y;
        poly.clear();
        poly = polygon;
        boolean isOverhorizont = false;
        boolean error = false;
        loop: for (int i = 0; i < polygon.size(); i++) {
            float xcoord = polygon.get(i).getX() + x;
            float ycoord = polygon.get(i).getY() + y;
            // ||xcoord<getResources()
            // .getDisplayMetrics().widthPixels||ycoord<getResources()
            // .getDisplayMetrics().heightPixels
            Log.e(this.getClass().getSimpleName(), "width: "
                    + getResources().getDisplayMetrics().widthPixels);

            if (xcoord < 1 || ycoord < 1) {
                error = true;
                Log.e(this.getClass().getSimpleName(),
                        "move: point outside of touchview" + xcoord);
                break loop;
            }

            if (cameraAssistView.overHorizont(new Point(xcoord, ycoord))) {
                Log.d(this.getClass().getSimpleName(), "move: isOverHorizont");
                isOverhorizont = true;
                // return false;
                break loop;
            }
            Log.d(this.getClass().getSimpleName(), "move: point" + xcoord);
            // polygon.set(i, new Point(xcoord, ycoord));
            poly.set(i, new Point(xcoord, ycoord));

        }
        if (!isOverhorizont || !error) {
            polygon = poly;
        }
        Log.d(this.getClass().getSimpleName(), "move: polygon=poly");
        postInvalidate();
        // return true;
    }

    /**
     * This function determines if there is a Point of the polygon close to the
     * given coordinates.
     * 
     * @author konerman
     * 
     * @param x
     *            the X-value of the point
     * @param y
     *            the Y-value of the point
     * @param maxDistance
     *            the max distance between the x/y and the Point
     * @return the closest point to the given x/y or {@code null} if the nearest
     *         point is more than maxDistance away
     *
     */
    public Point lookUp(float x, float y, float maxDistance) {
        double shortest = maxDistance;
        Point closePoint = null;
        if (!polygon.isEmpty()) {
            // runs through the list of points in the polygon and checks which
            // point is the closest
            for (Point p : polygon) {
                final double distance = Math.hypot(x - p.getX(), y - p.getY());
                Log.d(this.getClass().getSimpleName(), "distance:" + distance);
                if (distance <= shortest) {
                    shortest = distance;
                    closePoint = p;
                }
            }
        }
        Log.d(this.getClass().getSimpleName(), "shortest distance:" + shortest);
        return closePoint;
    }

    /**
     * Returns a {@link PointMover} for the given {@link Point}.<br/>
     * 
     * Use moveTo() afterwards to actually move the point.
     * 
     * @author konerman
     * @param point
     *            the point you want to move
     * 
     * @return A {@link PointMover} for moving this point
     * 
     */
    public PointMover movePoint(Point point) {
        final int i = polygon.indexOf(point);
        redoUndo.add(point, moveFrom, i);
        if (i == -1) {
            Log.d(this.getClass().getSimpleName(), "Point is not in polygon");
            return null;
        } else {
            Log.d(this.getClass().getSimpleName(), "PointMover for index " + i);
            return new PointMover(i);
        }
    }

    /**
     * Sets the type of interpretation for this {@link TouchView}.
     * 
     * @param type
     *            The {@link InterpretationType} to set
     * @throws IllegalArgumentException
     *             if {@code type} is {@code null}
     */
    public void setInterpretationType(InterpretationType type) {
        switch (type) {
        case AREA:
            interpreter = new AreaMotionInterpreter(pointTrans);
            break;
        case POINT:
            interpreter = new PointMotionInterpreter(pointTrans);
            break;
        case BUILDING:
            interpreter = new BuildingMotionInterpreter(pointTrans);
            break;
        case WAY:
            interpreter = new WayMotionInterpreter(pointTrans);
            break;
        default:
            throw new IllegalArgumentException("'type' cannot be null");
        }
    }

    /**
     * Adds the last removed point to the current polygon.
     * 
     * @author vkochno
     */
    public void redo() {
        final String action = redoUndo.getAction();
        final int location = redoUndo.getLocation();
        Point point = redoUndo.redo();
        Log.d(this.getClass().getSimpleName(), action + "LOCATION: " + location);
        if (action.equals(add)) {
            newPolygon.add(point);
        }
        if (action.equals(delete)) {
            newPolygon.remove(point);
        }
        if (action.equals(moveTo) || action.equals(moveFrom)) {
            mover = new PointMover(location);
            point = redoUndo.redo();
            mover.moveTo(point.getX(), point.getY());
            mover = null;
        }
        polygon = newPolygon;
        this.redoUseable();
        this.undoUseable();
        undoRedoListener.okUseable(hasEnoughNodes());
    }

    /**
     * Removes the last point in the current polygon.
     * 
     * @author vkochno
     */
    public void undo() {
        Point point = redoUndo.undo();
        final String action = redoUndo.getAction();
        final int location = redoUndo.getLocation();
        Log.d(this.getClass().getSimpleName(), action + "LOCATION: " + location);
        if (action.equals(add)) {
            newPolygon.remove(point);
        }
        if (action.equals(delete)) {
            newPolygon.add(location, point);
        }
        if (action.equals(moveFrom) || action.equals(moveTo)) {
            mover = new PointMover(location);
            point = redoUndo.undo();
            mover.moveTo(point.getX(), point.getY());
            mover = null;
        }
        polygon = newPolygon;
        this.redoUseable();
        this.undoUseable();
        undoRedoListener.okUseable(hasEnoughNodes());
    }

    /**
     * Set the actual PointToCoordsTransformUtil with the actual location and
     * camera parameters.
     * 
     * @author sbollen
     * @param pointTrans
     *            the actual object
     */
    public void setTransformUtil(PointToCoordsTransformUtil pointTrans) {
        this.pointTrans = pointTrans;
    }

    /**
     * Sets the {@link UndoRedoListener} to use.
     * 
     * @param undoRedoListener
     *            The {@link UndoRedoListener} to set
     */
    public void setUndoRedoListener(UndoRedoListener undoRedoListener) {
        this.undoRedoListener = undoRedoListener;
    }

    /**
     * Create an AbstractDataElement from the given polygon.
     * 
     * 
     * @author sbollen
     * @param rotation
     *            create the element with the givin rotation
     * @return the created AbstractDataElement (with located nodes)
     */
    public AbstractDataElement create(int rotation) {
        return interpreter.create(polygon, rotation);
    }

    /**
     * Remove all recorded DrawingMotions from this TouchView.
     */
    public void clearMotions() {
        if (polygon != null) {
            polygon.clear();
            redoUndo = new RedoUndo();
            this.undoUseable();
            this.redoUseable();
            this.undoRedoListener.okUseable(hasEnoughNodes());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View#onFinishInflate()
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        interpreter = new WayMotionInterpreter(pointTrans);
        pathPaint.setColor(MotionInterpreter.POINT_COLOR);
        pathPaint.setColor(MotionInterpreter.PATH_COLOR);
        pathPaint.setStrokeWidth(MotionInterpreter.PATH_STROKE_WIDTH);
        areaPaint.setColor(MotionInterpreter.AREA_COLOR);
        areaPaint.setStyle(Paint.Style.FILL);
        areaPaint.setAlpha(100);
        redoUndo = new RedoUndo();
    }

    /**
     * Testing if the redo function is able to use
     * 
     * @author vkochno
     * 
     * @return If redo can be used
     */
    public boolean redoUseable() {
        if (!(interpreter instanceof BuildingMotionInterpreter)
                && redoUndo.getCurrent() == redoUndo.getMax()
                || interpreter instanceof BuildingMotionInterpreter
                && redoUndo.getCurrent() == redoUndo.getMax()) {
            Log.d(this.getClass().getSimpleName(), "false redo");
            if (undoRedoListener != null) {
                undoRedoListener.canRedo(false);
            }
            return true;
        } else {
            Log.d(this.getClass().getSimpleName(), "true redo");
            if (undoRedoListener != null) {
                undoRedoListener.canRedo(true);
            }
            return false;
        }
    }

    /**
     * Testing if the undo function is able to use
     * 
     * @author vkochno
     * 
     * @return If undo can be used
     */
    public boolean undoUseable() {
        if (!(interpreter instanceof BuildingMotionInterpreter)
                && redoUndo.getMax() != 0 && redoUndo.getCurrent() != 0
                || interpreter instanceof BuildingMotionInterpreter
                && redoUndo.getMax() != 0 && redoUndo.getCurrent() != 0
                && redoUndo.getMax() == 4) {
            Log.d(this.getClass().getSimpleName(), "true undo");
            if (undoRedoListener != null) {
                undoRedoListener.canUndo(true);
            }
            return true;
        } else {
            Log.d(this.getClass().getSimpleName(), "false undo");
            if (undoRedoListener != null) {
                undoRedoListener.canUndo(false);
            }
            return false;
        }
    }

    /**
     * Returns <code>true</code> if the drawing has the minimum of nodes for its
     * InterpretationType.
     * 
     * @author konerman
     * 
     * @return <code>true</code> if the polygon has enough nodes;
     *         <code>false</code> otherwise
     */
    public boolean hasEnoughNodes() {
        return interpreter.minNodes() <= polygon.size();
    }

    /**
     * Create empty RedoUndo for tests
     */
    public void emptyRedoUndo() {
        redoUndo = new RedoUndo();
    }

    /**
     * Return the current size setted in the settings or the default value
     * 
     * @return size of a point
     */
    private int getPointsize() {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getContext());
        final Resources res = getContext().getResources();
        final String key = res.getString(R.string.pref_pointsize_key);
        final String size = prefs.getString(key, null);
        if (TextUtils.isEmpty(size)) {
            final int defaultValue = res
                    .getInteger(R.integer.pref_pointsize_default);
            // Save the default value
            prefs.edit().putString(key, "" + defaultValue).commit();
            return defaultValue;
        } else {
            final int pointsize = Integer.parseInt(size);
            return pointsize;
        }

    }

    /**
     * Pointer of the position of a point in the polygon.
     * 
     * @author konerman
     */
    public class PointMover {
        private final int idx;

        /**
         * Constructs a PointMover for the given index.
         * 
         * @param idx
         *            The index of the point in the polygon
         */
        public PointMover(int idx) {
            this.idx = idx;
        }

        /**
         * Moves the {@link Point} to the new coordinates and invalidates its
         * {@link TouchView} afterwards.
         * 
         * @author konerman
         * 
         * @param x
         *            the new x-coordinate
         * @param y
         *            the new y-coordinate
         * 
         */
        public void moveTo(float x, float y) {
            polygon.set(idx, new Point(x, y));
            postInvalidate();
        }

        /**
         * Return the current X-Coordinate of the moving Point<br\>
         * 
         * @return the x-coordinate
         * 
         * @author vkochno
         */
        public float getX() {
            return polygon.get(idx).getX();
        }

        /**
         * Return the current Y-Coordinate of the moving Point<br\>
         * 
         * @return the Y-coordinate
         * 
         * @author vkochno
         */
        public float getY() {
            return polygon.get(idx).getY();
        }

        /**
         * return the location in the polygon of the current moved point
         * 
         * @return location of the point
         */
        public int getIdx() {
            return this.idx;
        }
    }

    public CaptureAssistView getCameraAssistView() {
        return cameraAssistView;
    }

    public void setCameraAssistView(CaptureAssistView cameraAssistView) {
        this.cameraAssistView = cameraAssistView;
    }
}
