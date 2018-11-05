import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class JavaFXThreadMonitor extends Application{

    private BorderPane rootPane;
    private TextField searchTextField, filterTextField;

    private final Image threadGroupImage = new Image(getClass().getResourceAsStream("ThreadGroupGraphic.png"));
    private final Image threadImage = new Image(getClass().getResourceAsStream("ThreadGraphic.png"));

    private List<TreeItem<String>> treeItems;

    private String threadFilter, threadGroupFilter;

    public static void main(String[] args) {
        launch(args);
    }

    private VBox setUpActionButtons(){
        VBox vBox = new VBox();
        vBox.getChildren().add(new Label("Search Thread:"));

        searchTextField = new TextField();

        searchTextField.setOnKeyPressed((event) -> {
            threadFilter = searchTextField.getText().toLowerCase();
            setThreadScene();
        });

        vBox.getChildren().add(searchTextField);
        vBox.getChildren().add(new Label("Filter ThreadGroup:"));
        filterTextField = new TextField();

        filterTextField.setOnKeyPressed((event) -> {
            threadGroupFilter = filterTextField.getText().toLowerCase();
            setThreadScene();
        });

        vBox.getChildren().add(filterTextField);

        Button newThreadBtn = new Button("Start New Thread");
        newThreadBtn.setOnAction(event -> new Thread(() ->{
            System.out.println("New Thread sleeping!");
            try{
                Thread.sleep(15000);
                System.out.println("Done");
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }){{
            this.setDaemon(true);
            start();
            setThreadScene();}});
        vBox.getChildren().add(newThreadBtn);

        return vBox;
    }

    private ThreadGroup getSystemThreadGroup(){
        ThreadGroup current;
        do{
            current = Thread.currentThread().getThreadGroup().getParent();
        }while (!current.getName().equals("system"));

        return current;
    }

    private void setChildrenThreads(TreeItem<String> threadGroupTreeItem, ThreadGroup threadGroup){
        int threadCount = threadGroup.activeCount();
        Thread[] threadArray = new Thread[threadCount];
        threadGroup.enumerate(threadArray, false);

        for(Thread thread : threadArray){
            if(thread == null){
                break;
            }
            String name = thread.getName();
            if(!name.toLowerCase().contains(threadFilter)){
                continue;
            }
            TreeItem<String> threadTreeItem = new TreeItem<>(thread.getName(), new ImageView(threadImage));
            setExpandedTreeItem(threadTreeItem, false);
            setThreadInfo(threadTreeItem, thread);
            threadGroupTreeItem.getChildren().add(threadTreeItem);
        }
    }

    private void setThreadInfo(TreeItem<String> thread, Thread t){
        ArrayList<TreeItem<String>> infoArray = new ArrayList<>();
        infoArray.add(new TreeItem<>("ID: " + t.getId()));
        infoArray.add(new TreeItem<>("State: " + t.getState()));
        infoArray.add(new TreeItem<>("Priority: " + t.getPriority()));
        infoArray.add(new TreeItem<>("Daemon: " + t.isDaemon()));
        for(TreeItem<String> infoItem : infoArray){
            thread.getChildren().add(infoItem);
        }
    }

    private void fillTreeWithThreadInfo(TreeItem<String> parentTreeItem, ThreadGroup threadGroup){
        int threadGroupCount = threadGroup.activeGroupCount();
        ThreadGroup[] threadGroupArray = new ThreadGroup[threadGroupCount];
        threadGroup.enumerate(threadGroupArray, false);

        for(ThreadGroup tg : threadGroupArray){
            if(tg == null){
                break;
            }
            TreeItem<String> threadGroupTreeItem = new TreeItem<>(tg.getName(), new ImageView(threadGroupImage));
            setExpandedTreeItem(threadGroupTreeItem, false);
            if(!threadGroupTreeItem.getValue().toLowerCase().contains(threadGroupFilter)){
                continue;
            }
            setChildrenThreads(threadGroupTreeItem, tg);
            fillTreeWithThreadInfo(threadGroupTreeItem, tg);
            parentTreeItem.getChildren().add(threadGroupTreeItem);
        }
    }

    private void setThreadScene(){
        ThreadGroup systemTG = getSystemThreadGroup();

        TreeItem<String> systemTreeItem = new TreeItem<> (systemTG.getName(), new ImageView(threadGroupImage));

        setExpandedTreeItem(systemTreeItem, true);

        if(systemTreeItem.getValue().toLowerCase().contains(threadGroupFilter)){
            setChildrenThreads(systemTreeItem, systemTG);
        }

        fillTreeWithThreadInfo(systemTreeItem, systemTG);

        ContextMenu popupMenu = new ContextMenu();
        TreeView<String> tree = new TreeView<> (systemTreeItem);
        tree.setContextMenu(popupMenu);
        createPopupMenu(popupMenu, tree);

        treeItems = buildTreeItemList(systemTreeItem);

        rootPane.setCenter(tree);
    }

    private void createPopupMenu(ContextMenu menu, TreeView<String> tree) {
        MenuItem item = new MenuItem("Interrupt Thread");
        item.setOnAction(event -> {
            TreeItem<String> selectedItem = tree.getSelectionModel().getSelectedItem();
            if(!isTreeItemThreadGroup(selectedItem)) {
                interruptThread(selectedItem.getValue(), selectedItem.getParent().getValue());
            }
        });
        menu.getItems().add(item);
    }

    private void interruptThread(String threadName, String parentName){
        ThreadGroup threadGroup = getSystemThreadGroup();
        int allActiveThreads = threadGroup.activeCount();
        Thread[] allThreads = new Thread[allActiveThreads];
        threadGroup.enumerate(allThreads);

        for(Thread thread : allThreads){
            if(thread.getName().equals(threadName) && thread.getThreadGroup().getName().equals(parentName)){
                thread.interrupt();
            }
        }
    }

    private List<TreeItem<String>> buildTreeItemList(TreeItem<String> root){
        List<TreeItem<String>> treeItemList = new ArrayList<>();

        treeItemList.add(root);
        for(TreeItem<String> treeItemLoop : root.getChildren()){
            if(isTreeItemThreadGroup(treeItemLoop)){
                treeItemList.addAll(buildTreeItemList(treeItemLoop));
            }else {
                treeItemList.add(treeItemLoop);
            }
        }

        return treeItemList;

    }

    private boolean getExpandedInfo(TreeItem<String> treeItem) throws NoSuchElementException{
        if(treeItems == null){
            throw new NoSuchElementException();
        }
        for(TreeItem<String> treeItemLoop : treeItems){
            if(areSameTreeItem(treeItemLoop, treeItem)){
                return treeItemLoop.isExpanded();
            }
        }
        throw new NoSuchElementException();
    }

    private boolean isTreeItemThreadGroup(TreeItem<String> treeItem){
        ImageView imageView = (ImageView) treeItem.getGraphic();
        if (imageView == null){
            return false;
        }
        return (imageView.getImage().equals(threadGroupImage));
    }

    private boolean areSameTreeItem(TreeItem<String> treeItemOne, TreeItem<String> treeItemTwo){
        if(treeItemOne.getValue().equals(treeItemTwo.getValue())){
            ImageView imageViewOne = (ImageView) treeItemOne.getGraphic();
            ImageView imageViewTwo = (ImageView) treeItemTwo.getGraphic();
            return imageViewOne.getImage().equals(imageViewTwo.getImage());
        }
        return false;
    }

    private void setExpandedTreeItem(TreeItem<String> treeItem, boolean defaultValue){
        try{
            treeItem.setExpanded(getExpandedInfo(treeItem));
        }catch (NoSuchElementException e){
            treeItem.setExpanded(defaultValue);
        }
    }

    @Override
    public void start(Stage primaryStage) {

        threadFilter = "";
        threadGroupFilter = "";

        primaryStage.setTitle("Thread Monitor");

        rootPane = new BorderPane();
        rootPane.setLeft(setUpActionButtons());

        Timeline fiveSecondsWonder = new Timeline(new KeyFrame(Duration.seconds(5), event -> setThreadScene()));
        fiveSecondsWonder.setCycleCount(Timeline.INDEFINITE);
        fiveSecondsWonder.play();

        setThreadScene();
        primaryStage.setScene(new Scene(rootPane, 500, 500));
        primaryStage.show();
    }
}
