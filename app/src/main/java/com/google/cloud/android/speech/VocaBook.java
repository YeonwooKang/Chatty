package com.google.cloud.android.speech;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;


public class VocaBook extends Service {

    static SQLiteDatabase VocaDB;

    public VocaBook() {


    }

    @Override
    public void onCreate() {

        createDatabase("VocaDB");
        Log.d("보카 디비","create");
        createInitTable();
        //insertDefaultRecord();
        //확인용
        openAllRecord("user2_voca");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void createDatabase(String name){
        VocaDB=openOrCreateDatabase(name, MODE_PRIVATE,null);
    }

    //1012 하늘 수정 완료
    public static void createInitTable(){

        createVocaTable("user2_voca");
        createVocaTable("user3_voca");
    }

    //1012 하늘 수정
    public static boolean checkVocaTable(String name){
        Cursor cursor = VocaDB.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name ='"+name+"'" , null);
        cursor.moveToFirst();

        if(cursor.getCount()>0){
            return true;
        }else{
            return false;
        }
    }

    //1012 하늘 수정
    public static void createVocaTable(String name){
        if(checkVocaTable(name)){
            Log.d("보카 테이블","있음");
        }else{
            Log.d("보카 테이블","없음");
            VocaDB.execSQL("create table "+name+"(id integer PRIMARY KEY autoincrement, scriptTitle text, word text);");
            insertDefaultRecord(name);
        }
    }

    public static void deleteTable(String name){
        VocaDB.execSQL("drop table if exists "+name+";");
    }

    //1012 하늘 수정
    public static void insertDefaultRecord(String name){
        /* user1, user2, user3 보카 넣어두기 */


        if(name.equals("user2_voca")) {

            addWord("user2_voca","sample","wrote");
            addWord("user2_voca","sample","couch");
            addWord("user2_voca","sample","morning");
            addWord("user2_voca","sample2","separate");
            addWord("user2_voca","sample2","losers");

        }else if(name.equals("user3_voca")){

            addWord("user3_voca","sample","separate");
            addWord("user3_voca","sample","credit");
            addWord("user3_voca","sample","winners");

        }else if(name.equals("tester1_voca")){
            /*
            addWord("tester1_voca","title2","separate");
            addWord("tester1_voca","title2","credit");
            addWord("tester1_voca","title2","winners");
            */
        }else{
            Log.d(name,"초기 데이터 없음");
        }

    }

    public static void addWord(String tableName, String scriptTitle, String word){

        VocaDB.execSQL("insert into "+tableName+" (scriptTitle, word) values ('"+scriptTitle+"', '"+word+"');");

    }

    // 구현 메소드 1 : 틀린 단어를 저장한 다음에 다시 뽑아 배열에 저장하구 중복된 값을 없애는 메소드
    // 반환값 : 없다
    public static void removeDupAndAddWords(String tableName, String scriptTitle, ArrayList<String> words){

        // 1:일단 틀린단어 저장 (중복 상관없음)
        for(int i=0; i<words.size(); i++){
            addWord(tableName, scriptTitle, words.get(i));
        }

        // 2:중복된 값 제거
        ArrayList<String> duplicateWords=searchVoca(tableName,scriptTitle);

        HashSet<String> hs=new HashSet<String>(duplicateWords);
        ArrayList<String> removeDuplicateWords=new ArrayList<String>(hs); //중복된 단어가 제거된 배열

        resetScriptVoca(tableName,scriptTitle,removeDuplicateWords); //예에 다시 저장.
    }

    /*// 구현 메소드 2 : sciprt 별로 따로 저장.
    public static void removeDupAndAddWords(String tableName, String scriptTitle, String word){
        ArrayList<String> voca=searchVoca(tableName,scriptTitle);



        VocaDB.execSQL("insert into "+tableName+" (scriptTitle, word) values ('"+scriptTitle+"', '"+word+"');");
    }*/

    // 이 메소드 전단계는 새로히 저장할 단어가 중복된 값이 없어야한다.
    // 구현 메소드 3 : 테이블 안에 스크립트 별 리셋해서 저장. - 인수로는 테이블 이름과 스크립트 이름 그리고 새로히 저장할 단어 배열을 받아온다.
    // 즉 이 메소드는 스크립트로 구분되어야 한다.
    public static void resetScriptVoca(String tableName,String scriptTitle, ArrayList<String> newWords){

        //일단 스크립트 해당하는 데이터를 모두 삭제한다.
        VocaDB.execSQL("delete from "+tableName+" where scriptTitle = '"+ scriptTitle +"';");

        //새로 저장한다.
        for(int i=0; i<newWords.size(); i++){
            VocaDB.execSQL("insert into "+tableName+" (scriptTitle, word) values ('"+scriptTitle+"', '"+newWords.get(i)+"');");
        }


    }

    public static void addCustomWord(String tableName, String word){
        VocaDB.execSQL("insert into "+tableName+" (scriptTitle, word) values ('custom', '"+word+"');");

        ArrayList<String> duplicateWords=searchVoca(tableName,"custom");

        HashSet<String> hs=new HashSet<String>(duplicateWords);
        ArrayList<String> removeDuplicateWords=new ArrayList<String>(hs); //중복된 단어가 제거된 배열

        resetScriptVoca(tableName,"custom",removeDuplicateWords); //예에 다시 저장.

    }

    public static String openAllRecord(String tableName){

        String sql="select id, scriptTitle, word from "+tableName+";";
        Cursor c=VocaDB.rawQuery(sql,null);
        String record="";
        int recordCount=c.getCount();

        for(int i=0; i<recordCount; i++){
            c.moveToNext();
            String index=c.getString(0);
            String scriptTitle=c.getString(1);
            String word=c.getString(2);
            record=record+index+","+scriptTitle+","+word+"\n";
        }

        Log.d("tag-VocaDB",record);

        return record;
    }

    // 모든 단어를 검색할 때 사용하는 메소드 - 단어장에서 모든 단어 출력
    public static ArrayList<String> getWords(String tableName){

        ArrayList<String> words=new ArrayList<String>();
        String sql="select word from "+tableName+";";
        Cursor c=VocaDB.rawQuery(sql,null);
        String record="";
        int recordCount=c.getCount();

        for(int i=0; i<recordCount; i++){
            c.moveToNext();
            String word=c.getString(0);
            words.add(word);
            record=record+word+"\n";
        }

        Log.d("tag-VocaDB",record);

        return words;
    }

    // 테이블과 타이틀별 단어를 검색할 때 사용하는 메소드 - 단어장에서 스크립트별 출력
    public static ArrayList<String> searchVoca(String tableName, String scriptTitle){
        ArrayList<String> words=new ArrayList<String>();
        String sql="select word from "+tableName+" where scriptTitle=?";
        String[] args={scriptTitle};
        Cursor c=VocaDB.rawQuery(sql,args);

        for(int i=0; i<c.getCount(); i++){
            c.moveToNext();
            String word=c.getString(0);
            words.add(word);
        }

        return words;
    }

    // 스크립트 타이틀을 출력하는 메소드 - 스크립트 타이틀은 중복하므로 중복된 값을 제거하고 출력한다.
    public static ArrayList<String> searchScriptList(String tableName){
        ArrayList<String> scriptList=new ArrayList<String>();
        String sql="select scriptTitle from "+tableName+";";
        Cursor c=VocaDB.rawQuery(sql,null);

        for(int i=0; i<c.getCount(); i++){
            c.moveToNext();
            String scriptName=c.getString(0);
            scriptList.add(scriptName);
        }

        // 배열 안에 중복된 값 제거
        HashSet<String> hs=new HashSet<String>(scriptList);
        ArrayList<String> removeDuplicateWords=new ArrayList<String>(hs);

        return removeDuplicateWords;
    }


}
