package com.google.cloud.android.speech;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Grade extends Service {

    static SQLiteDatabase GradeDB;
    static boolean DEF_FLAG=false;

    public Grade() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //1012 하늘 수정
        createDatabase("GradeDB");
        Log.d("성적 디비","create");
        createInitTable();

        DEF_FLAG=true;
    }


    public void createDatabase(String name){
        GradeDB=openOrCreateDatabase(name, MODE_PRIVATE, null);
    }


    //1010 하늘 추가 - static 메소드로도 추가, 성적 테이블 유무를 확인하는 코드
    public static boolean checkGradeTable(String name){
        Cursor cursor = GradeDB.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name ='"+name+"'" , null);
        cursor.moveToFirst();

        if(cursor.getCount()>0){
            return true;
        }else{
            return false;
        }
    }

    //1012 하늘 수정 완료
    public static void createInitTable(){

        createGradeTable("user2_grade");
        createGradeTable("user3_grade");
    }

    // 1012 하늘 수정 완료
    public static void createGradeTable(String name){

        if(checkGradeTable(name)){
            Log.d("성적 테이블","있음");
        }else{
            Log.d("성적 테이블","없음");
            GradeDB.execSQL("create table if not exists "+name+"(gradeid integer primary key autoincrement, exdate text, score real);");
            insertDefaultRecord(name);
        }
    }

    public void deleteTable(String name){
        GradeDB.execSQL("drop table if exists "+name+";");
    }

    // 2017.10.11 연우 추가
    // 날짜와 점수를 가져오기 위한 메소드들
    public static ArrayList getGradeList(String name){
        ArrayList grades = new ArrayList();
        String sql="select score from " + name;

        Cursor c= GradeDB.rawQuery(sql,null);
        for(int i=0; i<c.getCount(); i++){
            c.moveToNext();
            double singleScore= c.getDouble(0);
            grades.add(singleScore);
        }
        return grades;
    }


    public static ArrayList<String> getDateList(String name){
        ArrayList<String> dates = new ArrayList<String>();
        String sql="select exdate from " + name;

        Cursor c= GradeDB.rawQuery(sql,null);
        for(int i=0; i<c.getCount(); i++){
            c.moveToNext();
            String singleDate=c.getString(0);
            dates.add(singleDate);
        }
        return dates;
    }


    //1012 하늘 수정
    public static void insertDefaultRecord(String name){

        if(name.equals("user2_grade")) {
            addGrade("user2_grade", "0", 0);
            addGrade("user2_grade", "17-09-27", 67.5);
            addGrade("user2_grade", "17-10-01", 53.3);
        }else if(name.equals("user3_grade")){
            addGrade("user3_grade","0",0);
            addGrade("user3_grade","2017-08-26",100);
            addGrade("user3_grade","2017-08-27",80);
            addGrade("user3_grade","2017-08-28",100);
        }else if(name.equals("tester1_grade")){
            addGrade("tester1_grade","0",0);
            addGrade("tester1_grade","2017-08-26",100);
            addGrade("tester1_grade","2017-08-27",80);
        }else{
            //1012 저녁 하늘 추가
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String getTime = sdf.format(date);
            addGrade(name,"0",0);

            Log.d(name,getTime);
        }

    }

    public static void addGrade(String tableName, String exdate, double score){
        GradeDB.execSQL("insert into "+tableName+"(exdate, score) values ('"+exdate+"', '"+score+"');");
    }

    // 이건 출력 확인용. - 로그
    public static String openAllGradeRecord(String tableName){

        String sql="select exdate, score from "+tableName+";";
        Cursor c=GradeDB.rawQuery(sql,null);
        String record="";
        int recordCount=c.getCount();

        for(int i=0; i<recordCount; i++){
            c.moveToNext();
            String date=c.getString(0);
            int score=c.getInt(1);
            record=record+date+","+String.valueOf(score)+"\n";
        }

        Log.d("tag OpenGrade-GradeDB",record);

        return record;
    }


    // 날짜만 배열에 넣어서 출력
    public static ArrayList<String> getDateArray(String tableName){

        ArrayList<String> dates=new ArrayList<String>();
        String sql="select exdate from "+tableName+";";
        Cursor c=GradeDB.rawQuery(sql,null);
        int recordCount=c.getCount();

        for(int i=0; i<recordCount; i++){
            c.moveToNext();
            String date=c.getString(0);
            dates.add(date);
        }

        return dates;
    }

    // 점수만 배열에 넣어서 출력
    public static ArrayList<Integer> getScoreArray(String tableName){
        ArrayList<Integer> scores=new ArrayList<Integer>();
        String sql="select score from "+tableName+";";
        Cursor c=GradeDB.rawQuery(sql,null);
        int recordCount=c.getCount();

        for(int i=0; i<recordCount; i++){
            c.moveToNext();
            int score=c.getInt(0);
            scores.add(score);
        }

        return scores;
    }
}
