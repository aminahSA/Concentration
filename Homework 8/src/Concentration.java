import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.*;

class Card {
  int rank;
  String suit;
  int x;
  int y;
  WorldImage pic;
  WorldImage picDown;
  WorldImage picUp;
  boolean faceup;
  boolean active;

  Card(int rank, String suit) {

    if ((rank <= 13) && (rank > 0)) {
      this.rank = rank;
    }

    else {
      throw new IllegalArgumentException("Invalid Rank");
    }

    if ((suit.equals("♥")) || (suit.equals("♦")) || (suit.equals("♠")) || (suit.equals("♣"))) {
      this.suit = suit;
    }

    else {
      throw new IllegalArgumentException("Invalid Suit");
    }
    
    this.faceup = false;
    this.active = true;
    WorldImage rankpic = new Utils().numberToImage(this.rank);
    WorldImage suitpic = new TextImage(this.suit, 10, Color.WHITE);
    this.picUp = new OverlayOffsetImage(suitpic, 10, 10,
        new OverlayImage(rankpic, new RectangleImage(50, 70, OutlineMode.SOLID, Color.RED)));
    this.picDown = new RectangleImage(50, 70, OutlineMode.OUTLINE, Color.BLUE);
    this.pic = picDown;

  }

  // Draws this card onto the background at its appropriate coordinates based on
  // the index
  public WorldScene drawOn(WorldScene background, int index) {

    int row;
    int col;
    if (index < 13) {
      row = 1;
      col = index + 1;
    }

    else if (index < 26) {
      row = 2;
      col = index - 12;
    }

    else if (index < 39) {
      row = 3;
      col = index - 25;
    }

    else {
      row = 4;
      col = index - 38;
    }

    this.x = col * 50 - 25;
    this.y = row * 70 - 35;
    background.placeImageXY(this.pic, this.x, this.y);
    return background;

  }

  // flips the card over, displaying a new image
  public void flip() {
    if (this.faceup) {
      this.pic = this.picDown;
      this.faceup = false;
    }

    else {
      this.pic = this.picUp;
      this.faceup = true;
    }

  }

}

class Concentration extends World {

  ArrayList<Card> cards;
  int score;
  int tickCount; // standardized the amount of ticks after flipping two cards over

  Concentration(ArrayList<Card> cards) {
    this.cards = cards;
    this.score = 26;
    this.tickCount = 0;
  }

  // draws the game
  public WorldScene makeScene() {

    WorldScene bg = new WorldScene(650, 350);
    WorldImage scoreImg = new TextImage("Score: ", 10, Color.WHITE);
    for (int i = 26; i >= 0; i--) {
      if (i == this.score) {
        scoreImg = new TextImage("Score: " + i, 20, Color.WHITE);
      }
    }

    WorldImage window = new RectangleImage(650, 70, OutlineMode.SOLID, Color.BLUE);
    WorldImage scoreDisplay = new OverlayImage(scoreImg, window);
    bg.placeImageXY(scoreDisplay, 325, 315);
    for (Card c : cards) {
      bg = c.drawOn(bg, cards.indexOf(c));
    }

    return bg;
  }

  // stuff
  public void onTick() {
    
    if(this.score == 0) {
      this.endOfWorld("You win!");
    }
     
    ArrayList<Card> faceupCards = new ArrayList<Card>(2);
    for (Card c : cards) {
      if (c.faceup && c.active) {
        faceupCards.add(c);
      }
    }

    if (faceupCards.size() == 2) {
      this.tickCount++;
      if ((this.tickCount % 30) == 0) {
        new Utils().anyMatches(faceupCards, this);
      }
    }
  }

  // flips a card if the mouse is clicked on it
  public void onMouseClicked(Posn pos) {
    ArrayList<Card> faceupCards = new ArrayList<Card>();
    int n = 0;
    for (Card c : cards) {
      if (c.faceup && c.active) {
        n++;
        faceupCards.add(c);
      }
    }

    if (n < 2) {
      for (Card c : cards) {
        if (pos.x <= c.x + 25 && pos.x >= c.x - 25 && pos.y <= c.y + 35 && pos.y >= c.y - 35
            && c.active && faceupCards.size() < 2) {
          c.flip();
        }
      }
    }
  }

