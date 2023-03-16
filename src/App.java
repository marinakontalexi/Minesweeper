import java.io.*;                               
import java.util.*;
import javafx.application.*;
import javafx.stage.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.geometry.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert.AlertType;

public class App extends Application {
    private static File file = null;
    public static String path_to_project = App.class.getClassLoader().getResource("").getPath().replace("/bin/", "").replace("/", "\\\\").replace("\\\\C", "C");
    private static Scene mainScene;
    private static FlowPane root, mainroot;
    private static Stage mainStage;
    private static State current_game = null, next_game = null;
    private static File history;
    private static boolean saved = false;
    Timeline timeline = new Timeline();
    
    @Override 
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("MediaLab Minesweeper"); // Set the stage title
        MenuBar uppermenu = new MenuBar();
        Menu app = new Menu("Application"), det = new Menu("Details");
        MenuItem m1 = new MenuItem("Create");
        MenuItem m2 = new MenuItem("Load");
        MenuItem m3 = new MenuItem("Start");
        MenuItem m4 = new MenuItem("Exit");
        app.getItems().add(m1);
        app.getItems().add(m2);
        app.getItems().add(m3);
        app.getItems().add(m4);
        
        MenuItem d1 = new MenuItem("Rounds");
        MenuItem d2 = new MenuItem("Solution");
        det.getItems().add(d1);
        det.getItems().add(d2);

        uppermenu.getMenus().add(app);
        uppermenu.getMenus().add(det);

