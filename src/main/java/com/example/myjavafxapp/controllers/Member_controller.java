package com.example.myjavafxapp.controllers;

import com.example.myjavafxapp.database.EventDAO;
import com.example.myjavafxapp.database.MemberDAO;
import com.example.myjavafxapp.models.Breed;
import com.example.myjavafxapp.models.Dog;
import com.example.myjavafxapp.models.Event;
import com.example.myjavafxapp.models.Member;
import com.example.myjavafxapp.utils.DatabaseToXMLExporter;
import com.example.myjavafxapp.utils.ReportGenerator;
import com.example.myjavafxapp.utils.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Контроллер для страницы, где отображаются участники (Members).
 * <ul>
 *     <li>Слева список участников в виде кнопок.</li>
 *     <li>Справа детали выбранного участника, режим редактирования, удаление и т.д.</li>
 *     <li>Поддерживается поиск (Ctrl+T), переход на нужного участника, судью или породу.</li>
 * </ul>
 */
public class Member_controller {

    private static final Logger logger = LoggerFactory.getLogger(Member_controller.class);

    /**
     * Если хотим при переходе сразу открыть конкретного участника (по ID).
     */
    private static int memberToOpen = 0;

    /**
     * Устанавливает memberToOpen (ID участника, которого нужно открыть).
     * Вызывается из других контроллеров, если нужно перейти к участнику.
     * @param memberId ID участника
     */
    public static void setMemberToOpen(int memberId) {
        memberToOpen = memberId;
    }

    // Ссылка на текущее событие (загружаем ID=1, если оно есть).
    private Event currentEvent;

    @FXML private Button Member;
    @FXML private Button Judge;
    @FXML private Button Breed;
    @FXML private Button About_app;
    @FXML private Button Event;
    @FXML private Button Create_pdf;

    @FXML private VBox space_for_list_applications;
    @FXML private VBox dataContainer_applications;
    @FXML private HBox buttonContainer;

    @FXML private Button closeButton;
    @FXML private Button minimizeButton;
    @FXML private Button hideButton;

    // Список VBox, каждый из которых содержит поля для описания одной собаки
    private final List<VBox> dogFieldsList = new ArrayList<>();

    // Режим редактирования (true) или просмотра (false)
    private boolean isEditMode = false;

    // Список id участников, отмеченных как победители (Winner)
    private final List<Integer> selectedWinners = new ArrayList<>();

    /**
     * Инициализация контроллера.
     * Загружаем текущее событие (ID=1) и формируем список участников слева.
     * Настраиваем кнопку PDF, поиск (Ctrl+T).
     * Если memberToOpen != 0, то сразу открываем этого участника.
     */
    @FXML
    public void initialize() {
        logger.info("Инициализация Member_controller");

        // Кнопки управления окном
        closeButton.setOnAction(e -> {
            logger.debug("Закрытие приложения (Member_controller)");
            closeApplication();
        });
        minimizeButton.setOnAction(e -> {
            logger.debug("Сворачивание приложения (Member_controller)");
            minimizeApplication();
        });
        hideButton.setOnAction(e -> {
            logger.debug("Полноэкранный режим (Member_controller)");
            maximizeApplication();
        });

        // Пытаемся загрузить событие ID=1
        currentEvent = EventDAO.getEventById(1);
        if (currentEvent == null) {
            logger.warn("Событие ID=1 не найдено в БД. Создаём пустой объект Event.");
            currentEvent = new Event();
            currentEvent.setId(1);
        }

        // Слева список участников
        loadDynamicButtons();

        // Навигация по кнопкам
        Event.setOnAction(e -> {
            SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_event.fxml", getStage());
        });
        Member.setOnAction(e -> {
            SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_applications.fxml", getStage());
        });
        Judge.setOnAction(e -> {
            SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_judge.fxml", getStage());
        });
        Breed.setOnAction(e -> {
            SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_breed.fxml", getStage());
        });
        About_app.setOnAction(e -> {
            SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_aboutapp.fxml", getStage());
        });

        // Кнопка PDF
        Create_pdf.setOnAction(event -> {
            logger.debug("Нажата кнопка PDF (Member_controller)");
            // 1) генерируем XML
            DatabaseToXMLExporter.exportAllTablesToXML("all_tables.xml");
            // 2) генерируем PDF
            new ReportGenerator().createPDF();
        });

        // Хоткей Ctrl+T для поиска
        initSearchHotkey();

        // Если memberToOpen != 0, то сразу открываем этого участника
        if (memberToOpen != 0) {
            logger.debug("Нужно сразу открыть участника ID={}", memberToOpen);
            Member mem = MemberDAO.getMemberById(memberToOpen);
            if (mem != null) {
                displayMemberDetails(mem);
            }
            memberToOpen = 0; // сбрасываем
        }
    }

