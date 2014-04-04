/**
 * CS349 Winter 2014
 * Assignment 4 Demo Code
 * Jeff Avery & Michael Terry
 */
package com.example.a4;

import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CopyOnWriteArrayList;

/*
 * Class the contains a list of fruit to display.
 * Follows MVC pattern, with methods to add observers,
 * and notify them when the fruit list changes.
 */
public class Model extends Observable {
    // List of fruit that we want to display
    private CopyOnWriteArrayList<Fruit> shapes = new CopyOnWriteArrayList<Fruit>();
	private View titleView, mainView;

    // Constructor
    Model() {
        shapes.clear();
    }

    // Model methods
    // You may need to add more methods here, depending on required functionality.
    // For instance, this sample makes to effort to discard fruit from the list.
    public void add(Fruit s) {
        shapes.add(s);
        setChanged();
        notifyObservers();
    }

    public void remove(Fruit s) {
        shapes.remove(s);
    }

    public CopyOnWriteArrayList<Fruit> getShapes() {
        return (CopyOnWriteArrayList<Fruit>) shapes.clone();
    }

    // MVC methods
    // Basic MVC methods to bind view and model together.
    public void addObserver(Observer observer) {
        super.addObserver(observer);
        
        if (observer instanceof MainView) {
        	mainView = (View) observer;
        } else if (observer instanceof TitleView) {
        	titleView = (View) observer;
        }
    }

    // a helper to make it easier to initialize all observers
    public void initObservers() {
        setChanged();
        notifyObservers();
    }

    @Override
    public synchronized void deleteObserver(Observer observer) {
        super.deleteObserver(observer);
        setChanged();
        notifyObservers();
    }

    @Override
    public synchronized void deleteObservers() {
        super.deleteObservers();
        setChanged();
        notifyObservers();
    }

	public void updateFruitPositions() {
		for (Fruit f: shapes) {
			f.updatePosition();
			
			if (f.shouldDestroy() || f.getPosY() < 50) {
				if (!f.getSplit()) {
					((TitleView) titleView).decrementLives();
				}
				
				remove(f);
			}
		}
	}

	public void incrementScore() {
		((TitleView) titleView).incrementScore();
	}
	
	public void displayGameOver() {
		shapes.clear();
		((MainView) mainView).displayGameOver();
	}
}
