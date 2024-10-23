package ch.vilki.secured;

import com.github.windpapi4j.InitializationFailedException;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Pair;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class MainWindowController {

    public TreeView<GuiProp> _treeViewEntries;

    @FXML TableView _tableView;
    @FXML  TableColumn<SecProp,String> _columnLabel;
    @FXML  TableColumn<SecProp,String> _columnKey ;
    @FXML  TableColumn<SecProp,String> _columnValue;
    @FXML  TableColumn<SecProp,Boolean> _columnSecured;

    ObservableList<SecProp> _items ; //= FXCollections.observableArrayList();

    TreeItem<GuiProp> _selectedProp;

    @FXML ToolBar _toolBar;
    @FXML Button _buttonOpenFile;
    @FXML Button _buttonCreateFile;
    @FXML Button _buttonAddProperty;
    @FXML Button _buttonImportCSV;

    //@FXML  NotificationPane _notificationPane;
    Gui _gui;
    SecStorage _secStorage = null;

    final ContextMenu _attributeValuesContextMenu = new ContextMenu();
    MenuItem _delete = new MenuItem("DELETE");
    MenuItem _add = new MenuItem("ADD");
    MenuItem _clipboard = new MenuItem("CLIPBOARD");

    final ContextMenu _treeContextMenu = new ContextMenu();
    MenuItem _deleteFromTree = new MenuItem("DELETE");
    MenuItem _renameFromTree = new MenuItem("RENAME");


    class SecProp {

        public StringProperty Key = new SimpleStringProperty();
        public StringProperty Value = new SimpleStringProperty();
        public StringProperty Label = new SimpleStringProperty();
        public SimpleBooleanProperty Enc = new SimpleBooleanProperty();
        public SecureProperty get_secureProperty() {
            return _secureProperty;
        }
        private SecureProperty _secureProperty = null;

        public void setValue(String value) {
            this.Value.set(value);
        }
        public SecProp(SecureProperty secureProperty)
        {
            Key.set(secureProperty.get_valueKey());
            Value.set(secureProperty.get_value());
            Enc.set(secureProperty.is_encrypted());
            Label.set(SecureProperty.getLabel(secureProperty.get_key()));
            Value.set(secureProperty.get_value());

            if(!secureProperty.is_encrypted())
            {
                Value.set(secureProperty.get_value());
            }
            else
            {
                Value.set(_secStorage.getPropValue(SecureProperty.createKeyWithSeparator(secureProperty.get_key())).toString());
            }

            _secureProperty = secureProperty;
        }

        public void update(SecureProperty secureProperty)
        {
            Key.set(secureProperty.get_valueKey());
            Value.set(secureProperty.get_value());
            Enc.set(secureProperty.is_encrypted());
            Label.set(SecureProperty.getLabel(secureProperty.get_key()));
            if(!secureProperty.is_encrypted())
            {
                Value.set(secureProperty.get_value());
            }
            else
            {
                Value.set(_secStorage.getPropValue(SecureProperty.createKeyWithSeparator(secureProperty.get_key())).toString());
            }
            _secureProperty = secureProperty;
        }
    }

    @FXML
    private void initialize() {

         _columnKey.setCellValueFactory(param -> param.getValue().Key);
        _columnValue.setCellValueFactory(param -> param.getValue().Value);
        _columnSecured.setCellValueFactory(param -> param.getValue().Enc);
        _columnLabel.setCellValueFactory(param -> param.getValue().Label);
        _tableView.setEditable(true);
        _tableView.getSelectionModel().cellSelectionEnabledProperty().set(true);
        _columnValue.setCellFactory( TextFieldTableCell.forTableColumn());
        _columnKey.setCellFactory( TextFieldTableCell.forTableColumn());
        _tableView.getSelectionModel().setCellSelectionEnabled(false);


        _items = FXCollections.observableArrayList(new Callback<SecProp, Observable[]>() {

            @Override
            public Observable[] call(SecProp param) {
                return new Observable[] {param.Enc};
            }
        });
        _tableView.setItems(_items);

        _items.addListener(new ListChangeListener<SecProp>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends SecProp> c) {
                SecureProperty s = null;
                SecProp modified = null;

                while (c.next()) {
                    if (c.wasUpdated()) {
                        modified = _items.get(c.getFrom());
                        if(_items.get(c.getFrom()).Enc.get())
                        {
                             s = _secStorage.secure(_items.get(c.getFrom()).get_secureProperty());
                        }
                        else {
                            s = _secStorage.unsecure(_items.get(c.getFrom()).get_secureProperty());
                        }
                    }
                }
                if(s!= null && modified != null)
                {
                    modified.update(s);
                    //_notificationPane.show("Item updated ->" + s.get_valueKey());
                }
            }
        });
        _columnSecured.setCellFactory(CheckBoxTableCell.forTableColumn(_columnSecured));

        _columnValue.setOnEditCommit(x->{
            if(x.getNewValue().equalsIgnoreCase(x.getOldValue())) return;
            if(x.getNewValue().equalsIgnoreCase(""))
            {
                SecProp secProp = (SecProp) _tableView.getSelectionModel().getSelectedItem();
                try {
                    //_notificationPane.show("Delete property");
                    _secStorage.deleteProperty(secProp.get_secureProperty());
                } catch (IOException e) {
                    GuiHelper.EXCEPTION("Error deleting",e.getMessage(),e);
                }
                _selectedProp.getValue().get_secureProperties().remove(secProp.get_secureProperty());
                showProperty(_selectedProp);
            }
            else
            {
                SecureProperty secureProperty = x.getRowValue().get_secureProperty();
                secureProperty.set_value(x.getNewValue());
                try {
                     if(secureProperty.is_encrypted()) {
                         _secStorage.addSecuredProperty(SecureProperty.createKeyWithSeparator(secureProperty.get_key())
                                 , new SecureString(x.getNewValue()));
                     }
                     else
                     {
                         _secStorage.addUnsecuredProperty(SecureProperty.createKeyWithSeparator(secureProperty.get_key())
                                 , x.getNewValue());
                     }
                }
                catch (SecureStorageException e) {
                        GuiHelper.EXCEPTION("Could not modify value",e.getMessage(),e);
                        return;
                }
                //_notificationPane.show("Entry modified OK->" + x.getRowValue().get_secureProperty().get_valueKey());
                x.getRowValue().update(_secStorage.getProperty(SecureProperty.createKeyWithSeparator(secureProperty.get_key())));

            }

        });

        _columnKey.setOnEditCommit(x->{
            if(x.getNewValue().equalsIgnoreCase(x.getOldValue())) return;
            if(x.getNewValue().equalsIgnoreCase(""))
            {
                GuiHelper.ERROR("KEY ERROR", "KEY Value missing");
                return;
            }
            SecProp secProp = (SecProp) _tableView.getSelectionModel().getSelectedItem();
            SecureProperty secureProperty = x.getRowValue().get_secureProperty();
            String newKey = secProp.Label.get() + "@@" + x.getNewValue();
            try {
                    SecureString secureString = _secStorage.getPropValue(SecureProperty.createKeyWithSeparator(secureProperty.get_key()));
                    if(secureProperty.is_encrypted()) {

                        _secStorage.addSecuredProperty(newKey,secureString);
                    }
                    else
                    {
                        _secStorage.addUnsecuredProperty(newKey,secureString.toString());
                    }

                     _secStorage.deleteProperty(secureProperty);
            } catch (SecureStorageException | IOException e) {
                GuiHelper.EXCEPTION("Error modify the key",e.getMessage(),e);
                return;
            }
            //_notificationPane.show("KEY MODIFICATION  OK->" + x.getRowValue().get_secureProperty().get_valueKey());
            _selectedProp.getValue().get_secureProperties().remove(secProp.get_secureProperty());
            _selectedProp.getValue().get_secureProperties().add(_secStorage.getProperty(newKey));
            showProperty(_selectedProp);
        });

        _tableView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                _attributeValuesContextMenu.show(_gui.get_primaryStage(),mouseEvent.getScreenX(), mouseEvent.getScreenY());
            }
         });

        _treeViewEntries.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                _treeContextMenu.show(_gui.get_primaryStage(),mouseEvent.getScreenX(), mouseEvent.getScreenY());
            }
        });

        _treeContextMenu.getItems().addAll(_renameFromTree,_deleteFromTree);
        _deleteFromTree.setOnAction(x->{
            if(_selectedProp == null || _selectedProp.getValue() == null) return;

            List<SecureProperty> allProperties = _secStorage.getAllProperties(_selectedProp.getValue().get_label());
            StringBuilder stringBuilder = new StringBuilder();
            for(SecureProperty secureProperty: allProperties)
            {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(SecureProperty.createKeyWithSeparator(secureProperty.get_key()));
            }

            if(!GuiHelper.confirm("Delete property?","Confirm property deletion !", "Following properties will be " +
                    "deleted: " + stringBuilder.toString())) return;
            for(SecureProperty secureProperty: allProperties)
            {
                _secStorage.deleteProperty(SecureProperty.createKeyWithSeparator(secureProperty.get_key()));
            }
            _items.clear();
            GuiProp guiProp = new GuiProp();
            guiProp.set_name(_treeViewEntries.getRoot().getValue().get_name());
            _treeViewEntries.setRoot(null);
            TreeItem<GuiProp> rootProp = new TreeItem<>();
            rootProp.setValue(guiProp);
            _treeViewEntries.setRoot(rootProp);
            _treeViewEntries.getRoot().setExpanded(true);
            Set<String> labels = _secStorage.getAllLabels();
            for(String s: labels.stream().sorted().collect(Collectors.toList()))
            {
                List<SecureProperty> d = _secStorage.getAllProperties(s);
                addProperty(s,d,_treeViewEntries.getRoot());
            }
            _selectedProp = null;
        });

        _renameFromTree.setOnAction(x->{
           renameProperty();
        });
        _attributeValuesContextMenu.getItems().addAll(_clipboard,_add,_delete);
       _delete.setOnAction(x->{
             if(_tableView.getSelectionModel().getSelectedItem() == null) return;
             deleteProperty();
       });

       _clipboard.setOnAction(x->{
                    if(_tableView.getSelectionModel().getSelectedItem() == null) return;
                    ObservableList cells = _tableView.getSelectionModel().getSelectedCells();
                    int index = _tableView.getSelectionModel().getSelectedIndex();
                    ObservableList indices = _tableView.getSelectionModel().getSelectedIndices();

                   SecProp secProp = (SecProp) _tableView.getSelectionModel().getSelectedItem();

                    SecureString secureString = null;

                   if(!secProp.get_secureProperty().is_encrypted())
                   {
                       secureString = new SecureString(secProp.get_secureProperty().get_value());
                   }
                   else
                   {
                        secureString = _secStorage.getPropValue(
                               SecureProperty.createKeyWithSeparator(secProp.get_secureProperty().get_key()));
                   }
                   Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                           new StringSelection(secureString.toString()), null
                   );
                   secureString.destroyValue();

       });

        _add.setOnAction(x->{
            Optional<Pair<String, String>> values = GuiHelper.addNewProperty();
            if(!values.isPresent())
                return;

            String newKey = "";
            String label ="";
            if(_selectedProp.getValue()!= null
                    && _selectedProp.getValue().get_label() != null
                    && !_selectedProp.getValue().get_label().equalsIgnoreCase("NO_LABEL"))
           label = _selectedProp.getValue().get_label() + "@@";
           newKey = label  +      values.get().getKey();

            Set<String> keys = _selectedProp.getValue().get_secureProperties().stream().map(y->y.get_valueKey()).collect(Collectors.toSet());
            if(keys.contains(values.get().getKey()))
            {
                GuiHelper.ERROR("Error adding property","Can not add, key exists allready ->" + values.get().getKey());
                return;
            }
            try {
                _secStorage.addUnsecuredProperty(newKey,values.get().getValue());
            } catch (SecureStorageException e) {
                GuiHelper.EXCEPTION("Error adding property",e.getMessage(),e);
                return;
            }
            SecureProperty newProp = _secStorage.getProperty(newKey);
            _selectedProp.getValue().get_secureProperties().add(newProp);
            showProperty(_selectedProp);

        });

        _treeViewEntries.setCellFactory(new Callback<TreeView<GuiProp>, TreeCell<GuiProp>>() {
            @Override
            public TreeCell<GuiProp> call(TreeView<GuiProp> param) {
                return new TreeCell<GuiProp>() {
                    @Override
                    protected void updateItem(GuiProp item, boolean empty) {
                        textProperty().unbind();
                        styleProperty().unbind();
                        if (empty || item == null) {
                            setGraphic(null);
                            textProperty().set(null);
                            styleProperty().set(null);
                            return;
                        }
                        if (item != null) {
                            textProperty().setValue(item.get_name());
                            if(item.get_style() != null)
                            {
                                setStyle(item.get_style());
                            }

                        }
                        super.updateItem(item, empty);
                    }
                };
            }
        });

        _buttonOpenFile.setGraphic(new ImageView(new Image(MainWindowController.class.getResourceAsStream("/ch/vilki/secured/editFile.png"))));
        _buttonCreateFile.setGraphic(new ImageView(new Image(MainWindowController.class.getResourceAsStream("/ch/vilki/secured/addFile.png"))));
        _buttonAddProperty.setGraphic(new ImageView(new Image(MainWindowController.class.getResourceAsStream("/ch/vilki/secured/addProperty.png"))));
        _buttonImportCSV.setGraphic(new ImageView(new Image(MainWindowController.class.getResourceAsStream("/ch/vilki/secured/importFromCSV.png"))));
        _buttonOpenFile.setOnAction(x->openFile());
        _buttonCreateFile.setOnAction(x-> {
            try {
                createFile();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        _treeViewEntries.getSelectionModel().selectedItemProperty().addListener((observable,oldValue,newValue)->{
            showProperty(newValue);
        });

        _buttonAddProperty.setOnAction(x->{
            String label="";
            if(_selectedProp != null) label = _selectedProp.getValue().get_label();
            Optional<SecureProperty> prop = GuiHelper.addNewProp(label);
            if(prop.isPresent())
            {
                try {
                    if (prop.get().is_encrypted()) {
                        _secStorage.addSecuredProperty(prop.get().get_key(), new SecureString(prop.get().get_value()));
                    } else {
                        _secStorage.addUnsecuredProperty(prop.get().get_key(), prop.get().get_value());
                    }
                }
                catch (SecureStorageException e){
                    GuiHelper.EXCEPTION("Error adding property ",e.getMessage(),e);
                    return;
                }
                _treeViewEntries.getRoot().getChildren().clear();
                Set<String> labels = _secStorage.getAllLabels();
                for(String s: labels.stream().sorted().collect(Collectors.toList()))
                {
                    List<SecureProperty> d = _secStorage.getAllProperties(s);
                    addProperty(s,d,_treeViewEntries.getRoot());
                }
                List<SecureProperty> d = _secStorage.getAllProperties("");
                if(!d.isEmpty()) addProperty("",d,_treeViewEntries.getRoot());

            }

        });
        _buttonImportCSV.setDisable(true);

        _buttonImportCSV.setOnAction(x->{
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("CSV files (*.csv)","*.csv");
            File file = GuiHelper.selectFile(filter,"Enter fileName", GuiHelper.FILE_OPTIONS.OPEN_FILE,_gui.get_primaryStage());
            if(file == null) return;

            CsvReader csvReader = new CsvReader();
            try {
                csvReader.readCSVFile(file.getAbsolutePath(),"utf-8");
            }
            catch (Exception e)
            {
                GuiHelper.EXCEPTION("Exception occured",e.getMessage(),e);
                return;
            }

            ArrayList<HashMap<String, ArrayList<String>>> parsedEntries = csvReader.getParsedEntries();
            if(parsedEntries.isEmpty())
            {
                GuiHelper.INFO("Nothing to import","Found no entries to import");
                return;
            }

            ObservableList<String> _observable = FXCollections.observableArrayList();
            ChoiceBox<String> labelChoiceBox = new ChoiceBox<>();
            ChoiceBox<String> keyChoiceBox = new ChoiceBox<>();
            ChoiceBox<String> valueChoiceBox = new ChoiceBox<>();
            labelChoiceBox.setItems(_observable);
            keyChoiceBox.setItems(_observable);
            valueChoiceBox.setItems(_observable);

            for(String k: parsedEntries.get(0).keySet())
            {
                _observable.add(k);
            }
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("MAPPING");
            dialog.setHeaderText("DEFINE COLUMN MAPPING");
            ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            grid.add(new Label("LABEL COLUMN"), 0, 1);
            grid.add(labelChoiceBox, 1, 1);
            grid.add(new Label("KEY COLUMN"), 0, 2);
            grid.add(keyChoiceBox, 1, 2);
            grid.add(new Label("VALUE COLUMN"), 0, 3);
            grid.add(valueChoiceBox, 1, 3);
            dialog.getDialogPane().setContent(grid);
            dialog.showAndWait();
            Platform.runLater(() -> labelChoiceBox.requestFocus());

            if(valueChoiceBox.getSelectionModel().getSelectedItem() == null || keyChoiceBox.getSelectionModel().getSelectedItem() == null) return;

           for(HashMap<String, ArrayList<String>> entry: parsedEntries) {
               String label = labelChoiceBox.getSelectionModel().getSelectedItem();
               String key = keyChoiceBox.getSelectionModel().getSelectedItem();
               String value = valueChoiceBox.getSelectionModel().getSelectedItem();
               ArrayList<String> labels = null;
               ArrayList<String> keys = null;
               ArrayList<String> values = null;

               if (label != null) labels = entry.get(label);
               if (key != null) keys = entry.get(key);
               if (value != null) values = entry.get(value);
               String createdLabel = "";

               if(labels != null) createdLabel = labels.get(0) + "@@" + keys.get(0);
               else  createdLabel = keys.get(0);
               try {
                   _secStorage.addUnsecuredProperty(createdLabel,values.get(0));
               } catch (SecureStorageException e) {
                   GuiHelper.EXCEPTION("Import error",e.getMessage(),e);
                   return;
               }
           }
            _items.clear();
            GuiProp guiProp = new GuiProp();
            guiProp.set_name(_treeViewEntries.getRoot().getValue().get_name());
            _treeViewEntries.setRoot(null);
            TreeItem<GuiProp> rootProp = new TreeItem<>();
            rootProp.setValue(guiProp);
            _treeViewEntries.setRoot(rootProp);
            _treeViewEntries.getRoot().setExpanded(true);
            Set<String> labels = _secStorage.getAllLabels();
            for(String s: labels.stream().sorted().collect(Collectors.toList()))
            {
                List<SecureProperty> d = _secStorage.getAllProperties(s);
                addProperty(s,d,_treeViewEntries.getRoot());
            }
            _selectedProp = null;

        });

         //WebView webView = new WebView();
        //_notificationPane.setContent(webView);
        Tab tab1 = new Tab("Notifications");
        //tab1.setContent(_notificationPane);
        tab1.styleProperty().set(" -fx-background-color: GREEN");
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(tab1);
        tabPane.styleProperty().setValue(" -fx-background-color: RED");

        _treeViewEntries.addEventFilter(KeyEvent.KEY_RELEASED, (KeyEvent event) -> {
            if (KeyCode.F2 == event.getCode()) {
                renameProperty();

            }
        });


    }

    public void set_main(Gui gui)
    {
        _gui = gui;
    }

    public void cleanUp()
    {
        _items.clear();
        _treeViewEntries.setRoot(null);
    }

    public void createFile() throws Exception {
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Properties files (*.properties)","*.properties");
        File file = GuiHelper.selectFile(filter,"Enter fileName", GuiHelper.FILE_OPTIONS.SAVE_AS,_gui.get_primaryStage());
        if(file == null) return;
        SecureString pass = GuiHelper.enterPassword("Master Password required","Enter master password for the storage ");
         try {
            SecStorage.createNewSecureStorage(file.getAbsolutePath(),pass,true);
            _secStorage = SecStorage.open_SecuredStorage(file.getAbsolutePath(),true);
        } catch (SecureStorageException | NoSuchAlgorithmException | InvalidKeySpecException | InitializationFailedException| IOException e) {
           GuiHelper.EXCEPTION("Creation failed",e.getMessage(),e);
           return;
        }
        GuiProp guiProp = new GuiProp();
        guiProp.set_name(file.getName());
        _treeViewEntries.setRoot(new TreeItem<GuiProp>(guiProp));
        _treeViewEntries.getRoot().setExpanded(true);
        Set<String> labels = _secStorage.getAllLabels();
        for(String s: labels.stream().sorted().collect(Collectors.toList()))
        {
            List<SecureProperty> d = _secStorage.getAllProperties(s);
            addProperty(s,d,_treeViewEntries.getRoot());
        }
        _buttonImportCSV.setDisable(false);
    }

    public void openFile()
    {
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("ALL files (*.*)","*.*");
        File file = GuiHelper.selectFile(filter,"Enter fileName", GuiHelper.FILE_OPTIONS.OPEN_FILE,_gui.get_primaryStage());
        if(file == null) return;
        try
        {
            if(SecStorage.isWindowsSecured(file.getAbsolutePath()))
            {
                if(!SecStorage.isSecuredWithCurrentUser(file.getAbsolutePath()))
                {
                    if(!GuiHelper.confirm("Open File","Can not open file ","This file has been secured with" +
                            " other windows user, to use this file you need master password. Proceed?")) return;
                    SecureString mPassword = GuiHelper.enterPassword("Master Password","Enter master password");
                    if (mPassword == null) return;
                    if(mPassword.get_value().length < 8)
                    {
                        GuiHelper.ERROR("Master password to short","Password to short");
                        return;
                    }
                    try
                    {
                        SecStorage.secureWithCurrentUser(file.getAbsolutePath(),mPassword);
                    }
                    catch (Exception e)
                    {
                        GuiHelper.EXCEPTION("Secure with current user failed",e.getMessage(),e);
                        return;
                    }
                }
                _secStorage = SecStorage.open_SecuredStorage(file.getAbsolutePath(),true);
           }

        }
        catch (Exception e)
        {
            GuiHelper.EXCEPTION("Error opening file",e.getMessage(),e);
        }
        GuiProp secureProperty = new GuiProp();
        secureProperty.set_name(file.getName());
        TreeItem<GuiProp> root = new TreeItem<>();
        secureProperty.set_style("-fx-text-fill: red;-fx-font-weight:bold;");
        root.setValue(secureProperty);
         _treeViewEntries.setRoot(root);
        _treeViewEntries.getRoot().setExpanded(true);
        Set<String> labels = _secStorage.getAllLabels();
        for(String s: labels.stream().sorted().collect(Collectors.toList()))
        {
            List<SecureProperty> d = _secStorage.getAllProperties(s);
            addProperty(s,d,_treeViewEntries.getRoot());
        }
        //_notificationPane.show("File opened");
        _buttonImportCSV.setDisable(false);
    }

     private void addProperty(String key, List<SecureProperty> properties ,TreeItem<GuiProp> parent)
    {
        String[] split = key.split("@@");
        if(split == null || split.length == 1){
            TreeItem<GuiProp> child = findChild(parent,key);
            if(child == null)
            {
                GuiProp guiProp = new GuiProp();
                if(key.equalsIgnoreCase("")) guiProp.set_name("NO_LABEL");
                else guiProp.set_name(key);
                String label = SecureProperty.getLabel(properties.get(0).get_key());
                if(label.equalsIgnoreCase("")) label = "NO_LABEL";
                if(properties.size() > 0) guiProp.set_label(label);

                guiProp.set_secureProperties(properties);
                TreeItem<GuiProp> treeItem = new TreeItem<>(guiProp);
                parent.getChildren().add(treeItem);
                parent.setExpanded(true);
            }
            return;
        }
        StringBuilder builder = new StringBuilder();
        for(int i=1; i< split.length  ; i++)
        {
            builder.append(split[i]);
            builder.append("@@");
        }
        builder.deleteCharAt(builder.length()-1);
        builder.deleteCharAt(builder.length()-1);
        TreeItem<GuiProp> child = findChild(parent,split[0]);
        String parentLabel = "";
        if(!_treeViewEntries.getRoot().equals(parent)) parentLabel = parent.getValue().get_label() + "@@";
        if(child == null)
        {
            GuiProp guiProp = new GuiProp();
            guiProp.set_name(split[0]);
            if(properties.size() > 0) guiProp.set_label(parentLabel + split[0]);

            child = new TreeItem<>(guiProp);
            parent.getChildren().add(child);
            parent.setExpanded(true);
        }
        addProperty(builder.toString(),properties,child);
    }

    private TreeItem<GuiProp> findChild(TreeItem<GuiProp> entry, String rdn)
    {
        if(entry.getChildren() == null || entry.getChildren().isEmpty()) return null;
        for(TreeItem<GuiProp> e : entry.getChildren() )
        {
            String compare = e.getValue().get_name();
            if(compare != null && compare.equalsIgnoreCase(rdn)) return  e;
        }
        return null;
    }

    private void showProperty(TreeItem<GuiProp> selectedProp)
    {
        _selectedProp = selectedProp;
        if(selectedProp == null)
        {
            return;
        }
        _items.clear();
        if(selectedProp.getValue().get_secureProperties() == null)
        {
            return;
        }
        List<SecureProperty> secureProperties = selectedProp.getValue().get_secureProperties();
        for(SecureProperty secureProperty: secureProperties.stream().sorted().collect(Collectors.toList()))
        {
           _items.add(new SecProp(secureProperty));
        }

        double tablewidth = _tableView.getWidth();
        _columnSecured.setPrefWidth(tablewidth/10.0);
        _columnLabel.setPrefWidth(tablewidth/5.0);
        _columnKey.setPrefWidth(tablewidth/5.0);
        _columnValue.setPrefWidth(tablewidth/2.0);
    }

    private void deleteProperty()
    {
        SecProp secProp = (SecProp) _tableView.getSelectionModel().getSelectedItem();
        try {
            _secStorage.deleteProperty(secProp.get_secureProperty());
        } catch (IOException e) {
            GuiHelper.EXCEPTION("Error deleting",e.getMessage(),e);
        }
        _selectedProp.getValue().get_secureProperties().remove(secProp.get_secureProperty());
        showProperty(_selectedProp);
    }

    private void renameProperty()
    {
        if(_selectedProp == null) return;
        String newValue = GuiHelper.enterValue("LABEL","NEW LABEL NAME",
                "ENTER NEW LABEL NAME", _selectedProp.getValue().get_name(),false);
        if(newValue==null) return;
        if(newValue.equalsIgnoreCase(_selectedProp.getValue().get_name()))return;
        String newLabel = _selectedProp.getValue().get_label().replace(_selectedProp.getValue().get_name(),newValue);

        List<SecureProperty> allProperties = _secStorage.getAllProperties(_selectedProp.getValue().get_label());
        for(SecureProperty secureProperty: allProperties)
        {
            SecureProperty newProp = secureProperty.copy();
            String key = newLabel + "@@" + secureProperty.get_valueKey();
            newProp.set_key(SecureProperty.createKey(key));
            _secStorage.addProperty(newProp);
        }
        for(SecureProperty secureProperty: allProperties)
        {
            _secStorage.deleteProperty(SecureProperty.createKeyWithSeparator(secureProperty.get_key()));
        }
        _items.clear();
        GuiProp guiProp = new GuiProp();
        guiProp.set_name(_treeViewEntries.getRoot().getValue().get_name());
        _treeViewEntries.setRoot(null);
        TreeItem<GuiProp> rootProp = new TreeItem<>();
        rootProp.setValue(guiProp);
        _treeViewEntries.setRoot(rootProp);
        _treeViewEntries.getRoot().setExpanded(true);
        Set<String> labels = _secStorage.getAllLabels();
        String select = null;
        for(String s: labels.stream().sorted().collect(Collectors.toList()))
        {
            List<SecureProperty> d = _secStorage.getAllProperties(s);
            addProperty(s,d,_treeViewEntries.getRoot());
        }
        _treeViewEntries.getSelectionModel().clearSelection();
        _selectedProp = null;
        //_notificationPane.show("Property renamed");
    }
}
