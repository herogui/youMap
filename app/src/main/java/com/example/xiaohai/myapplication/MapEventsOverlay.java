package com.example.xiaohai.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

public class MapEventsOverlay extends Overlay {

    private MapEventsReceiver mReceiver;

    /**
     * @param ctx the context
     * @param receiver the object that will receive/handle the events.
     * 必须实现 MapEventsReceiver 接口.
     */
    public MapEventsOverlay(Context ctx, MapEventsReceiver receiver) {
        super(ctx);
        mReceiver = receiver;
    }

    @Override
    public void draw(Canvas c, MapView osmv, boolean shadow) {
        //Nothing to draw
    }

    @Override public boolean onSingleTapUp(MotionEvent e, MapView mapView){
        Projection proj = mapView.getProjection();
        IGeoPoint p = proj.fromPixels((int)e.getX(), (int)e.getY());
        return true;
    }

    @Override public boolean onLongPress(MotionEvent e, MapView mapView) {
        Projection proj = mapView.getProjection();
        IGeoPoint p = proj.fromPixels((int)e.getX(), (int)e.getY());
        //throw event to the receiver:
        return true;
    }

}

