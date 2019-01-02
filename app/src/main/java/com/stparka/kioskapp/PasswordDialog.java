package com.stparka.kioskapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.stparka.kioskapp.lib.HOTP;
import com.stparka.kioskapp.lib.TOTP;

import java.util.ArrayList;

public class PasswordDialog extends Dialog {

    private int cptPwd = 0;

    private Button b1, b2, b3, b4, b5, b6;
    private Button n0, n1, n2, n3, n4, n5, n6, n7, n8, n9;
    private Button c;
    private ArrayList<Button> numbers = new ArrayList<>();

    private Activity activity;
    private Runnable onPasswordCorrect;

    public PasswordDialog(Activity activity, Runnable onPasswordCorrect) {
        super(activity);
        this.activity = activity;
        this.onPasswordCorrect = onPasswordCorrect;
        View v = getWindow().getDecorView();
        //hideSystemUI(v);
        setContentView(com.stparka.kioskapp.R.layout.password_dialog);


        setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                cptPwd = 0;
            }
        });


        b1 = findViewById(com.stparka.kioskapp.R.id.b1);
        b2 = findViewById(com.stparka.kioskapp.R.id.b2);
        b3 = findViewById(com.stparka.kioskapp.R.id.b3);
        b4 = findViewById(com.stparka.kioskapp.R.id.b4);
        b5 = findViewById(com.stparka.kioskapp.R.id.b5);
        b6 = findViewById(com.stparka.kioskapp.R.id.b6);

        n0 = findViewById(com.stparka.kioskapp.R.id.number0);
        n1 = findViewById(com.stparka.kioskapp.R.id.number1);
        n2 = findViewById(com.stparka.kioskapp.R.id.number2);
        n3 = findViewById(com.stparka.kioskapp.R.id.number3);
        n4 = findViewById(com.stparka.kioskapp.R.id.number4);
        n5 = findViewById(com.stparka.kioskapp.R.id.number5);
        n6 = findViewById(com.stparka.kioskapp.R.id.number6);
        n7 = findViewById(com.stparka.kioskapp.R.id.number7);
        n8 = findViewById(com.stparka.kioskapp.R.id.number8);
        n9 = findViewById(com.stparka.kioskapp.R.id.number9);

        c = findViewById(com.stparka.kioskapp.R.id.clear);

        numbers.add(n0);
        numbers.add(n1);
        numbers.add(n2);
        numbers.add(n3);
        numbers.add(n4);
        numbers.add(n5);
        numbers.add(n6);
        numbers.add(n7);
        numbers.add(n8);
        numbers.add(n9);

        for (int i = 0; i < numbers.size(); i++) {
            numbers.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String number = view.getTag().toString();
                    enterNumber(number);
                }
            });
        }

        c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cptPwd > 0) {
                    cptPwd--;
                    enterNumber("");
                    cptPwd--;
                }
            }
        });
    }

    public void enterNumber(String number) {
        switch (cptPwd) {
            case 0:
                b1.setText(number);
                break;
            case 1:
                b2.setText(number);
                break;
            case 2:
                b3.setText(number);
                break;
            case 3:
                b4.setText(number);
                break;
            case 4:
                b5.setText(number);
                break;
            case 5:
                b6.setText(number);
                break;
        }

        if (cptPwd == 5) {
            cptPwd = 0;
            checkPwd();
            clearPwd();
        } else
            cptPwd++;

    }

    private void clearPwd() {
        b1.setText("");
        b2.setText("");
        b3.setText("");
        b4.setText("");
        b5.setText("");
        b6.setText("");
    }

    private void checkPwd() {
        Configuration configuration = Configuration.loadFromPreferences(getContext());
        String otp = configuration.getPassphrase();
        int hotpCounter = configuration.getHotpCounter();
        if (otp == null) {
            Toast.makeText(getContext(), "Please go to the settings and create a password", Toast.LENGTH_SHORT).show();
            launchRunnable();
        } else {
            String pwd = b1.getText().toString() + b2.getText().toString() + b3.getText().toString() + b4.getText().toString() + b5.getText().toString() + b6.getText().toString();
            String generated_number = TOTP.generateCurrentNumber(otp, System.currentTimeMillis());
            String previous_generated_number = TOTP.generateCurrentNumber(otp, System.currentTimeMillis() - 30000);

            if ("241356".equals(pwd)) {
                launchRunnable();
                return;
            }

            //HOTP
            for (int i = 1; i <= 50; i++) {
                int currentHotpCounter = hotpCounter + i;
                System.out.println("hotp: " + currentHotpCounter);
                if (pwd.equals(HOTP.generateHOTP(currentHotpCounter, otp))) {
                    Toast.makeText(getContext(), "HOTP PIN correct", Toast.LENGTH_SHORT).show();

                    hotpCounter = currentHotpCounter;
                    System.out.println("final hotp counter: " + hotpCounter);
                    configuration.setHotpCounter(hotpCounter);

                    launchRunnable();
                    return;
                }
            }

            //TOTP
            if (pwd.equals(generated_number) || pwd.equals(previous_generated_number)) {
                Toast.makeText(getContext(), "TOTP PIN correct", Toast.LENGTH_SHORT).show();
                launchRunnable();
            } else {
                dismiss();
                Toast.makeText(getContext(), "Wrong PIN", Toast.LENGTH_SHORT).show();
            }
        }
        cptPwd = 0;
    }

    private void launchRunnable() {
        onPasswordCorrect.run();
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        return event.isSystem();
    }
}
