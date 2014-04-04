/**
 * CS349 Winter 2014
 * Assignment 4 Demo Code
 * Jeff Avery & Michael Terry
 */
package com.example.a4;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import com.example.a4complete.R;

/*
 * View of the main game area.
 * Displays pieces of fruit, and allows players to slice them.
 */
public class MainView extends View implements Observer {
    private final Model model;
    private final MouseDrag drag = new MouseDrag();
    private int windowWidth, windowHeight;
    private boolean gameOver;
    private TextView gameOverText;
    
    // Constructor
    MainView(Context context, Model m) {
        super(context);
        
        setBackgroundColor(Color.WHITE);
        
        gameOver = false;
        gameOverText = new TextView(context);
        gameOverText.setText("Game Over. Tap to play again.");
        gameOverText.setTextSize(16);
        gameOverText.setVisibility(0);
        
        // set view context to this to pass to timer tasks
        final View mainView = this;
        
        // register this view with the model
        model = m;
        model.addObserver(this);
        
        // timer task to spawn fruits
        class spawnTimerTask extends TimerTask {
        	Handler spawnFruitHandler;
        	
        	public spawnTimerTask() {
		        spawnFruitHandler = new Handler() {
					float[] fruitShape = new float[] {0, 20, 20, 0, 40, 0, 60, 20, 60, 40, 40, 60, 20, 60, 0, 40};
					
		        	@Override
		        	public void handleMessage(Message msg) {
		        		if (!gameOver) {
			        		Fruit f = new Fruit(fruitShape, mainView, false);
							model.add(f);
							invalidate();
		        		}
		        	}
		        };
        	}
        	
        	@Override
			public void run() {
				spawnFruitHandler.sendEmptyMessage(0);
			}
        }
        
        // set a timer to spawn fruits every 2s
        final Timer spawnTimer = new Timer();
        spawnTimer.schedule(new spawnTimerTask(), 1000, 2000);
        
        //timer task to update the positions of the fruits
        class animateTimerTask extends TimerTask {
        	Handler animateFruitHandler;
        	
        	public animateTimerTask() {
		        animateFruitHandler = new Handler() {
		        	@Override
		        	public void handleMessage(Message msg) {
		        		model.updateFruitPositions();
		        		invalidate();
		        	}
		        };
			}
        	
        	@Override
			public void run() {
				animateFruitHandler.sendEmptyMessage(0);
			}
        }
        
        Timer animateTimer = new Timer();
        animateTimer.schedule(new animateTimerTask(), 0, 10);
        
        // add controller
        // capture touch movement, and determine if we intersect a shape
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    	if (gameOver){
                    		setBackgroundColor(Color.GREEN);
                    		break;
                    	}
                    	
                        // Log.d(getResources().getString(R.string.app_name), "Touch down");
                        drag.start(event.getX(), event.getY());
                        break;

                    case MotionEvent.ACTION_UP:
                    	if (gameOver) {
                    		setBackgroundColor(Color.WHITE);
                    		gameOverText.setVisibility(0);
                    		gameOver = false;
                    		break;
                    	}                    	
                    	
                        // Log.d(getResources().getString(R.string.app_name), "Touch release");
                        drag.stop(event.getX(), event.getY());

                        // find intersected shapes
                        Iterator<Fruit> i = model.getShapes().iterator();
                        while(i.hasNext()) {
                            Fruit s = i.next();
                            if (s.intersects(drag.getStart(), drag.getEnd())) {
                            	//s.setFillColor(Color.RED);
                                try {
                                	Fruit[] newFruits = s.split(drag.getStart(), drag.getEnd());

                                	if (newFruits.length == 2) {
	                                	newFruits[0].translate(-20, 0);
	                                    newFruits[1].translate(20, 0);
	                                    // TODO END CS349
	                                    model.add(newFruits[0]);
	                                    model.add(newFruits[1]);
	
	                                    // delete the original fruit
	                                    model.remove(s);
	                                    
	                                    model.incrementScore();
                                	}
                                } catch (Exception ex) {
                                    Log.e("fruit_ninja", "Error: " + ex.getMessage());
                                }
                            } else {
                                s.setFillColor(Color.BLUE);
                            }
                            invalidate();
                        }
                        break;
                }
                return true;
            }
        });
    }

    // inner class to track mouse drag
    // a better solution *might* be to dynamically track touch movement
    // in the controller above
    class MouseDrag {
        private float startx, starty;
        private float endx, endy;

        protected PointF getStart() { return new PointF(startx, starty); }
        protected PointF getEnd() { return new PointF(endx, endy); }

        protected void start(float x, float y) {
            this.startx = x;
            this.starty = y;
        }

        protected void stop(float x, float y) {
            this.endx = x;
            this.endy = y;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (gameOver) {
        	float x = 20;
        	float y = getHeight()/2;
        	
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.RED);
            paint.setTextSize(25);
        	
        	canvas.drawText("GAME OVER. TAP FOR A NEW GAME.", x, y, paint);
        }
        
        // draw all pieces of fruit
        for (Fruit s : model.getShapes()) {
            s.draw(canvas);
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        invalidate();
    }

	public void displayGameOver() {
		gameOver = true;
		gameOverText.setVisibility(1);
	}
}
