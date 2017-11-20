package com.google.cloud.android.speech;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import static com.google.cloud.android.speech.ChatActivity.userId;
import static com.google.cloud.android.speech.VocaBook.getWords;
import static com.google.cloud.android.speech.VocaBook.searchScriptList;
import static com.google.cloud.android.speech.VocaBook.searchVoca;


public class VocaActivity extends AppCompatActivity {

    Button allBtn, scriptBtn, practiceBtn, exitBtn;
    String title;
    ListView listView ;
    ArrayList<String> test1, test2;
    ArrayList<String> arr = new ArrayList<String>();

    // 스크립트 버튼 > 즉 단어가 아니라 타이틀이 떴을 때의 플래그
    public boolean ScriptListWordFLAG=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voca);

        // 커스텀 액션바 사용
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);

        allBtn = (Button)findViewById(R.id.allBtn);
        scriptBtn = (Button)findViewById(R.id.scriptBtn);
        practiceBtn=(Button)findViewById(R.id.practiceBtn);
        //1012 하늘 수정
        exitBtn=(Button)findViewById(R.id.exitBtn);
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1,arr);

        listView =(ListView)findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setDividerHeight(2);

        //리스트 선택시 > 스크립트 버튼 플래그가 true/false에 따라 구분됨. - true이면 title을 누른거니까 스크립트별 단어가 출력.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(ScriptListWordFLAG){
                    String titleName=test2.get(position);
                    title=titleName;

                    listView.setVisibility(View.VISIBLE);

                    arr.clear();

                    test2 = searchVoca(userId+"_voca",titleName);

                    for(int i=0; i<test2.size(); i++){
                        arr.add(i,test2.get(i).toString());
                        //arr.add("hjihi");
                    }

                    adapter.notifyDataSetChanged();
                    //ScriptListWordFLAG=false;
                }else{
                    ScriptListWordFLAG=false;
                }
            }
        });


        // 모든 단어 버튼
        allBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ScriptListWordFLAG=false;
                listView.setVisibility(View.VISIBLE);

                arr.clear();

                test1 = getWords(userId+"_voca");
                for(int i=0; i<test1.size(); i++){
                    arr.add(i,test1.get(i).toString());
                }
                adapter.notifyDataSetChanged();

            }
        });

        // 스크립트별 단어 버튼
        scriptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ScriptListWordFLAG=true;
                listView.setVisibility(View.VISIBLE);

                arr.clear();

                test2 = searchScriptList(userId+"_voca");

                for(int i=0; i<test2.size(); i++){
                    arr.add(i,test2.get(i).toString());
                }
                adapter.notifyDataSetChanged();

            }
        });

        practiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //리스트뷰에 스크립트 별 단어가 출력되어 있는 경우에만 연습 가능
                if(ScriptListWordFLAG){
                    Intent intent= new Intent(getApplicationContext(), ChatActivity.class);
                    String[] words=new String[arr.size()];

                    for(int i=0; i<arr.size(); i++){
                        words[i]=arr.get(i);
                    }
                    intent.putExtra("scriptTitle",title);
                    intent.putExtra("words",words);
                    setResult(RESULT_OK,intent);
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(), "모든 단어는 연습하실 수 없습니다.", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(arr.isEmpty()){
            Intent intent= new Intent(getApplicationContext(), ChatActivity.class);
            setResult(RESULT_OK,intent);
            finish();
        }
    }
}
