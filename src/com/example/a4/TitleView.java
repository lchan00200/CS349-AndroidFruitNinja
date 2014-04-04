/**
 * CS349 Winter 2014
 * Assignment 4 Demo Code
 * Jeff Avery & Michael Terry
 */
package com.example.a4;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.example.a4complete.R;

import java.util.Observable;
import java.util.Observer;

/*
 * View to display the Title, and Score
 * Score currently just increments every time we get an update
 * from the model (i.e. a new fruit is added).
 */
public class TitleView extends TextView implements Observer {
    private int score, highScore, lives;
    private Model model;
    
    // Constructor requires model reference
    public TitleView(Context context, Model model) {
        super(context);
        
        this.model = model;
        highScore = 0;
        score = 0;
        lives = 5;
        
        setPadding(0, 20, 0, 0);
        setTextSize(20);
        setTextAlignment(TEXT_ALIGNMENT_CENTER);
        
        // register with model so that we get updates
        model.addObserver(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        setText("Score: " + score + "      High Score: " + highScore + "\nLives Left: " + lives);
    }

    // Update from model
    // ONLY useful for testing that the view notifications work
    @Override
    public void update(Observable observable, Object data) {
        invalidate();
    }

    public void incrementScore() {
    	score += 10;
    }
    
    public void decrementLives() {
    	lives--;
    	
    	if (lives < 0) {
    		model.displayGameOver();

    		highScore = score > highScore? score: highScore;
    		score = 0;
    		lives = 5;
    	}
    }
}
