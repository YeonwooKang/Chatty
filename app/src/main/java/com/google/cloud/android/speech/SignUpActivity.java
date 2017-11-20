package com.google.cloud.android.speech;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

// 로그인 화면으로부터 가져온 메소드
import static com.google.cloud.android.speech.LoginActivity.addUser;
import static com.google.cloud.android.speech.LoginActivity.checkUserExsists;
import static com.google.cloud.android.speech.VocaBook.createVocaTable;
import static com.google.cloud.android.speech.Grade.createGradeTable;

// 회원가입 화면
public class SignUpActivity extends AppCompatActivity {

    // 화면 구성 객체
    Button btnSignUp, btnCancel;
    EditText editID, editPW, editName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // 커스텀 액션바 사용
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);

        // 버튼
        btnSignUp=(Button)findViewById(R.id.btnSignUp);
        btnCancel=(Button)findViewById(R.id.btnCancel);

        // 텍스트
        editID=(EditText)findViewById(R.id.editID);
        editName=(EditText)findViewById(R.id.editName);
        editPW=(EditText)findViewById(R.id.editPW);

        //1012 하늘 수정
        // 회원가입 버튼 클릭시 회원 정보 저장
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 회원 정보 추가
                // 회원 아이디가 기존에 존재하지 않은 경우에 추가
                if(!checkUserExsists(editID.getText().toString())) {
                    Log.d("회원가입","성공");
                    String userID=editID.getText().toString();
                    addUser(userID, editName.getText().toString(), editPW.getText().toString());
                    createVocaTable(userID+"_voca");
                    createGradeTable(userID+"_grade");
                    // Toast.makeText(getApplicationContext(), "가입 완료! 로그인 해주세요.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    editID.setText("");
                    Toast.makeText(getApplicationContext(), "중복된 아이디 입니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 뒤로가기 버튼 클릭시 액티비티 종료
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

}
