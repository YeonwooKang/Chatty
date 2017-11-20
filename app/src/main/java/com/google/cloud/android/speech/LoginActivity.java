package com.google.cloud.android.speech;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static com.google.cloud.android.speech.ChatActivity.db;

// 로그인 화면
public class LoginActivity extends AppCompatActivity {
    EditText edtId;
    EditText edtPw;
    TextView tvJoin;

    static String userName="unknown";



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 스플래쉬 화면 띄우기
        startActivity(new Intent(this, SplashActivity.class));
        // 애니메이션 효과
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        //1012 하늘 수정 다 주석처리해주세요.
        //primary key ㅇㅅㅇ...
       // createDatabase("chatty");
       // Log.d("chatty 디비","생성");
        // 사용자 계정 테이블 테스트
        //dropTable("member");
        createTable("member");



        // EditText 가져오기
        edtId = (EditText) findViewById(R.id.edtId);
        edtPw = (EditText) findViewById(R.id.edtPw);

        // 임시 로그인 정보 저장
        edtId.setText("user2");
        edtPw.setText("1234");

        // 로그인 버튼 설정
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 사용자 계정 정보 저장
                String userId = edtId.getText().toString();
                String userPw = edtPw.getText().toString();

                // 로그인 성공시 사용자 계정 정보 전달
                if (checkUserLogin(userId, userPw)) {
                    Intent intent = new Intent();
                    intent.putExtra("userId", userId); // 사용자 아이디
                    intent.putExtra("userName", userName); // 사용자 이름
                    setResult(RESULT_OK, intent);
                    finish();

                } else { // 로그인 실패 처리
                    Toast.makeText(getApplicationContext(), "사용자 정보가 정확하지 않습니다.", Toast.LENGTH_SHORT).show();
                    edtId.setText("");
                    edtPw.setText(""); // 입력된 텍스트 초기화
                }
            }
        });

        // 회원가입 텍스트(버튼) 클릭 처리
        tvJoin = (TextView) findViewById(R.id.tvJoin);
        tvJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 회원가입 액티비티를 띄움
                Intent intent =new Intent(getApplicationContext(),SignUpActivity.class);
                startActivity(intent);
                // fade in/out 애니메이션 효과
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    //1012 하늘 chat으로 뺌
    /*// Database
    public void createDatabase(String name){
        try {
            db = openOrCreateDatabase(name, MODE_PRIVATE, null);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }*/

    //1012 하늘 수정
    public boolean checkMemberTable() {

        try{
            Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name ='member'", null);
            cursor.moveToFirst();

            if (cursor.getCount() > 0) {
                return true;
            } else {
                return false;
            }
        }catch (NullPointerException ex){
            return false;
        }

    }

    //1012 하늘 수정
    // 테이블이 없는 경우에만 새로 생성함
    public void createTable(String name){

        if (checkMemberTable()) {
            Log.d("스크립트 테이블", "있음");
        } else {
            Log.d("스크립트 테이블", "없음");
            db.execSQL("create table if not exists "+name+"(id text PRIMARY KEY, name text, pw text);");
            // 임시 사용자 데이터 삽입
            insertDefaultUserRecord();
        }


    }

    // 테이블 삭제
    public void dropTable(String name){
        db.execSQL("drop table if exists "+name+";");
    }

    public static void addUser(String id, String name, String pw){
        db.execSQL("insert into member(id, name, pw) values ('"+id+"', '"+name+"', '"+pw+"');");
    }

    public static boolean checkUserLogin(String userId, String userPw) {
        String sql="select id, name, pw from "+"member" + " where id = ?";
        String[] args={userId};
        Cursor c= db.rawQuery(sql,args);

        // 로그인 일치 확인
        while(c.moveToNext()){
            if(c.getString(0).equals(userId)) { //아이디가 존재하고
                if(c.getString(2).equals(userPw)) { // 비밀번호가 일치하면
                    String id=c.getString(0);
                    userName=c.getString(1);
                    String pw=c.getString(2);
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 시작될 때 토스트박스
        // Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
    }

    // 회원가입시 계정 중복 여부 체크
    public static boolean checkUserExsists(String userId){
        String sql="select id from "+"member" + " where id=?";
        String[] args={userId};
        Cursor c= db.rawQuery(sql,args);

        while(c.moveToNext()){
            if(c.getString(0).equals(userId))
                return true; // 아이디가 이미 존재하면 true 반환
        }
        return false;
    }

    public static void insertDefaultUserRecord(){

        addUser("user1","haneul1","1234");
        addUser("user2","haneul2","1234");
        addUser("user3","haneul3","1234");

        addUser("tester1", "한이음", "1234");
    }

}
