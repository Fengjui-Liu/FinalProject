package FinalProject.view;

import FinalProject.dao.DiaryDAO;
import FinalProject.model.Diary;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.util.Optional;
import javafx.scene.control.TextInputDialog;


import java.io.*;

public class DiaryView {
    private final String date;
    private final Runnable onSaveCallback;
    private Stage stage;
    private File selectedImageFile = null;
    private ComboBox<String> moodBox;
    private ComboBox<String> weatherBox;
    private TextArea content;
    private ImageView imagePreview;

    public DiaryView(String date, Runnable onSaveCallback) {
        this.date = date;
        this.onSaveCallback = onSaveCallback;
    }

    public void show() {
        stage = new Stage();
        stage.setTitle("日記 - " + date);

        BorderPane root = new BorderPane();

        // Menu Bar
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("\uD83D\uDCC1 檔案");
        MenuItem exportItem = new MenuItem("\uD83D\uDCE4 匯出為 PDF");
        exportItem.setOnAction(e -> exportToPDF());
        MenuItem exitItem = new MenuItem("\u274C 關閉");
        exitItem.setOnAction(e -> stage.close());
        fileMenu.getItems().addAll(exportItem, exitItem);

        Menu navMenu = new Menu("\uD83C\uDFE0 首頁");
        MenuItem goHome = new MenuItem("HomeView");
        goHome.setOnAction(e -> {
            new HomeView().show();
            stage.close();
        });
        navMenu.getItems().add(goHome);

        menuBar.getMenus().addAll(fileMenu, navMenu);
        root.setTop(menuBar);

        // Diary Content
        VBox diaryCard = new VBox(10);
        diaryCard.setPadding(new Insets(20));

        Label title = new Label("\uD83D\uDCC5 " + date);
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold");

        moodBox = new ComboBox<>();
        moodBox.getItems().addAll("\uD83D\uDE0A 很棒", "\uD83D\uDE42 普通", "\uD83D\uDE00 超好", "\uD83D\uDE15 難過", "\uD83E\uDD2F 爆炸了");

        weatherBox = new ComboBox<>();
        weatherBox.getItems().addAll("\u2600\uFE0F 晴朗", "\u26C5 多雲", "\uD83C\uDF27 下雨", "\u26C8 雷雨", "\u2744\uFE0F 下雪");

        content = new TextArea();
        content.setPrefRowCount(10);

        Button insertImageBtn = new Button("\uD83D\uDDBC 插入圖片");
        insertImageBtn.setOnAction(e -> chooseImage());

        imagePreview = new ImageView();
        imagePreview.setFitWidth(300);
        imagePreview.setFitHeight(200);
        imagePreview.setPreserveRatio(true);

        Button saveBtn = new Button("\u2705 儲存日記");
        saveBtn.setOnAction(e -> {
            saveDiary();
            saveToLocalFile();
            if (onSaveCallback != null) onSaveCallback.run();
            stage.close();
        });

        diaryCard.getChildren().addAll(title,
                new Label("心情："), moodBox,
                new Label("天氣："), weatherBox,
                new Label("內容："), content,
                insertImageBtn, imagePreview, saveBtn);

        ScrollPane scroll = new ScrollPane(diaryCard);
        scroll.setFitToWidth(true);
        root.setCenter(scroll);

        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
        stage.show();

        loadDiaryFromDB();

        weatherBox.setOnAction(e -> updateBackground(root, weatherBox.getValue()));
    }

