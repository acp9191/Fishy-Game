import tester.*;                // The tester library
import javalib.worldimages.*;   // images, like RectangleImage or OverlayImages
import javalib.funworld.*;      // the abstract World class and the big-bang library
import java.awt.Color;          // general colors (as triples of red,green,blue values)
// and predefined colors (Red, Green, Yellow, Blue, Black, White)
import java.util.Random;

/* Interfaces */

//Interface for representing lists of any type
interface IList<T> {

  <U> IList<U> map(IFunc<T, U> f);

  <U> U foldr(IFunc2<T, U, U> func, U base);

  <U> boolean ormap(IPredicate<T> pred);

  <U> boolean andmap(IPredicate<T> pred);

  IList<T> filter(IPredicate<T> pred);

}

//Interface for one-argument function objects iwth signature [A -> R]
interface IFunc<A, R> {
  R apply(A arg);
}

//Interface for two-argument function-objects with signature [A1, A2 -> R]
interface IFunc2<A1, A2, R> {
  R apply(A1 argument1, A2 argument2);
}

//Interface to a boolean Predicate
interface IPredicate<T> {
  boolean apply(T t);
}

//class that represents a ConsList
class Cons<T> implements IList<T> {
  T first;
  IList<T> rest;

  Cons(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
  }

  public <U> IList<U> map(IFunc<T, U> f) {
    return new Cons<U>(f.apply(this.first), this.rest.map(f));
  }

  public <U> U foldr(IFunc2<T, U, U> func, U base) {
    return func.apply(this.first,
        this.rest.foldr(func, base));
  }

  public <U> boolean ormap(IPredicate<T> pred) {
    if (pred.apply(this.first)) {
      return true;
    } else {
      return this.rest.ormap(pred);
    }
  }

  public <U> boolean andmap(IPredicate<T> pred) {
    if (pred.apply(this.first)) {
      return this.rest.andmap(pred);
    } else {
      return false;
    }
  }

  public IList<T> filter(IPredicate<T> pred) {
    if (pred.apply(this.first)) {
      return new Cons<T>(this.first, this.rest.filter(pred));
    } else {
      return this.rest.filter(pred);
    }
  }
}

//class that represents an Emptylist
class Empty<T> implements IList<T> {

  public <U> IList<U> map(IFunc<T, U> f) { 
    return new Empty<U>(); 
  }

  public <U> U foldr(IFunc2<T, U, U> func, U base) {
    return base;
  }

  public <U> boolean ormap(IPredicate<T> pred) {
    return false;
  }

  public <U> boolean andmap(IPredicate<T> pred) {
    return true;
  }


  public IList<T> filter(IPredicate<T> pred) {
    return this;
  }
}

//class that moves the background Fish
class Movefishes implements IFunc<Fish, Fish> {
  public Fish apply(Fish f) {
    if (f.isRight) {
      if (f.location.x >= FishyGame.GAME_WIDTH + f.width) {
        return new Fish(new Posn(1 + f.width,
            (int)(Math.random() * 800 + 1)), f.width, f.col, f.isRight);
      }
      else if (f.location.x <= 0 - f.width) {
        return new Fish(new Posn(799 - f.width, 
            (int)(Math.random() * 800 + 1)), f.width,  f.col, f.isRight);
      }
      else {
        if (f.width >= 0 && f.width < 20) {
          return new Fish(new Posn(f.location.x + 15, f.location.y ),f.width,f.col,f.isRight);
        }
        else if (f.width >= 20 && f.width < 35) {
          return new Fish(new Posn(f.location.x + 8, f.location.y),f.width,f.col,f.isRight);
        }
        else if (f.width >= 35 && f.width < 50) {
          return new Fish(new Posn(f.location.x+5, f.location.y),f.width,f.col,f.isRight);
        }
        else {
          return new Fish(new Posn(f.location.x + 3, f.location.y),f.width,f.col,f.isRight);
        }
      }
    } else {
      if (f.location.x >= FishyGame.GAME_WIDTH + f.width) {
        return new Fish(new Posn(1 + f.width, 
            (int)(Math.random() * 800 + 1)), f.width, f.col, f.isRight);
      }
      else if (f.location.x <= 0 - f.width) {
        return new Fish(new Posn(799 - f.width, 
            (int)(Math.random() * 800 + 1)), f.width, f.col, f.isRight);
      }
      else {
        if (f.width >= 0 && f.width < 20) {
          return new Fish(new Posn(f.location.x - 15, f.location.y ),f.width,f.col,f.isRight);
        }
        else if (f.width >= 20 && f.width < 35) {
          return new Fish(new Posn(f.location.x - 8, f.location.y),f.width,f.col,f.isRight);
        }
        else if (f.width >= 35 && f.width < 50) {
          return new Fish(new Posn(f.location.x - 5, f.location.y),f.width,f.col,f.isRight);
        }
        else {
          return new Fish(new Posn(f.location.x - 3, f.location.y),f.width,f.col,f.isRight);
        }
      }
    }


  }  
}

