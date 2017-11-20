package com.google.cloud.android.speech;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.StringTokenizer;

import static com.google.cloud.android.speech.ChatActivity.db;

public class Script extends Service {
    public Script() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private ArrayList<String> script = new ArrayList<String>();
    //private int scriptCnt;

    @Override
    public void onCreate() {
        super.onCreate();

        createScriptTable(); // 테이블 생성
        Log.d("스크립트 테이블", "생성");
        logScriptTitle();

    }

    // 스크립트 DB에서 가져오기
    public void selectScript(String title) {
        String sql = "select title, content from scripts where title=?;";
        String[] args = {title};
        Cursor c = db.rawQuery(sql, args);
        script = new ArrayList<String>();

        while (c.moveToNext()) {
            if (c.getString(0).equals(title)) { // 타이틀이 존재하는 경우
                String content = c.getString(1); // 컨텐츠를 가져옴
                StringTokenizer tokenizer = new StringTokenizer(content, ".");
                int countTokens = tokenizer.countTokens(); // 구둣점 단위로 구분

                for (int i = 0; i < countTokens; i++) {
                    String data = tokenizer.nextToken();
                    script.add(data); // 구분되어진 문장을 배열에 삽입
                }
                break;
            } else { // 타이틀이 존재하지 않는 경우 오류 메시지 출력
                script = new ArrayList<String>();
                script.add("잘못된 명령입니다. 스트립트를 다시 선택해주세요.");
            }
        }
    }

    // 배열 크기 반환
    public int getScriptCnt() {
        return script.size();
    }

    //1010 헉헉;
    public void deleteScript() {
        db.execSQL("delete from scripts where title=title1;");
        db.execSQL("delete from scripts where title=title2;");
    }

    // 1010 수정 완료 ㅠㅠㅠ 크흨ㅎ크규ㅠ 스크립트 테이블 생성
    public void createScriptTable() {
        if (checkScriptTable()) {
            Log.d("스크립트 테이블", "있음");
        } else {
            Log.d("스크립트 테이블", "없음");
            db.execSQL("create table if not exists scripts(id integer PRIMARY KEY autoincrement, title text, content text);");
            initScriptData();
        }


    }

