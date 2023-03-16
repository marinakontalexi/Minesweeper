## Getting Started

This is a Minesweeper Application developed for the course Multimedia Technologies 2022-2023, ECE, NTUA.

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

## Compile and run 

In order to compile the project you need to run the command below from the folder src:
javac -d ../bin -Xlint --module-path "{path_to_javafx-sdk-19\lib}" --add-modules javafx.controls App.java  

In order to run the application you need to run the command below from any command window:
java -cp {path_to_project}/bin --module-path {path_to_javafx-sdk-19\lib} --add-modules javafx.controls,javafx.fxml App