/* Predicates */ 

//Function object that prints the background fish on the scene
class PrintBackgroundFish implements IFunc2<Fish, WorldScene, WorldScene> {

  public WorldScene apply(Fish f, WorldScene w) {
    return w.placeImageXY(f.fishImage(), f.location.x, f.location.y);
  }
}

//Function object that determines when two fish have collided
class CheckCollision implements IPredicate<Fish> {
  Fish player;

  CheckCollision(Fish player) {
    this.player = player;
  }

  public boolean apply(Fish other) {
    return (int)(Math.abs(this.player.location.x - other.location.x)) <= other.width
        && (int)(Math.abs(this.player.location.y - other.location.y)) <= other.width;
  }
}


//Function object that negates a given predicate
class Negate<T> implements IPredicate<T> {
  IPredicate<T> p;

  Negate(IPredicate<T> p) {
    this.p = p;
  }

  public boolean apply(T t) {
    return !p.apply(t);
  }
}

//Function object that determines if the player fish is larger than the given fish
class IsBiggerFish implements IPredicate<Fish> {
  Fish player;

  IsBiggerFish(Fish player) {
    this.player = player;
  }

  public boolean apply(Fish other) {
    return (this.player.sizeOfFish() > other.sizeOfFish());
  }
}

// ** Class that represents a fishy that moves around the canvas */
class Fish {
  Posn location; // represents the location of the fish
  int width;    // represents the size of the fish
  Color col;    // represents the color of the fish
  boolean isRight; // represents the direction of the fish

  /** The constructor. */
  Fish(Posn location, int width,Color col, boolean isRight) {
    this.location = location;
    this.width = width;
    this.col = col;
    this.isRight = isRight;

  }

  /* Another constructor. */
  Fish() {
    this(new Posn((int)(800),(int)(Math.random() * 800 + 1)), 
        (int)(Math.random() * 50 + 5), 
        new Color((int)(Math.random() * 255 + 0),
            (int)(Math.random() * 255 + 0),(int)(Math.random() * 255 + 0)), 
        new Random().nextBoolean());
  }


  /* produce the image of this fish. */
  WorldImage fishImage() {
    if (this.isRight) {
      return new BesideImage(
          new RotateImage(new EquilateralTriangleImage(this.width, "solid", this.col),90),
          new CircleImage(this.width,"solid", this.col));
    } else {
      return new BesideImage(
          new CircleImage(this.width,"solid", this.col),
          new RotateImage(new EquilateralTriangleImage(this.width, "solid", this.col),270));
    }

  }

  /* produce a new fish that has grown. */
  Fish grow(Cons<Fish> other) {
    if (other.first.width >= 0 && other.first.width < 10) {
      return new Fish(this.location,this.width + 2,this.col,this.isRight);
    }
    else if (other.first.width >= 10 && other.first.width < 20) {
      return new Fish(this.location,this.width + 3,this.col,this.isRight);
    }
    else if (other.first.width >= 20 && other.first.width < 30) {
      return new Fish(this.location,this.width + 5,this.col,this.isRight);
    }
    else {
      return new Fish(this.location,this.width + 7,this.col,this.isRight);
    }

  }



  /* Determines sizeOf Fish */
  int sizeOfFish() {
    return this.width * this.width;
  }

