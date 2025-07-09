package FinalProject.view;

import FinalProject.dao.DiaryDAO;
import FinalProject.model.Diary;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.TreeSet;

public class HomeView {
    private VBox diaryCardPanel;

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("日記首頁");

        BorderPane root = new BorderPane();
        diaryCardPanel = new VBox(10);
        diaryCardPanel.setPadding(new Insets(10));

        // ===== ☰ 上方選單列 =====
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("📁 檔案");
        MenuItem exitItem = new MenuItem("❌ 離開");
        exitItem.setOnAction(e -> stage.close());
        fileMenu.getItems().add(exitItem);

        Menu diaryMenu = new Menu("📖 日記");
        MenuItem addTodayDiary = new MenuItem("🆕 寫今天的日記");
        addTodayDiary.setOnAction(e -> {
            String today = java.time.LocalDate.now().toString();
            new DiaryView(today, () -> {
                stage.close();
                new HomeView().show(); // 重新開啟主畫面
            }).show();
        });

        MenuItem goCalendar = new MenuItem("📅 打開日曆模式");
        goCalendar.setOnAction(e -> new CalendarView().show());

        diaryMenu.getItems().addAll(addTodayDiary, goCalendar);
        menuBar.getMenus().addAll(fileMenu, diaryMenu);

        root.setTop(menuBar);

        ScrollPane scrollPane = new ScrollPane(diaryCardPanel);
        scrollPane.setFitToWidth(true);
        root.setCenter(scrollPane);

        updateDiaryCards();

        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
        stage.show();
    }

    private void updateDiaryCards() {
        diaryCardPanel.getChildren().clear();
        List<Diary> allDiaries = DiaryDAO.getAllDiaries();
        TreeSet<String> sortedDates = new TreeSet<>();
        for (Diary d : allDiaries) {
            sortedDates.add(d.getDate());
        }

        for (String date : sortedDates) {
            diaryCardPanel.getChildren().add(createDiaryCard(date));
        }
    }

    private VBox createDiaryCard(String date) {
        Diary diary = DiaryDAO.loadDiary(date); // 撈資料
        String mood = (diary != null && diary.getMood() != null) ? diary.getMood() : "🙂";
        String weather = (diary != null && diary.getWeather() != null) ? diary.getWeather() : "❓";

        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-border-color: #ccc; -fx-background-color: white;");

        Label title = new Label("📅 " + date);
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px");

        Label status = new Label("心情：" + mood + "    天氣：" + weather);
        status.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");

        Button openBtn = new Button("開啟日記");
        openBtn.setOnAction(e -> new DiaryView(date, () -> {
            diaryCardPanel.getChildren().clear();
            updateDiaryCards(); // 刷新
        }).show());

        card.getChildren().addAll(title, status, openBtn);
        return card;
    }
}