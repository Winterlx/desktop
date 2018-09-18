package sample.Model;

import javafx.beans.property.SimpleStringProperty;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.math.BigInteger;
import java.text.DecimalFormat;

import static org.web3j.utils.Convert.Unit.ETHER;

public class KeyStore {

    private final SimpleStringProperty fromAddress = new SimpleStringProperty("");
    private final SimpleStringProperty path = new SimpleStringProperty("");
    private SimpleStringProperty balance = new SimpleStringProperty("");


    public KeyStore(String fromAddress, String path, String balance) {
        setFromAddress(fromAddress);
        setPath(path);
        setBalance(balance);
    }

    private void setBalance(String balance) {
        this.balance.set(balance);
    }

    public String getBalance(String address) {
        return balance.get();
    }

    public void queryBalance() {
        new Thread(run).start();
    }

    private Runnable run = () -> {
        BigInteger b;
        String str2 = "";
        try {
            final String rpcUrl = "https://rpc2.newchain.cloud.diynova.com";
            Web3j web3 = Web3j.build(new HttpService(rpcUrl));
            EthGetBalance balance = web3.ethGetBalance(fromAddress.getValue(), DefaultBlockParameterName.LATEST).send();
            System.out.println("fromAddress.getValue()" + fromAddress.getValue());
            b = balance.getBalance();
            if (b.equals(BigInteger.valueOf(0))) {
                str2 = "0.000000";
            } else {
                DecimalFormat df2 = new DecimalFormat("#.000000");//保留6位小数
                str2 = df2.format(Convert.fromWei(b.toString(), ETHER));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        balance = new SimpleStringProperty(str2);
    };


    public SimpleStringProperty balanceProperty() {
        return balance;
    }

    private void setFromAddress(String fromAddress) {
        this.fromAddress.set(fromAddress);
    }

    private void setPath(String path) {
        this.path.set(path);
    }

    public String getFromAddress() {
        return fromAddress.get();
    }

    public SimpleStringProperty fromAddressProperty() {
        return fromAddress;
    }

    public String getPath() {
        return path.get();
    }

    public SimpleStringProperty pathProperty() {
        return path;
    }
}
