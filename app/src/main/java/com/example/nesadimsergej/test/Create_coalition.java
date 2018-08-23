package com.example.nesadimsergej.test;

import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tuples.generated.Tuple2;

public class Create_coalition extends SceneController {

    EditText coalitionName;
    Button createCoalitionBtn;

    ConstraintLayout createCoalitionPart,coalitionCreated;

    CoalitionPage coalitionPage = null;

    public Create_coalition(View _page){
        super();
        page = _page;

        SetUpScene();
    }

    @Override
    void SetUpScene(){
        super.SetUpScene();
        createCoalitionPart = page.findViewById(R.id.createCoalitionPart);
        coalitionCreated = page.findViewById(R.id.coalitionCreated);

        coalitionName = createCoalitionPart.findViewById(R.id.coalitionName);
        createCoalitionBtn = createCoalitionPart.findViewById(R.id.createCoalitionBtn);


        CoalitionNotExists();
        createCoalitionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateCoalition();
            }
        });

    }

    @Override
    void OnSelected() {
        super.OnSelected();
        if(isCoalitionExists()){
            CoaltionExists();
            coalitionPage.OnSelected();
        }else{

        }

    }

    void CoalitionNotExists(){
        createCoalitionPart.setVisibility(View.VISIBLE);
        coalitionCreated.setVisibility(View.GONE);
    }

    void CoaltionExists(){
        createCoalitionPart.setVisibility(View.GONE);
        coalitionCreated.setVisibility(View.VISIBLE);
        if(coalitionPage == null) {
            coalitionPage = new CoalitionPage(coalitionCreated, ((Office) page.getContext()).credentials.getAddress());
        }
    }

    Boolean isCoalitionExists(){
        Credentials credentials = ((Office)page.getContext()).credentials;
        Web3j web3 = ((Office)page.getContext()).web3;

        Loyalty contract = Loyalty.load(Config.contractAdress,web3,
                credentials,
                Loyalty.GAS_PRICE,Loyalty.GAS_LIMIT);
        try {
            Tuple2<Boolean, String> s = contract.coalitions(credentials.getAddress()).send();
            //System.out.println(s);
            return s.getValue1();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    void CreateCoalition(){
        Toast.makeText(page.getContext(), "Успех",
                Toast.LENGTH_SHORT).show();
        String cName = coalitionName.getText().toString();
        Credentials credentials = ((Office)page.getContext()).credentials;
        Web3j web3 = ((Office)page.getContext()).web3;

        Loyalty contract = Loyalty.load(Config.contractAdress,web3,
                credentials,
                Loyalty.GAS_PRICE,Loyalty.GAS_LIMIT);
       try {

            contract.addCoalition(credentials.getAddress(), cName).send();
        }catch (Exception e){
            e.printStackTrace();
        }

        /*try {
            Tuple2<Boolean, String> s = contract.coalitions(credentials.getAddress()).send();
            System.out.println(s);
        }catch (Exception e){
            e.printStackTrace();
        }
        */
        // Создаем коалицию
    }


}

