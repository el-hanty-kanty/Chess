
package mygame;

import GamePackage.*;
import PlayerPackage.*;
import Maps.DefaultMap;
import Maps.Map;
import PiecesAndAnimation.PiecesBehaviors;
import PiecesAndAnimation.PiecesFactory;
import Tools.Vector3i;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.collision.CollisionResults;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.ArrayList;
import javafx.util.Pair;


/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class Main extends SimpleApplication
{
    
    private Map defaultMap; 
    private Pair currentSelected, lastSelected, lastSelectedPiece; 
    private PiecesBehaviors piecesTypeSelected;
    private boolean firstPlayer = true, moveDone = false, promotionDone = false, checkPromotion, engineMove = false, ok = false, updateCalledEngine = false;
    private final Vector3f camLocation1 = new Vector3f(-6.5f, 8.0f, 3.5f), camLocation2 = new Vector3f(13.5f, 8.0f, 3.5f), camDirection = new Vector3f(3.5f, 0.0f, 3.5f);
    private final Player p1 = new Player("hahaha", Color.White) ;
    private final Player p2 = new Player("hehehee", Color.Black) ;
    private final AI ai = new AI(Color.Black);
    private final Game game = new Game(GameMode.Multiplayer, p1, ai) ;

    private final ActionListener actionListener = new ActionListener() 
    {
        @Override
        public void onAction(String name, boolean keyPressed, float tpf) 
        {
            if(name.equals("FlyByTheCam"))
            {
                if(!keyPressed)
                {   
                    updateCam();
                    flyCam.setEnabled(false);
                    inputManager.setCursorVisible(true);
                }
                else
                {
                    flyCam.setEnabled(true); 
                    inputManager.setCursorVisible(false);
                }
            }
            else if (name.equals("pick target") && !keyPressed) 
            {
                
                lastSelected = currentSelected;
                CollisionResults results = new CollisionResults();
                // Convert screen click to 3d position
                Vector2f click2d = inputManager.getCursorPosition(); // get mouse position on the screen
                Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone(); // converting mouse position to game world position
                Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
                // Aim the ray from the clicked spot forwards.
                Ray ray = new Ray(click3d, dir);
                // Collect intersections between ray and all nodes in results list.
                rootNode.collideWith(ray, results);
                Vector3i dimention = null;
                
                if(results.size() > 0)
                {
                    Node selectedModel = results.getCollision(0).getGeometry().getParent();
                    if(lastSelected == null || !((String)lastSelected.getValue()).equalsIgnoreCase("Piece"))
                        dimention = piecesTypeSelected.getPieceIndex(selectedModel);
                    
                    if(dimention != null)
                    {
                        currentSelected = new Pair(dimention, "Piece");
                        Vector3i pieceHighLgiht = (dimention);
                        defaultMap.removeHighlights();
                        HighlightAvailableMoves(pieceHighLgiht.getX(), pieceHighLgiht.getZ());
                    }
                    else
                    {
                        dimention = piecesTypeSelected.getPieceDimension(selectedModel);
            
                        if(dimention != null)
                            currentSelected = new Pair(dimention, "Map");
                        else
                         dimention = defaultMap.getCellIndex(selectedModel);
                        
                        if(dimention != null)
                        {
                            currentSelected = new Pair(dimention, "Map");
                        }
                    }
                    //System.out.println(currentSelected + " " + lastSelected);
                }

            }
           
        }
    };
    
    
    public static void main(String[] args) 
    {
        Main app = new Main();      
        app.start();
    }

    
    public void initModels()
    {
        defaultMap = new DefaultMap(this);
        piecesTypeSelected = PiecesFactory.GetPiecesType(this, "whiteAndBlackOriginal");
        
        currentSelected = null;
        lastSelected = null;
    }

    public void initKeys()
    {
        inputManager.addMapping("FlyByTheCam", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping("pick target", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener, "FlyByTheCam");
        inputManager.addListener(actionListener, "pick target");
    }
    
    private void updatePieces()
    {
        if(isUpdatePiecesVaild())
        {  
           // System.out.println("Update Pieces");
            Vector3i from = (Vector3i)lastSelected.getKey();
            Vector3i to = (Vector3i)currentSelected.getKey();
            String type = (String)lastSelected.getValue();
            lastSelectedPiece = new Pair(from.x, from.z);
            Cell fromC = new Cell(piecesTypeSelected.getPieceDimension(from.x, from.z).x, piecesTypeSelected.getPieceDimension(from.x, from.z).z), toC = new Cell(to.x, to.z);
            game.update(fromC, toC);
            piecesTypeSelected.Move(from, to);
            defaultMap.removeHighlights();
            
            
           // System.out.println(fromC.getRow() + " " + fromC.getColumn()  + " To " + to.x + " " + to.z);
            if(fromC.getRow() < 2)
                System.out.println("Black Move");
            else
                System.out.println("White Move");
            engineMove = !engineMove;
            if(((String)lastSelected.getValue()).equalsIgnoreCase("Engine"))
            {
                updateCalledEngine = false;
            }
            //TODO: gui display who wins the game
            if(game.isCheckmated(Color.Black))
            {
                System.out.println(" hhhh 5srt ");
              //  app.stop() ; 
            }
            else if(game.isCheckmated(Color.White))
            {
                System.out.println(" hhhh 5rst tany :P ");
            }
            else if(game.draw(Color.Black) || game.draw(Color.White))
            {
                System.out.println(" Draaaaaaaaaaaaaaaaaaaw ");
            }
            currentSelected = null;
            lastSelected = null;
        }
    }
    
    @Override
    public void simpleInitApp() 
    {
        initKeys();
        initModels();
        addLight();
        setCam();
        stateManager.attach(defaultMap);
        stateManager.attach((AppState)piecesTypeSelected); 
    }    
    
    private void HighlightAvailableMoves(int i, int j)
    {
        if (currentSelected != null  && ((String)currentSelected.getValue()).equalsIgnoreCase("Piece"))
        {
            
            Vector3i dimension = piecesTypeSelected.getPieceDimension(i, j);
           // System.out.println("Dimention " + dimension);
            ArrayList<Cell> list;
            list = game.board.pieces[dimension.x][dimension.z].possible_moves(new Cell(dimension.x, dimension.z), game.board);
   
            for(Cell c : list)
            {
                if (c.special_move != 0)
                {
                    System.out.println("Special Move");
                               // spetial mpve found
                }
                defaultMap.highLightCell(c, "Move");
            }
            
            Vector3i xyz=(Vector3i)currentSelected.getKey() ;
            Pair<Integer,Integer>pp=new Pair<>((int)xyz.x,(int)xyz.z);
        }
        
    }
    
    @Override
    public void simpleUpdate(float tpf) 
    {
        //TODO: add update code
        updatePieces(); // user Interaction
        updateCam();
        if(!updateCalledEngine)
            Engine();
        stateManager.update(tpf);
        /*
            hna 7rk el pieces bta3tk bra7tk
            al3po hna mtl3po4 fe 7ta tnya
            lw fe bug aw exception zhr 2b2 2oly
            tb3n dh m4 el 25eer bs deh 7aga t3ml beha test le el engine
            @1st paramter mkan el piece 3bara 3n vector3i (new Vector3i(r, 0, c))
            @2nd paramter el mkan el 3awz trw7w bardo 3bara 3n vector3i (new Vector3i(r, 0, c))
           originPiece.Move(1st, 2nd);
        */
    }
    
    // handling user "Weird" clicks
    private boolean isUpdatePiecesVaild()
    {
        if(lastSelected == null || currentSelected == null)
            return false;
        
        Vector3i toI = (Vector3i)currentSelected.getKey();
        Cell to = new Cell(toI.x, toI.z);
        //System.out.println(to.getRow() + " " + to.getColumn() + " " +defaultMap.isCellHighlighted(to));
        int x = ((Vector3i)lastSelected.getKey()).x, z = ((Vector3i)lastSelected.getKey()).z;
        
        return (currentSelected != null && lastSelected != null && !currentSelected.equals(lastSelected) && ((String)lastSelected.getValue()).equalsIgnoreCase("Piece") 
            && !(piecesTypeSelected.getPieceDimension(x, z).x == ((Vector3i)currentSelected.getKey()).x && piecesTypeSelected.getPieceDimension(x, z).z == ((Vector3i)currentSelected.getKey()).z)
            &&  defaultMap.isCellHighlighted(to) && ((firstPlayer && x < 2) || (!firstPlayer && x > 1))) || (engineMove && ((String)lastSelected.getValue()).equalsIgnoreCase("Engine") && x > 1);
    }
    
    // lighting the game
    private void addLight()
    {
        // for "unshaded" material
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(3.5f, 0.0f, 3.5f));
        rootNode.addLight(dl);
        
        // for "lighting" material "Doesn't affect unshadedMaterial"
        Vector3f lightTarget = new Vector3f(12, 3.5f, 30);
        SpotLight spot=new SpotLight();
        spot.setSpotRange(1000);
        spot.setSpotInnerAngle(5*FastMath.DEG_TO_RAD);
        spot.setSpotOuterAngle(10*FastMath.DEG_TO_RAD);
        spot.setPosition(new Vector3f(3.5f, 50.0f, 3.5f));
        spot.setDirection(new Vector3f(3.5f, 0.0f, 3.5f).subtract(spot.getPosition()));     
        spot.setColor(ColorRGBA.White.mult(1));
        rootNode.addLight(spot);
    }
    
    // setting cam location for first time
    private void setCam()
    {
        flyCam.setMoveSpeed(20);
        cam.setLocation(camLocation1);
        inputManager.setCursorVisible(false);
        cam.lookAt(camDirection, Vector3f.ZERO);
    }
    
    private void Engine()
    {
       if(engineMove && ok)
       {
            updateCalledEngine = true;
            SingleMove sm = ai.root(3, true, game.board);
            Cell from = sm.getFrom(), to = sm.getTo();
            System.out.println("Engine Move");
            System.out.println(from.getRow() + " " + from.getColumn()  + " to " +  to.getRow() +  " " + to.getColumn() + " color " + game.board.pieces[from.getRow()][from.getColumn()].getColor());
            
            if(from.getRow() == -1 && from.getColumn() == -1 && to.getRow() == -1 && to.getColumn() == -1)
            {
                System.out.print("Out of Moves");
            }
            else
            {
                int r = piecesTypeSelected.getPieceIndex(from.getRow(), from.getColumn()).x, c = piecesTypeSelected.getPieceIndex(from.getRow(), from.getColumn()).z;
                lastSelected = new Pair(new Vector3i(r, 0, c), "Engine");
                currentSelected = new Pair(new Vector3i(to.getRow(), 0, to.getColumn()), "Map");
            }
       }
    }
    
    // update the cam with each move
    private void updateCam()
    {
        // using OR gatae as "simpleUpdate" is being called every single Second xD  
        moveDone |= piecesTypeSelected.isMoveDone();
        
        if(moveDone)
        {
            int i = (int)lastSelectedPiece.getKey(), j = (int)lastSelectedPiece.getValue();
            checkPromotion |= piecesTypeSelected.checkPromotion(i, j);
            
            if(!checkPromotion)
            {
                promotionDone = true;
            }
            else
            {
               // System.out.println("Promotion is Vaild");
                int type = 3;
                Vector3i to = piecesTypeSelected.getPieceDimension(i, j);
                Cell toC = new Cell(to.x, to.z);
                //type = uerChoice() ;  .// xml 
                //Todo: get user selcted type "GUI" 0 -> rock, 1 -> bishop, 2 -> knight, 3 -> queen
                game.makePromotion(toC, type);
                piecesTypeSelected.promote(i, j, type);
                checkPromotion = false;
                promotionDone = true;
            }
        }
        
        if(moveDone && promotionDone)
        {   
            ok = !ok;
            firstPlayer = !firstPlayer;
            if(firstPlayer)
                cam.setLocation(camLocation1);
            else
                cam.setLocation(camLocation2);
        
            moveDone = promotionDone = false;
          //  System.out.println("Move is done");
        }
        cam.lookAt(camDirection, Vector3f.ZERO);  
    }
}