    /**
     * Настраивает клавишу Ctrl+T для вызова окна поиска.
     */
    private void initSearchHotkey() {
        space_for_list_applications.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getAccelerators().put(
                        KeyCombination.keyCombination("Ctrl+T"),
                        this::showSearchDialog
                );
            }
        });
    }

    /**
     * Показывает окно поиска (SearchDialog).
     * Если пользователь выберет другого участника, судью или породу,
     * может произойти переход на соответствующую страницу.
     */
    private void showSearchDialog() {
        Stage parentStage = getStage();
        SearchDialog dialog = new SearchDialog(parentStage);

        dialog.setSearchDialogListener(selectedItem -> {
            logger.debug("Из поиска выбрано: {}", selectedItem);

            if (selectedItem.startsWith("Member:")) {
                // Получаем ID участника
                int idIndex = selectedItem.indexOf("#");
                if (idIndex != -1) {
                    String idStr = selectedItem.substring(idIndex+1, selectedItem.length()-1);
                    int memId = Integer.parseInt(idStr);
                    Member mem = MemberDAO.getMemberById(memId);
                    if (mem != null) {
                        displayMemberDetails(mem);
                    }
                }
            } else if (selectedItem.startsWith("Judge:")) {
                // Переходим на Judge_controller
                int idx = selectedItem.indexOf("#");
                if (idx != -1) {
                    String idStr = selectedItem.substring(idx+1, selectedItem.length()-1);
                    int jId = Integer.parseInt(idStr);
                    Judge_controller.setJudgeToOpen(jId);
                    SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_judge.fxml", parentStage);
                }
            } else if (selectedItem.startsWith("Breed:")) {
                // Переходим на Breed_controller
                int idx = selectedItem.indexOf("#");
                if (idx != -1) {
                    String idStr = selectedItem.substring(idx+1, selectedItem.length()-1);
                    int bId = Integer.parseInt(idStr);
                    Breed_controller.setBreedToOpen(bId);
                    SceneSwitcher.switchScene("/com/example/myjavafxapp/fxml/page_breed.fxml", parentStage);
                }
            }
        });

        dialog.showDialog();
    }

    /**
     * Загружает список участников из БД и создает кнопки для каждого из них слева.
     */
    private void loadDynamicButtons() {
        logger.debug("Загрузка списка участников из БД (Member_controller)");
        List<Member> members = MemberDAO.getAllMembers();
        space_for_list_applications.getChildren().clear();

        for (Member member : members) {
            Button memberButton = new Button(member.getName());
            memberButton.setPrefWidth(400);
            memberButton.setOnAction(e -> {
                isEditMode = false;
                displayMemberDetails(member);
            });
            space_for_list_applications.getChildren().add(memberButton);
        }

        // Кнопка "Add new member"
        Button addMemberButton = new Button("Add new member");
        addMemberButton.setPrefWidth(400);
        addMemberButton.setOnAction(e -> {
            // Переходим в режим добавления
            isEditMode = true;
            addNewMember();
        });
        space_for_list_applications.getChildren().add(addMemberButton);
    }

    /**
     * Отображает подробные сведения о выбранном участнике (справа).
     */
    private void displayMemberDetails(Member member) {
        logger.debug("Отображение деталей участника ID={}", member.getId());
        dataContainer_applications.getChildren().clear();

        Label nameLabel = new Label("Name: " + member.getName());
        Label ageLabel = new Label("Age: " + member.getAge());

        // Проверяем, является ли участник победителем
        List<Member> eventWinners = EventDAO.getEventWinners(currentEvent.getId());
        boolean isWinner = eventWinners.stream().anyMatch(w -> w.getId() == member.getId());
        Label winnerLabel = new Label("Winner: " + (isWinner ? "Yes" : "No"));

        dataContainer_applications.getChildren().addAll(nameLabel, ageLabel, winnerLabel);

        // Собаки
        for (Dog dog : member.getDogs()) {
            Label dogLabel = new Label("Dog Name: " + dog.getName() +
                    ", Age: " + dog.getAge() +
                    ", Breed: " + (dog.getBreed() != null ? dog.getBreed().getName() : "N/A"));
            dataContainer_applications.getChildren().add(dogLabel);
        }

        showViewModeButtons(member);
    }

    /**
     * Показывает кнопки "Redact" и "Delete" в режиме просмотра участника.
     */
    private void showViewModeButtons(Member member) {
        buttonContainer.getChildren().clear();

        Button redactButton = new Button("Redact");
        redactButton.setOnAction(e -> {
            isEditMode = true;
            enableEditing(member);
        });

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> {
            boolean confirmed = showConfirmationDialog("Are you sure you want to delete this member?");
            if (confirmed) {
                boolean deleted = MemberDAO.deleteMember(member.getId());
                if (deleted) {
                    logger.info("Участник ID={} удалён", member.getId());
                    loadDynamicButtons();
                    dataContainer_applications.getChildren().clear();
                } else {
                    logger.warn("Не удалось удалить участника ID={}", member.getId());
                }
            }
        });

        buttonContainer.getChildren().addAll(redactButton, deleteButton);
    }

    /**
     * Переводит интерфейс в режим редактирования: поля для имени, возраста, собаки, чекбокс Winner.
     */
    private void enableEditing(Member member) {
        dataContainer_applications.getChildren().clear();
        dogFieldsList.clear();

        TextField nameField = new TextField(member.getName());
        nameField.setPromptText("Enter the person's name");
        restrictToLetters(nameField);

        TextField ageField = new TextField(String.valueOf(member.getAge()));
        ageField.setPromptText("Enter the person's age");
        restrictToDigits(ageField);

        // WinnerCheckBox
        CheckBox winnerCheckBox = new CheckBox("Winner");
        List<Member> winners = EventDAO.getEventWinners(currentEvent.getId());
        boolean isCurrentWinner = winners.stream().anyMatch(w -> w.getId() == member.getId());
        winnerCheckBox.setSelected(isCurrentWinner);
        // При изменении winner
        winnerCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                if (!selectedWinners.contains(member.getId())) {
                    selectedWinners.add(member.getId());
                }
            } else {
                selectedWinners.remove((Integer) member.getId());
            }
        });

        // Собаки
        VBox dogsContainer = new VBox(10);
        if (member.getDogs().isEmpty()) {
            VBox dogFields = createDogFields(null);
            dogsContainer.getChildren().add(dogFields);
            dogFieldsList.add(dogFields);
        } else {
            for (Dog dog : member.getDogs()) {
                VBox dogFields = createDogFields(dog);
                dogsContainer.getChildren().add(dogFields);
                dogFieldsList.add(dogFields);
            }
        }

        Button addDogButton = new Button("Add one more dog");
        addDogButton.setOnAction(e -> {
            if (isLastDogFieldsValid()) {
                VBox df = createDogFields(null);
                dogsContainer.getChildren().add(df);
                dogFieldsList.add(df);
            } else {
                showValidationError("Please fill the last dog fields before adding a new one.");
            }
        });

        showEditModeButtons(member, nameField, ageField, dogsContainer, winnerCheckBox);

        dataContainer_applications.getChildren().addAll(nameField, ageField, winnerCheckBox, dogsContainer, addDogButton);
    }

    /**
     * Создаёт поля для новой собаки или редактирования существующей.
     */
    private VBox createDogFields(Dog dog) {
        Label dogNameLabel = new Label("Dog Name:");
        TextField dogNameField = new TextField(dog != null ? dog.getName() : "");
        dogNameField.setPromptText("Enter the dog's name");
        restrictToLetters(dogNameField);

        Label dogAgeLabel = new Label("Dog Age:");
        TextField dogAgeField = new TextField(dog != null ? String.valueOf(dog.getAge()) : "");
        dogAgeField.setPromptText("Enter the dog's age");
        restrictToDigits(dogAgeField);

        Label breedLabel = new Label("Breed:");
        ComboBox<Breed> breedCombo = new ComboBox<>();
        breedCombo.getItems().addAll(MemberDAO.getAllBreeds());
        if (dog != null && dog.getBreed() != null) {
            breedCombo.setValue(dog.getBreed());
        }

        Button removeButton = new Button("-");
        removeButton.setOnAction(e -> {
            if (dogFieldsList.size() == 1) {
                showValidationError("Cannot delete the only dog field.");
                return;
            }
            VBox dogBox = (VBox) removeButton.getParent();
            dogFieldsList.remove(dogBox);
            ((VBox) dogBox.getParent()).getChildren().remove(dogBox);
        });

        return new VBox(10, dogNameLabel, dogNameField, dogAgeLabel, dogAgeField, breedLabel, breedCombo, removeButton);
    }

    private VBox createDogFields() {
        return createDogFields(null);
    }

    /**
     * Проверяет, заполнены ли поля последней собаки.
     */
    private boolean isLastDogFieldsValid() {
        if (dogFieldsList.isEmpty()) return true;
        VBox lastDogFields = dogFieldsList.get(dogFieldsList.size() - 1);

        TextField dogNameField = (TextField) lastDogFields.getChildren().get(1);
        TextField dogAgeField = (TextField) lastDogFields.getChildren().get(3);
        @SuppressWarnings("unchecked")
        ComboBox<Breed> breedComboBox = (ComboBox<Breed>) lastDogFields.getChildren().get(5);

        String dogName = dogNameField.getText().trim();
        String dogAgeStr = dogAgeField.getText().trim();
        Breed breed = breedComboBox.getValue();

        return !(dogName.isEmpty() || dogAgeStr.isEmpty() || breed == null);
    }

    /**
     * Показывает кнопки "Save" и "Clean" для режима редактирования.
     */
    private void showEditModeButtons(Member member, TextField nameField, TextField ageField,
                                     VBox dogsContainer, CheckBox winnerCheckBox) {
        buttonContainer.getChildren().clear();

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            logger.debug("Сохранение изменений участника ID={}", member.getId());
            if (!validateInput(member, nameField, ageField)) {
                return;
            }
            StringBuilder errors = new StringBuilder();
            List<Dog> dogs = gatherDogsFromFields(member.getId());
            for (Dog d : dogs) {
                d.validate(errors);
            }
            if (errors.length() > 0) {
                showValidationError(errors.toString());
                return;
            }
            member.setDogs(dogs);
            saveMemberEdits(member, nameField, ageField);

            isEditMode = false;

            // Обновляем победителей
            EventDAO.updateWinners(currentEvent.getId(), selectedWinners);

            if (member.getId() != 0) {
                Member updated = MemberDAO.getMemberById(member.getId());
                if (updated != null) {
                    displayMemberDetails(updated);
                }
            } else {
                loadDynamicButtons();
                dataContainer_applications.getChildren().clear();
            }
        });

        // Кнопка Clean: удаляет все пустые поля собак (кроме одного, если все пустые)
        Button cleanButton = new Button("Clean");
        cleanButton.setOnAction(e -> {
            logger.debug("Очистка пустых полей собак (Clean) в Member_controller");
            cleanEmptyFields(dogsContainer);
        });

        buttonContainer.getChildren().addAll(cleanButton, saveButton);
    }

    /**
     * Собирает объекты Dog из полей ввода.
     */
    private List<Dog> gatherDogsFromFields(int ownerId) {
        List<Dog> dogs = new ArrayList<>();
        for (VBox dogFields : dogFieldsList) {
            TextField dogNameField = (TextField) dogFields.getChildren().get(1);
            TextField dogAgeField = (TextField) dogFields.getChildren().get(3);
            @SuppressWarnings("unchecked")
            ComboBox<Breed> breedComboBox = (ComboBox<Breed>) dogFields.getChildren().get(5);

            String dogName = dogNameField.getText().trim();
            if (dogName.isEmpty()) {
                continue;
            }
            int dogAge;
            try {
                dogAge = Integer.parseInt(dogAgeField.getText().trim());
            } catch (NumberFormatException e) {
                dogAge = -1;
            }
            Breed br = breedComboBox.getValue();
            dogs.add(new Dog(0, dogName, dogAge, br, ownerId));
        }
        return dogs;
    }

    /**
     * Удаляет пустые поля собак, если их больше одного.
     * Если после удаления все поля исчезли, то добавляем одно пустое поле.
     */
    private void cleanEmptyFields(VBox dogsContainer) {
        logger.debug("cleanEmptyFields() in Member_controller");

        // Собираем те, что надо удалить
        List<VBox> toRemove = new ArrayList<>();
        for (VBox dogFields : dogFieldsList) {
            TextField dogNameField = (TextField) dogFields.getChildren().get(1);
            TextField dogAgeField = (TextField) dogFields.getChildren().get(3);

            boolean empty = dogNameField.getText().trim().isEmpty()
                    && dogAgeField.getText().trim().isEmpty();
            if (empty && dogFieldsList.size() > 1) {
                // Удаляем эту группу полей, но только если не последняя
                toRemove.add(dogFields);
            }
        }

        dogFieldsList.removeAll(toRemove);
        for (VBox rm : toRemove) {
            dogsContainer.getChildren().remove(rm);
        }

        // Если в итоге нет ни одного поля, добавим одно пустое
        if (dogFieldsList.isEmpty()) {
            VBox newField = createDogFields(null);
            dogsContainer.getChildren().add(newField);
            dogFieldsList.add(newField);
        }
    }

    /**
     * Валидирует имя и возраст участника (через member.validate()).
     */
    private boolean validateInput(Member member, TextField nameField, TextField ageField) {
        member.setName(nameField.getText().trim());
        try {
            member.setAge(Integer.parseInt(ageField.getText().trim()));
        } catch (NumberFormatException e) {
            member.setAge(-1);
        }
        return member.validate();
    }

    /**
     * Сохраняет участника в БД (нового или обновляет существующего).
     */
    private void saveMemberEdits(Member member, TextField nameField, TextField ageField) {
        try {
            member.setName(nameField.getText().trim());
            member.setAge(Integer.parseInt(ageField.getText().trim()));

            if (!member.validate()) {
                return;
            }

            if (member.getId() == 0) {
                // Новый участник
                if (MemberDAO.saveMember(member)) {
                    int memberId = MemberDAO.getLastInsertedMemberId();
                    member.setId(memberId);
                    loadDynamicButtons();
                    dataContainer_applications.getChildren().clear();
                } else {
                    logger.warn("Не удалось сохранить нового участника");
                }
            } else {
                // Обновляем существующего
                if (MemberDAO.updateMember(member)) {
                    logger.info("Участник ID={} обновлён", member.getId());
                    loadDynamicButtons();
                } else {
                    logger.warn("Не удалось обновить участника ID={}", member.getId());
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please ensure all numeric fields contain valid numbers.");
        }
    }

    /**
     * Создаёт форму для добавления нового участника (ID=0),
     * задаёт поля, кнопку "Add one more dog" и кнопки Save/Clean.
     */
    private void addNewMember() {
        logger.debug("Переход в режим добавления нового участника");
        dataContainer_applications.getChildren().clear();
        dogFieldsList.clear();

        TextField nameField = new TextField();
        nameField.setPromptText("Enter the person's name");
        restrictToLetters(nameField);

        TextField ageField = new TextField();
        ageField.setPromptText("Enter the person's age");
        restrictToDigits(ageField);

        CheckBox winnerCheckBox = new CheckBox("Winner");
        winnerCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                logger.debug("Новый участник будет победителем после сохранения");
            }
        });

        VBox dogsContainer = new VBox(10);
        VBox initDogFields = createDogFields(null);
        dogsContainer.getChildren().add(initDogFields);
        dogFieldsList.add(initDogFields);

        Button addDogButton = new Button("Add one more dog");
        addDogButton.setOnAction(e -> {
            if (isLastDogFieldsValid()) {
                VBox df = createDogFields(null);
                dogsContainer.getChildren().add(df);
                dogFieldsList.add(df);
            } else {
                showValidationError("Please fill the last dog fields before adding a new one.");
            }
        });

        Member newMember = new Member(0, "", 0, null, null, null);

        showEditModeButtons(newMember, nameField, ageField, dogsContainer, winnerCheckBox);

        dataContainer_applications.getChildren().addAll(nameField, ageField, winnerCheckBox, dogsContainer, addDogButton);
    }

    private boolean showConfirmationDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setContentText(message);
        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }

    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText("Invalid Input");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Stage getStage() {
        return (Stage) Member.getScene().getWindow();
    }

    private void closeApplication() {
        getStage().close();
    }

    private void minimizeApplication() {
        getStage().setIconified(true);
    }

    private void maximizeApplication() {
        getStage().setFullScreen(!getStage().isFullScreen());
    }

    private void restrictToLetters(TextField textField) {
        textField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (!event.getCharacter().matches("[a-zA-Z]")) {
                event.consume();
            }
        });
    }

    private void restrictToDigits(TextField textField) {
        textField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (!event.getCharacter().matches("[0-9]")) {
                event.consume();
            }
        });
    }
}
