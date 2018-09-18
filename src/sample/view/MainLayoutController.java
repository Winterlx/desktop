package sample.view;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.utils.Convert;
import sample.Main;
import sample.Model.KeyStore;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import static org.web3j.utils.Convert.Unit.ETHER;

public class MainLayoutController implements Initializable {
    @FXML
    private TableView<KeyStore> keyStoreTable;
    @FXML
    private TableColumn<KeyStore, String> addressColumn;
    @FXML
    private TableColumn<KeyStore, String> balanceColumn;
    @FXML
    private Button refreshBalanceButton;
    @FXML
    private Button setPathButton;
    @FXML
    private Button createKeyStoreButton;

    private Main main;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Controller内控件以及数据初始化初始化
        addressColumn.setCellValueFactory(cellData -> cellData.getValue().fromAddressProperty());
        balanceColumn.setCellValueFactory(cellData -> cellData.getValue().balanceProperty());
        //设置balance内容靠右对齐
        balanceColumn.setCellFactory(new Callback<TableColumn<KeyStore, String>, TableCell<KeyStore, String>>() {
            @Override
            public TableCell<KeyStore, String> call(TableColumn<KeyStore, String> param) {
                TableCell<KeyStore, String> cell = new TableCell<KeyStore, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? null : getString());
                    }

                    private String getString() {
                        return getItem() == null ? "" : getItem();
                    }
                };
                cell.setStyle("-fx-alignment: CENTER-RIGHT;");
                return cell;
            }
        });
        menuInit();
    }

    private void menuInit() {
        final ContextMenu contextMenu = new ContextMenu();
        MenuItem item1 = new MenuItem("Pay");
        item1.setOnAction((ActionEvent event) -> {
            KeyStore item = keyStoreTable.getSelectionModel().getSelectedItem();
            String path = item.getPath();
            System.out.println("path0 : " + path);
            showPayDialog(path);
        });
        MenuItem item2 = new MenuItem("Show QRCode");
        item2.setOnAction((ActionEvent event) -> {
            KeyStore item = keyStoreTable.getSelectionModel().getSelectedItem();
            String address = item.getFromAddress();
            showAddressQRCode(address);

        });
        MenuItem item3 = new MenuItem("Copy Address to Clipboard");
        item3.setOnAction((ActionEvent event) -> {
            KeyStore item = keyStoreTable.getSelectionModel().getSelectedItem();
            String address = item.getFromAddress();
            setClipboardString(address);
            showCopySucceedAlert(address);
        });
        contextMenu.getItems().addAll(item1, item2, item3);
        keyStoreTable.setContextMenu(contextMenu);
    }

    private void showPayDialog(String path) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PayDialogLayout.fxml"));
        Parent parent = null;
        try {
            parent = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        PayDialogController payDialogController = fxmlLoader.getController();
        System.out.println("path1 : " + path);
        payDialogController.setPath(path);
        System.out.println("path4 : " + path);
        assert parent != null;
        Scene scene = new Scene(parent, 450, 200);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void showCopySucceedAlert(String address) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText("succeed to copy to clipboard ! \n" + "Address :" + address);

        alert.showAndWait();
    }

    private void showAddressQRCode(String address) {
        File file = generateQRCode(address);
        showQRCodeAlert(file, address);

    }

    private File generateQRCode(String address) {
        String path = main.path;

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        Map<EncodeHintType, String> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        BitMatrix bitMatrix;
        File file = null;
        try {
            bitMatrix = multiFormatWriter.encode(address, BarcodeFormat.QR_CODE, 200, 200, hints);
            file = new File(path, "test.jpg");
            MatrixToImageWriter.writeToStream(bitMatrix, "jpg", new FileOutputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private void showQRCodeAlert(File file, String address) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Show address with QRCode");
        // Header Text: null
        alert.setHeaderText("Scan the QRCode to get the address.");
        alert.setContentText("Address : " + address);
        Image image = new Image("file:" + file.getAbsolutePath());
        ImageView imageView = new ImageView();
        imageView.setImage(image);
        imageView.fitHeightProperty();
        alert.setGraphic(imageView);
        alert.showAndWait();
    }

    public void setMain(Main main) {
        this.main = main;
        keyStoreTable.setItems(main.getKeyStores());
    }

    private void showAddressAlert(String address) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Create successfully~!");
        alert.setHeaderText(null);
        alert.setContentText(address);
        alert.showAndWait();
    }


    @FXML
    public void handleCreateKeyStore() {
        //CreateKeyStore的单击事件
        showCreateKeyStoreDialog();
    }


    private void showCreateKeyStoreDialog() {
        //显示CreateKeyStoreDialog
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Create KeyStore");
        dialog.setHeaderText("input password(at least 8 characters)");
        ButtonType loginButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, cancelButtonType);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm Password");
        grid.add(new Label("Password:"), 0, 0);
        grid.add(password, 1, 0);
        grid.add(new Label("Confirm Password:"), 0, 1);
        grid.add(confirmPassword, 1, 1);
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);
        loginButton.addEventFilter(MouseEvent.ANY, event -> {
            if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
                System.out.println("you click the button");
            }
        });

        password.textProperty().addListener((observable, oldValue, newValue) -> loginButton.setDisable(newValue.trim().isEmpty()));
        dialog.getDialogPane().setContent(grid);
        Platform.runLater(password::requestFocus);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(password.getText(), confirmPassword.getText());
            }
            return null;
        });
        Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent(usernamePassword -> {
            String password1 = usernamePassword.getKey();
            String password2 = usernamePassword.getValue();
            if (password1.length() >= 8) {
                if (password1.equals(password2)) {
                    System.out.println("Password1 = " + usernamePassword.getKey() + ", Password2 = " + usernamePassword.getValue());
                    File file = createKeyStoreMethod(password1, main.prefs.get("walletPath", null));
                    Credentials credentials;
                    try {
                        credentials = WalletUtils.loadCredentials(password1, file);
                        String address = credentials.getAddress();
                        BigInteger i = main.refreshBalance(address);
                        String str2;
                        if (i.equals(BigInteger.valueOf(0))) {
                            str2 = "0.000000";
                        } else {
                            DecimalFormat df2 = new DecimalFormat("#.000000");//保留6位小数
                            str2 = df2.format(Convert.fromWei(i.toString(), ETHER));
                        }
                        main.getKeyStores().add(new KeyStore(address, file.getAbsolutePath(), str2));
                        keyStoreTable.refresh();
                        showAddressAlert(address);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    showAlert("password don`t match");
                }
            } else {
                System.out.println("not equals");
                showAlert("at least 8 characters");
                showCreateKeyStoreDialog();
            }
        });
    }

    private File createKeyStoreMethod(String password, String path) {
        //传入密码以及路径，返回所创建的钱包的fromAddress
        File file = null;
        try {
            String fileName = WalletUtils.generateNewWalletFile(password, new File(path));
            file = new File(path + "\\" + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public void handleRefreshBalance() {
        //Refresh的单击事件
        for (int i = 0; i < main.getKeyStores().size(); i++) {
            KeyStore ks = main.getKeyStores().get(i);
            ks.queryBalance();
            keyStoreTable.refresh();
        }
    }

    private static void setClipboardString(String text) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable trans = new StringSelection(text);
        clipboard.setContents(trans, null);
    }

    public void handleSetPath(ActionEvent actionEvent) {
        showSetPathDialog(actionEvent);
        System.out.println("MainLayout prefs :" + main.prefs.get("walletPath", null));
    }

    private Runnable run = new Runnable() {
        @Override
        public void run() {
            String path = main.prefs.get("walletPath", null);
            main.traverseWalletFolder(path);
        }
    };

    private void showSetPathDialog(ActionEvent event) {
        //选择文件用FileChooser
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        File dir = directoryChooser.showDialog(stage);
        if (dir != null) {
            System.out.println("dir" + dir.getAbsolutePath());
            main.prefs.put("walletPath", dir.getAbsolutePath());
            main.getKeyStores().clear();
            keyStoreTable.refresh();
            new Thread(run).start();
        }
    }

    private void showAlert(String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Alert");
        alert.setHeaderText(null);
        alert.setContentText(message);
        ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
//        Node loginButton = alert.getDialogPane().lookupButton(loginButtonType);
        alert.getButtonTypes().setAll(loginButtonType);
        //设置按钮
        alert.showAndWait();
    }
}
