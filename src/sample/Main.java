package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.json.JSONObject;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import sample.Model.KeyStore;
import sample.view.MainLayoutController;

import java.io.*;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.prefs.Preferences;

import static org.web3j.utils.Convert.Unit.ETHER;

public class Main extends Application {

    private final static String rpcUrl = "https://rpc3.newchain.cloud.diynova.com";
    public String path;

    private Stage primaryStage;
    private BorderPane rootLayout;

    private ObservableList<KeyStore> keyStores = FXCollections.observableArrayList();
    public Preferences prefs = Preferences.userNodeForPackage(Main.class);

    @Override
    public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("NewPay Desktop");

        Image image = new Image("file:ic_launcher.png");
        primaryStage.getIcons().add(image);

        if (prefs.get("walletPath", null) == null) {
            // TODO: 2018/9/18 判断系统
            path = "C:\\Files\\wallet";

            prefs.put("walletPath", path);
            File file = new File(path);
            if (!file.exists()) {
                boolean isCreate = file.mkdirs();
                System.out.println("result : " + Boolean.toString(isCreate));
            }
        } else {
            path = prefs.get("walletPath", null);
        }

        initRootLayout();
        showPersonOverview();
        new Thread(run).start();

    }

    private Runnable run = new Runnable() {
        @Override
        public void run() {
            traverseWalletFolder(path);
        }
    };

    /**
     * Initializes the root layout.
     */
    private void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("view/RootLayout.fxml"));
            rootLayout = loader.load();

            Scene scene = new Scene(rootLayout);
            primaryStage.setResizable(false);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the person overview inside the root layout.
     */
    private void showPersonOverview() {
        try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("view/MainLayout.fxml"));
            GridPane mainLayout = loader.load();

            rootLayout.setCenter(mainLayout);
            MainLayoutController controller = loader.getController();
            controller.setMain(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);

    }

    public void traverseWalletFolder(String path) {
        File file = new File(path);
        System.out.println("path:" + path);
        if (file.exists()) {
            File[] files = file.listFiles();
            assert files != null;
            for (File file2 : files) {
                System.out.println("file name 1:" + file2.getName());
                if (file2.getName().endsWith(".json")) {
                    System.out.println("file name 2:" + file2.getName());
                    String address = "0x" + getAddressFromFile(file2);
                    String absolutePath = file2.getAbsolutePath();
                    BigInteger i = refreshBalance(address);
                    String str2;
                    if (i.equals(BigInteger.valueOf(0))) {
                        str2 = "0.000000";
                    } else {
                        DecimalFormat df2 = new DecimalFormat("#.000000");//保留6位小数
                        str2 = df2.format(Convert.fromWei(i.toString(), ETHER));
                    }
                    keyStores.add(new KeyStore(address, absolutePath,/* i.toString()*/str2));
                    System.out.println("absolutePath:" + absolutePath);
                } else {
                    System.out.println(file2.getName() + " is not json file");
                }
            }
        } else {
            System.out.println("文件不存在!");
        }
    }

    public BigInteger refreshBalance(String fromAddress) {
        Web3j web3 = Web3j.build(new HttpService(rpcUrl));
        System.out.println("refreshBalance");
        BigInteger b = BigInteger.valueOf(0);
        try {
            EthGetBalance balance = web3.ethGetBalance(fromAddress, DefaultBlockParameterName.LATEST).send();
            b = balance.getBalance();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("b :" + b);
        return b;
    }

    private static String getAddressFromFile(File file) {
        String jsonFile = null;
        try {
            jsonFile = readFile(file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = new JSONObject(jsonFile);
        return jsonObject.getString("address");
    }

    private static String readFile(String filePath) throws IOException {
        StringBuffer sb = new StringBuffer();
        FileUtils.readToBuffer(sb, filePath);
        return sb.toString();
    }

    public ObservableList<KeyStore> getKeyStores() {
        return keyStores;
    }
}

