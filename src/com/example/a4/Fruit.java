/**
 * CS349 Winter 2014
 * Assignment 4 Demo Code
 * Jeff Avery
 */
package com.example.a4;
import android.graphics.*;
import android.util.Log;
import android.view.View;
import android.webkit.WebView.FindListener;

/**
 * Class that represents a Fruit. Can be split into two separate fruits.
 */
public class Fruit {
    private Path path = new Path();
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Matrix transform = new Matrix();
    private int windowWidth, windowHeight;
    private int translateX = 0;
    
    private float posY;
    private float minVelocityY = (float) 3;
    private float velocityY;
    private float gravity = (float) 0.015;
    private float spacing = 50;
    
    private View mainView;
	private boolean isSplit;
	
	// for calculation of split
	private double m, y_intercept, a, b, c, discriminant;
    
    /**
     * A fruit is represented as Path, typically populated 
     * by a series of points 
     */
    Fruit(float[] points, View parent, boolean isSplit) {
    	this.isSplit = isSplit;
    	
        init(parent);
        this.path.reset();
        this.path.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) {
            this.path.lineTo(points[i], points[i + 1]);
        }
        this.path.moveTo(points[0], points[1]);
        
        //shift fruits to correct initial position
        if (!isSplit) {
        	this.translate(translateX, windowHeight);	
        }
    }

    Fruit(Region region, View parent, boolean isSplit) {
    	this.isSplit = isSplit;
    	
    	init(parent);
        this.path = region.getBoundaryPath();

        //shift fruits to correct initial position
        if (!isSplit) {
        	this.translate(translateX, windowHeight);	
        }
    }

    Fruit(Path path, View parent, boolean isSplit) {
    	this.isSplit = isSplit;
    	
        init(parent);
        this.path = path;
    
        //shift fruits to correct initial position
        if (!isSplit) {
        	this.translate(translateX, windowHeight);	
        }
    }

    private void init(View parentView) { 
    	mainView = parentView;
    	
    	this.windowHeight = mainView.getHeight();
    	this.windowWidth = mainView.getWidth();
    	
    	if (!isSplit) {
    		this.posY = windowHeight;
    	
    		this.velocityY = (float) (Math.random() + minVelocityY);
    		
    		// set the initial x, y to spawn the fruit
            this.translateX = (int) (spacing + Math.random() * (windowWidth - 2*spacing));
    	} else {
    		this.velocityY = 0;
    	}
    	
        this.paint.setColor(Color.BLUE);
        this.paint.setStrokeWidth(5);
    }

    /**
     * The color used to paint the interior of the Fruit.
     */
    public int getFillColor() { return paint.getColor(); }
    public void setFillColor(int color) { paint.setColor(color); }

    /**
     * The width of the outline stroke used when painting.
     */
    public double getOutlineWidth() { return paint.getStrokeWidth(); }
    public void setOutlineWidth(float newWidth) { paint.setStrokeWidth(newWidth); }

    /**
     * Concatenates transforms to the Fruit's affine transform
     */
    public void rotate(float theta) { transform.postRotate(theta); }
    public void scale(float x, float y) { transform.postScale(x, y); }
    public void translate(float tx, float ty) { transform.postTranslate(tx, ty); }

    /**
     * Returns the Fruit's affine transform that is used when painting
     */
    public Matrix getTransform() { return transform; }

    /**
     * The path used to describe the fruit shape.
     */
    public Path getTransformedPath() {
        Path originalPath = new Path(path);
        Path transformedPath = new Path();
        originalPath.transform(transform, transformedPath);
        return transformedPath;
    }

    /**
     * Paints the Fruit to the screen using its current affine
     * transform and paint settings (fill, outline)
     */
    public void draw(Canvas canvas) {
        canvas.drawPath(getTransformedPath(), paint);
    }

    /**
     * Tests whether the line represented by the two points intersects
     * this Fruit.
     */
    public boolean intersects(PointF p1, PointF p2) {
    	// basic check to make sure the cut exists outside of the shape
    	if (isSplit || contains(p1) || contains(p2)) {
    		return false;
    	}
    	
    	// create a clip rect
    	int width = (int) Math.max(p1.x, p2.x);
    	int height = (int) Math.max(p1.y, p2.y);
    	
    	Rect clipRect = new Rect(0,0,width,height);
    	Region clip = new Region(clipRect);
    	
    	// create a region from the line specified by the 2 points
    	Path linePath = new Path();
    	linePath.lineTo(p1.x, p1.y);
    	linePath.lineTo(p2.x, p2.y);
    	
    	Region lineRegion = new Region();
    	lineRegion.setPath(linePath, clip);
    	
    	// create a region from the fruit shape
    	Region fruitRegion = new Region();
    	fruitRegion.setPath(getTransformedPath(), clip);
    	
    	return !lineRegion.quickReject(fruitRegion);
    }

    /**
     * Returns whether the given point is within the Fruit's shape.
     */
    public boolean contains(PointF p1) {
        Region region = new Region();
        boolean valid = region.setPath(getTransformedPath(), new Region());
        return valid && region.contains((int) p1.x, (int) p1.y);
    }

    /**
     * This method assumes that the line represented by the two points
     * intersects the fruit. If not, unpredictable results will occur.
     * Returns two new Fruits, split by the line represented by the
     * two points given.
     */
    public Fruit[] split(PointF p1, PointF p2) {
    	Path topPath = null;
    	Path bottomPath = null;
    	
    	// Get the bounding rectangle of the fruit
    	RectF r = new RectF();
    	getTransformedPath().computeBounds(r, true);
    	
    	// get the discriminant
    	double radius = r.width()/2;
    	m = (p2.y - p1.y)/(p2.x - p1.x);
    	y_intercept = p1.y - m*p1.x;
    	
    	a = Math.pow(m, 2) + 1;
    	b = 2*(m*y_intercept - m*r.centerY() - r.centerX());
    	c = Math.pow(r.centerY(),  2) - Math.pow(radius, 2) + Math.pow(r.centerX(), 2) -
    			2*y_intercept*r.centerY() + Math.pow(y_intercept,2);
    	
    	discriminant = Math.pow(b,2) - 4*a*c;
    	
    	if (discriminant < 0) {
            return new Fruit[0];
    	}
    	
    	// calculate intersection points
    	float x1 = (float) ((-b + Math.sqrt(discriminant))/(2*a));
    	float x2 = (float) ((-b - Math.sqrt(discriminant))/(2*a));
    	float y1 = (float) (m * x1 + y_intercept);
    	float y2 = (float) (m * x2 + y_intercept);
    	
    	// calculate angle of intersection relative to horizontal axis
    	float degrees = (float) Math.toDegrees(-Math.atan((y2-y1)/(x2-x1)));
    	
    	// create transform to shift slices to origin centered on midpoint of cut
    	Matrix t = new Matrix();
    	t.preRotate(degrees);
    	t.preTranslate(-x1, -y1);
    	
    	// create top and bottom cuts
    	topPath = getTransformedPath();
    	bottomPath = getTransformedPath();
    	topPath.transform(t);
    	bottomPath.transform(t);
    	
    	Rect topRect = new Rect(-100, -100, 100, 0);
    	Rect bottomRect = new Rect(-100, 0, 100, 100);
    	Region topRegion = new Region();
    	Region bottomRegion = new Region();
    	topRegion.setPath(topPath, new Region(topRect));
    	bottomRegion.setPath(bottomPath, new Region(bottomRect));
    	
    	topPath = topRegion.getBoundaryPath();
    	bottomPath = bottomRegion.getBoundaryPath();
    	
    	// move back to original position
    	Matrix t_inverse = new Matrix();
    	t.invert(t_inverse);
    	topPath.transform(t_inverse);
    	bottomPath.transform(t_inverse);
    	
        if (topPath != null && bottomPath != null) {
        	Fruit topFruit, bottomFruit;
        	
        	if (degrees > 0) {
        		topFruit = new Fruit(topPath, mainView, true);
        		bottomFruit = new Fruit(bottomPath, mainView, true);
        	} else {
        		topFruit = new Fruit(bottomPath, mainView, true);
        		bottomFruit = new Fruit(topPath, mainView, true);        		
        	}
        	
        	return new Fruit[] {topFruit, bottomFruit};
        }
        return new Fruit[0];
    }
    
    public void updatePosition() {
    	velocityY = velocityY - gravity;
    	
    	posY = posY - velocityY;
    	translate(0, -velocityY);
    }

	public boolean shouldDestroy() {
		if (posY > windowHeight + 50) {
			return true;
		}
		return false;
	}
	
	public boolean getSplit() {
		return isSplit;
	}

	public int getPosY() {
		RectF bounds = new RectF();
		getTransformedPath().computeBounds(bounds, true);
		
		return (int) bounds.top;
	}
}
