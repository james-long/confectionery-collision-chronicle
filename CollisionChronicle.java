/**************************************************************************************************************
 * Assignment: Confectionery Collision Chronicle 
 * Description: This program is a match-3 game containing a leaderboard, instructions, and two game modes
 *              (timed and moves). The goal is to move blocks around and create matches of 3 or more, which
 *              removes the matched blocks and adds to the player's score. A score of 250000 is needed to win,
 *              and reaching any lose condition (running out of 1:30 in timed mode and running out of 30 moves
 *              in moves mode) will cause a loss. The game has three cheats: the basic cheat which stops the
 *              lose condition from ever activating (either stops time or stops move consumption), the "Win"
 *              cheat which causes a near-instant win, and the "Lose" cheat which causes a near-instant loss.
 * Author: James Long
 * Date: June 13, 2016
 * Course: ICS3U
 ***************************************************************************************************************/

//import required packages
import java.util.ArrayList;
import javax.swing.Timer;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Scanner;

public class CollisionChronicle 
  extends JPanel 
  implements ActionListener
{
  //#variable
  /*******************************************************************************************
    * Variable Dictionary : Animation Variables
    * static boolean pausing - stores whether or not the pause animation is in progress
    * static boolean interactive - stores whether or not the user can interact with the game
    * static boolean animating - stores whether or not the detonate animation is in progress
    * static Timer pauseTimer - when started, pauses the game for 0.1s
    * static Timer detonateTimer - when started, detonates all matches with an animation
    * static ArrayList<Integer> detonateRow - stores row values of blocks to be detonated
    * static ArrayList<Integer> detonateCol - stores column values of blocks to be detonated
    * static ImageIcon[] detonateIcons - stores the frames for the detonation animation
    ******************************************************************************************/
  static boolean pausing = false;
  static boolean interactive = true;
  static boolean animating = false;
  static Timer pauseTimer = new Timer(100, new ActionListener(){
    public void actionPerformed(ActionEvent e) {
      pausing = false;
      pauseTimer.stop();
      updateBoard();
    } //once animation is done, stop pausing and update the board
  });
  static Timer detonateTimer = new Timer(50, new ActionListener(){
    int detIndex = 0; //current detonation animation frame
    public void actionPerformed(ActionEvent e)
    {
      //cycle through frames of detonation animation for all blocks to be detonated
      for(int i = 0; i < detonateRow.size(); i++)
      {
        gameBtns[detonateRow.get(i)][detonateCol.get(i)].setIcon(detonateIcons[detIndex]);
      }
      if (detIndex < detonateIcons.length - 1) 
      {
        detIndex++;
      }
      //return control to player, reset variables, and stop animation timer 
      else 
      {
        detIndex = 0;
        animating = false;
        detonateRow.clear();
        detonateCol.clear();
        interactive = true;
        detonateTimer.stop();
        updateBoard();
      }//end if
    }
  });
  static ArrayList<Integer> detonateRow = new ArrayList<Integer>();
  static ArrayList<Integer> detonateCol = new ArrayList<Integer>();
  static ImageIcon[] detonateIcons = new ImageIcon [6];
  
  //#variable
  /*******************************************************************************************
    * Variable Dictionary : Highscore & Game Over Menu
    * static ArrayList<String> goodScore - stores highscores in the format "<name> <score>"
    * static int lengthOfDots - amount of separating dots are needed between the name and score
    * static String dots - stores separating dots
    * static JLabel lblGoodScores - displays 10 highest scores to the screen
    * static JPanel pnlGameOver - stores components for game over menu
    * JPanel pnlHighscores - stores components for highscore menu
    * JLabel lblEnterName - prompts user to enter name between 3-10 characters
    * JTextField nameField - receives user name input
    * JButton btnSubmit - submits user score and returns to main menu
    * JButton newBtnToMenu - brings the user back to main menu (used for leaderboards)
    * JLabel lblLeaderboards - displays leaderboards title
    ******************************************************************************************/
  static ArrayList<String> goodScore = new ArrayList<String>();
  static int lengthOfDots;
  static String dots = "";
  static JLabel[] lblGoodScores = new JLabel [10];
  static JPanel pnlGameOver = new JPanel();
  JPanel pnlHighscores = new JPanel();
  JLabel lblEnterName = new JLabel("Enter your name (3-10 letters):", JLabel.CENTER);
  JTextField nameField = new JTextField();
  JButton btnSubmit = new JButton("Submit");
  JButton newBtnToMenu = new JButton("Return to menu");
  JLabel lblLeaderboards = new JLabel("Leaderboards", JLabel.CENTER);
  
  //#variable
  /*******************************************************************************************
    * Variable Dictionary : Menu, Game, & Shiffling
    * static JFrame frame - holds the entire game
    * static JFrame shuffleFrame - holds shuffling popup
    * JFrame instructFrame - holds instructions popup
    * JPanel pnlInstruct - contains actual instructions
    * JPanel pnlShuffle - contains actual shuffling information
    * JLabel lblInstruct - displays instructions
    * JLabel lblShuffle - displays shuffling info
    * JPanel pnlMenu - contains the game menu
    * JButton btnToMenu - brings the user back to the menu
    * JPanel pnlMenuBtns - contains buttons on the menu
    * JLabel lblTitle - displays the name of the game
    * static JLabel lblScore - displays the user's score
    * static JLabel lblGameOver - displays whether the user won or lost
    * JLabel lblTimed - displays the game mode (timed)
    * JLabel lblMoves - displays the game mode (moves)
    * JLabel lblTimer - displays the time the user has remaining
    * static JLabel lblMoveCount - displays the amount of moves the user has remaining
    * JButton[] menuBtns - stores the 4 buttons on the menu
    * JButton btnWin - causes the user to win near-instantly
    * JButton btnLose - causes the user to lose near-instantly
    * JPanel pnlWinLose - contains the win and lose buttons
    * static JButton[][] gameBtns - stores the block buttons used in the main game
    * JButton btnCheat - either stops time or stops move consumption depending on game mode
    * JButton btnShuffle - generates a new board and displays shuffle message
    * JPanel pnlCheatShuffle - stores the cheat and shuffle buttons
    * static ImageIcon[] gameBtnIcons - stores the images used for the game buttons
    * Border selected - border applied to game button if selected
    * Border baseBorder - border applied to game button if unselected
    * Timer gameTimer - determines the time the player has left
    * static byte[][] board - stores numerical representation of blocks on the board
    * static int event - stores the button that the user presses
    * static int score - stores the score the user has gained in the game
    * int pressedRow, pressedCol, prevRow, prevCol - stores the coordinates of the previous
    *                                                and current block selections
    * int timerPart1, timerPart2 - stores the minute and second the timer is currently at
    * String timerNumber - stores the actual number displayed on the timer (includes colon)
    * static boolean gameModeMoves - stores whether or not the game mode is currently "moves"
    * boolean cheating - stores whether or not the general cheat has been activated
    ******************************************************************************************/
  static JFrame frame = new JFrame();
  static JFrame shuffleFrame = new JFrame();
  JFrame instructFrame = new JFrame();
  JPanel pnlInstruct = new JPanel();
  JPanel pnlShuffle = new JPanel();
  //#cheat (the cheats are explained in the instructions)
  JLabel lblInstruct = new JLabel("<html>Click two confectioneries to swap their positions. If a match of "
                                    + "3 or more is created, you will recieve some score. If the swap does "
                                    + "not result in a match, nothing will happen. The higher your match "
                                    + "chain is, the higher your gained score will be. Obtain a score of "
                                    + "250,000 before you either run out of time or moves to win!<BR><BR>The game "
                                    + "menu contains a few special options:<BR>\"Cheat\" toggles the cheat on and off "
                                    + "(either stops time or stops move consumption depending on game mode).<BR>"
                                    + "\"Shuffle\" simulates what'd happen if no possible matches existed "
                                    + "on the board.<BR>\"Win\" and \"Lose\" are essentially insta-win "
                                    + "and insta-lose conditions.<html>",
                                  JLabel.CENTER);
  JLabel lblShuffle = new JLabel("No matches left, generating new board!");
  JPanel pnlMenu = new JPanel();
  JButton btnToMenu = new JButton("Return to menu");
  JPanel pnlMenuBtns = new JPanel();
  JLabel lblTitle = new JLabel("Confectionery Collision Chronicle", JLabel.CENTER);
  static JLabel lblScore = new JLabel("Score: 0", JLabel.CENTER);
  static JLabel lblGameOver = new JLabel("", JLabel.CENTER);
  JLabel lblTimed = new JLabel("Timed Mode", JLabel.CENTER);
  JLabel lblMoves = new JLabel("Moves Mode", JLabel.CENTER);
  JLabel lblTimer = new JLabel("", JLabel.CENTER);
  static JLabel lblMoveCount = new JLabel("", JLabel.CENTER);
  JButton[] menuBtns = {new JButton("Instructions"), new JButton("Timed Mode"), 
    new JButton("Moves Mode"), new JButton("Leaderboards")};
  JButton btnWin = new JButton("Win");
  JButton btnLose = new JButton("Lose");
  JPanel pnlWinLose = new JPanel();
  //#array
  static JButton[][] gameBtns = new JButton [9][9];
  JButton btnCheat = new JButton("Cheat");
  JButton btnShuffle = new JButton("Shuffle");
  JPanel pnlCheatShuffle = new JPanel();
  static ImageIcon[] gameBtnIcons = new ImageIcon [7];
  Border selected = new LineBorder(Color.WHITE, 1);
  Border baseBorder = new LineBorder(Color.BLACK, 1);
  Timer gameTimer = new Timer(1000, this);
  //#array
  static byte[][] board = new byte [9][9];
  static int event = 0;
  static int score = 0;
  int pressedRow, pressedCol, prevRow = -2, prevCol;
  int timerPart1, timerPart2;
  String timerNumber;
  static boolean gameModeMoves = false;
  boolean cheating = false;
  
  //constructor
  public CollisionChronicle()
  {
    //set up properties of shuffling popup window
    shuffleFrame.setTitle("Shuffling");
    shuffleFrame.setSize(300, 90);
    shuffleFrame.setResizable(false);
    shuffleFrame.setLocation(550, 200);
    shuffleFrame.setLayout(new BorderLayout());
    
    //add components to shuffling popup window
    pnlShuffle.setLayout(new BoxLayout(pnlShuffle, BoxLayout.Y_AXIS));
    pnlShuffle.add(Box.createRigidArea(new Dimension (0,20)));
    pnlShuffle.add(lblShuffle);
    shuffleFrame.add(pnlShuffle);
    
    //set up properties of instructions popup window
    instructFrame.setTitle("Instructions");
    instructFrame.setSize(300, 375);
    instructFrame.setResizable(false);
    instructFrame.setLocation(550, 200);
    instructFrame.setLayout(new BorderLayout());
    
    //add components to instructions popup window
    pnlInstruct.setLayout(new BoxLayout(pnlInstruct, BoxLayout.Y_AXIS));
    pnlInstruct.add(Box.createRigidArea(new Dimension (0,20)));
    pnlInstruct.add(lblInstruct);
    instructFrame.add(pnlInstruct);
    
    //set up properties of main frame 
    frame.setTitle("James Long - ICS3U Summative");
    frame.setSize(400, 605);
    frame.setResizable(false);
    frame.setLocation(500, 50);
    frame.setLayout(new BorderLayout());
    
    //ensure closing the window terminates the program
    frame.addWindowListener(new WindowAdapter(){
      public void windowClosing(WindowEvent e)
      {
        System.exit(0);
      }
    });
    
    //set up colour and font of various titles
    lblTitle.setForeground(Color.RED);
    lblGameOver.setFont(new Font("Verdana", Font.PLAIN, 18));
    lblGameOver.setForeground(Color.RED);
    lblScore.setFont(new Font("Verdana", Font.PLAIN, 18));
    lblScore.setForeground(Color.RED);
    lblTitle.setFont(new Font("Verdana", Font.PLAIN, 18));
    lblTimed.setForeground(Color.RED);
    lblTimed.setFont(new Font("Verdana", Font.PLAIN, 18));
    lblTimer.setForeground(Color.RED);
    lblTimer.setFont(new Font("Verdana", Font.PLAIN, 24));
    lblMoves.setForeground(Color.RED);
    lblMoves.setFont(new Font("Verdana", Font.PLAIN, 18));
    lblLeaderboards.setForeground(Color.RED);
    lblLeaderboards.setFont(new Font("Verdana", Font.PLAIN, 24));
    lblEnterName.setForeground(Color.RED);
    lblEnterName.setFont(new Font("Verdana", Font.PLAIN, 14));
    lblInstruct.setForeground(Color.RED);
    lblInstruct.setFont(new Font("Verdana", Font.PLAIN, 12));
    lblShuffle.setForeground(Color.RED);
    lblShuffle.setFont(new Font("Verdana", Font.PLAIN, 12));
    lblMoveCount.setForeground(Color.RED);
    lblMoveCount.setFont(new Font("Verdana", Font.PLAIN, 24));
    
    //import highscores from a .txt file
    try
    {
      //Scanner readScores - reads from scores.txt in resources
      Scanner readScores = new Scanner(new File("resources/scores.txt"));
      //read 10 lines of highscores
      for(int i = 0; i < 10; i++)
        goodScore.add(readScores.nextLine());
    }catch(FileNotFoundException e){e.printStackTrace();}
    
    //fill highscore label with highscores from file
    for(int i = 0; i < 10; i++)
    {
      //generate filler dots so that the line is exactly 30 letters
      lengthOfDots = 31 - goodScore.get(i).length();
      for(int j = 0; j < lengthOfDots; j++)
        dots+=".";
      //#string (uses String.split())
      //break "<name> <score>" format down into "<name>" and <score>
      lblGoodScores[i] = new JLabel(goodScore.get(i).split("\\ ")[0] + dots + goodScore.get(i).split("\\ ")[1],
                                    JLabel.CENTER); 
      //set colour and font for highscore display
      lblGoodScores[i].setFont(new Font("Courier New", Font.PLAIN, 18));
      lblGoodScores[i].setForeground(Color.RED);
      dots = "";
    }
    
    //put menu buttons into a vertical orientation
    pnlMenuBtns.setLayout(new BoxLayout(pnlMenuBtns, BoxLayout.Y_AXIS));
    
    //#error (try/catch)
    //import detonate animation frames and block icons
    try
    {
      for(int i = 0; i < detonateIcons.length; i++)
        detonateIcons[i] = new ImageIcon(ImageIO.read((new File("resources/detonate/DET" + i + ".bmp"))));
      for(int i = 0; i < gameBtnIcons.length; i++)
        gameBtnIcons[i] = new ImageIcon(ImageIO.read((new File("resources/blocks/BLOCK" + i + ".bmp"))));
    }catch(IOException e){e.printStackTrace();}
    
    //set up properties of menu buttons
    for(int i = 0; i < menuBtns.length; i++)
    {
      //ensure buttons are same width
      menuBtns[i].setMaximumSize(new Dimension (200,50));
      pnlMenuBtns.add(menuBtns[i]);
      menuBtns[i].addActionListener(this);
      //separate the buttons with a space of 10px
      if(i < menuBtns.length - 1)
        pnlMenuBtns.add(Box.createRigidArea(new Dimension(0, 10)));
    }    
    
    //set up properties of miscellaneous buttons & add them to main action listener
    btnToMenu.setMaximumSize(new Dimension(250, 20));
    btnToMenu.addActionListener(this);
    btnCheat.setMaximumSize(new Dimension(125, 20));
    btnCheat.addActionListener(this);
    btnShuffle.setMaximumSize(new Dimension(125, 20));
    btnShuffle.addActionListener(this);
    btnWin.setMaximumSize(new Dimension(125, 20));
    btnWin.addActionListener(this);
    btnLose.setMaximumSize(new Dimension(125, 20));
    btnLose.addActionListener(this);
    
    //set up properties of miscellaneous panels and add buttons to them
    pnlWinLose.setLayout(new BoxLayout(pnlWinLose, BoxLayout.X_AXIS));
    pnlWinLose.add(btnWin);
    pnlWinLose.add(btnLose);
    pnlCheatShuffle.setLayout(new BoxLayout(pnlCheatShuffle, BoxLayout.X_AXIS));
    pnlCheatShuffle.add(btnCheat);
    pnlCheatShuffle.add(btnShuffle);
    
    //generate game over panel
    pnlGameOver.setLayout(new BoxLayout(pnlGameOver, BoxLayout.Y_AXIS));
    pnlGameOver.add(Box.createRigidArea(new Dimension (0,150)));
    pnlGameOver.add(lblGameOver);
    lblGameOver.setAlignmentX(Component.CENTER_ALIGNMENT);
    pnlGameOver.add(Box.createRigidArea(new Dimension (0,50)));
    pnlGameOver.add(lblEnterName);
    lblEnterName.setAlignmentX(Component.CENTER_ALIGNMENT);
    pnlGameOver.add(Box.createRigidArea(new Dimension (0,50)));
    nameField.setHorizontalAlignment(JTextField.CENTER);
    pnlGameOver.add(nameField);
    nameField.setAlignmentX(Component.CENTER_ALIGNMENT);
    
    //ensure that the entered name is no longer than 10 characters
    nameField.addKeyListener(new KeyAdapter() {
      public void keyTyped(KeyEvent e) {
        if (nameField.getText().length() >= 10 )
          e.consume();
      }});
    
    pnlGameOver.add(Box.createRigidArea(new Dimension (0,50)));
    btnSubmit.setMaximumSize(new Dimension(100, 50));
    btnSubmit.addActionListener(this);
    pnlGameOver.add(btnSubmit);
    btnSubmit.setAlignmentX(Component.CENTER_ALIGNMENT);
    pnlGameOver.add(Box.createRigidArea(new Dimension (0,150)));
    
    //generate leaderboards panel
    pnlHighscores.setLayout(new BoxLayout(pnlHighscores, BoxLayout.Y_AXIS));
    pnlHighscores.add(Box.createRigidArea(new Dimension (0,50)));
    pnlHighscores.add(lblLeaderboards);
    lblLeaderboards.setAlignmentX(Component.CENTER_ALIGNMENT);
    pnlHighscores.add(Box.createRigidArea(new Dimension (0,50)));
    for(int i = 0; i < 10; i++)
    {
      pnlHighscores.add(lblGoodScores[i]);
      lblGoodScores[i].setAlignmentX(Component.CENTER_ALIGNMENT);
      pnlHighscores.add(Box.createRigidArea(new Dimension (0,10)));
    }
    pnlHighscores.add(Box.createRigidArea(new Dimension (0,20)));
    pnlHighscores.add(newBtnToMenu);
    newBtnToMenu.addActionListener(this);
    newBtnToMenu.setAlignmentX(Component.CENTER_ALIGNMENT);
    
    //generate menu panel
    pnlMenu.setLayout(new BoxLayout(pnlMenu, BoxLayout.Y_AXIS));
    pnlMenu.add(Box.createRigidArea(new Dimension (0, 125)));
    pnlMenu.add(lblTitle);
    pnlMenu.add(Box.createRigidArea(new Dimension (0,40)));
    pnlMenu.add(pnlMenuBtns);
    pnlMenuBtns.setAlignmentX(Component.CENTER_ALIGNMENT);
    lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
    frame.add(pnlMenu);
    //start program window
    frame.setVisible(true);
  }//end constructor
  
  //main action listener
  public void actionPerformed(ActionEvent e)
  {
    //#condition
    //stop any actions if the user is not allowed to interact
    if(!interactive)
      return;
    
    //update in-game timer when a second passes if in timed mode
    else if(gameTimer == e.getSource() && !gameModeMoves)
    {
      //#string (uses String.split(), String,valueOf, string addition, etc.)
      //breaks in-game timer into minutes and seconds
      timerPart1 = Integer.parseInt(lblTimer.getText().split("\\:")[0]);
      timerPart2 = Integer.parseInt(lblTimer.getText().split("\\:")[1]) - 1; //assume a second has passed
      
      //case: game is over
      if(timerPart1 == 0 && timerPart2 == 0)
      {
        timerNumber = "0:00";
        gameTimer.stop();
        gameDone();
      }
      
      //case: minute is over
      else if(timerPart2 == -1) 
      {
        timerNumber = String.valueOf(timerPart1 - 1) + ':' + "59"; //minute passed
      }
      
      //case: minute still going on
      else 
      {
        //case: extra 0s need to be added to the seconds
        if(timerPart2 < 10)
          timerNumber = String.valueOf(timerPart1) + ":0" + String.valueOf(timerPart2);
        //case: no extra 0s need to be added
        else
          timerNumber = String.valueOf(timerPart1) + ":" + String.valueOf(timerPart2);
      }
      lblTimer.setText(timerNumber);
    }
    
    //shuffle board if shuffle button is pressed
    else if (btnShuffle == e.getSource())
    {
      shuffle();
    }
    
    //update leaderboards if score is submitted
    else if (btnSubmit == e.getSource())
    {
      //only change if entered name is greater than 3 letters
      if(nameField.getText().trim().length() >= 3)
      {
        //replace any score that's lower than the submitted score on the leaderboards
        for(int i = 0; i < 10; i++)
        {
          if(score > Integer.valueOf(goodScore.get(i).split("\\ ")[1]))
          {
            goodScore.add(i, nameField.getText().trim() + " " + score);
            goodScore.remove(9);
            nameField.setText("");
            updateLeaderboards();
            break;
          }
        }
        //go back to menu after submitting score
        changePanel(pnlMenu);
      }
    }
    
    //go back to menu if the button to go back is pressed
    else if (btnToMenu == e.getSource() || newBtnToMenu == e.getSource())
    {
      if(!gameModeMoves)
        gameTimer.stop();
      changePanel(pnlMenu);
    }
    
    //#cheat (cheat button functionality: triggers a flag and starts/stops game timer if applicable)
    //turn on/off cheat when cheat button is pressed
    else if (btnCheat == e.getSource())
    {
      //reverse the state of the button
      cheating = !cheating;
      if(cheating)
      {
        btnCheat.setText("Un-cheat");
        //stop timer since cheat has been turned on
        if(!gameModeMoves)
          gameTimer.stop();
      }
      else
      {
        btnCheat.setText("Cheat");
        //start timer since cheat has been turned off
        if(!gameModeMoves)
          gameTimer.start();
      }
    }
    
    //either near-win or near-lose the game depending on button pressed
    else if(btnWin == e.getSource() || btnLose == e.getSource())
    {
      lblMoveCount.setText("Moves: 1");
      lblTimer.setText("0:03");
      if(btnWin == e.getSource())
        score = 250000;
      else
        score = 0;
      lblScore.setText("Score: " + score);
    }
    
    //handle either a menu button press or a game button press
    else
    {
      //iterate through all menu buttons to see if one was pressed
      for (int i = 0; i < menuBtns.length; i++)
      {
        if (menuBtns[i] == e.getSource())
          event = i;
      }
      
      //iterate through all game buttons to see if one was pressed
      for(int i = 0; i < gameBtns.length; i++)
      {
        for(int j = 0; j < gameBtns[i].length; j++)
        {
          if (gameBtns[i][j] == e.getSource())
          {
            event = 4;
            pressedRow = i;
            pressedCol = j;
          }//end if
        }
      }
      
      //act according to whichever button was pressed
      switch(event)
      {
        //case: instructions button pressed
        case 0: instructFrame.setVisible(true); 
        break;
        
        //case: timed mode button pressed
        case 1: startGame("timed");
        break;
        
        //case: moves mode button pressed
        case 2: startGame("moves");
        gameModeMoves = true;
        break;
        
        //case: leaderboards pressed
        case 3: changePanel(pnlHighscores);
        break;
        
        //case: game block pressed
        case 4: if (swapIsValid(pressedRow, pressedCol, prevRow, prevCol))
        {
          //if a swap is valid, swap and then pause
          swapBlocks(pressedRow, pressedCol, prevRow, prevCol);
          gameBtns[prevRow][prevCol].setBorder(baseBorder);
          pausing = true;
          interactive = false;
          pauseTimer.start();
          
          //#string (uses String.split() and parsing to Integer)
          //reduce moves when a move is made in Moves mode
          if(gameModeMoves)
          {
            //isolate the numerical portion of the moves display
            int moveCount = Integer.parseInt(lblMoveCount.getText().split("\\ ")[1]) - 1; //assume a move has been made
            
            //#cheat (the moves only change if the cheat is disabled)
            //reduce the amount of moves adequately if not cheating
            if(!cheating)
            {
              if(moveCount > 0)
                lblMoveCount.setText("Moves: " + moveCount);
              else
                lblMoveCount.setText("Moves: 0"); //end if
            }//end if
          }//end if
        }
        
        //set pressed button as the highlighted one if it's the first to be pressed
        if(prevRow == -2)
        {
          prevRow = pressedRow;
          prevCol = pressedCol;
          gameBtns[prevRow][prevCol].setBorder(selected);
        }
        
        //reset highlights around buttons if the pressed button is the second
        else
        {
          gameBtns[prevRow][prevCol].setBorder(baseBorder);
          prevRow = -2;
        }//end if
        
        //keep the board up to date with the swaps
        updateBoard();
        
        //shuffle if no matches remain
        while(!matchPossible())
          shuffle();
        break;
        
        //this case will never happen but is required
        default:break;
      }
    }//end if
  }
  
  //write stored highscores to the .txt file and update the in-game leaderboard
  public static void updateLeaderboards()
  {
    //#error (try/catch)
    try
    {
      //BufferedWriter toFile - writes to scores.txt in resources
      BufferedWriter toFile = new BufferedWriter(new FileWriter(new File("resources/scores.txt")));
      //write all highscores to file in "<name> <score>" format
      for(int i = 0; i < 9; i++)
      {
        toFile.write(goodScore.get(i));
        toFile.newLine();
      }
      toFile.write(goodScore.get(9));
      toFile.close();
    }catch(IOException e){e.printStackTrace();}
    
    //update in-game leaderboards
    for(int i = 0; i < 10; i++)
    {
      //#string (uses String.length(), String.split(), and String addition)
      //ensure that there are enough filler dots for the leaderboard entry to be 30 characters
      lengthOfDots = 31 - goodScore.get(i).length();
      //generate the filler dots
      for(int j = 0; j < lengthOfDots; j++)
        dots+=".";
      lblGoodScores[i].setText(goodScore.get(i).split("\\ ")[0] + dots + goodScore.get(i).split("\\ ")[1]);
      //reset filler dot count
      dots = "";
    }
  }
  
  //change the panel displayed on the frame
  public static void changePanel(JPanel target)
  {
    /********************************************************************************
      * Variable Dictionary - changePanel
      * JPanel target - stores the panel to switch the view to
      *******************************************************************************/
    
    //remove all current panels
    frame.getContentPane().removeAll();
    frame.repaint();
    
    //add new panels
    frame.add(target);
    frame.revalidate();
  }
  
  //#method
  //start the game in the selected mode 
  public void startGame (String gameMode)
  { 
    /********************************************************************************
      * Variable Dictionary - startGame
      * String gameMode - stores the type of game (timed or moves) to start
      *******************************************************************************/
    
    //reset variables
    cheating = false;
    btnCheat.setText("Cheat");
    score = 0;
    lblScore.setText("Score: 0");
    
    //generate game panel
    JPanel pnlGame = new JPanel();
    pnlGame.setLayout(new BoxLayout(pnlGame, BoxLayout.Y_AXIS));
    pnlGame.add(Box.createRigidArea(new Dimension (0,20)));
    
    //add timer if the mode is timed
    if(gameMode.equals("timed"))
    {
      lblTimer.setText("1:30");
      pnlGame.add(lblTimed);
      lblTimed.setAlignmentX(Component.CENTER_ALIGNMENT);
      pnlGame.add(lblTimer);
      lblTimer.setAlignmentX(Component.CENTER_ALIGNMENT);
      gameTimer.start();
    }
    
    //add move counter if the mode is moves
    else
    {
      lblMoveCount.setText("Moves: 30");
      pnlGame.add(lblMoves);
      lblMoves.setAlignmentX(Component.CENTER_ALIGNMENT);
      pnlGame.add(lblMoveCount);
      lblMoveCount.setAlignmentX(Component.CENTER_ALIGNMENT);
    }//end if
    
    //add other components that are included no matter what mode it is
    pnlGame.add(lblScore);
    lblScore.setAlignmentX(Component.CENTER_ALIGNMENT);
    pnlGame.add(Box.createRigidArea(new Dimension (0,10)));
    pnlGame.add(pnlCheatShuffle);
    btnCheat.setAlignmentX(Component.CENTER_ALIGNMENT);
    pnlGame.add(pnlWinLose);
    pnlWinLose.setAlignmentX(Component.CENTER_ALIGNMENT);
    pnlGame.add(btnToMenu);
    btnToMenu.setAlignmentX(Component.CENTER_ALIGNMENT);
    pnlGame.add(Box.createRigidArea(new Dimension (0,10)));
    
    //generate a blank array for the board
    for(int i = 0; i < board.length; i++)
    {
      for(int j = 0; j < board[i].length; j++)
      {
        gameBtns[i][j] = new JButton();
      }
    }
    
    //generate values for the board and set up the game grid
    genBoard(6);
    JPanel pnlGameGrid = new JPanel();
    pnlGameGrid.setLayout(new GridLayout(9, 9));
    pnlGameGrid.setMaximumSize(new Dimension (360, 360));
    
    //populate the game grid with generated values
    for(int i = 0; i < board.length; i++)
    {
      for(int j = 0; j < board[i].length; j++)
      {
        gameBtns[i][j].setIcon(gameBtnIcons[board[i][j]]);
        gameBtns[i][j].setBorder(baseBorder);
        pnlGameGrid.add(gameBtns[i][j]);
        gameBtns[i][j].addActionListener(this);
      }
    }
    pnlGame.add(pnlGameGrid);
    pnlGame.setAlignmentX(Component.CENTER_ALIGNMENT);
    
    //set the visible panel to the game panel
    changePanel(pnlGame);
  }
  
  //start the timer to detonate all blocks that are in a match
  public static void detonate()
  {
    //set flags so that blocks cannot move and the user cannot press anything while detonating
    animating = true;
    interactive = false;
    detonateTimer.start();
    
    //set all detonated blocks to 0 in the byte array representation of the board
    for(int i = 0; i < detonateRow.size(); i++)
    {
      board[detonateRow.get(i)][detonateCol.get(i)] = (byte)0;
    }
  }
  
  //#method
  //make changes to the board if necessary
  public static void updateBoard()
  {
    /*******************************************************************************
       * Variable Dictionary - updateBoard
       * boolean MatchExists - stores whether or not there is a match in the board
       * int matchLength - stores the longest chain a matched block is in 
       *****************************************************************************/

    //only change things if there isn't a pause going on
    if(!pausing)
    {
      
      boolean matchExists = false;
      int matchLength;
      
      //#loop
      //check for matches in the game grid
      for(int i = 0; i < board.length; i++)
      {
        for(int j = 0; j < board[i].length; j++)
        {
          matchLength = isMatch(i, j);
          //#condition
          //set a block up for detonation if it's in a match of 3 or more
          if(matchLength > 2)
          {
            detonateRow.add(i);
            detonateCol.add(j);
            //add score relative to the longest chain the block is in
            score += (matchLength - 2) * 1000;
            //update displayed score
            lblScore.setText("Score: " + score);
          }//end if
        }
      }
      
      //#error (ensures that matches exist before calling detonate)
      //detonate matches if any exist
      if(detonateRow.size() > 0)
      {
        detonate();
      }//end if
      
      //only change things if there isn't an animation going on
      if(!animating)
      {
        //iterate from second-last row to the top
        for(int i = board.length - 2; i >= 0; i--) 
        {
          for(int j = board[i].length - 1; j >= 0; j--)
          {
            //if there is a block with an empty space under it, drop that block down until it gets blocked
            if(board[i][j] != 0 && board[i + 1][j] == 0)
            {
              dropBlocks(i, j);
            }//end if
          }
        }
        
        //fill remaining empty blocks
        for(int i = 0; i < board.length; i++)
        {
          for(int j = 0; j < board[i].length; j++)
          {
            //#random
            //generate a random block to fill the empty block
            if(board[i][j] == 0)
            {
              board[i][j] = (byte)((int)(Math.random() * 6) + 1);
            }//end if
          }
        }
        
        //#loop
        //update JButton board and check if any matches now exist
        for(int i = 0; i < board.length; i++)
        {
          for(int j = 0; j < board[i].length; j++)
          {
            //#condition
            //update JButton board if it doesn't correspond with the byte array board
            if(gameBtns[i][j].getText() != String.valueOf(board[i][j]))
            {
              gameBtns[i][j].setIcon(gameBtnIcons[board[i][j]]);
            }//end if
            
            //raise flag if a match still exists in the board
            if(isMatch(i, j) > 2)
            {
              matchExists = true;
            }//end if
          }
        }
        
        //pause, and then keep updating until no matches exist
        if(matchExists)
        {
          pausing = true;
          interactive=false;
          pauseTimer.start();
        }
        else if(lblMoveCount.getText().equals("Moves: 0")) //ensure that all blocks are cleared before game ends
        {
          lblMoveCount.setText("Moves: 30");
          gameDone();
        }
      }//end if
    }//end if
  }
  
  //move blocks down until they hit the bottom or another block
  public static void dropBlocks(int sr, int sc)
  {
    /********************************************************************************
      * Variable Dictionary - dropBlocks
      * int sr, sc - stores the row and column of the block to drop
      *******************************************************************************/
    
    while(board[sr + 1][sc] == 0)
    {
      //move block down by 1 space
      swapBlocks(sr + 1, sc, sr, sc);
      
      //ensure that the lowest a block can go is the bottom
      if (sr < board.length - 2)
      {
        sr++;
      }//end if
    }
  }
  
  //swap the positions of two blocks on both the byte array board and the JButton array board
  public static void swapBlocks(int sr, int sc, int pr, int pc) 
  {
    /********************************************************************************
      * Variable Dictionary - swapBlocks
      * int sr, sc - stores the row and column of the selected block location
      * int pr, pc - stores the row and column of the previous block location
      *******************************************************************************/
    
    byte temp = board[sr][sc];
    board[sr][sc] = board[pr][pc];
    board[pr][pc] = temp;
    
    //update JButton array board to keep in line with the byte array board
    gameBtns[sr][sc].setIcon(gameBtnIcons[board[sr][sc]]);
    gameBtns[pr][pc].setIcon(gameBtnIcons[board[pr][pc]]);
  }
  
  //#method
  //return the longest match a block is part of
  public static int isMatch(int row, int col)
  {
    /********************************************************************************
      * Variable Dictionary - isMatch
      * int row, col - stores the row and column of the block to check
      * int counter - stores the distance that has been moved in a certain direction
      * byte original - stores the byte representation of the block to check
      * byte[] matches - stores the amount of matches in all directions
      *                  matches[0] is up, matches[1] is down,
      *                  matches[2] is left, matches[3] is right
      *******************************************************************************/
    
    //declare variables
    int counter = 1;
    byte original = board[row][col];
    
    //refuse to check for matches with blank slots
    if(original == 0)
      return 0;
    
    //#array
    byte[] matches = {0, 0, 0, 0};
    
    //#error (ensures that checking is in range, applies to all checking in this method)
    //check upwards if in range
    while(row - counter >= 0) 
    {
      if(board[row - counter][col] == original)
        matches[0]++;
      //stop checking if a non-match is found
      else
        break;
      counter++;
    }
    //reset counter
    counter = 1;
    
    //check downwards if in range
    while(row + counter < 9)
    {
      if(board[row + counter][col] == original)
        matches[1]++;
      //stop checking if a non-match is found
      else
        break;
      counter++;
    }
    //reset counter
    counter = 1;
    
    //check left if in range
    while(col - counter >= 0)
    {
      if(board[row][col - counter] == original)
        matches[2]++;
      //stop checking if a non-match is found
      else
        break;
      counter++;
    }
    //reset counter
    counter = 1;
    
    //check right if in range
    while(col + counter < 9)
    {
      if(board[row][col + counter] == original)
        matches[3]++;
      //stop checking if a non-match is found
      else
        break;
      counter++;
    }
    //return the longest chain the block is in, either horizontally or vertically
    return Math.max(matches[0] + matches[1], matches[2] + matches[3]) + 1;
  }
  
  //return whether or not a swap results in a match
  public static boolean swapIsValid(int sr, int sc, int pr, int pc)
  {
    /********************************************************************************
      * Variable Dictionary - swapIsValid
      * int sr, sc - stores the row and column of the selected block location
      * int pr, pc - stores the row and column of the previous block location
      *******************************************************************************/
    
    //check if the blocks are beside each other
    if(Math.abs(sr - pr) == 1 && sc == pc || Math.abs(sc - pc) == 1 && sr == pr)
    {
      //swap the two blocks
      swapBlocks(sr, sc, pr, pc);
      
      //check if any of the two blocks are now in a match 
      if (isMatch(sr, sc) > 2 || isMatch(pr, pc) > 2)
      {
        //return grid to original state
        swapBlocks(sr, sc, pr, pc);
        //the swap is valid
        return true;
      }//end if
      //return grid to original state
      swapBlocks(sr, sc, pr, pc);
      //the swap did not result in a match, thus the swap is invalid
      return false;
    }
    //the blocks were not beside each other, thus the swap is invalid
    return false;
  }
  
  //return whether or not creating a match in the current board is possible
  public static boolean matchPossible()
  {
    //iterate through the 8*8 grid on the top left of the 9*9 game board
    for(int i = 0; i < board.length - 1; i++)
    {
      for(int j = 0; j < board[i].length - 1; j++)
      {
        //check if swapping with the next row will create a match
        if(swapIsValid(i + 1, j, i, j))
          return true;
        
        //check if swapping with the next column will create a match
        if(swapIsValid(i, j + 1, i, j))
          return true;
      }
    }
    
    //iterate through the 9th row and column
    for(int i = 0; i < board.length - 1; i++)
    {
      //check if swapping in the last row will result in a match
      if(swapIsValid(board.length - 1, i + 1, board.length - 1, i))
        return true;
      
      //check if swapping in the last column will result in a match
      if(swapIsValid(i + 1, board[0].length - 1, i, board[0].length - 1))
        return true;
    }
    //it is impossible to create any more matches
    return false;
  }
  
  //generate 9*9 game board array with a specified amount of block types
  public static void genBoard(int uniques)
  {
    /********************************************************************************
      * Variable Dictionary - genBoard
      * int uniques - the amount of unique block types to generate 
      *******************************************************************************/
    
    //#random
    //#loop
    //generate preemptive random values for the byte array board
    for(int i = 0; i < board.length; i++)
    {
      for(int j = 0; j < board[i].length; j++)
      {
        board[i][j] = (byte)((int)(Math.random() * uniques) + 1);
      }
    }
    
    //replace any matches of 3 or more on the board with a new random value
    for(int i = 0; i < board.length; i++)
    {
      for(int j = 0; j < board[i].length; j++)
      {
        //keep replacing with a random value until the match is no longer valid
        while(isMatch(i, j) > 2)
        {
          board[i][j] = (byte)((int)(Math.random() * uniques) + 1);
        }
      }
    }
    
    //if a board with no possible matches is generated, try again
    while(!matchPossible())
    {
      genBoard(6);
    }
  }
  
  //pop up the shuffling window and re-generate the board from scratch
  public static void shuffle()
  {
    shuffleFrame.setVisible(true);
    genBoard(6);
    updateBoard();
  }
  
  //determine if the player won or lost
  public static void gameDone()
  {
    //display losing text if score is too low
    if(score < 250000)
    {
      lblGameOver.setText("You lose!");
    }
    //display winning text if score is high enough
    else
    {
      lblGameOver.setText("You win!");
    }//end if
    
    //reset variables
    interactive = true;
    event = -1;
    gameModeMoves = false;
    
    //move to game over screen
    changePanel(pnlGameOver);
  }
  
  //#main (this is the main method of the file to run)
  //main method
  public static void main(String[] args) 
  { 
    //start constructor
    new CollisionChronicle();
  }//end main
}//end CollisionChronicle