  /*
   * Handles fish-player movement | collisions
   */
  public Fish moveFish(String ke) {
    /* Handles player boundaries */
    if (this.location.x >= FishyGame.GAME_WIDTH + this.width) {
      return new Fish(new Posn(1 + this.width, this.location.y), 
          this.width, this.col, this.isRight);
    }
    else if (this.location.x <= 0 - this.width) {
      return new Fish(new Posn(799 - this.width, this.location.y), 
          this.width, this.col, this.isRight);
    }

    else if (this.location.y == FishyGame.GAME_HEIGHT - this.width) {
      return new Fish(new Posn(this.location.x, this.location.y - 5), 
          this.width, this.col, this.isRight);
    }
    else if (this.location.y == 0 + this.width) {
      return new Fish(new Posn(this.location.x, this.location.y + 5), 
          this.width, this.col, this.isRight);
    }
    /* Handles player movement controlled by keystrokes */
    else if (ke.equals("right")) {
      return new Fish(new Posn(this.location.x + 5, this.location.y),
          this.width, this.col,true); 
    } else if (ke.equals("left")) {
      return new Fish(new Posn(this.location.x - 5, this.location.y),
          this.width,  this.col,false);
    } else if (ke.equals("up")) {
      return new Fish(new Posn(this.location.x, this.location.y - 5),
          this.width, this.col, this.isRight);
    } else if (ke.equals("down")) {
      return new Fish(new Posn(this.location.x, this.location.y + 5),
          this.width,this.col, this.isRight);
    } 
    // change the color to a random color
    else if (ke.equals("R")) {
      return new Fish(this.location, this.width,new Color((int)(Math.random() * 255 + 0),
          (int)(Math.random() * 255 + 0),(int)(Math.random() * 255 + 0)),
          this.isRight);
    }
    //incognito mode
    else if (ke.equals("T")) {
      return new Fish(this.location, this.width,  new Color(255,255,255), this.isRight);
    }

    else {
      return this;
    }
  }
}

class FishyGame extends World {
  final static int GAME_WIDTH = 800;
  final static int GAME_HEIGHT = 800;
  Fish player;
  IList<Fish> fishies;
  IFunc<Fish,Fish> movefishies = new Movefishes();
  IFunc2<Fish, WorldScene, WorldScene> printbgf = new PrintBackgroundFish();
  IPredicate<Fish> checkCollisions; 

  IPredicate<Fish> isBiggerFish; 
  IPredicate<Fish> noCollisions;
  IPredicate<Fish> isSmallerFish;



  /* The constructor */
  public FishyGame(Fish player, IList<Fish> fishies) {
    super();
    this.player = player;
    this.fishies = fishies;
    checkCollisions = new CheckCollision(this.player);

    isBiggerFish = new IsBiggerFish(this.player);
    noCollisions = new Negate<Fish>(checkCollisions);
    isSmallerFish = new Negate<Fish>(isBiggerFish);
  }

  /*Move the Fish when the player presses a key */
  public World onKeyEvent(String ke) {
    if (ke.equals("x")) {
      return this.endOfWorld("Goodbye");
    }
    else {
      return new FishyGame(this.player.moveFish(ke), this.fishies);
    }
  }

  /* produce the last image of this world by adding text to the image */
  public WorldScene lastScene(String s) {
    return this.makeScene().placeImageXY(new TextImage(s, 100, Color.red), 400,
        400);
  }

  public FishyGame eatFishy() {

    if (this.fishies.ormap(checkCollisions)) {
      if (this.fishies.filter(checkCollisions).andmap(isBiggerFish)) {
        return new FishyGame(this.player.grow((Cons<Fish>)(this.fishies.filter(checkCollisions))), 
            new Cons<Fish>((new Fish()),
                this.fishies.filter(noCollisions)));
      } else {
        return this;
      }
    }
    else {
      return this;
    }
  }
  
  public WorldEnd worldEnds() {
    if (this.fishies.filter(checkCollisions).ormap(isSmallerFish)) {
      return new WorldEnd(true,
          this.lastScene("YOU LOST!"));
    }
    else if (this.fishies.andmap(isBiggerFish)) {
      return new WorldEnd(true,
          this.lastScene("YOU WON!"));
    } 
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }





  public boolean getRandomBoolean() {
    Random random = new Random();
    return random.nextBoolean();
  }

  public World onTick() {
    return new FishyGame(this.player, this.fishies.map(movefishies)).eatFishy();
  }


  public WorldScene makeScene() {
    return 
        this.fishies.foldr(printbgf, getEmptyScene())
        .placeImageXY(this.player.fishImage(), this.player.location.x,
            this.player.location.y);
  }

}