  // resets the game with a "r" key press
  public void onKeyEvent(String key) {
    ArrayList<Card> newDeck = new Utils().makeDeck();
    if (key.equals("r")) {
      this.cards = newDeck;
      this.score = 26;
      this.tickCount = 0;
    }
  }

  //last scene
  public WorldScene lastScene(String msg) {
    WorldImage message = new TextImage(msg, 20, Color.RED);

    WorldScene scene = new WorldScene(650, 350);
    scene.placeImageXY(message, 325, 175);
    return scene;
  }

}

class Utils {

  // makes a standard deck in randomized order
  ArrayList<Card> makeDeck() {

    ArrayList<String> suits = new ArrayList<String>(Arrays.asList("♥", "♦", "♠", "♣"));
    ArrayList<Card> deck = new ArrayList<Card>();
    
    for (String suit : suits) {
      ArrayList<Card> cards = new ArrayList<Card>();
      for (int j = 0; j < 13; j++) {
        cards.add(new Card(j + 1, suit));
      }
      deck.addAll(cards);
    }

    Collections.shuffle(deck);
    return deck;

  }

  public void anyMatches(ArrayList<Card> faceupCards, Concentration world) {

    if (faceupCards.get(0).rank == faceupCards.get(1).rank) {
      faceupCards.get(0).active = false;
      faceupCards.get(0).pic = new RectangleImage(50, 70, OutlineMode.SOLID, Color.LIGHT_GRAY);
      faceupCards.get(1).active = false;
      faceupCards.get(1).pic = new RectangleImage(50, 70, OutlineMode.SOLID, Color.LIGHT_GRAY);
      world.score = world.score - 1;
    }

    else {
      faceupCards.get(0).flip();
      faceupCards.get(1).flip();
    }

  }

  // produces an image from a rank number

  WorldImage numberToImage(int rank) {

    ArrayList<String> strings = new ArrayList<String>(

        Arrays.asList("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"));

    WorldImage img = new RectangleImage(5, 5, OutlineMode.SOLID, Color.BLUE);

    for (int i = 0; i < strings.size(); i++) {

      if (rank == i + 1) {

        img = new TextImage(strings.get(i), 10, Color.WHITE);

      }

    }

    return img;

  }

}

class Examples {

  Card aceC;

  Card kingD;

  Card aceH;

  Card sevenS;

  ArrayList<Card> exampleList;
  
  ArrayList<Card> listWithMatch;

  ArrayList<Card> lst;

  ArrayList<Card> mydeck;

  ArrayList<Card> empty;

  Concentration world;

  WorldScene background;

  RectangleImage cardTable;

  RectangleImage cardFD;

  ArrayList<Card> scrambledList;

  ArrayList<Card> singleItemList;

  ArrayList<Card> twoItemList;

  Concentration worldTestEmpty;

  Concentration worldTestSmaller;

  Concentration worldTestSmaller2;
  
  Card aceUp;

  // initializes the data
  void initData() {

    mydeck = new Utils().makeDeck();
    world = new Concentration(mydeck);
    background = new WorldScene(650, 350);
    cardFD = new RectangleImage(50, 70, OutlineMode.OUTLINE, Color.BLUE);
    empty = new ArrayList<Card>();
    this.aceC = new Card(1, "♣");
    this.kingD = new Card(13, "♦");
    this.aceH = new Card(1, "♥");
    this.sevenS = new Card(7, "♠");
    exampleList = new ArrayList<Card>(Arrays.asList(this.aceC, this.kingD, this.aceH, this.sevenS));
    
    this.aceUp = new Card(1, "♣");
    aceUp.flip();
    listWithMatch = new ArrayList<Card>(
        Arrays.asList(this.aceUp, this.aceUp, this.kingD, this.aceH, this.sevenS));
    
    worldTestEmpty = new Concentration(empty);
    worldTestSmaller = new Concentration(singleItemList);
    worldTestSmaller2 = new Concentration(twoItemList);
    scrambledList = new ArrayList<Card>(
        Arrays.asList(this.kingD, this.sevenS, this.aceC, this.aceH));
    singleItemList = new ArrayList<Card>(Arrays.asList(this.kingD));
    twoItemList = new ArrayList<Card>(Arrays.asList(this.kingD, this.aceC));
  }

