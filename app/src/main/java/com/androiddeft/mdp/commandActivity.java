package com.androiddeft.mdp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class commandActivity extends AppCompatActivity {
    Button f1, f2;
    EditText txtFunction1, txtFunction2;
    SharedPreferences sharedPreferences;

    public static final String MY_PREFERENCE = "MyPref";
    public static final String FUNCTION_1 = "function1String";
    public static final String FUNCTION_2 = "function2String";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command);

        sharedPreferences = getSharedPreferences(MY_PREFERENCE, Context.MODE_PRIVATE);

        f1 = findViewById(R.id.function1BTN);
        f2 = findViewById(R.id.function2BTN);
        txtFunction1 = findViewById(R.id.txtFunction1);
        txtFunction2 = findViewById(R.id.txtFunction2);

        txtFunction1.setText(sharedPreferences.getString(FUNCTION_1, "Function 1 not set"));
        txtFunction2.setText(sharedPreferences.getString(FUNCTION_2, "Function 2 not set"));

        f1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(FUNCTION_1, txtFunction1.getText().toString());
                editor.commit();
                commandActivity.this.finish();
            }
        });

        f2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(FUNCTION_2, txtFunction2.getText().toString());
                editor.commit();
                commandActivity.this.finish();
            }
        });


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * 0.8), (int) (height * 0.5));
    }
}
