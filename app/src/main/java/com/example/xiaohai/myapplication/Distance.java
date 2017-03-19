package com.example.xiaohai.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.MotionEvent;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaohai on 2017/3/19.
 */
public class Distance {
    MainActivity  activity;
    List<GeoPoint> pntList;
    Polyline polyline;
    MapEventsOverlay mo;
    public  Distance(MainActivity activity)
    {
        this.activity = activity;
        mo = new   MapEventsOverlay(this.activity.getApplicationContext());

        this.polyline = new Polyline();
        this.pntList = new ArrayList<GeoPoint>();
        this.polyline.setColor(Color.GREEN);
        this.polyline.setWidth(11);
    }

   public   void  start()
    {
        this.pntList.clear();
        this.activity.map.getOverlayManager().add(mo);
    }

    public double stop()
    {
        activity.map.getOverlayManager().remove(mo);

        int size = this.pntList.size();

        double dis = 0.0;
        for(int i =1;i<size;i++)
        {
            GeoPoint p1 =pntList.get(i);
            GeoPoint p2 =pntList.get(i-1);
            dis += p1.distanceTo(p2);
        }
        return  dis;
    }

    public  void  clear()
    {
        activity.map.getOverlayManager().remove(polyline);
    }

    //测距离
    class MapEventsOverlay extends Overlay {
        @Override
        public void draw(Canvas c, MapView osmv, boolean shadow) {
        }
        public MapEventsOverlay(Context ctx) {
            super(ctx);
        }

        @Override
        public boolean onLongPress(MotionEvent e,MapView mv){
            Projection proj = mv.getProjection();//获得投影对象
            GeoPoint gp = (GeoPoint) proj.fromPixels((int)e.getX(), (int)e.getY());//坐标转换
            pntList.add(gp);
            if(pntList.size()>1)
            {
                activity.map.getOverlayManager().remove(polyline);
                polyline.setPoints(pntList);
                activity.map.getOverlayManager().add(polyline);
            }
            mv.invalidate();//重绘地图
            return true;
        }
    }
}