  // Test Big Bang
  void testBigBang(Tester t) {

    this.initData();
    Concentration world = new Concentration(mydeck);
    int worldWidth = 650;
    int worldHeight = 350;
    double tickRate = .02;
    world.bigBang(worldWidth, worldHeight, tickRate);
  }

//  // Testing for illegal card constructions -> illegal rank
//  void testIllegalCardRank(Tester t) {
//    this.initData();
//    t.checkConstructorException(new IllegalArgumentException("Invalid Rank"), "Card", 0, "♠");
//    t.checkConstructorException(new IllegalArgumentException("Invalid Rank"), "Card", 14, "♠");
//  }
//
//  // Testing for illegal card constructions -> illegal suit
//  void testIllegalCardSuit(Tester t) {
//    this.initData();
//    t.checkConstructorException(new IllegalArgumentException("Invalid Suit"), "Card", 4, "spade");
//  }

//  // Testing the drawOn method
//  void testDrawOn(Tester t) {
//    this.initData();
//    t.checkExpect(this.background, new WorldScene(650, 350));
//    this.aceC.drawOn(this.background, 0);
//
//    WorldScene check1 = new WorldScene(650, 3500);
//
//    check1.placeImageXY(aceC.picDown, 25, 35);
//
//    t.checkExpect(this.background, check1);
//
//    this.initData();
//
//    t.checkExpect(this.aceH.pic, this.cardFD);
//
//    this.aceH.flip();
//
//    t.checkExpect(this.aceH.pic, this.aceH.picUp);
//
//    this.aceH.drawOn(this.background, 0);
//
//    WorldScene check = new WorldScene(650, 280);
//
//    check.placeImageXY(aceH.picUp, 25, 35);
//
//    t.checkExpect(this.background, check);
//
//    this.initData();
//
//    t.checkExpect(this.background, new WorldScene(650, 280));
//
//    this.kingD.drawOn(background, 13);
//
//    WorldScene check2 = new WorldScene(650, 280);
//
//    check2.placeImageXY(kingD.picDown, 25, 105);
//
//    t.checkExpect(this.background, check2);
//
//  }
//
//  // Testing the flip method
//
//  void testFlip(Tester t) {
//
//    this.initData();
//
//    // testing before flipping the card
//
//    t.checkExpect(this.aceC.pic, this.cardFD);
//
//    // flip the card and check to see if it has changed
//
//    this.aceC.flip();
//
//    t.checkExpect(this.aceC.pic, this.aceC.picUp);
//
//    // testing the ability to flip the same card back
//
//    this.aceC.flip();
//
//    t.checkExpect(this.aceC.pic, this.cardFD);
//
//    // testing an additional card -> same facedown default
//
//    t.checkExpect(this.aceH.pic, this.cardFD);
//
//    // testing the other card w/ a flip -> will have a different face-up look
//
//    // compared to other cards
//
//    this.aceH.flip();
//
//    t.checkExpect(this.aceH.pic, this.aceH.picUp);
//
//  }
//
//  // Testing the makeScene method
//
//  void testMakeScene(Tester t) {
//
//    this.initData();
//
//    // testing creating an empty worldScene
//
//    t.checkExpect(this.worldTestEmpty.makeScene(), this.background);
//
//    // testing creating WorldScene with one card
//
//    this.background.placeImageXY(cardFD, 25, 35);
//
//    t.checkExpect(this.worldTestSmaller.makeScene(), this.background);
//
//    // testing creating a WorldScene with two cards
//
//    this.background.placeImageXY(cardFD, 75, 35);
//
//    t.checkExpect(this.worldTestSmaller2.makeScene(), this.background);
//
//  }
//
//  // Testing the onMouseClicked method
//
//  void testOnMouseClicked(Tester t) {
//
//    this.initData();
//
//    Concentration c1 = new Concentration(this.exampleList);
//
//    // testing that before any click the card is face-down
//
//    t.checkExpect(this.kingD.pic, this.kingD.picDown);
//
//    // testing that clicking the card will flip it
//
//    c1.makeScene();
//
//    c1.onMouseClicked(new Posn(78, 32));
//
//    t.checkExpect(this.kingD.pic, this.kingD.picUp);
//
//    // testing that clicking the card will flip it again
//
//    c1.onMouseClicked(new Posn(76, 40));
//
//    t.checkExpect(this.kingD.pic, this.kingD.picDown);
//
//    // checking that a different card can also be flipped
//
//    c1.onMouseClicked(new Posn(175, 10));
//
//    t.checkExpect(this.sevenS.pic, this.sevenS.picUp);
//
//  }
//
//  // Testing the makeDeck method
//
//  void testMakeDeck(Tester t) {
//
//    this.initData();
//
//    // testing the size of the shuffled deck
//
//    t.checkExpect(new Utils().makeDeck().size(), 52);
//
//    // testing that another created deck is of the same size
//
//    t.checkExpect(new Utils().makeDeck().size(), 52);
//
//    // testing that there are 4 of a given rank
//
//    ArrayList<Card> deck = new Utils().makeDeck();
//
//    int rankcount = 0;
//
//    for (Card c : deck) {
//
//      if (c.rank == 1) {
//
//        rankcount++;
//
//      }
//
//    }
//
//    t.checkExpect(rankcount, 4);
//
//    // testing that there are 13 cards of a given suit
//    ArrayList<Card> deck2 = new Utils().makeDeck();
//
//    int suitcount = 0;
//
//    for (Card c : deck2) {
//
//      if (c.suit.equals("♠")) {
//
//        suitcount++;
//
//      }
//
//    }
//
//    t.checkExpect(suitcount, 13);
//
//  }
//
//  // Testing the Collections.shuffle method
//  void testShuffle(Tester t) {
//
//    this.initData();
//
//    t.checkFail(this.exampleList, this.scrambledList);
//
//    Random ran = new Random();
//
//    ran.setSeed(123456789);
//
//    Collections.shuffle(this.exampleList, ran);
//
//    t.checkExpect(this.exampleList, this.scrambledList);
//
//  }
//
//  // Testing the numberToImage method
//  void testNumberToImage(Tester t) {
//    this.initData();
//
//    // example for getting Ace
//    t.checkExpect(new Utils().numberToImage(1), new TextImage("A", 10, Color.WHITE));
//
//    // example for number & using a card's rank
//    t.checkExpect(new Utils().numberToImage(this.sevenS.rank), new TextImage("7", 10, Color.WHITE));
//
//    // example for a king
//    t.checkExpect(new Utils().numberToImage(this.kingD.rank), new TextImage("K", 10, Color.WHITE));
//
//  }
  
