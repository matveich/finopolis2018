package com.example.nesadimsergej.test;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.math.BigInteger;
import java.util.concurrent.Future;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;

import android.widget.TextView;
import android.widget.Toast;

public class Register extends AppCompatActivity {

    ConstraintLayout UserLayout,TCPLayout;
    Spinner dropdown;
    Button userRegisterButton,tcpRegisterButton;
    EditText phoneUSER,phoneTCP,nameTCP;
    Web3j web3;
    String[] items = new String[]{"Клиент-покупатель","ТСП"};

    Context context;
    Register _this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LoadAll();
        _this = this;
        // Настраиваем список для выбора роли
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setSelection(0);

        // Обработчик выбора роли
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
            {
                HideAllPgs();
                switch (pos){
                    case 1:
                            TCPLayout.setVisibility(View.VISIBLE);
                        break;
                    case 0:
                            UserLayout.setVisibility(View.VISIBLE);
                        break;
                    default:break;
                }
            }
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        userRegisterButton.setOnClickListener(v -> RegisterUser());
        tcpRegisterButton.setOnClickListener(v -> RegisterTCP());

        context = this;
        //_this = ;

    }



    void RegisterUser(){
        String s = checkUserFields();
        if(!s.equals("")) {
            Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
            return;
        }

        userRegisterButton.setEnabled(false);
        tcpRegisterButton.setEnabled(false);
        Utils.sendNotification(this, String.format(Utils.longLoadingMsg, "создание кошелька"), 1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Заводин новый адресс, публичный ключ и приватный ключ
                    File folder = new File(getApplicationContext().getFilesDir(),"");
                    String str = WalletUtils.generateLightNewWalletFile(" ", folder);
                    Credentials credentials = WalletUtils.loadCredentials(" ",folder.getAbsolutePath() + "/" +str);
                    // Сохраняем всю интересующую нас информацию
                    /**
                     * Загружаем смарт контракт ( все действия будут выполнены не от имени регистрируемого пользователя,
                     * а от имени владельца контракта( того, кто его залил)
                     **/


                    String phoneNumber = phoneUSER.getText().toString();
                    // Если номер это просто то 1, то текущий пользователь не регистрируется( кул хак)
                    String newFileName = str;
                    File crFile = new File(folder.getAbsolutePath() + "/" +str);
                    if (!(phoneNumber.length() == 1 && phoneNumber.charAt(0) == '1')) {
                        Loyalty contract = Loyalty.load(
                                Config.contractAdress,
                                web3,
                                Credentials.create(Config.bankPrivateKey, Config.bankPublicKey),
                                Loyalty.GAS_PRICE,
                                Loyalty.GAS_LIMIT);

                        // Хэш номера телефона, который мы будем отправлять в блокчейн
                        BigInteger phoneHash = new BigInteger(
                                String.valueOf(phoneNumber.hashCode())
                        );
                        newFileName = phoneHash.toString()+".json";
                        Boolean s = crFile.renameTo(new File(folder.getAbsolutePath() + "/"+newFileName));

                        File crFile1 = new File(folder.getAbsolutePath() + "/" +str);
                        System.out.println(phoneNumber);
                        System.out.println(phoneHash);
                        System.out.println(phoneHash.bitLength());

                        // Регистрируем пользователя
                        RemoteCall<TransactionReceipt> c = contract.addCustomer(
                                credentials.getAddress(),
                                phoneHash
                        );

                        Future<TransactionReceipt> a = c.sendAsync();
                        System.out.println(a.toString());
                        a.get();

                    }else{
                        //System.out.println("COOL HACK");
                    }
                    SharedPreferences sharedPref = getSharedPreferences(Config.AccountInfo, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("NAME",newFileName);
                    editor.putString("PATH",folder.getAbsolutePath());
                    editor.putBoolean(Config.IS_TCP,false);
                    editor.apply();

                    Utils.sendNotification(context, "Создание кошелька завершено, теперь вы можете войти!", 2);
                    // Перезагружаемся, иначе вылетает

                    if(!(phoneNumber.length() == 1 && phoneNumber.charAt(0) == '1')) {
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }else {
                        Intent intent = new Intent(getBaseContext(), Office_User.class);
                        startActivity(intent);
                    }
                }catch(Exception e){
                    _this.runOnUiThread(() -> userRegisterButton.setEnabled(true));
                    _this.runOnUiThread(() -> ((TextView)(findViewById(R.id.textView2))).setText(e.toString()));
                    e.printStackTrace();
                }
            }
        }).start();
    }
    void RegisterTCP(){
        String s = checkTCPFields();
        if(!s.equals("")) {
            Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
            return;
        }
        userRegisterButton.setEnabled(false);
        tcpRegisterButton.setEnabled(false);
        Utils.sendNotification(this, String.format(Utils.longLoadingMsg, "создание кошелька"), 1);
        new Thread(new Runnable() {
            public void run() {
                try {
                    //Заводин новый адресс, публичный ключ и приватный ключ
                    File folder = new File(getApplicationContext().getFilesDir(),"");
                    String str = WalletUtils.generateLightNewWalletFile(" ", folder);
                    Credentials credentials = WalletUtils.loadCredentials(" ",folder.getAbsolutePath() + "/" +str);

                    // Сохраняем всю интересующую нас информацию


                    /**
                     * Загружаем смарт контракт ( все действия будут выполнены не от имени регистрируемого пользователя,
                     * а от имени владельца контракта( того, кто его залил)
                     **/
                    Loyalty contract = Loyalty.load(
                            Config.contractAdress,/*Адресс контракта (указан в конфиге) */
                            web3,/* */
                            Credentials.create(Config.bankPrivateKey, Config.bankPublicKey),/**/
                            Loyalty.GAS_PRICE,
                            Loyalty.GAS_LIMIT);

                    String phoneNumber = phoneTCP.getText().toString();
                    String companyName = nameTCP.getText().toString();


                    String newFileName = str;
                    File crFile = new File(folder.getAbsolutePath() + "/" +str);

                    // Если номер это просто то 1, то текущий пользователь не регистрируется( кул хак)
                    if (!(phoneNumber.length() == 1 && phoneNumber.charAt(0) == '1')) {

                        // Хэш номера телефона, который мы будем отправлять в блокчейн
                        BigInteger phoneHash = new BigInteger(
                                String.valueOf(phoneNumber.hashCode())
                        );

                        newFileName = phoneHash.toString()+".json";
                        Boolean s = crFile.renameTo(new File(folder.getAbsolutePath() + "/"+newFileName));

                        if(crFile.delete())
                        {
                            System.out.println("File deleted successfully");
                        }
                        else
                        {
                            System.out.println("Failed to delete the file");
                        }

                        System.out.println(phoneNumber);
                        System.out.println(phoneHash);
                        System.out.println(phoneHash.bitLength());

                        // Регистрируем пользователя
                        contract.addCompany(
                                credentials.getAddress(),
                                companyName,
                                phoneHash
                        ).send();

                    }else{
                        System.out.println("COOL HACK");
                    }

                    SharedPreferences sharedPref = getSharedPreferences(Config.AccountInfo, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("NAME", newFileName);
                    editor.putString("PATH",folder.getAbsolutePath());
                    editor.putBoolean(Config.IS_TCP,false);
                    editor.apply();

                    Utils.sendNotification(context, "Создание кошелька завершено, теперь вы можете войти!", 2);
                    Intent i = getBaseContext().getPackageManager()
                            .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    // Переходим на сцену с личным кабинетом компании
                    //Intent intent = new Intent(getBaseContext(), Office_TCP.class);
                    //startActivity(intent);

                }catch(Exception e){
                    userRegisterButton.setEnabled(true);
                    ((TextView)(findViewById(R.id.textView2))).setText(e.toString());
                    e.printStackTrace();
                }
            }
        }).start();

    }
    String checkUserFields(){
        String phoneNumber = phoneUSER.getText().toString();
        if(phoneNumber.length() == 0){
            return "Не введен номер телефона";
        }
        return "";
    }
    String checkTCPFields(){
        String phoneNumber = phoneTCP.getText().toString();
        if(phoneNumber.length() == 0){
            return "Не введен номер телефона";
        }

        String companyName = nameTCP.getText().toString();

        if(companyName.length() == 0){
            return "Не введено имя компании";
        }

        return "";
    }



    // Функция для загрузки всех нужных эелементов сцены
    void LoadAll(){
        web3 = Web3jFactory.build(new HttpService(Config.web3Address));
        // Кнопки регистрации
        userRegisterButton = findViewById(R.id.userRegisterButton);
        tcpRegisterButton = findViewById(R.id.tcpRegisterButton);

        // Поля для регистрации пользователя
        phoneUSER = findViewById(R.id.phoneUSER);

        // Поля регистрации компании
        phoneTCP = findViewById(R.id.phoneTCP);
        nameTCP = findViewById(R.id.nameTCP);

        // Список для выбора кого регистрировать
        dropdown = findViewById(R.id.userSelector);

        TCPLayout = findViewById(R.id.TCP);
        UserLayout = findViewById(R.id.User);

    }

    void HideAllPgs(){
        TCPLayout.setVisibility(View.GONE);
        UserLayout.setVisibility(View.GONE);
    }
}


