package com.androiddeft.mdp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Fragment created for supporting persistent user configurable string commands to the robot (C8 requirement)
 */
public class ConfigurableCommActivity extends AppCompatActivity {


  //tag for logging
  private static final String TAG = "ConfigurableCommActivity";

  private Button btnSave;
  private EditText editText1;
  private EditText editText2;
  private String StoredString1;
  private String StoredString2;
  SharedPreferences preferences;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_configurable_comm);

    editText1 =  (EditText)findViewById(R.id.editText1);
    editText2  = (EditText)findViewById(R.id.editText2);

    btnSave = (Button)findViewById(R.id.btnSave);
    preferences = getSharedPreferences("MyCommandFile", Context.MODE_PRIVATE);
    StoredString1 = preferences.getString("key_cmd1", "Hello");
    StoredString2 = preferences.getString("key_cmd2", "World");
    editText1.setText(StoredString1);
    editText2.setText(StoredString2);

    btnSave.setOnClickListener(new android.view.View.OnClickListener()
    {
      public void onClick(View v)
      {
        //if edittext fields are empty, set a default value to prevent null exception
        if (editText1.getText().toString().trim().length() <= 0){
          editText1.setText("Command 1 Text");
        }

        if (editText2.getText().toString().trim().length() <= 0){
          editText2.setText("Command 2 Text");
        }

        saveStrings();
        finish();
      }
    });
  }



  //method for saving strings
  public void saveStrings() {
    super.onPause();
    android.content.SharedPreferences.Editor editor = preferences.edit();
    editor.putString("key_cmd1", editText1.getText().toString());
    editor.putString("key_cmd2", editText2.getText().toString());
    editor.commit();

    Toast.makeText(getApplicationContext(), "Commands Saved Successfully", Toast.LENGTH_SHORT).show();
  }

}