  //test on tick
  void testonTick(Tester t) {
    this.initData();

    Concentration c1 = new Concentration(this.listWithMatch);
    c1.tickCount = 29;
    t.checkExpect(this.aceUp.pic, this.aceUp.picUp);

    c1.onTick();
    t.checkExpect(this.aceUp.active, false);
    t.checkExpect(this.aceUp.pic, new RectangleImage(50, 70, OutlineMode.SOLID, Color.LIGHT_GRAY));

    
    this.initData();
    this.aceC.flip();
    ArrayList<Card> twoAces = new ArrayList<Card>(
        Arrays.asList(this.aceUp, this.aceC));

    Concentration c2 = new Concentration(twoAces);
    c2.tickCount = 29;
    c2.onTick();
    t.checkExpect(this.aceC.pic, new RectangleImage(50, 70, OutlineMode.SOLID, Color.LIGHT_GRAY));
    
    
    this.initData();
    this.aceC.flip();
    this.kingD.flip();
    ArrayList<Card> twoDifferent = new ArrayList<Card>(
        Arrays.asList(this.kingD, this.aceC));

    Concentration c3 = new Concentration(twoDifferent);
    c3.tickCount = 29;
    c3.onTick();
    t.checkExpect(this.aceC.pic, this.aceC.picDown);
    t.checkExpect(this.kingD.pic, this.kingD.picDown);
  }

}