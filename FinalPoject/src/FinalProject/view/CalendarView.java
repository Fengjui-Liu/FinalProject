package FinalProject.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.YearMonth;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import FinalProject.dao.DiaryDAO;
import FinalProject.model.Diary;
import java.io.InputStream;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;





public class CalendarView {
    private Stage stage;
    private GridPane calendarGrid;
    private ComboBox<Integer> yearBox;
    private ComboBox<Integer> monthBox;
    private final int DAYS_IN_WEEK = 7;

    public void show() {
        stage = new Stage();
        stage.setTitle("日曆日記本");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F5F7FA;");

        yearBox = new ComboBox<>();
        monthBox = new ComboBox<>();

        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 5; i <= currentYear + 5; i++) {
            yearBox.getItems().add(i);
        }
        yearBox.setValue(currentYear);

        for (int i = 1; i <= 12; i++) {
            monthBox.getItems().add(i);
        }
        monthBox.setValue(LocalDate.now().getMonthValue());

        Button loadButton = new Button("📅 載入日曆");
        loadButton.setStyle("-fx-background-color: #667EEA; -fx-text-fill: white; -fx-font-weight: bold;");
        loadButton.setOnAction(e -> updateCalendar());

        HBox topPanel = new HBox(10, new Label("年份:"), yearBox,
                new Label("月份:"), monthBox, loadButton);
        topPanel.setPadding(new Insets(15));
        topPanel.setStyle("-fx-background-color: #E4E9F2;");
        root.setTop(topPanel);

        calendarGrid = new GridPane();
        calendarGrid.setHgap(10);
        calendarGrid.setVgap(10);
        calendarGrid.setPadding(new Insets(15));
        calendarGrid.setAlignment(Pos.CENTER);
        StackPane centerPane = new StackPane(calendarGrid);
        root.setCenter(centerPane);


        updateCalendar();

        Scene scene = new Scene(root, 880, 680);
        stage.setScene(scene);
        stage.show();
    }

    private void updateCalendar() {
        calendarGrid.getChildren().clear();

        String[] weekNames = {"一", "二", "三", "四", "五", "六", "日"};
        for (int i = 0; i < weekNames.length; i++) {
            Label label = new Label(weekNames[i]);
            label.setFont(Font.font("SansSerif", FontWeight.BOLD, 16));
            label.setStyle("-fx-alignment: center; -fx-background-color: #DCE3F0; -fx-padding: 8 0 8 0; -fx-pref-width: 100; -fx-text-fill: #333;");
            calendarGrid.add(label, i, 0);
        }

        int year = yearBox.getValue();
        int month = monthBox.getValue();

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDay = yearMonth.atDay(1);
        int dayOfWeek = firstDay.getDayOfWeek().getValue();
        int daysInMonth = yearMonth.lengthOfMonth();

        int col = (dayOfWeek == 7 ? 0 : dayOfWeek);
        int row = 1;

        for (int day = 1; day <= daysInMonth; day++) {
            Button dayButton = new Button(String.valueOf(day));
            dayButton.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #CCC; -fx-font-size: 14px;");
            dayButton.setPrefSize(100, 60);
            dayButton.setOnAction(e -> {
                String yStr = String.valueOf(year);
                String mStr = month < 10 ? "0" + month : String.valueOf(month);
                String dStr = dayButton.getText().length() < 2 ? "0" + dayButton.getText() : dayButton.getText();
                String date = yStr + "-" + mStr + "-" + dStr;
                Diary diary = DiaryDAO.loadDiary(date);
                if (diary != null && diary.getPassword() != null && !diary.getPassword().isBlank()) {
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("🔐 密碼保護");
                    dialog.setHeaderText("這篇日記已加密");
                    dialog.setContentText("請輸入密碼：");

                    Optional<String> result = dialog.showAndWait();
                    String input = result.isPresent() ? result.get().trim() : "";

                    if (!input.equals(diary.getPassword())) {
                        new Alert(Alert.AlertType.ERROR, "❌ 密碼錯誤，無法開啟此篇日記").showAndWait();
                        return; // 中止開啟
                    }
                }

                // ✅ 通過密碼或未加密，才開啟
                new DiaryView(date, null).show();

            });

            String dateStr = String.format("%04d-%02d-%02d", year, month, day);
            Diary diary = DiaryDAO.loadDiary(dateStr);

            StackPane dayCell = new StackPane(dayButton);

            if (diary != null && diary.getMood() != null) {
                String mood = diary.getMood();
                String moodIconFile = switch (mood) {
                    case "😊 很棒" -> "/images/happy1.png";
                    case "🙂 普通" -> "/images/notbad.png";
                    case "😀 超好" -> "/images/happyy.png";
                    case "😕 難過" -> "/images/sad.png";
                    case "🤯 爆炸了" -> "/images/reallybad.png";
                    default -> null;
                };

                if (moodIconFile != null) {
                    InputStream iconStream = getClass().getResourceAsStream(moodIconFile);
                    if (iconStream != null) {
                        ImageView moodIcon = new ImageView(new Image(iconStream));
                        moodIcon.setFitWidth(22);
                        moodIcon.setFitHeight(22);
                        StackPane.setAlignment(moodIcon, Pos.BOTTOM_RIGHT);
                        StackPane.setMargin(moodIcon, new Insets(0, 4, 4, 0));
                        dayCell.getChildren().add(moodIcon);
                    } else {
                        System.out.println("⚠️ 找不到圖片資源：" + moodIconFile);
                    }
                }
            }


            calendarGrid.add(dayCell, col % DAYS_IN_WEEK, row);

            col++;
            if (col % DAYS_IN_WEEK == 0) {
                row++;
            }
        }
    }
}