    private void updateBackground(Region root, String weather) {
        String imagePath = switch (weather) {
            case "\u2600\uFE0F 晴朗" -> "/images/sky1.png";
            case "\u26C5 多雲" -> "/images/cloudynew.png";
            case "\uD83C\uDF27 下雨" -> "/images/rainnew.png";
            case "\u26C8 雷雨" -> "/images/tn.png";
            case "\u2744\uFE0F 下雪" -> "/images/snownew.png";
            default -> null;
        };

        if (imagePath != null) {
            try (InputStream is = getClass().getResourceAsStream(imagePath)) {
                if (is != null) {
                    Image image = new Image(is);
                    BackgroundImage bgImage = new BackgroundImage(
                            image,
                            BackgroundRepeat.NO_REPEAT,
                            BackgroundRepeat.NO_REPEAT,
                            BackgroundPosition.CENTER,
                            new BackgroundSize(100, 100, true, true, true, false)
                    );
                    root.setBackground(new Background(bgImage));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedImageFile = file;
            imagePreview.setImage(new Image(file.toURI().toString()));
        }
    }

    private void saveDiary() {
    	String mood = moodBox.getValue();
        String weather = weatherBox.getValue();
        String text = content.getText();
        String imagePath = selectedImageFile != null ? selectedImageFile.getAbsolutePath() : null;

        // ✅ 詢問是否加密
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("日記加密");
        dialog.setHeaderText("是否要為這篇日記設定密碼？");
        dialog.setContentText("請輸入密碼（可留空）：");

        Optional<String> result = dialog.showAndWait();
        String password = result.isPresent() ? result.get().trim() : null;

        // ✅ 建立日記
        Diary diary = new Diary(date, mood, weather, text, imagePath);
        diary.setPassword(password == null || password.isEmpty() ? null : password);

        DiaryDAO.saveDiary(diary);

        Alert alert = new Alert(Alert.AlertType.INFORMATION, "✅ 日記已儲存到資料庫與本機！");
        alert.showAndWait();
    }

    private void saveToLocalFile() {
        File folder = new File("diary");
        if (!folder.exists()) folder.mkdir();
        File file = new File(folder, "Diary_" + date + ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(date + "\n");
            writer.write(moodBox.getValue() + "\n");
            writer.write(weatherBox.getValue() + "\n");
            writer.write(content.getText());
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "\u274C 匯出失敗：" + e.getMessage()).showAndWait();
        }
    }

    private void exportToPDF() {
        System.out.println("目前執行目錄：" + System.getProperty("user.dir"));

        if ((content.getText() == null || content.getText().trim().isEmpty()) && selectedImageFile == null) {
            new Alert(Alert.AlertType.WARNING, "⚠️ 請先輸入內容或插入圖片再匯出！").showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("另存 PDF");
        fileChooser.setInitialFileName("Diary_" + date + ".pdf");
        File pdfFile = fileChooser.showSaveDialog(stage);

        if (pdfFile != null) {
            final BaseColor bgColor;

            String mood = moodBox.getValue();
            String weather = weatherBox.getValue();

            if ("😊 很棒".equals(mood) && "☀️ 晴朗".equals(weather)) bgColor = new BaseColor(255, 253, 231);
            else if ("😕 難過".equals(mood) && "🌧 下雨".equals(weather)) bgColor = new BaseColor(214, 219, 223);
            else if ("😀 超好".equals(mood)) bgColor = new BaseColor(224, 247, 250);
            else if ("🤯 爆炸了".equals(mood)) bgColor = new BaseColor(248, 215, 218);
            else bgColor = BaseColor.WHITE;

            try {
                Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
                PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(pdfFile));

                writer.setPageEvent(new PdfPageEventHelper() {
                    public void onEndPage(PdfWriter writer, Document document) {
                        PdfContentByte canvas = writer.getDirectContentUnder();
                        Rectangle pageSize = document.getPageSize();
                        canvas.saveState();
                        canvas.setRGBColorFill(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue());
                        canvas.rectangle(pageSize.getLeft(), pageSize.getBottom(), pageSize.getWidth(), pageSize.getHeight());
                        canvas.fill();
                        canvas.restoreState();
                    }
                });

                doc.open();

                String bgImagePath = switch (weather) {
                    case "☀️ 晴朗" -> "/images/sky1.png";
                    case "⛅ 多雲" -> "/images/cloudynew.png";
                    case "🌧 下雨" -> "/images/rainnew.png";
                    case "⛈ 雷雨" -> "/images/tn.png";
                    case "❄️ 下雪" -> "/images/snownew.png";
                    default -> null;
                };

                if (bgImagePath != null) {
                    try (InputStream imageStream = getClass().getResourceAsStream(bgImagePath)) {
                        if (imageStream != null) {
                            byte[] imageBytes = imageStream.readAllBytes();
                            com.itextpdf.text.Image bg = com.itextpdf.text.Image.getInstance(imageBytes);
                            bg.setAbsolutePosition(0, 0);
                            bg.scaleAbsolute(PageSize.A4.getWidth(), PageSize.A4.getHeight());
                            doc.add(bg);
                        }
                    }
                }

                BaseFont baseFont = BaseFont.createFont("C:/Windows/Fonts/msjh.ttc,0", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                Font font = new Font(baseFont, 16);

                String fullText = String.format("📅 日期: %s\n😊 心情: %s\n🌦 天氣: %s\n\n✏️ 內容:\n%s",
                        date,
                        mood != null ? mood : "未選擇",
                        weather != null ? weather : "未選擇",
                        content.getText().isEmpty() ? "(尚未填寫內容)" : content.getText());

                
             // ...（略過前段）

                // 插入圖片（優先顯示）
                if (selectedImageFile != null && selectedImageFile.exists()) {
                    com.itextpdf.text.Image img = com.itextpdf.text.Image.getInstance(selectedImageFile.getAbsolutePath());
                    img.setAlignment(Element.ALIGN_CENTER);
                    img.scaleToFit(400, 300);
                    doc.add(img);

                    
                }
                Paragraph spacing = new Paragraph(" ");
                spacing.setSpacingAfter(10);
                doc.add(spacing);


                // 加入設計感文字卡片
                PdfPTable table = new PdfPTable(1);
                table.setWidthPercentage(100);
                PdfPCell cell = new PdfPCell();
                cell.setPadding(15);
                cell.setBorderColor(new BaseColor(220, 220, 220)); // 淺灰邊框
                cell.setBackgroundColor(new BaseColor(255, 255, 255, 230)); // 微透明白底

                Paragraph para = new Paragraph(String.format(
                        "📅 日期: %s\n😊 心情: %s\n🌦 天氣: %s\n\n✏️ 內容:\n%s",
                        date,
                        mood != null ? mood : "未選擇",
                        weather != null ? weather : "未選擇",
                        content.getText().isEmpty() ? "(尚未填寫內容)" : content.getText()
                ), new Font(baseFont, 14));
                para.setSpacingBefore(10);
                para.setSpacingAfter(10);
                cell.addElement(para);
                table.addCell(cell);
                doc.add(table);

                doc.close();
                new Alert(Alert.AlertType.INFORMATION, "✅ PDF 匯出成功！").showAndWait();


            } catch (Exception e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "❌ 匯出失敗：" + e.getMessage()).showAndWait();
            }
        }
    }

    private void loadDiaryFromDB() {
        Diary diary = DiaryDAO.loadDiary(date);
        if (diary != null) {
            moodBox.setValue(diary.getMood());
            weatherBox.setValue(diary.getWeather());
            content.setText(diary.getContent());
            if (diary.getImagePath() != null) {
                selectedImageFile = new File(diary.getImagePath());
                imagePreview.setImage(new Image(selectedImageFile.toURI().toString()));
            }
        }
    }
}