    //1010 스크립트 테이블 유무를 확인하는 코드
    public boolean checkScriptTable() {

        try{
            Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name ='scripts'", null);
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

    public static void insertScript(String scriptTitle, String scriptContent) {
        db.execSQL("insert into scripts(title, content) values('" + scriptTitle + "', '" + scriptContent + "');");
    }

    // 초기 데이터 입력을 위한 메소드
    public void initScriptData() {
        /*db.execSQL("insert into scripts(title, content) values('conversation', " +
                "'All right I want to see a show of hands." +
                " how many of you have unfriended someone on Facebook." +
                " because they said something offensive about politics or religion child care food." +
                " And how many of you know at least one person that you avoid because you just do not want to talk to them.');");*/

        db.execSQL("insert into scripts(title, content) values('conversation', " +
                "' And how many of you know at least one person that you avoid because you just do not want to talk to them.');");

        db.execSQL("insert into scripts(title, content) values('5 ways to listen better', " +
                "'We are losing our listening. We spend roughly 60 percent of our communication time listening, but we are not very good at it." +
                " We retain just 25 percent of what we hear. Now not you, not this talk, but that is generally true. Let is define listening as making meaning from sound." +
                " It is a mental process, and it is a process of extraction. We use some pretty cool techniques to do this. One of them is pattern recognition." +
                " So in a cocktail party like this, if I say, David, Sara, pay attention some of you just sat up." +
                " We recognize patterns to distinguish noise from signal, and especially our name." +
                " Differencing is another technique we use. If I left this pink noise on for more than a couple of minutes, you would literally cease to hear it." +
                " We listen to differences we discount sounds that remain the same.');");

        db.execSQL("insert into scripts(title, content) values('10 ways to have a better conversation', " +
                "'I teach chemistry. All right, all right. So more than just explosions, chemistry is everywhere." +
                " Have you ever found yourself at a restaurant spacing out just doing this over and over?" +
                " Some people nodding yes. Recently, I showed this to my students, and I just asked them to try and explain why it happened." +
                " The questions and conversations that followed were fascinating. Check out this video that Maddie from my period three class sent me that evening." +
                " Now obviously, as Maddie is chemistry teacher, I love that she went home and continued to geek out about this kind of ridiculous demonstration that we did in class." +
                " But what fascinated me more is that Maddie is curiosity took her to a new level. If you look inside that beaker, you might see a candle." +
                " Maddie is using temperature to extend this phenomenon to a new scenario.');");

        db.execSQL("insert into scripts(title, content) values('A thousand times no', " +
                "'Two years ago, I was invited as an artist to participate in an exhibition commemorating 100 years of Islamic art in Europe." +
                " The curator had only one condition: I had to use the Arabic script for my artwork." +
                " Now, as an artist, a woman, an Arab, or a human being living in the world in 2010, I only had one thing to say I wanted to say no." +
                " And in Arabic, to say no, we say no, and a thousand times no. So I decided to look for a thousand different noes." +
                " on everything ever produced under Islamic or Arab patronage in the past 1,400 years, from Spain to the borders of China." +
                " I collected my findings in a book, placed them chronologically, stating the name, the patron, the medium and the date." +
                " Now, the book sat on a small shelf next to the installation, which stood three by seven meters, in Munich, Germany, in September of 2010.');");

        db.execSQL("insert into scripts(title, content) values('inside the egyptian revolution', " +
                "'This is Revolution 2.0. No one was a hero. No one was a hero. Because everyone was a hero." +
                " Everyone has done something. We all use Wikipedia. If you think of the concept of Wikipedia where everyone is collaborating on content, and at the end of the day you have built the largest encyclopedia in the world." +
                " From just an idea that sounded crazy, you have the largest encyclopedia in the world. And in the Egyptian revolution, the Revolution 2.0, everyone has contributed something, small or big." +
                " They contributed something to bring us one of the most inspiring stories in the history of mankind when it comes to revolutions." +
                " It was actually really inspiring to see all these Egyptians completely changing. If you look at the scene, Egypt, for 30 years, had been in a downhill going into a downhill." +
                " Everything was going bad. Everything was going wrong. We only ranked high when it comes to poverty, corruption, lack of freedom of speech, lack of political activism." +
                " Those were the achievements of our great regime. Yet, nothing was happening. And it is not because people were happy or people were not frustrated." +
                " In fact, people were extremely frustrated. But the reason why everyone was silent is what I call the psychological barrier of fear." +
                " Everyone was scared. Not everyone. There were actually a few brave Egyptians that I have to thank for being so brave going into protests as a couple of hundred, getting beaten up and arrested." +
                " But in fact, the majority were scared. Everyone did not want really to get in trouble.');");


        Log.d("스크립트", "입력됨");
    }

    // 테이블 삭제
    public void dropScriptTable() {
        db.execSQL("drop table scripts;");
    }

    //1009 잘들어갔는지 확인하는 로그 출력 (타이틀만)
    public static void logScriptTitle() {
        ArrayList<String> words = new ArrayList<String>();
        String sql = "select id from scripts";
        Cursor c = db.rawQuery(sql, null);
        String t = "";

        for (int i = 0; i < c.getCount(); i++) {
            c.moveToNext();
            String word = c.getString(0);
            t += word + " ";
        }
        //Log.d("로그 - 스크립트 id",t);
    }

    //
    public static String searchScriptString(String tableName, String title) {

        String sql = "select content from " + tableName + " where title=?";
        String[] args = {title};
        Cursor c = db.rawQuery(sql, args);

        c.moveToNext();
        String content = c.getString(0);
        //String record=id+","+pw+"\n";

        Log.d("tag-script", content);


        return content;
    }


    public static ArrayList<String> searchScriptArray(String tableName, String title) {
        ArrayList<String> script = new ArrayList<String>();

        Log.d("tag", tableName + "," + title);
        String sql = "select content from " + tableName + " where title=?";
        String[] args = {title};
        Cursor c = db.rawQuery(sql, args);

        c.moveToNext();
        String content = c.getString(0);
        //String record=id+","+pw+"\n";

        Log.d("tag-script", content);

        StringTokenizer tokenizer = new StringTokenizer(content, ".");
        int countTokens = tokenizer.countTokens();

        for (int i = 0; i < countTokens; i++) {
            String data = tokenizer.nextToken();
            script.add(data);
        }

        for (int h = 0; h < script.size(); h++) {
            Log.d("tag", script.get(h).toString());
        }

        return script;
    }

    /*
    public static ArrayList<String> searchScript(String tableName, String content){
        ArrayList<String> words=new ArrayList<String>();
        String sql="select content from "+tableName+" where content=?";
        String[] args={content};
        Cursor c=db.rawQuery(sql,args);

        for(int i=0; i<c.getCount(); i++){
            c.moveToNext();
            String word=c.getString(0);
            words.add(word);
        }

        return words;
    }
*/
    // 1009 1010 스크립트 출력위해 타이틀만 배열로 반환
    public static ArrayList<String> searchScriptTitle() {
        ArrayList<String> words = new ArrayList<String>();
        String sql = "select title from scripts";
        Cursor c = db.rawQuery(sql, null);

        for (int i = 0; i < c.getCount(); i++) {
            c.moveToNext();
            String word = c.getString(0);
            words.add(word);
            //Log.d("스크립트 타이틀",word);
        }
        return words;
    }

    // 2017.10.10 연우 추가
    // 스크립트 타이틀 배열 반환 메소드
    public ArrayList<String> getScriptTitle(){
        ArrayList<String> titles = new ArrayList<String>();
        String sql="select title from scripts";
        Cursor c=db.rawQuery(sql,null);
        for(int i=0; i<c.getCount(); i++){
            c.moveToNext();
            String singleTitle=c.getString(0);
            titles.add(singleTitle);
        }
        return titles;
    }

}
