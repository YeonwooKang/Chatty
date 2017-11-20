package com.google.cloud.android.speech;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static com.google.cloud.android.speech.Script.insertScript;
import static com.google.cloud.android.speech.VocaBook.addCustomWord;

public class SettingsActivity extends AppCompatActivity {

    EditText editWord, editScriptTitle, editScriptContent;
    Button btnAddWord, btnScript, btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 커스텀 액션바 사용
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);

        Intent intent = getIntent();
        final String userId = intent.getStringExtra("userId");

        editWord = (EditText) findViewById(R.id.editWord);
        editScriptTitle = (EditText) findViewById(R.id.editScriptTitle);
        editScriptContent = (EditText) findViewById(R.id.editScriptContent);

        btnAddWord = (Button) findViewById(R.id.BtnAddWord);
        btnScript = (Button) findViewById(R.id.BtnAddScript);
        btnClose = (Button) findViewById(R.id.BtnClose);
        //1012 하늘 수정
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        // 사용자 지정 단어는 단어장에 저장할 때 스크립트 제목이 "custom"이다.
        btnAddWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editWord.getText().toString().isEmpty()) {
                    addCustomWord(userId + "_voca", editWord.getText().toString());
                    Toast.makeText(getApplicationContext(), "성공적으로 추가하였습니다!", Toast.LENGTH_LONG).show();
                    editWord.setText("");
                } else {
                    Toast.makeText(getApplicationContext(), "단어를 입력해주세요!", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnScript.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editScriptTitle.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "스크립트 제목을 입력해주세요!", Toast.LENGTH_LONG).show();
                } else if (editScriptContent.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "스크립트 내용을 입력해주세요!", Toast.LENGTH_LONG).show();
                } else {
                    insertScript(editScriptTitle.getText().toString(), editScriptContent.getText().toString());
                    Toast.makeText(getApplicationContext(), "성공적으로 추가하였습니다!", Toast.LENGTH_LONG).show();
                    editScriptTitle.setText("");
                    editScriptContent.setText("");
                }
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
