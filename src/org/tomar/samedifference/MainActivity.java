package org.tomar.samedifference;

import java.util.ArrayList;

import android.support.v7.app.ActionBarActivity;

import org.ToMar.Utils.Functions;
import org.tomar.samedifference.AboutToMarGames;
import org.tomar.samedifference.HowToPlay;
import org.tomar.samedifference.MainActivity;
import org.tomar.samedifference.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity
{  
	public static final String GAMENAME = "SameDifference";
	static final String BEST = "best";
	SDFLayout sdfLayout;
	LinearLayout linearLayout;
	boolean gameOver;
	int bestScore = 0;
	int points;
	SharedPreferences gamePrefs;
	 
	public void log(String s)
	{
		System.out.println(Functions.getDateTimeStamp() + ": " + s);
	}
	public void quitGame()
	{
		this.finish();
	}
	protected void onDestroy()
	{
	    checkHighScore();
	    super.onDestroy();
	}
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		linearLayout = new LinearLayout(this);
		sdfLayout = new SDFLayout(this);
		linearLayout.setBackgroundColor(0xFFFFFFCC);
		linearLayout.addView(sdfLayout);
	    setContentView(linearLayout);
		gamePrefs = getSharedPreferences(GAMENAME, 0);
        String s;
        s = gamePrefs.getString(BEST, "");
        try
        {
        	bestScore = Integer.parseInt(s);
        }
        catch (Exception e)
        {
        	bestScore = 0;
        }
	}
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_how)
		{
			openHow();
			return true;
		}
		if (id == R.id.action_about)
		{
			openAbout();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	private void checkHighScore()
	{
		if (points > bestScore)
		{
			Toast.makeText(MainActivity.this, "New high score!", Toast.LENGTH_SHORT).show();
			bestScore = points;
			SharedPreferences.Editor scoreEdit = gamePrefs.edit();
			scoreEdit.putString(BEST, "" + bestScore);
			scoreEdit.commit();
		}
	}
	public void onBackPressed()
	{
		Toast.makeText(MainActivity.this, "Back button disabled: use Home", Toast.LENGTH_SHORT).show();
	}
	public void openHow()
	{
	    Intent i = new Intent(MainActivity.this, HowToPlay.class);
	    startActivity(i);
	}	
	public void openAbout()
	{
	    Intent i = new Intent(MainActivity.this, AboutToMarGames.class);
	    startActivity(i);
	}	
	private class SDFLayout extends View
	{
		Slot[] slots = new Slot[36];
		final int TOTALCARDS = 81;
		final int TOPMARGIN = 30;
		final int infoY = 30;
		final int NOGOOD = 0;
		final int ALLSAME = 1;
		final int ALLDIFFERENT = 2;
		int numberSelected;
		int numberOfSets;
		int cardPointer = -1;
		Paint p = new Paint();
		tmButtonA sShape, sColor, sShading, sNumber;			// Sort buttons
		
		private ArrayList<SDFCard> layout;
		private SDFCard[] cards;				// shuffled deck
	    private SDFCard[] selectedCards = new SDFCard[3];

	    public void reInit()
	    {
	        numberSelected = 0;
	        numberOfSets = 0;
			cards = createDeck();
			cardPointer = -1;
			points = 0;
			layout = new ArrayList<>();
			dealCards();
			sShape.setColor(tmColorA.SLATE);
			sShape.setLabel("Shape");
			sNumber.setColor(tmColorA.SLATE);
			sNumber.setLabel("Number");
			gameOver = false;
			forceRedraw();
	    }
		public SDFLayout(Context context)
		{
			super(context);
	        // 3 rows of slots, numbered vertically
	        for (int i = 0; i < slots.length; i++)
	        {
	        	slots[i] = new Slot(i);
	        }
	        sShape = new tmButtonA(300, infoY - 20, 37, 20, tmColorA.SLATE, 12, "Shape");
	        sColor = new tmButtonA(342, infoY - 20, 33, 20, tmColorA.SLATE, 12, "Color");
	        sShading = new tmButtonA(380, infoY - 20, 47, 20, tmColorA.SLATE, 12, "Shading");
	        sNumber = new tmButtonA(432, infoY - 20, 45, 20, tmColorA.SLATE, 12, "Number");
	        reInit();
			this.setOnTouchListener(new OnTouchListener()
			{
		        public boolean onTouch(View v, MotionEvent event) 
		        {
		            if (event.getAction() == MotionEvent.ACTION_DOWN)
		            {
	            		if (sColor.clicked((int)event.getX(), (int)event.getY()))
	            		{
	            			sort("getColor");
	            		}
	            		else if (sShape.clicked((int)event.getX(), (int)event.getY()))
	            		{
	            			if (gameOver)
	            			{
	            				reInit();
	            			}
	            			else
	            			{	
	            				sort("getShape");
	            			}	
	            		}
	            		else if (sShading.clicked((int)event.getX(), (int)event.getY()))
	            		{
            				sort("getShading");
	            		}
	            		else if (sNumber.clicked((int)event.getX(), (int)event.getY()))
	            		{
	            			if (gameOver)
	            			{
	            				quitGame();
	            			}
	            			else
	            			{	
	            				sort("getNumber");
	            			}	
	            		}
	            		else
	            		{	
			            	for (int i = 0; i < 25; i++)
			            	{
				            	if (slots[i].clicked((int)event.getX(), (int)event.getY()))
				            	{
				            		forceRedraw();
				                    if (numberSelected == 3)
				                    {
				                        int num = 3;
				                        int p = 0;
				                        for (int s = 0; s < layout.size(); s++)
				                        {
				                            if (layout.get(s).isSelected())
				                            {
				                                num -= 1;
				                                selectedCards[num] = layout.get(s);
				                            }
				                        }
				                        if ((p = goodSet()) > NOGOOD)
				                        {
				                        	// add 5 points for each 3 extra cards over 12 in layout
				                        	if (layout.size() > 12)
				                        	{	
				                        		p += (int)((layout.size() - 12)/3) * 5;
				                        	}
						            		Toast.makeText(MainActivity.this, "" + p + " points", Toast.LENGTH_SHORT).show();
				                            // need to remove each card in the set from the layout
				                            // need to relabel the positions of the cards that are left
				                        	points += p;
				                        	numberOfSets += 1;
				                            ArrayList<SDFCard> newLayout = new ArrayList<>();
				                            int layoutPointer = 0;
				                            for (int j = 0; j < layout.size(); j++)
				                            {
				                            	slots[layout.get(j).getPosition()].setCard(null);
				                                if (layout.get(j).isSelected() == false)
				                                {
				                                    layout.get(j).setPosition(layoutPointer++);
				                                    newLayout.add(layout.get(j));
				                                }
				                            }
				                            layout = newLayout;
				                            for (int j = 0; j < layout.size(); j++)
				                            {
				                            	slots[j].setCard(layout.get(j));
				                            }
				                            numberSelected = 0;
				                            if (!dealCards())
				                            {
							            		Toast.makeText(MainActivity.this, "No more sets.", Toast.LENGTH_SHORT).show();
							            		endGame();
				                            }
				                        }
				                        else
				                        {
						            		Toast.makeText(MainActivity.this, "Not a set, sorry!", Toast.LENGTH_SHORT).show();
				                        }
				                    }
				                    break;
				                }
				            }
			        	}
		            }	
		            return true;
		        }
		    });		
		}
		public void endGame()
		{
    		Toast.makeText(MainActivity.this, "Game over.", Toast.LENGTH_SHORT).show();
			checkHighScore();
			sShape.setColor(tmColorA.LIGHTGREEN);
			sShape.setLabel("Again");
			sNumber.setColor(tmColorA.LIGHTRED);
			sNumber.setLabel("Quit");
			gameOver = true;
		}
		public void forceRedraw()
		{
			this.invalidate();
		}
		public boolean dealCards()
		{
			// you will always deal 3 cards at a time
			// you will always want at least 12 cards in the layout, unless you're out of cards
			// always deal until you have a set, unless you're out of cards
//	        showLayout("before deal");
	        while (layout.size() < 12 || !(hasSet()))
	        {
	        	if (cardPointer == TOTALCARDS - 1)
	        	{
	        		if (hasSet())
	        		{
	        			return true;
	        		}
	        		return false;
	        	}
	            for (int i = 0; i < 3; i++)
	            {
	                cardPointer += 1;
	                layout.add(cards[cardPointer]);
	                cards[cardPointer].setPosition(layout.size() - 1);
		            slots[layout.size() - 1].setCard(cards[cardPointer]);
	            }
	        }
//	        showLayout("after deal");
	        return true;
		}    
	    private boolean hasSet()
	    {
	        for (int i = 0; i < layout.size(); i++)
	        {
	            selectedCards[0] = layout.get(i);
	            for (int j = i + 1; j < layout.size(); j++)
	            {
	                selectedCards[1] = layout.get(j);
	                for (int k = j + 1; k < layout.size(); k++)
	                {
	                    selectedCards[2] = layout.get(k);
	                    if (goodSet() > NOGOOD)
	                    {
	                        return true;
	                    }
	                }
	            }
	        }
	        return false;
	    }
	    private int goodSet()
	    {
	    	int setPoints = 0;
	    	int partialPoints = 0;
	        if((partialPoints = evaluate("getShading")) > 0)
	        {
	        	setPoints += partialPoints;
	            if ((partialPoints = evaluate("getColor")) > 0)
	            {
	            	setPoints += partialPoints;
	                if ((partialPoints = evaluate("getNumber")) > 0)
	                {
	                	setPoints += partialPoints;
	                    if ((partialPoints = evaluate("getShape")) > 0)
	                    {
	                    	setPoints += partialPoints;
	                    	return ((setPoints - 3) * 5);
	                    }
	                }
	            }
	        }
	        return NOGOOD;
	    }
	    private int evaluate(String methodName)
	    {
	        try
	        {
	            int value1 = ((int) selectedCards[0].getClass().getMethod(methodName, (Class<?>[]) null).invoke(selectedCards[0]));
	            int value2 = ((int) selectedCards[1].getClass().getMethod(methodName, (Class<?>[]) null).invoke(selectedCards[1]));
	            int value3 = ((int) selectedCards[2].getClass().getMethod(methodName, (Class<?>[]) null).invoke(selectedCards[2]));
	            if (value1 == value2 & value1 == value3)
	            {
	                return ALLSAME;
	            }
	            if (value1 != value2 & value1 != value3 & value2 != value3)
	            {
	                return ALLDIFFERENT;
	            }
	        }
	        catch (Exception e)
	        {
	            System.out.println("SDF.evaluate: ERROR!!: " + e);
	        }
	        return NOGOOD;
	    }
		protected void onDraw(Canvas canvas)
		{
			p.setColor(tmColorA.BLACK);
			p.setTextSize(14);
			canvas.drawText("Sets: ", 1, infoY, p);
			canvas.drawText("Cards: ", 62, infoY, p);
			canvas.drawText("Best: ", 136, infoY, p);
			canvas.drawText("Points: ", 208, infoY, p);
			p.setColor(0xFF0000AA);
			p.setTextSize(20);
			canvas.drawText("" + numberOfSets, 33, infoY, p);
			canvas.drawText("" + (TOTALCARDS - cardPointer - 1), 106, infoY, p);
			canvas.drawText("" + bestScore, 170, infoY, p);
			canvas.drawText("" + points, 253, infoY, p);
			sShape.draw(canvas);
			sColor.draw(canvas);
			sShading.draw(canvas);
			sNumber.draw(canvas);
			for (int i = 0; i < 25; i++)
			{	
				slots[i].draw(canvas);
			}	
		}
		public SDFCard[] createDeck()
		{
			cardPointer = 0;
			SDFCard[] deck = new SDFCard[TOTALCARDS];
			SDFCard[] shuffledDeck = new SDFCard[TOTALCARDS];
			// create the string of 81 cards to be shuffled
			for (int color = 0; color < 3; color++)
			{
				for (int shape = 0; shape < 3; shape++)
				{
					for (int shading = 0; shading < 3; shading++)
					{
						for (int number = 0; number < 3; number++)
						{
							deck[cardPointer] = new SDFCard();
							deck[cardPointer].setColor(color);
							deck[cardPointer].setShape(shape);
							deck[cardPointer].setShading(shading);
							deck[cardPointer].setNumber(number);
							cardPointer += 1;
						}
					}
				}
			}
			String shuffler = "00010203040506070809101112131415161718192021222324252627282930" +
								"313233343536373839404142434445464748495051525354555657585960" +
								"6162636465666768697071727374757677787980";
			// shuffle the cards and put them in the cards array
			for (int i = 0; i < TOTALCARDS; i++)
			{
				int rnd = Functions.getRnd(TOTALCARDS - i) * 2;
				String cardString = shuffler.substring(rnd, rnd + 2);
				shuffler = shuffler.substring(0, rnd) + shuffler.substring(rnd + 2, shuffler.length());
				shuffledDeck[i] = deck[Integer.parseInt(cardString)];
				shuffledDeck[i].setSelected(false);
			}
			return shuffledDeck;
		}
		public void showLayout(String comment)
		{
			log(comment);
			for (int i = 0; i < layout.size(); i++)
			{
				log(layout.get(i).toString());
			}
			for (int i = 0; i < slots.length; i++)
			{
				log("" + i + ": " + slots[i].toString());
			}
		}
		public void sort(String methodName)
		{
			points -= 1;
			int[] sortedCounts = {0, 0, 0};
			try
			{
				for (int n = 0; n < 3; n++)
				{
					for (int i = 0; i < layout.size(); i++)
					{
						if ((int) (layout.get(i).getClass().getMethod(methodName,(Class<?>[]) null).invoke(layout.get(i))) == n)
						{
							int sortedSlot = sortedCounts[n] * 3 + n;
							int currentSlot = layout.get(i).getPosition();
							SDFCard holdCard = slots[sortedSlot].getCard();
							slots[sortedSlot].setCard(layout.get(i));
							layout.get(i).setPosition(sortedSlot);
							slots[currentSlot].setCard(holdCard);
							if (holdCard != null)
							{
								holdCard.setPosition(currentSlot);
							}
							sortedCounts[n] += 1;
						}
					}
				}
				forceRedraw();
//				showLayout("after sort");
			}
			catch (Exception e)
			{
				System.out.println("ERROR!!: " + e);
			}
		}
		private final class tmColorA
		{
			static final int BLACK = 0xFF000000;
	        static final int WHITE = 0xFFFFFFFF;
	        static final int GREY = 0xFFCCCCCC;
	        static final int LIGHTRED = 0xFFDD0000;
	        static final int LIGHTGREEN = 0xFF00CC00;
	        static final int LIGHTBLUE = 0xFF0000DD;
	        static final int CREAM = 0xFFFFFFCC;
	        static final int SLATE = 0xFFB0C4DE;
		}
		
		private class tmButtonA extends Path
		{
			int width;
			int height;
			int color;
			int textSize;
			private int x;
			private int y;
			private String label;
		
			public tmButtonA(int x, int y, int width, int height, int color, int textSize, String label)
			{
				this.x = x;
				this.y = y;
				this.width = width;
				this.height = height;
				this.color = color;
				this.textSize = textSize;
	            this.addRect(x, y, x + width, y + height, Path.Direction.CW);
	            this.label = label;
			}
			public int getColor()
			{
				return color;
			}
			public void setColor(int color)
			{
				this.color = color;
			}
			public int getTextSize()
			{
				return textSize;
			}
			public void setTextSize(int textSize)
			{
				this.textSize = textSize;
			}
			public int getWidth()
			{
				return width;
			}
			public void setWidth(int width)
			{
				this.width = width;
			}
			public int getHeight()
			{
				return height;
			}
			public void setHeight(int height)
			{
				this.height = height;
			}
			public String getLabel()
			{
				return label;
			}
			public void setLabel(String label)
			{
				this.label = label;
			}
			public void setX(int x)
			{
				this.x = x;
			}
			public void setY(int y)
			{
				this.y = y;
			}
			public int getX()
			{
				return x;
			}
			public int getY()
			{
				return y;
			}
			public void draw(Canvas c)
			{
				p.setColor(color);
				p.setTextSize(textSize);
				p.setStyle(Paint.Style.FILL);
				c.drawPath(this, p);
				p.setColor(tmColorA.BLACK);
				p.setStyle(Paint.Style.STROKE);
				c.drawPath(this, p);
				c.drawText(label, x + 1, y + textSize + 2, p);
			}
			public boolean clicked(int x, int y)
			{
				if (x > this.x && 
					x < this.x + width && 
					y > this.y && 
					y < this.y + height)
				{
					return true;
				}
				return false;
			}
		}
		private class Slot extends Path
		{
			public static final int WIDTH = 50;
			public static final int HEIGHT = 70;
			public static final int MARGIN = 3;
			public static final int SIDE = 3;
			private SDFCard card;
			private int x;
			private int y;
		
			public Slot(int i)
			{
	            x = i/SIDE *(WIDTH + MARGIN) + MARGIN;
	            y = TOPMARGIN + i%SIDE * (HEIGHT + MARGIN) + MARGIN + MARGIN;
	            this.addRect(x, y, x + WIDTH, y + HEIGHT, Path.Direction.CW);
	            card = null;
			}
			public String toString()
			{
				return (card == null) ? "No card" : card.toString();
			}
			public SDFCard getCard()
			{
				return card;
			}
			public int getX()
			{
				return x;
			}
			public int getY()
			{
				return y;
			}
			public void setCard(SDFCard card)
			{
				this.card = card;
			}
			public void draw(Canvas c)
			{
				if (this.getCard() != null)
				{
					this.getCard().draw(c, this);
				}
			}
			public boolean clicked(int x, int y)
			{
				if (this.getCard() != null)
				{
					if (x > this.x && 
						x < this.x + WIDTH && 
						y > this.y && 
						y < this.y + HEIGHT)
					{
						this.getCard().clicked();
						return true;
					}		
				}
				return false;
			}
		}
		private class SDFCard 
		{
	        private int number;
	        private int color;
	        private int shading;
	        private int shape;
	        private int position = 0;   // slot number
	        private int outlineColor = tmColorA.BLACK;
	        private int normalColor = tmColorA.WHITE;
	        private int selectColor = tmColorA.GREY;
	        private int colorIndex = 0;
	        private int[][] colors = {{tmColorA.LIGHTRED, tmColorA.LIGHTGREEN, tmColorA.LIGHTBLUE}};
	        private boolean selected;
	
	        public SDFCard()
	        {
	            selected = false;
	        }
			public SDFCard(String s)			// constructor used to restore saved game
			{
				this.number = "123".indexOf(s.substring(0,1));
				this.shading = "ESD".indexOf(s.substring(1,2));
				this.color = "RGP".indexOf(s.substring(2,3));
				this.shape = "SCT".indexOf(s.substring(3,4));
	            selected = false;
			}
	        public String toString()
	        {
	            String shd = "ESD";
	            String col = "RGP";
	            String num = "123";
	            String shp = "SCT";
	            return num.substring(number, number + 1)
	                 +  shd.substring(shading, shading + 1)
	                 +  col.substring(color, color + 1)
	                 +  shp.substring(shape, shape + 1) 
	                 +  " " + position;
	
	        }
	        public void clicked()
	        {
	            selected = (selected == false) ? true : false;
	            if (selected)
	            {
	                numberSelected += 1;
	                if (numberSelected > 3)
	                {
	                    selected = false;
	                    numberSelected = 3;
	                }
	            }
	            else
	            {
	                numberSelected -= 1;
	            }
	        }
	        public void draw(Canvas c, Path path)
	        {
				p.setAntiAlias(true);
				if (selected)
				{
					p.setColor(selectColor);
				}
				else
				{
					p.setColor(normalColor);
				}
				p.setStyle(Paint.Style.FILL);
				c.drawPath(path, p);
				p.setColor(outlineColor);
				p.setStyle(Paint.Style.STROKE);
				c.drawPath(path, p);
                p.setColor(colors[colorIndex][color]);
                int x = slots[position].getX();
                int y = slots[position].getY();
                int marg = 5;
                int shapeSize = (Slot.HEIGHT - (4 * marg))/3;
                int dotSize = 5;
                int xDot = x + Slot.WIDTH/2 - dotSize/2;
                int xShape = x + Slot.WIDTH/3;
                Path pShape;
                // y will have 5 positions, staggered by half of cardHeight
                // if one, use position 3
                // if two, use positions 2 and 4
                // if three, use 1, 3, and 5
                // shape size will be cardHeight
                int[] yShape = {y + marg, y + marg + shapeSize/2 + 1, y + 2 * marg + shapeSize, y + 2 * marg + shapeSize * 3/2 + 1, y + 3 * marg + 2 * shapeSize};
                int[] yDot = {y + marg + shapeSize/2 - dotSize/2, y + marg + shapeSize/2 + shapeSize/2 - dotSize/2 + 1, y + 2 * marg + shapeSize + shapeSize/2 - dotSize/2, y + 2 * marg + shapeSize * 3/2 + shapeSize/2 - dotSize/2 + 1, y + 3 * marg + 2 * shapeSize + shapeSize/2 - dotSize/2};
                int[][] patterns = {{2, 0, 0}, {1, 3, 0}, {0, 2, 4}};
                Style[] styles = {Paint.Style.STROKE, Paint.Style.FILL, Paint.Style.STROKE};
                for (int i = 0; i < number + 1; i++)
                {
    				p.setStyle(styles[shading]);
                    if (shape == 0)
                    {
                    	pShape = new SDFSquare(xShape, yShape[patterns[number][i]], shapeSize);
                    }
                    else if (shape == 1)
                    {
                    	pShape = new SDFCircle(xShape + shapeSize/2, yShape[patterns[number][i]] + shapeSize/2, shapeSize);
                    }	
                    else
                    {
                    	pShape = new SDFTriangle(xShape, yShape[patterns[number][i]] + shapeSize, shapeSize);
                    }
                    c.drawPath(pShape, p);
                    if (shading == 2)
                    {
                    	int yF = (shape == 2) ? 4 : 2;
                    	p.setStyle(Paint.Style.FILL);
                    	c.drawPath(new SDFCircle(xDot + 1, yDot[patterns[number][i]] + yF, dotSize), p);
	                }
	            }
	        }
	        public int getPosition()
	        {
	            return position;
	        }
	        public void setPosition(int position)
	        {
	            this.position = position;
	        }
	        public void setSelected(boolean selected)
	        {
	            this.selected = selected;
	        }
	        public boolean isSelected()
	        {
	            return this.selected;
	        }
	        public void setNumber(int number)
	        {
	            this.number = number;
	        }
	        public void setColor(int color)
	        {
	            this.color = color;
	        }
	        public void setShading(int shading)
	        {
	            this.shading = shading;
	        }
	        public void setShape(int shape)
	        {
	            this.shape = shape;
	        }
	        public int getNumber()
	        {
	            return this.number;
	        }
	        public int getColor()
	        {
	            return this.color;
	        }
	        public int getShading()
	        {
	            return this.shading;
	        }
	        public int getShape()
	        {
	            return this.shape;
	        }
		}
		private class SDFTriangle extends Path
		{
			public SDFTriangle(int x, int y, int lgth)
			{
				this.moveTo(x - lgth/3, y);
				this.lineTo(x + lgth + lgth/3 , y);
				this.lineTo(x + lgth/2, y - lgth);
				this.lineTo(x - lgth/3, y);
				this.close();
			}	
		}
		private class SDFSquare extends Path
		{
			public SDFSquare(int x, int y, int lgth)
			{
				this.addRect(x, y, x + lgth, y + lgth, Direction.CW);
			}
		}
		private class SDFCircle extends Path
		{
			public SDFCircle(int x, int y, int lgth)
			{
				this.addCircle(x, y, lgth/2, Direction.CW);
			}
		}
	}	
}
