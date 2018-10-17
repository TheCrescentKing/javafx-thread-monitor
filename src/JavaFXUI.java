import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class JavaFXUI extends Application{

        private BorderPane rootPane;
        private TextField searchTextField, filterTextField;

        public static void main(String[] args) {
            launch(args);
        }

        private List<Button> makeButtonsForThreadGroups(){
            ThreadMonitor monitor = new ThreadMonitor(new Thread());
            ThreadGroup[] tgArray = monitor.getThreadGroupArray();
            List<Button> buttonList = new ArrayList<>();
            for(ThreadGroup tg : tgArray){
                if(tg == null){
                    break;
                }
                Button btn = new Button();
                btn.setText(tg.getName());

                btn.setOnAction(event -> {
                    Thread[] threadArray = monitor.getThreadsInGroup(tg);
                    setThreadScene(threadArray, tg);
                });

                buttonList.add(btn);
            }
            return buttonList;
        }

        private void addButtonsToStackPane(List<Button> btnList, Pane pane){
            for(Button btn : btnList){
                pane.getChildren().add(btn);
            }
        }

        private void addTextTitle(String text, Pane pane){
            Text t = new Text(10, 50, text);
            pane.getChildren().add(t);
        }

        private void setThreadGroupScene(){

            List<Button> threadGroupButtonList = makeButtonsForThreadGroups();
            FlowPane flowPane = new FlowPane();
            addTextTitle("Thread Groups", flowPane);
            addButtonsToStackPane(threadGroupButtonList, flowPane);
            rootPane.setCenter(flowPane);
        }

        private List<Button> makeButtonsForThreads(Thread[] threadArray){
            List<Button> buttonList = new ArrayList<>();
            for(Thread thread : threadArray){
                if(thread == null){
                    break;
                }

                Button btn = new Button();
                btn.setText(thread.getName());

                btn.setOnAction(event -> setThreadInfoPane(thread));

                buttonList.add(btn);
            }
            return buttonList;
        }

        private void setThreadInfoPane(Thread thread){
            VBox infoPane = new VBox();
            infoPane.getChildren().add(new Label("Name: " + thread.getName()));
            infoPane.getChildren().add(new Label("ID: " + thread.getId()));
            infoPane.getChildren().add(new Label("State: " + thread.getState()));
            infoPane.getChildren().add(new Label("Priority: " + thread.getPriority()));
            infoPane.getChildren().add(new Label("Daemon: " + thread.isDaemon()));
            infoPane.getChildren().add(new Label("ThreadGroup: " + thread.getThreadGroup().getName()));
            Button stopThreadBtn = new Button("Stop Thread");
            stopThreadBtn.setOnAction(event -> {
                thread.interrupt();
                /*
                if(thread.isInterrupted()){
                    thread.stop();
                }
                */
            });
            infoPane.getChildren().add(stopThreadBtn);

            rootPane.setRight(infoPane);
        }

        private void setThreadScene(Thread[] threadArray, ThreadGroup tg){

            List<Button> threadButtonList = makeButtonsForThreads(threadArray);
            FlowPane flowPane = new FlowPane();
            addTextTitle("Threads under " + tg.getName() + "\n", flowPane);
            Button backBtn = new Button();
            backBtn.setText("Go back.");
            backBtn.setStyle("-fx-background-color: af0");
            backBtn.setOnAction(event -> setThreadGroupScene());
            flowPane.getChildren().add(backBtn);
            addButtonsToStackPane(threadButtonList, flowPane);
            rootPane.setCenter(flowPane);
        }

        private void searchThreadsAndSetPane(){

            String searchTerm = searchTextField.getText();

            if(searchTerm == null || searchTerm.equals("")){
                return;
            }

            FlowPane searchPane = new FlowPane();

            ThreadMonitor monitor = new ThreadMonitor(new Thread());
            ThreadGroup[] tgArray = monitor.getThreadGroupArray();

            for(ThreadGroup tg : tgArray){
                if(tg == null)
                    break;

                Thread[] threadArray = monitor.getThreadsInGroup(tg);
                for(Thread thread : threadArray){
                    if(thread == null)
                        break;

                    String threadNameLowerCase = thread.getName().toLowerCase();

                    if(threadNameLowerCase.contains(searchTerm.toLowerCase())){
                        Button btn = new Button(thread.getName());
                        btn.setOnAction(event -> setThreadInfoPane(thread));
                        searchPane.getChildren().add(btn);
                    }
                }
            }

            if(searchPane.getChildren().isEmpty()){
                searchPane.getChildren().add(new Label("No Threads Found!"));
                Button backButton = new Button("Main menu");
                backButton.setOnAction(event -> setThreadGroupScene());
                backButton.setStyle("-fx-background-color: af0");
                searchPane.getChildren().add(backButton);
            }

            rootPane.setCenter(searchPane);
        }

        private void filterThreadGroupsAndSetPane(){

            String filterText = filterTextField.getText();

            if(filterText == null || filterText.equals("")){
                return;
            }

            ThreadMonitor monitor = new ThreadMonitor(new Thread());
            ThreadGroup[] tgArray = monitor.getThreadGroupArray();

            FlowPane filterPane = new FlowPane();

            for(ThreadGroup tg : tgArray){
                if(tg == null)
                    break;

                String threadGroupNameLowerCase = tg.getName().toLowerCase();

                if(threadGroupNameLowerCase.contains(filterText.toLowerCase())) {
                    Button btn = new Button();
                    btn.setText(tg.getName());

                    btn.setOnAction(event -> {
                        Thread[] threadArray = monitor.getThreadsInGroup(tg);
                        setThreadScene(threadArray, tg);
                    });

                    filterPane.getChildren().add(btn);
                }
            }

            if(filterPane.getChildren().isEmpty()){
                filterPane.getChildren().add(new Label("No ThreadGroups Found!"));
                Button backButton = new Button("Main menu");
                backButton.setOnAction(event -> setThreadGroupScene());
                backButton.setStyle("-fx-background-color: af0");
                filterPane.getChildren().add(backButton);
            }


            rootPane.setCenter(filterPane);
        }

        private VBox setUpActionButtons(){
            VBox vBox = new VBox();
            vBox.getChildren().add(new Label("Search Thread:"));

            searchTextField = new TextField();

            searchTextField.setOnKeyPressed((event) -> {
                if(event.getCode() == KeyCode.ENTER) {
                    searchThreadsAndSetPane();
                }
            });

            vBox.getChildren().add(searchTextField);
            vBox.getChildren().add(new Label("Filter ThreadGroup:"));
            filterTextField = new TextField();

            filterTextField.setOnKeyPressed((event) -> {
                if(event.getCode() == KeyCode.ENTER) {
                    filterThreadGroupsAndSetPane();
                }
            });

            vBox.getChildren().add(filterTextField);
            Button newThreadBtn = new Button("Start New Thread");
            newThreadBtn.setOnAction(event -> new Thread(() ->{
                System.out.println("New Thread sleeping!");
                try{
                    Thread.sleep(5000);
                    System.out.println("Done");
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }){{start();}});
            vBox.getChildren().add(newThreadBtn);
            return vBox;
        }

        @Override
        public void start(Stage primaryStage) {
            primaryStage.setTitle("Thread Monitor");
            rootPane = new BorderPane();
            rootPane.setLeft(setUpActionButtons());
            setThreadGroupScene();
            primaryStage.setScene(new Scene(rootPane, 500, 500));
            primaryStage.show();

        }
}
