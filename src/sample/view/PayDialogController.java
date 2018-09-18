package sample.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.ResourceBundle;

public class PayDialogController implements Initializable {
    public Button cancelButton;
    public Button confirmButton;
    public Label fromAddressLabel;
    public PasswordField passwordField;
    public TextField toAddressTextField;
    public TextField valueTextField;
    private String path;
    private boolean isPaySucceed = true;


    private String getPath() {
        return path;
    }


    void setPath(String path) {
        this.path = path;
        System.out.println("path3 : " + getPath());
    }

    @FXML
    private void closeStageMethod(ActionEvent event) {
        closeStage(event);
    }

    private void closeStage(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        fromAddressLabel.setText(getPath());
        System.out.println("init2");
    }


    public void confirmButton(ActionEvent actionEvent) {
        String password = passwordField.getText();
        String address = toAddressTextField.getText();
        if ("".equals(password)) {
            showPayResultAlert("password is empty!");
        } else if ("".equals(address)) {
            showPayResultAlert("address is empty!");
        } else {

            Thread t = new Thread(run);
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (isPaySucceed) {
                showPayResultAlert("succeed to pay");
                closeStage(actionEvent);
            }else {
                showPayResultAlert("fail to pay");
                isPaySucceed = true;
//                closeStage(actionEvent);
            }
        }
    }

    private Runnable run = new Runnable() {
        @Override
        public void run() {
            String password = passwordField.getText();
            String address = toAddressTextField.getText();
            int value = Integer.parseInt(valueTextField.getText());
            if ("".equals(password)) {
                showPayResultAlert("password is empty!");
            } else if ("".equals(address)) {
                showPayResultAlert("address is empty!");
            } else {
                payMethod(address, value, password);
            }
//            payMethod(address, value, password);
        }
    };

    private void showPayResultAlert(String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Pay result");
        alert.setHeaderText(null);
        alert.setContentText(message);
        ButtonType loginButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
//        Node loginButton = alert.getDialogPane().lookupButton(loginButtonType);
        alert.getButtonTypes().setAll(loginButtonType);
        //设置按钮
        alert.showAndWait();
    }

    private void payMethod(String address, int value, String password) {

        Web3j web3 = Web3j.build(new HttpService("https://rpc2.newchain.cloud.diynova.com"));
        NetVersion netVersion;
        String clientVersion = "";
        try {
            netVersion = web3.netVersion().send();
            clientVersion = netVersion.getNetVersion();
            System.out.println(clientVersion);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Credentials credentials;
        try {
            credentials = WalletUtils.loadCredentials(password, path);
            String fromAddress = credentials.getAddress();
            System.out.println(fromAddress);
            EthGetBalance balance = web3.ethGetBalance(fromAddress, DefaultBlockParameterName.LATEST).send();
            BigInteger b = balance.getBalance();
            System.out.println(b);


            // get the next available nonce
            EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(
                    fromAddress, DefaultBlockParameterName.LATEST).send();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            System.out.println(nonce);
            //toAddreess直接用
            EthGasPrice ethGasPrice = web3.ethGasPrice().send();
            BigInteger gasPrice = ethGasPrice.getGasPrice();
            System.out.println(gasPrice);

            Transaction tx = Transaction.createEtherTransaction(fromAddress, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, address, BigInteger.ONE);
            EthEstimateGas ethEstimateGas = web3.ethEstimateGas(tx).send();
            BigInteger gasLimit = ethEstimateGas.getAmountUsed();
            System.out.println(gasLimit);

            // create our transaction
            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                    nonce, gasPrice, gasLimit, address, Convert.toWei(BigDecimal.valueOf(value), Convert.Unit.ETHER).toBigInteger());
            //第五个参数为value
            System.out.println("to address :" + address);
            System.out.println("from address :" + fromAddress);
            // sign & send our transaction
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, Integer.parseInt(clientVersion), credentials);
            String hexValue = Numeric.toHexString(signedMessage);
            EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).send();
            String hash = ethSendTransaction.getTransactionHash();
            System.out.println(hash);
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            //可能出现密码错误
            isPaySucceed = false;
        }
    }
}
