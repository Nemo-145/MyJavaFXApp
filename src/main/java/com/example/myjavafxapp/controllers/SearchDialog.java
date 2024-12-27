package com.example.myjavafxapp.controllers;

import com.example.myjavafxapp.database.BreedDAO;
import com.example.myjavafxapp.database.JudgeDAO;
import com.example.myjavafxapp.database.MemberDAO;
import com.example.myjavafxapp.models.Breed;
import com.example.myjavafxapp.models.Judge;
import com.example.myjavafxapp.models.Member;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>Модальное окно поиска.
 * Вызывается при нажатии Ctrl + T в основном окне.</p>
 *
 * <ul>
 *     <li>Поиск нечувствителен к регистру.</li>
 *     <li>Поддерживается формат поиска по #id.</li>
 *     <li>Результаты среди: Участников (Member), Судей (Judge), Пород (Breed).</li>
 *     <li>При клике на элемент вызывается {@link SearchDialogListener#onItemSelected(String)}.</li>
 * </ul>
 */
public class SearchDialog {

    private static final Logger logger = LoggerFactory.getLogger(SearchDialog.class);

    private final Stage dialogStage;
    private final TextField searchField;
    private final ListView<String> resultList;
    private final ObservableList<String> dataList;

    public interface SearchDialogListener {
        void onItemSelected(String selectedItem);
    }

    private SearchDialogListener listener;

    /**
     * Создаёт окно поиска (модальное), привязанное к parentStage.
     * @param parentStage родительское окно
     */
    public SearchDialog(Stage parentStage) {
        logger.info("Создание окна поиска SearchDialog");

        dialogStage = new Stage();
        dialogStage.setTitle("Search");
        dialogStage.initOwner(parentStage);
        dialogStage.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        searchField = new TextField();
        searchField.setPromptText("Enter user name or ID in the format #id");

        resultList = new ListView<>();
        dataList = FXCollections.observableArrayList();
        resultList.setItems(dataList);

        // При вводе символов - обновляем результаты
        searchField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            updateResults(searchField.getText() + event.getCharacter());
        });

        // При клике на элемент списка
        resultList.setOnMouseClicked(e -> {
            String selected = resultList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (listener != null) {
                    listener.onItemSelected(selected);
                }
                dialogStage.close();
            }
        });

        // Изначально грузим всё
        updateResults("");

        root.getChildren().addAll(searchField, resultList);

        Scene scene = new Scene(root, 400, 300);
        dialogStage.setScene(scene);

        // Закомментированная возможность подключить CSS
        // String cssPath = getClass().getResource("/com/example/myjavafxapp/styles/search_dialog.css").toExternalForm();
        // scene.getStylesheets().add(cssPath);

        logger.info("Окно поиска SearchDialog создано");
    }

    /**
     * Устанавливает слушатель (колбэк), получающий строку выбранного элемента.
     */
    public void setSearchDialogListener(SearchDialogListener listener) {
        this.listener = listener;
    }

    /**
     * Отображает диалог поиска (модально).
     */
    public void showDialog() {
        dialogStage.showAndWait();
    }

    /**
     * Обновляет список результатов на основе query (без учёта регистра).
     * Если начинается с "#", значит ищем по ID.
     */
    private void updateResults(String query) {
        query = query.trim().toLowerCase(Locale.ROOT);

        boolean isIdSearch = query.startsWith("#");
        String idValue = isIdSearch && query.length() > 1 ? query.substring(1) : "";

        List<String> allItems = new ArrayList<>();

        // 1) Участники
        List<Member> members = MemberDAO.getAllMembers();
        for (Member m : members) {
            String display = String.format("Member: %s (#%d)", m.getName(), m.getId());
            allItems.add(display);
        }

        // 2) Судьи
        List<Judge> judges = JudgeDAO.getAllJudges();
        for (Judge j : judges) {
            String display = String.format("Judge: %s (#%d)", j.getName(), j.getId());
            allItems.add(display);
        }

        // 3) Породы
        List<Breed> breeds = BreedDAO.getAllBreedsAsObjects();
        for (Breed b : breeds) {
            String display = String.format("Breed: %s (#%d)", b.getName(), b.getId());
            allItems.add(display);
        }

        String finalQuery = query;
        List<String> filtered = allItems.stream()
                .filter(item -> {
                    if (finalQuery.isEmpty()) {
                        return true; // Пустая строка -> все
                    }
                    String lowerItem = item.toLowerCase(Locale.ROOT);
                    if (isIdSearch) {
                        return lowerItem.contains("#" + idValue);
                    } else {
                        return lowerItem.contains(finalQuery);
                    }
                })
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toList());

        dataList.setAll(filtered);
    }
}