class ExamplesFishy {
  Fish fishy = new Fish(new Posn(150,100), 15,  Color.RED, true);
  Fish efish = new Fish(new Posn(120,100),15,Color.RED, false);
  Fish efish2 = new Fish(new Posn(150,50),15,Color.CYAN, true);
  Fish fishy1 = new Fish();
  Fish Fishy1b = new Fish(new Posn(150,100), 100000, Color.RED, false);
  Fish efish3 = new Fish();
  Fish efish4 = new Fish();
  Fish efish5 = new Fish();
  Fish efish6 = new Fish();

  Fish fishyU = new Fish(new Posn(155,100), 15,Color.RED, true);
  Fish efishU = new Fish(new Posn(115,100), 15,Color.RED, false);
  Fish efish2U = new Fish(new Posn(155,50),15,Color.CYAN, true);

  IFunc<Fish,Fish> movefishies = new Movefishes();
  IList<Fish> Empty = new Empty<Fish>();
  Cons<Fish> bgfish = new Cons<Fish>(fishy,
      new Cons<Fish>(efish,
          new Cons<Fish>(efish2, Empty)));
  IList<Fish> bgfishu = new Cons<Fish>(fishy1,
      new Cons<Fish>(efish3,
          new Cons<Fish>(efish4, Empty)));
  IList<Fish> bgfishu2 = new Cons<Fish>(fishyU,
      new Cons<Fish>(efishU,
          new Cons<Fish>(efish2U, Empty)));

  FishyGame fishTank = new FishyGame(this.fishy, this.bgfish);
  Fish player = new Fish(new Posn(150,100), 20, Color.RED, true);

  boolean testWinnerFish(Tester t) {
    return t.checkExpect(this.bgfishu.andmap(new IsBiggerFish(this.Fishy1b)), true)
        && t.checkExpect(this.bgfishu.andmap(new IsBiggerFish(this.efish4)), false);
  }


  boolean testMoveFishes(Tester t) {
    return t.checkExpect(this.bgfish.map(movefishies), bgfishu2);
  }

  boolean testfishGrow(Tester t) {
    return t.checkExpect(this.fishy.grow(bgfish), 
        new Fish(new Posn(150,100), 20, Color.RED, true));
  }

  boolean testcheckCollisions(Tester t) {
    return t.checkExpect(this.bgfish.filter(new CheckCollision(this.fishy)), 
        new Cons<Fish>(this.fishy , Empty));
  }

  boolean testEatFishy(Tester t) {
    return t.checkExpect(this.fishTank.worldEnds(), new FishyGame(this.player, this.bgfish));
  }

  boolean testCheckCollisions(Tester t) {
    return t.checkExpect(this.bgfish.ormap(new CheckCollision(this.fishy)), true) 
        && t.checkExpect(this.bgfish.ormap(new CheckCollision(this.efish3)), false)
        && t.checkExpect(this.bgfish.ormap(new CheckCollision(this.efish5)), true)
        && t.checkExpect(this.Empty.ormap(new CheckCollision(this.efish5)), false);

  }

  /** test the method moveBlob in the Blob class */

  public static void main(String[] argv) {
    Fish player = new Fish(new Posn(400,400), 18,  Color.RED, true);
    Fish efish =  new Fish();
    Fish efish3 = new Fish();
    Fish efish5 = new Fish();
    Fish efish6 = new Fish();
    Fish efish7 = new Fish(new Posn(500,300), 10,  Color.RED, true);
    Fish efish8 = new Fish(new Posn(200,300), 15,  Color.magenta, true);
    Fish efish9 = new Fish(new Posn(100,700), 18,  Color.green, true);
    Fish efish10 = new Fish(new Posn(700,100), 20, Color.cyan, true);
    IList<Fish> Empty = new Empty<Fish>();

    IList<Fish> bgfish1 = new Cons<Fish>(efish3,
        new Cons<Fish>(efish,
            new Cons<Fish>(efish5,
                new Cons<Fish>(efish6, 
                    new Cons<Fish>(efish5,
                        new Cons<Fish>(efish7, 
                            new Cons<Fish>(efish8,
                                new Cons<Fish>(efish9,
                                    new Cons<Fish>(efish10,
                                        Empty)))))))));

    FishyGame w = new FishyGame(player, bgfish1);
    w.bigBang(800, 800, .2);

  }

}

