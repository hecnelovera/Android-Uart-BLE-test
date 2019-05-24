package com.nordicsemi.nrfUARTv2.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.nordicsemi.nrfUARTv2.MainActivity;
import com.nordicsemi.nrfUARTv2.R;


public class CustomView extends View {

    private Rect mRectSquare;
    private Paint mPaintSquare;
    private static int frame_size = 0;
    private static int current_frame_size = 0;
    private static short[] Data = new short[2000];
    private static byte overflow;

    DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
    int width = metrics.widthPixels;
    int height = (int)((double)width * 1.5);


    //Timer
    private static long startTime = 0;
    private static long endTime = 0;

    public CustomView(Context context) {
        super(context);
        init(null);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(@Nullable AttributeSet set){
        mRectSquare = new Rect();
        mPaintSquare = new Paint(Paint.ANTI_ALIAS_FLAG);

        mRectSquare.left = 10;
        mRectSquare.top = 10;
        mRectSquare.right = mRectSquare.left + 10;
        mRectSquare.bottom = mRectSquare.top + 10;

        mPaintSquare.setColor(Color.GREEN);
    }


    public void draw_square_one(byte[] Positions) {
        //public void draw_square_one(){
        //mRectSquare.left =      Positions[0];
        //mRectSquare.top =       Positions[1];
       /* mRectSquare.right =     Positions[2];
        mRectSquare.bottom =    Positions[3];
        postInvalidate();*/
        short[] to_short = new short[256];
        //mPaintSquare.setColor(mPaintSquare.getColor() == Color.GREEN ? Color.RED : Color.GREEN);


        endTime = System.currentTimeMillis();

        for (int i = 0; i < Positions.length; i++)
        //for (int i : Positions)
        {
            if (Positions[i] < 0) {
                to_short[i] = (short) (256 + ((short) (Positions[i])));
            } else to_short[i] = ((short) (Positions[i]));
        }

        if ((endTime - startTime) > 1000) {        //Timeout has occurred, Next command is coming
            current_frame_size = 0;
            frame_size = 0;
            startTime = System.currentTimeMillis();
            endTime = 0;
            //Draw
            mRectSquare.left = (Data[35]*width)/64;
            mRectSquare.top =  (Data[34]*width)/64;
            mRectSquare.right = (Data[41]*width)/64;
            mRectSquare.bottom = (Data[40]*width)/64;
            postInvalidate();
        }


        if ((current_frame_size + Positions.length) < 2000) {
            System.arraycopy(to_short, 0, Data, current_frame_size, Positions.length);
            current_frame_size = current_frame_size + Positions.length;
        } else {
            current_frame_size = 0;
            frame_size = 0;
            overflow = 1;
        }

        if ((current_frame_size > 6) && (current_frame_size < 256)) {                       //&& (current_frame_size < 256 -> This part should be improved
            if (overflow == 1) {                                                            //We reach the end of buffer and we didn't get the right qty of RX bytes
                for (int i = 0; i < Positions.length; i++) {
                    if ((to_short[i] == 0xBE)) {                    //"BE0A1234" will be outside the 0 position
                        if (to_short[i + 1] == 0xA0) {
                            current_frame_size = 0;
                            System.arraycopy(to_short, i, Data, current_frame_size, Positions.length - i);      //Length will be shift i times
                            current_frame_size = current_frame_size + Positions.length - i;
                            //frame_size = to_short[5 + i] + to_short[6 + i] * 256;
                            overflow = 0;
                        }
                    }
                }
            }
            if ((Data[0] == 0xBE)) {
                //mPaintSquare.setColor(mPaintSquare.getColor() == Color.GREEN ? Color.RED : Color.GREEN);
                postInvalidate();
                if (Data[1] == 0xA0) {
                    //current_frame_size = 0;
                    frame_size = Data[5] + Data[6] * 256;
                }
            }
        /*if (current_frame_size > 40){
            mRectSquare.left = Data[33];
            mRectSquare.top =  Data[44];
            mRectSquare.right = Data[39]+100;
            mRectSquare.bottom = Data[40]+100;
            postInvalidate();

        }*/
        }

        if (( current_frame_size >= frame_size) && (frame_size > 0)){     //frame_size must not be zero here
            current_frame_size = 0;
            frame_size = 0;
            mRectSquare.left = (Data[35]*width)/64;
            mRectSquare.top =  (Data[34]*width)/64;
            mRectSquare.right = (Data[41]*width)/64;
            mRectSquare.bottom = (Data[40]*width)/64;
            //mPaintSquare.setColor(mPaintSquare.getColor() == Color.GREEN ? Color.RED : Color.GREEN);
            postInvalidate();

         }
    }

    @Override
    protected void onDraw(Canvas canvas){



        //mPaintSquare.setColor(Color.GREEN);

        canvas.drawRect(mRectSquare, mPaintSquare);

    }

}
