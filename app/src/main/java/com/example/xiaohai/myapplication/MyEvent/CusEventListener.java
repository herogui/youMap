package com.example.xiaohai.myapplication.MyEvent;


        import java.util.EventListener;

public class CusEventListener implements EventListener {


    public void fireCusEvent(CusEvent e) {
        EventSourceObject eObject = (EventSourceObject) e.getSource();
        String res = eObject.getString();
    }
}