        m1.setOnAction(handlerAlloc(m1.getText()));
        m2.setOnAction(handlerAlloc(m2.getText()));
        m3.setOnAction(handlerAlloc(m3.getText()));
        m4.setOnAction(handlerAlloc(m4.getText()));
        d1.setOnAction(handlerAlloc(d1.getText()));
        d2.setOnAction(handlerAlloc(d2.getText()));
        Image background = new Image(path_to_project.replace("C", "file:\\C") + "\\media\\logo.png");
        //Image background = new Image("file:\\C:\\Users\\Marina\\Documents\\sxoli\\9\\multimedia\\Minesweeper_el18022\\media\\logo.png");
        ImageView imgview = new ImageView(background);
        root = new FlowPane(Orientation.VERTICAL, 20, 20);
        mainroot = new FlowPane(Orientation.VERTICAL, 20, 20);
        root = mainroot;
        mainScene = new Scene(new FlowPane(uppermenu, root), 600, 600, Color.web("#C8BFE7"));
        mainroot.getChildren().add(imgview);
        primaryStage.setScene(mainScene); // Place the scene in the stage
        mainStage = primaryStage;        
        root.setStyle("-fx-background-color: #C8BFE7");
        root.prefHeightProperty().bind(mainStage.heightProperty());
        mainroot.prefWidthProperty().bind(mainStage.widthProperty());
        mainroot.prefHeightProperty().bind(mainStage.heightProperty());
        uppermenu.prefWidthProperty().bind(mainStage.widthProperty());
        imgview.fitWidthProperty().bind(mainStage.widthProperty());
        imgview.fitHeightProperty().bind(mainStage.heightProperty());
        primaryStage.setResizable(false);
        primaryStage.show(); // Display the stage   
        history = new File(path_to_project + "\\details\\" + "history.txt");   
    }    

    private void set() {
        current_game = next_game;
        next_game = null;
        saved = false;
        try {
            int timeleft = current_game.getTime();
            Label timerLabel = new Label();
            Label markedLabel =  new Label(Integer.toString(current_game.getMarked()));

            GridPane boardgame = current_game.show_board(false);
            VBox vbox1 = new VBox(6), vbox2 = new VBox(6), vbox3 = new VBox(6);
            HBox details = new HBox(40);
            vbox1.getChildren().addAll(new Label("Total Mines"), new Label(Integer.toString(current_game.getTotalMines())));
            vbox2.getChildren().addAll(new Label("Marked Tiles"), markedLabel);
            vbox3.getChildren().addAll(new Label("Time Left"), timerLabel);
            details.getChildren().addAll(vbox1, vbox2, vbox3);
            IntegerProperty timeSeconds = new SimpleIntegerProperty(timeleft);
            timerLabel.textProperty().bind(timeSeconds.asString());
            timeSeconds.set(timeleft);
            timeline = new Timeline();
            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.seconds(timeleft+1),
                    new KeyValue(timeSeconds, 0)));
            timeline.setOnFinished(event ->  {
                timeline.stop();
                boardgame.setDisable(true);
                Text t = gameEnds(false);
                root.getChildren().add(1, new StackPane(boardgame, t));
            });
            details.setPadding(new Insets(0,0,0,170));
            boardgame.setPadding(new Insets(-1, (mainScene.getWidth()-boardgame.prefWidth(-1))/2, 0, (mainScene.getWidth()-boardgame.prefWidth(-1))/2));
            root.getChildren().clear(); 
            root.getChildren().add(details);
            root.getChildren().add(boardgame);
            boardgame.setOnMouseClicked(new EventHandler<MouseEvent> () {
                @Override 
                public void handle(MouseEvent e) {
                    markedLabel.setText(Integer.toString(current_game.getMarked()));
                    if (current_game.isWin() || current_game.isLoss()) {
                        timeline.stop();
                        boardgame.setDisable(true);
                        Text t = gameEnds(!current_game.isLoss());
                        root.getChildren().remove(1);
                        root.getChildren().add(1, new StackPane(boardgame, t));
                    }
                }
            });
            timeline.playFromStart();  
        }
        catch (Exception e) {
            // Took too long!
        }
    }

    private Text gameEnds(boolean win) {
        savegame(win ? "User" : "Computer");
        Text t = new Text(win ? "CONGRATS" : "BETTER LUCK\n  NEXT TIME");
        t.setFill(win ? Color.RED : Color.BLUE);
        t.setStroke(win ? Color.RED : Color.BLUE);
        t.setStrokeWidth(2);
        t.setFont(Font.font("Verdana", (current_game.getDifficulty() == 1) ? 30 : 40));
        return t;
    }
    public static void main(String args[]) {
        launch(args);
    }

    private static State initialize() throws FileNotFoundException, InvalidDescriptionException, InvalidValueException {
        int[] input = new int[4];           // diff, no of mines, time, supermine
        Scanner scanner;
        try {scanner = new Scanner(file);}
        catch (Exception exc) {throw new FileNotFoundException("No such file found in medialab folder");}
        for (int i = 0; i < 4; i++) {
            if (scanner.hasNextLine()) {
                input[i] = Integer.parseInt(scanner.nextLine());
            }
            else {
                scanner.close();
                throw new InvalidDescriptionException("SCENARIO-ID does not contain enough information.");
            }
            
        }
        // check if scenario-id is valid
        if (scanner.hasNextLine()) {
            scanner.close();
            throw new InvalidDescriptionException("SCENARIO-ID has more than 4 lines.");
        }
        scanner.close();

        if (input[0] != 1 && input[0] != 2) 
            throw new InvalidValueException("Invalid difficulty level.");
        if (input[0] == 1 && (input[1] < 9 || input[1] > 11)) 
            throw new InvalidValueException("Invalid number of mines.");
        if (input[0] == 1 && (input[2] < 120 || input[2] > 180)) 
            throw new InvalidValueException("Invalid time option.");
        if (input[0] == 2 && (input[1] < 35 || input[1] > 45)) 
            throw new InvalidValueException("Invalid number of mines.");
        if (input[0] == 2 && (input[2] < 240 || input[2] > 360)) 
            throw new InvalidValueException("Invalid time option.");
        if (input[0] == input[3] || input[3] > 1 || input[3] < 0)
            throw new InvalidValueException("Invalid super-mine option.");

        State initial = new State((input[0] == 1) ? 9:16, input[1], input[3], input[2]);
        return initial;
    }

    private EventHandler<ActionEvent> handlerAlloc (String s) {
        switch(s) {
            case("Create"):
                return new EventHandler<ActionEvent>() { 
                    @Override 
                    public void handle(ActionEvent e) { 
                        Stage popup = new Stage();
                        popup.setTitle("Details");
                        popup.initModality(Modality.APPLICATION_MODAL);
                        popup.initOwner(mainStage);
                        DialogPane dialog = new DialogPane();
                        TextField textField1 = new TextField();
                        TextField textField2 = new TextField();
                        TextField textField3 = new TextField();
                        TextField textField4 = new TextField();
                        TextField textField5 = new TextField();
                        VBox dialogVbox = new VBox(6);
                        dialogVbox.getChildren().addAll(new Label("Scenario-ID"), textField1);
                        dialogVbox.getChildren().addAll(new Label("Difficulty"), textField2);
                        dialogVbox.getChildren().addAll(new Label("Number of Mines"), textField3);
                        dialogVbox.getChildren().addAll(new Label("Supermine"), textField4);
                        dialogVbox.getChildren().addAll(new Label("Time"), textField5);
                        dialog.setContent(dialogVbox);
                        dialog.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                        dialog.lookupButton(ButtonType.OK).setOnMouseClicked(
                            new EventHandler<MouseEvent>() { 
                                @Override 
                                public void handle(MouseEvent e) { 
                                    file = new File(path_to_project + "\\medialab\\" + textField1.getText() + ".txt");
                                    try {
                                        if (file.exists()) {throw new SkipException("File already exists"); }
                                        FileWriter myWriter = new FileWriter(file);
                                        myWriter.write(textField2.getText() + '\n' + textField3.getText() + '\n' + textField5.getText() + '\n' + textField4.getText());
                                        myWriter.close(); 
                                        next_game = initialize();
                                        popup.close();
                                }
                                    catch (IOException p) {
                                        Alert alert = new Alert(AlertType.ERROR);
                                        alert.setContentText("Error occured when submitting Scenario-ID. Make sure to provide with the correct path to project");
                                        alert.show();
                                    }
                                    catch (InvalidDescriptionException | InvalidValueException exc) {
                                        Alert alert = new Alert(AlertType.ERROR);
                                        alert.setContentText(exc.getMessage());
                                        alert.show();
                                        file.delete();
                                    }
                                    catch (SkipException exc) {
                                        Alert alert = new Alert(AlertType.ERROR);
                                        alert.setContentText(exc.getMessage());
                                        alert.show();
                                    }
                                } 
                            }
                        ); 

                        dialog.lookupButton(ButtonType.CANCEL).setOnMouseClicked(
                            new EventHandler<MouseEvent>() { 
                                @Override 
                                public void handle(MouseEvent e) { 
                                    popup.close();
                                } 
                            }
                        );                         
                        Scene dialogScene = new Scene(dialog);
                        popup.setScene(dialogScene);
                        popup.show();
                }; 
            };
            case("Load"):
            return new EventHandler<ActionEvent>() { 
                @Override 
                public void handle(ActionEvent e) { 
                    Stage popup = new Stage();
                    popup.setTitle("Details");
                    popup.initModality(Modality.APPLICATION_MODAL);
                    popup.initOwner(mainStage);
                    DialogPane dialog = new DialogPane();
                    TextField textField1 = new TextField();
                    VBox dialogVbox = new VBox(6);
                    dialogVbox.getChildren().addAll(new Label("Scenario-ID"), textField1);
                    dialog.setContent(dialogVbox);
                    dialog.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                    dialog.lookupButton(ButtonType.OK).setOnMouseClicked(
                        new EventHandler<MouseEvent>() { 
                            @Override 
                            public void handle(MouseEvent e) { 
                                file = new File(path_to_project + "\\medialab\\" + textField1.getText() + ".txt");
                                try {
                                    if (file.createNewFile()) {
                                        file.delete();
                                        file = null;
                                    }
                                    try { 
                                        next_game = initialize(); 
                                        popup.close();
                                    }
                                    catch (InvalidDescriptionException | InvalidValueException | FileNotFoundException exc) {
                                        Alert alert = new Alert(AlertType.ERROR);
                                        alert.setContentText(exc.getMessage());
                                        alert.show();
                                        file = null;
                                    }
                                } 
                                catch (IOException p) {
                                    file.delete();
                                    file = null;
                                    Alert alert = new Alert(AlertType.ERROR);
                                    alert.setContentText("Error occured when submitting Scenario-ID. Make sure to provide with the correct path to project");
                                    alert.show();
                                }
                            }
                        } 
                    ); 

                    dialog.lookupButton(ButtonType.CANCEL).setOnMouseClicked(
                        new EventHandler<MouseEvent>() { 
                            @Override 
                            public void handle(MouseEvent e) { 
                                popup.close();
                            } 
                        }
                    ); 

                    Scene dialogScene = new Scene(dialog);
                    popup.setScene(dialogScene);
                    popup.show();
                };
            };  
            case("Start"):
                return new EventHandler<ActionEvent>() { 
                    @Override 
                    public void handle(ActionEvent e) { 
                        if (next_game != null) {
                            if (current_game != null && !saved) savegame("Computer");
                            if (current_game != null) root.getChildren().remove(1);
                            set();
                        }
                    }   
                }; 
            case("Exit"):
                return new EventHandler<ActionEvent>() { 
                    @Override 
                    public void handle(ActionEvent e) { 
                        if (current_game != null && !saved) savegame("Computer");
                        Platform.exit();
                    }   
                }; 
            case("Rounds"):
                return new EventHandler<ActionEvent>() { 
                    @Override 
                    public void handle(ActionEvent e) { 
                        Stage popup = new Stage();
                        popup.setTitle("Last 5 Rounds");
                        popup.initModality(Modality.NONE);
                        popup.initOwner(mainStage);
                        TableView<PrevGame> table = new TableView<PrevGame>();
                        table.setEditable(false);
                        TableColumn<PrevGame, String> numCol = new TableColumn<PrevGame, String>("");
                        TableColumn<PrevGame, String> minesCol = new TableColumn<PrevGame, String>("Total number of mines");
                        TableColumn<PrevGame, String> movesCol = new TableColumn<PrevGame, String>("Total moves");
                        TableColumn<PrevGame, String> timeCol = new TableColumn<PrevGame, String>("Total time");
                        TableColumn<PrevGame, String> winCol = new TableColumn<PrevGame, String>("Winner");
                        table.getColumns().addAll(numCol, minesCol, movesCol, timeCol, winCol);

                        ObservableList<PrevGame> data = FXCollections.observableArrayList();
                        try{       
                            Scanner scanner = new Scanner(history);
                            for (int i = 0; i < 5; i++) {
                                if (scanner.hasNextLine()) {
                                    String[] g = scanner.nextLine().split(" ");
                                    data.add(new PrevGame(Integer.toString(i+1), g[0], g[1], g[2], g[3]));
                                }
                            }
                            scanner.close();
                        }
                        catch (FileNotFoundException exc) {}
                        
                        table.setItems(data);
                        numCol.setCellValueFactory(new PropertyValueFactory<PrevGame, String>("s"));
                        minesCol.setCellValueFactory(new PropertyValueFactory<PrevGame, String>("mines"));
                        movesCol.setCellValueFactory(new PropertyValueFactory<PrevGame, String>("moves"));
                        timeCol.setCellValueFactory(new PropertyValueFactory<PrevGame, String>("time"));
                        winCol.setCellValueFactory(new PropertyValueFactory<PrevGame, String>("winner"));

                        Scene scene = new Scene(table);
                        popup.setScene(scene);
                        popup.show();
                    }
                };
            case("Solution"):
                return new EventHandler<ActionEvent>() { 
                    @Override 
                    public void handle(ActionEvent e) { 
                        if (current_game != null) {
                            timeline.stop();
                            GridPane solution = current_game.show_board(true);
                            solution.setPadding(new Insets(-1, (600-solution.prefWidth(-1))/2, 0, (mainScene.getWidth()-solution.prefWidth(-1))/2));
                            root.getChildren().remove(1);
                            root.getChildren().add(1, solution);
                            if (current_game != null && !saved) savegame("Computer");
                        }
                    }   
                };
            default: 
                return new EventHandler<ActionEvent>() { 
                    @Override 
                    public void handle(ActionEvent e) { 
                        // System.out.println("Wrong menu item"); 
                    }   
                }; 
            }
    }

    private void savegame(String winner) {
        try {
            saved = true;
            BufferedReader br = new BufferedReader(new FileReader(history));
            String result = "", line = "";
            for (int i = 0; i < 4; i++) {
                if((line = br.readLine()) != null) result = result + "\n" + line; 
            }
            br.close();
            result = Integer.toString(current_game.getTotalMines()) + " " + 
                     Integer.toString(current_game.getMoves()) + " " + 
                     Integer.toString(current_game.getTime()) + " " + winner  + result;
            history.delete();
            FileOutputStream fos = new FileOutputStream(history);
            fos.write(result.getBytes());
            fos.flush();
            fos.close();
        }
        catch (Exception e) {}
    }
}
