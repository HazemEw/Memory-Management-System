package com.example.mvtsimulation;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

public class MVTApplication extends Application {
    private int time = 0;
    private MemoryManager memoryManager;
    private Queue<Process> jobQueue;
    private List<Process> completedProcesses;
    private TextArea jobQueueTextArea;
    private TextArea memoryDiagramTextArea;
    private TextArea completedProcessesTextArea;
    private Label timeLabel;
    private Label skip = new Label();
    private Label processLiable = new Label();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("MVT Simulation");
        Button uploadReadyButton = new Button("Upload ready.txt (add process to the memory)");
        uploadReadyButton.setStyle(
                "-fx-background-color: #4CAF50; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-padding: 10 20 10 20; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px;"
        );
        VBox readyVBox = new VBox(10, uploadReadyButton);
        readyVBox.setAlignment(Pos.CENTER);
        readyVBox.setPadding(new Insets(10));
        Scene readyScene = new Scene(readyVBox, 400, 400);
        skip.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 16pt;");
        Button uploadJobButton = new Button("Upload job.txt ");
        uploadJobButton.setStyle(
                "-fx-background-color: #4CAF50; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-padding: 10 20 10 20; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px;"
        );
        VBox jobVBox = new VBox(10, uploadJobButton);
        jobVBox.setAlignment(Pos.CENTER);
        jobVBox.setPadding(new Insets(10));
        Scene jobScene = new Scene(jobVBox, 400, 400);

        uploadReadyButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open ready.txt File");
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    List<Process> readyProcesses = readProcesses(file.getAbsolutePath());
                    memoryManager = new MemoryManager(2048, 512); // Initialize memory manager
                    for (Process process : readyProcesses) {
                        if (!memoryManager.allocateProcess(process)) {
                            showAlert("Error", "Failed to allocate process " + process.processId + " from ready processes \n Size of Process Bigger Than Memory ");
                            return;
                        }
                    }
                    primaryStage.setScene(jobScene);
                } catch (IOException ex) {
                    showAlert("Error", "Failed to read the file: " + ex.getMessage());
                }
            }
        });

        // Set action for uploading job.txt
        uploadJobButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open job.txt File");
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    jobQueue = new LinkedList<>(readProcesses(file.getAbsolutePath()));
                    completedProcesses = new ArrayList<>();
                    initializeMainScene(primaryStage); // Initialize the main simulation scene
                } catch (IOException ex) {
                    showAlert("Error", "Failed to read the file: " + ex.getMessage());
                }
            }
        });
        primaryStage.setScene(readyScene);
        primaryStage.show();
    }

    private void initializeMainScene(Stage primaryStage) {
        // Initialize UI components
        jobQueueTextArea = new TextArea();
        jobQueueTextArea.setEditable(false);
        jobQueueTextArea.setPrefHeight(200);

        memoryDiagramTextArea = new TextArea();
        memoryDiagramTextArea.setEditable(false);
        memoryDiagramTextArea.setPrefHeight(400);

        completedProcessesTextArea = new TextArea();
        completedProcessesTextArea.setEditable(false);
        completedProcessesTextArea.setPrefHeight(200);

        Button nextButton = new Button("Next Time Slot");
        nextButton.setStyle(
                "-fx-background-color: #4CAF50; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-padding: 10 20 10 20; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px;"
        );
        nextButton.setOnAction(e -> simulateNextStep());

        timeLabel = new Label("Time: " + time);
        timeLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 20pt;");

        // Layout setup
        HBox jobQueueMemoryBox = new HBox(10,
                new VBox(10, new Label("Job Queue"), jobQueueTextArea),
                new VBox(10, new Label("Memory"), memoryDiagramTextArea),
                new VBox(10, new Label("Completed Processes"), completedProcessesTextArea));
        jobQueueMemoryBox.setPadding(new Insets(10));

        BorderPane centerPane = new BorderPane();
        centerPane.setCenter(jobQueueMemoryBox);

        VBox root = new VBox(10, timeLabel, centerPane, nextButton, skip, processLiable);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));

        // Menu Bar for Full Screen Toggle
        MenuBar menuBar = new MenuBar();
        Menu viewMenu = new Menu("View");
        MenuItem fullScreenToggle = new MenuItem("Toggle Full Screen");
        fullScreenToggle.setOnAction(e -> primaryStage.setFullScreen(!primaryStage.isFullScreen()));
        viewMenu.getItems().add(fullScreenToggle);
        menuBar.getMenus().add(viewMenu);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(menuBar);
        mainLayout.setCenter(root);

        Scene mainScene = new Scene(mainLayout, 1000, 900);

        // Allow exit from full screen with ESC key
        mainScene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    primaryStage.setFullScreen(false);
                    break;
            }
        });

        primaryStage.setScene(mainScene);
        primaryStage.show();

        updateTextAreas();
    }
    private void simulateNextStep() {
        timeLabel.setText("Time: " + (++time));
        skip.setText("");
        processLiable.setText("");
        List<Process> processesToRemove = new ArrayList<>();
        Iterator<Process> iterator = memoryManager.getAllocatedProcesses().iterator();
        while (iterator.hasNext()) {
            Process process = iterator.next();
            process.timeInMemory -= 1;
            if (process.timeInMemory <= 0) {
                processesToRemove.add(process);
                completedProcesses.add(process);
                iterator.remove();
                memoryManager.deallocateProcess(process);
            }
        }

        // Allocate new jobs if memory allows
        while (!jobQueue.isEmpty()) {
            Process newProcess = jobQueue.peek();
            if (memoryManager.allocateProcess(newProcess)) {
                jobQueue.poll();
            } else {
                System.out.println("Skipping process " + newProcess.processId + " due to insufficient memory");
                skip.setText("Skipping process " + newProcess.processId + " due to insufficient memory");
                break;
            }
        }

        updateTextAreas();

        // Compact memory if there are more than 3 free holes
        if (memoryManager.getFreeHoles().size() > 3) {
            String compactMessage = memoryManager.compactMemory();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Memory Compaction");
            alert.setHeaderText("Memory Compaction Triggered");
            alert.setContentText("Creating one big hole at high memory in the next time :\n" + compactMessage);
            alert.showAndWait();
        }

        // Check if the simulation is complete
        if (jobQueue.isEmpty() && memoryManager.getAllocatedProcesses().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Simulation Complete");
            alert.setHeaderText("Simulation Finished Successfully");
            alert.setContentText("All processes have been processed.");
            alert.showAndWait();
        }
    }

    private void updateTextAreas() {
        jobQueueTextArea.clear();
        memoryDiagramTextArea.clear();
        completedProcessesTextArea.clear();

        // Job Queue
        for (Process process : jobQueue) {
            jobQueueTextArea.appendText("Process ID: " + process.processId + ", Size: " + process.size + ", Time in Memory: " + process.timeInMemory + "\n");
        }

        // Memory Diagram
        memoryDiagramTextArea.appendText(memoryManager.getMemoryContents());

        // Completed Processes
        for (Process process : completedProcesses) {
            completedProcessesTextArea.appendText("Process ID: " + process.processId + ", Size: " + process.size + "\n");
        }
    }

    public static List<Process> readProcesses(String filename) throws IOException {
        List<Process> processes = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                int processId = Integer.parseInt(parts[0]);
                int size = Integer.parseInt(parts[1]);
                int timeInMemory = Integer.parseInt(parts[2]);
                processes.add(new Process(processId, size, timeInMemory));
            }
        }
        return processes;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
