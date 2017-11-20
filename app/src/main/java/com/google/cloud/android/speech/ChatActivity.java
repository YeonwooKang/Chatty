package com.google.cloud.android.speech;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import static com.google.cloud.android.speech.Comparison.compareContext;
import static com.google.cloud.android.speech.Comparison.compareWords;
import static com.google.cloud.android.speech.Grade.DEF_FLAG;
import static com.google.cloud.android.speech.Grade.addGrade;
import static com.google.cloud.android.speech.Grade.openAllGradeRecord;
import static com.google.cloud.android.speech.Script.searchScriptArray;
import static com.google.cloud.android.speech.VocaBook.removeDupAndAddWords;
import static com.google.cloud.android.speech.VocaBook.resetScriptVoca;
import static com.google.cloud.android.speech.VocaBook.searchVoca;

public class ChatActivity extends AppCompatActivity implements MessageDialogFragment.Listener, TextToSpeech.OnInitListener {

    //1012 하늘
    // 사용자 정보 데이터베이스
    static SQLiteDatabase db;

    //1012 연우 추가 - TTS 객체
    private TextToSpeech tts;
    String msgText = "";

    // Activity 요청 코드
    public static final int REQUEST_CODE_PRACTICE_WORD=102;
    public static final int REQUEST_CODE_LOGIN = 101; // 로그인 요청 코드
    private static final String FRAGMENT_MESSAGE_DIALOG = "message_dialog";
    private static final String STATE_RESULTS = "results";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    // 2017.10.11 연우 추가 - 스크립트 제목 배열
    ArrayList<String> scriptTitles;

    // 로그인 되었는지 확인할 boolean 변수
    public static boolean isLogined = false;

    // 메뉴 실행 확인할 boolean 변수
    private static boolean IS_MENU1 = false;
    private static boolean IS_MENU2 = false;
    private static boolean IS_MENU3 = false;

    // 사용자 계정
    public static String userId = null;
    private static String userName = null;
    private static String scriptTitle=null;

    // 정확도 계산을 위한 단어 개수
    public static int totalWordsCount = 0;
    public static int wrongWordsCount = 0;
    static double accuracy; // 정확도

    public static double accAvg = 0;
    public static int accCount = 0;
    public static double totalAccuracy = 0;

    boolean ScriptDB_SERVICE_FLAG=false;
    boolean VocaDB_SERVICE_FLAG=false;
    boolean GradeDB_SERVICE_FLAG=false;

    // 타이머 핸들러
    int mili = 10;
    ArrayList<String> userWords=new ArrayList<String>();
    ArrayList<String> userAns = new ArrayList<String>();
    private Boolean btnStatus = false; //버튼이 눌린 상태를 표시 : 눌렸으면 true / 안눌린상태면 false
    private Thread thread; // 타이머 사용을 위한 스레드
    private SpeechService mSpeechService;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mSpeechService = SpeechService.from(binder);
            mSpeechService.addListener(mSpeechServiceListener);
            //mStatus.setVisibility(View.VISIBLE);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSpeechService = null;
        }

    };
    private VoiceRecorder mVoiceRecorder;
    private final VoiceRecorder.Callback mVoiceCallback = new VoiceRecorder.Callback() {

        @Override
        public void onVoiceStart() {
            showStatus(true);
            if (mSpeechService != null) {
                mSpeechService.startRecognizing(mVoiceRecorder.getSampleRate());
            }
        }

        @Override
        public void onVoice(byte[] data, int size) {
            if (mSpeechService != null) {
                mSpeechService.recognize(data, size);
            }
        }

        @Override
        public void onVoiceEnd() {
            showStatus(false);
            if (mSpeechService != null) {
                mSpeechService.finishRecognizing();
            }
        }

    };

    // View references
    private TextView mText;
    // 리스트뷰 어댑터
    private ChatArrayAdapter chatArrayAdapter;
    // 채팅메시지 구분 플래그 (false: 좌, true: 우)
    private boolean mSide = true;

    // 스크립트 메뉴 실행을 위한 변수들
    //Script script = null;
    ArrayList<String> scriptList = null;
    int scrCnt = 0;

    boolean scriptRun = false;

    private final SpeechService.Listener mSpeechServiceListener =
            new SpeechService.Listener() {
                @Override
                public void onSpeechRecognized(final String text, final boolean isFinal) {
                    if (isFinal) {
                        mVoiceRecorder.dismiss();
                    }
                    if (mText != null && !TextUtils.isEmpty(text)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isFinal) {
                                    String text = mText.getText().toString();

                                    mText.setText("");
                                    chatArrayAdapter.add(new ChatMessage(!mSide, text));

                                    // 1번 스크립트 연습 메뉴 실행
                                    if (IS_MENU1) {
                                        try{
                                            startScriptDBservice();
                                        }catch (SQLException ex) {
                                            ex.printStackTrace();
                                        }

                                        // 2017.10.11 연우 수정 - 스크립트 내용 호출시 배열에 들어있는 타이틀을 이용해 호출
                                        if (text.equals("script one")) {
                                            scriptRun = true;
                                            scriptTitle = scriptTitles.get(0);
                                            scriptList = getScript(scriptTitle);

                                        }

                                        if (text.equals("script two") || text.equals("script to")) {
                                            scriptRun = true;
                                            scriptTitle = scriptTitles.get(1);
                                            // 채팅창에 출력
                                            scriptList = getScript(scriptTitle);

                                        }

                                        if(text.equals("script three")) {
                                            scriptRun = true;
                                            scriptTitle = scriptTitles.get(2);
                                            // 채팅창에 출력
                                            scriptList = getScript(scriptTitle);
                                        }

                                        if(text.equals("script four")||text.equals("script for")) {
                                            scriptRun = true;
                                            scriptTitle = scriptTitles.get(3);
                                            // 채팅창에 출력
                                            scriptList = getScript(scriptTitle);
                                        }

                                        if(text.equals("script five")) {
                                            scriptRun = true;
                                            scriptTitle = scriptTitles.get(4);
                                            // 채팅창에 출력
                                            scriptList = getScript(scriptTitle);
                                        }

                                        if (text.equals("goodbye")) {
                                            chatArrayAdapter.add(new ChatMessage(mSide, "[연습 결과 분석]"));
                                            // 2017.10.11 연우 수정 - 배열 비었을 시 삭제로 NullPoint 오류 발생
                                            scriptRun = false; // 스크립트 연습 종료
                                            // 배열이 비어있지 않다면.
                                            if(!userAns.isEmpty()){
                                                userAns.remove(0); //script one을 지움.
                                            }

                                            //비교 시작하는 메소드(하단에 구현) 내용 : comparison 클래스 내부에 있는 메소드를 호출함.
                                            startCompare(); //정확도까지 넣어둠.

                                        } else {
                                            if (scriptRun) {
                                                userAns.add(text);
                                            }
                                        }
                                    }
                                    if (IS_MENU2) {

                                        scriptRun = true;

                                        if (text.equals("goodbye")) {
                                            chatArrayAdapter.add(new ChatMessage(mSide, "[단어 연습 결과 분석]"));

                                            //비교 시작하는 메소드(하단에 구현) 내용 : comparison 클래스 내부에 있는 메소드를 호출함.
                                            startCompareWords();

                                        }else {
                                            if(scriptRun) {
                                                userAns.add(text);

                                            }
                                        }

                                    }

                                    if (IS_MENU3) {

                                    }
                                    // 메뉴 선택
                                    selectMenu(text);
                                } else {
                                    mText.setText(text);
                                }
                            }
                        });
                    }
                }
            };

    // 슬라이딩 메뉴 객체
    private boolean isPageOpen = false; // 슬라이딩 페이지 표시 여부
    private Animation translateLeftAnim; // 왼쪽으로 이동 애니메이션 객체
    private Animation translateRightAnim; // 오른쪽으로 이동 애니메이션 객체
    private Animation fadeInAnim; // 점점 선명하게 보여주는 애니메이션 객체
    private Animation fadeOutAnim; // 점점 사라지도록 하는 애니메이션 객체
    private LinearLayout page; // 슬라이딩으로 보여줄 페이지
    private LinearLayout page2; // 슬라이딩으로 보여줄 페이지(왼쪽 반투명 레이어)

    // 슬라이딩 페이지 안의 객체들
    private ImageView profileIv;
    private TextView userNameTv;
    private TextView userIdTv;
    private Button vocaBookBtn;
    private Button myScoreBtn;
    private Button settingBtn;
    private Button logoutBtn;

    // 화면 객체
    private ListView listView;
    private Button btnRecord;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            String s = b.getString("message");

            if (s.equals("timerStart")) {
                btnRecord.setText(Integer.toString(mili));
            } else if (s.equals("timeout")) {
                btnRecord.setBackgroundResource(R.drawable.btnrecord1);
                stopVoiceRecorder();
                btnStatus = false;
            }
        }
    };

    /**
     * 액션바 메뉴 생성 메서드 오버라이딩
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * 액션바 메뉴 클릭 처리
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 현재 선택된 메뉴
        int curId = item.getItemId();
        // 메뉴에 따른 처리
        switch (curId) {
            case R.id.slidingMenu:
                // 슬라이딩 메뉴 보이기
                if(isPageOpen){
                    page.startAnimation(translateRightAnim); // 페이지가 열려있으면 오른쪽으로 애니메이션
                    page2.startAnimation(fadeOutAnim);
                } else {
                    page.setVisibility(View.VISIBLE); // 페이지가 닫혀 있으면 보이도록 한 후
                    page2.setVisibility(View.VISIBLE);
                    page.startAnimation(translateLeftAnim);  // 왼쪽으로 애니메이션
                    page2.startAnimation(fadeInAnim);
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // 1012 하늘 Database
    public void createDatabase(String name){
        try {
            db = openOrCreateDatabase(name, MODE_PRIVATE, null);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
       // startActivity(new Intent(this, SplashActivity.class));

        createDatabase("chatty");
        Log.d("chatty 디비","생성");

        // 2017.10.12 연우 추가
        tts = new TextToSpeech(this, this);

        // 2017,10.11 연우 추가
        if(!ScriptDB_SERVICE_FLAG){
            startScriptDBservice();
            ScriptDB_SERVICE_FLAG=true;
        }

        if(!VocaDB_SERVICE_FLAG){
            startVocaDBservice();
            VocaDB_SERVICE_FLAG=true;
        }

        //1012 하늘 추가
        if(!GradeDB_SERVICE_FLAG){
            startGradeDBservice();
            GradeDB_SERVICE_FLAG=true;
        }


        // 리스트뷰 생성 및 어댑터 설정
        listView = (ListView) findViewById(R.id.listView);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.activity_chat_singlemessage);

        // 새 메시지가 추가되면 맨 아래로 자동 스크롤
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });

        //2017.10.12 연우 추가
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatMessage msg = chatArrayAdapter.getItem(position);
                msgText = msg.message;
                speakOut();
                Log.d("itemClick", msgText);
            }
        });

        listView.setAdapter(chatArrayAdapter);

        // 임시 텍스트 출력 창
        mText = (TextView) findViewById(R.id.mtext);

        // 커스텀 액션바 사용
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);

        // 녹음 버튼 설정
        btnRecord = (Button) findViewById(R.id.btnRecord);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            // 버튼 클릭시
            @Override
            public void onClick(View v) {
                if (btnStatus == false) {
                    // 버튼 색상 변경
                    btnRecord.setBackgroundResource(R.drawable.btnrecord2);
                    // 녹음 시작
                    startVoiceRecorder();
                    btnStatus = true;
                    // 타이머 실행
                    thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mili = 10; // 시작시 10초부터
                            while (thread == Thread.currentThread()) {
                                try {
                                    Thread.sleep(1000); // 1초 대기
                                } catch (InterruptedException ie) {
                                    ie.getStackTrace();
                                }
                                Message m1 = new Message();
                                Bundle b1 = new Bundle();
                                b1.putString("message", "timerStart");
                                m1.setData(b1);
                                handler.sendMessage(m1);

                                mili--; // 1초씩 감소

                                // 남은 시간이 0이면 초기 상태로
                                if (mili == 0) {
                                    Message m2 = new Message();
                                    Bundle b2 = new Bundle();
                                    b2.putString("message", "timeout");
                                    m2.setData(b2);
                                    handler.sendMessage(m2);
                                    mili = 10;
                                    break;
                                }
                            }
                        }
                    });
                    thread.start();
                }
            }
        });

        // 슬라이딩 페이지
        page = (LinearLayout) findViewById(R.id.page);
        page2 = (LinearLayout) findViewById(R.id.page2);
        translateLeftAnim = AnimationUtils.loadAnimation(this, R.anim.translate_left);
        translateRightAnim = AnimationUtils.loadAnimation(this, R.anim.translate_right);
        fadeInAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeOutAnim = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        SlidingPageAnimationListener animListener = new SlidingPageAnimationListener(); // 슬라이딩 애니메이션을 감시할 리스너
        translateLeftAnim.setAnimationListener(animListener);
        translateRightAnim.setAnimationListener(animListener);
        //fadeInAnim.setAnimationListener(animListener);
        //fadeOutAnim.setAnimationListener(animListener);

        // 슬라이딩 페이지 이미지, 텍스트, 버튼
        profileIv = (ImageView) findViewById(R.id.profileIv);
        userNameTv = (TextView) findViewById(R.id.userNameTv);
        userIdTv = (TextView) findViewById(R.id.userIdTv);
        vocaBookBtn = (Button) findViewById(R.id.vocaBtn);
        myScoreBtn = (Button) findViewById(R.id.scoreBtn);
        settingBtn = (Button) findViewById(R.id.settingBtn);

        // 단어장 버튼
        vocaBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                IS_MENU2 = true;

                if(!VocaDB_SERVICE_FLAG){
                    startVocaDBservice();
                    VocaDB_SERVICE_FLAG=true;
                }

                Intent intent = new Intent(getApplication(), VocaActivity.class);
                //startActivity(intent);
                startActivityForResult(intent,REQUEST_CODE_PRACTICE_WORD);
                // fade in/out 애니메이션 효과
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });



        // 나의 성적 버튼
        myScoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!GradeDB_SERVICE_FLAG){
                    startGradeDBservice();
                    GradeDB_SERVICE_FLAG=true;
                }

                Log.d("tag-user",userId);
                if(DEF_FLAG){
                    openAllGradeRecord(userId+"_grade");
                }
                Intent intent=new Intent(getApplicationContext(), GradeActivity.class);
                startActivity(intent);
                // fade in/out 애니메이션 효과
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            }
        });

        // 설정 버튼
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!VocaDB_SERVICE_FLAG){
                    startVocaDBservice();
                    VocaDB_SERVICE_FLAG=true;
                }

                Intent intent = new Intent(getApplication(), SettingsActivity.class);
                intent.putExtra("userId",userId);
                startActivity(intent);
                // fade in/out 애니메이션 효과
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        // 로그아웃 버튼
        logoutBtn = (Button) findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 로그인 정보 초기화
                isLogined = false;
                userId = userName = null;

                // 로그인 확인
                checkLogin();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // 로그인 확인하기
        checkLogin();

        // Prepare Cloud Speech API
        bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);

        // Start listening to voices
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            // startVoiceRecorder();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            showPermissionMessageDialog();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    /**
     * 다른 액티비티로 받아온 결과를 처리할 메서드 오버라이딩
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.activity_chat_singlemessage);
        listView.setAdapter(chatArrayAdapter);

        // 로그인 요청 처리
        if (requestCode == REQUEST_CODE_LOGIN) {
            // 로그인 성공시
            if (resultCode == RESULT_OK) {
                isLogined = true; // 로그인 되었음을 확인
                userId = data.getStringExtra("userId"); // 사용자 아이디 저장
                userName = data.getStringExtra("userName"); // 사용자 이름 저장

                // 슬라이딩 메뉴 프로필에 사용자 정보 표시
                userNameTv.setText(userName + " 님");
                userIdTv.setText("ID " + userId);

                // 앱 시작 인사말 보이기
                Toast.makeText(this, userName + "님 반가워요!", Toast.LENGTH_SHORT).show();
                chatArrayAdapter.add(new ChatMessage(mSide, "안녕하세요? " + userName + "님!\n" +
                        "오늘은 어떤 연습을 하시겠어요?"));
                chatArrayAdapter.add(new ChatMessage(mSide, "메뉴 실행을 위해 녹음 버튼을 누른 후 'number 번호'를 말해주세요."));
                chatArrayAdapter.add(new ChatMessage(mSide, "버튼은 한 번 클릭시 10초 동안 녹음됩니다."));
                chatArrayAdapter.add(new ChatMessage(mSide, "" +"0. 도움말\n" +
                        "1. 스크립트 읽기\n" +
                        "2. 단어장\n" +
                        "3. 나의 성적 보기"));

            }
        }else if(requestCode==REQUEST_CODE_PRACTICE_WORD){
            if(data!=null) {
                chatArrayAdapter.add(new ChatMessage(mSide, "단어 연습을 시작합니다!"));

                String[] selectedWords = data.getStringArrayExtra("words");
                scriptTitle = data.getStringExtra("scriptTitle");

                for (int i = 0; i < selectedWords.length; i++) {
                    userWords.add(selectedWords[i]);
                }

                for (int j = 0; j < selectedWords.length; j++) {
                    chatArrayAdapter.add(new ChatMessage(mSide, selectedWords[j]));
                }
            }else{
                Toast.makeText(this, userName + "님 반가워요!", Toast.LENGTH_SHORT).show();
                chatArrayAdapter.add(new ChatMessage(mSide, "안녕하세요? " + userName + "님!\n" +
                        "오늘은 어떤 연습을 하시겠어요?"));
                chatArrayAdapter.add(new ChatMessage(mSide, "메뉴 실행을 위해 녹음 버튼을 누른 후 'number 번호'를 말해주세요."));
                chatArrayAdapter.add(new ChatMessage(mSide, "버튼은 한 번 클릭시 10초 동안 녹음됩니다."));
                chatArrayAdapter.add(new ChatMessage(mSide, "" +"0. 도움말\n" +
                        "1. 스크립트 읽기\n" +
                        "2. 단어장\n" +
                        "3. 나의 성적 보기"));
            }
        }

    }

    /**
     * 로그인 확인 및 처리 메서드
     */
    private void checkLogin() {

        // 로그인 확인
        isLogined = userId != null;

        // 로그인 되지 않았으면 로그인 액티비티로 이동
        if (!isLogined) {

            //로그인 액티비티 띄우기
            getLoginActivity();
        }
    }

    /**
     * 로그인 액티비티를 불러올 메서드
     */
    private void getLoginActivity() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivityForResult(intent, REQUEST_CODE_LOGIN);
    }

    @Override
    protected void onStop() {
        // Stop listening to voice
        stopVoiceRecorder();

        // Stop Cloud Speech API
        mSpeechService.removeListener(mSpeechServiceListener);
        unbindService(mServiceConnection);
        mSpeechService = null;

        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (permissions.length == 1 && grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecorder();
            } else {
                showPermissionMessageDialog();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void startVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
        }
        mVoiceRecorder = new VoiceRecorder(mVoiceCallback);
        mVoiceRecorder.start();
    }

    private void stopVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
            mVoiceRecorder = null;
        }
    }

    private void showPermissionMessageDialog() {
        MessageDialogFragment
                .newInstance(getString(R.string.permission_message))
                .show(getSupportFragmentManager(), FRAGMENT_MESSAGE_DIALOG);
    }

    private void showStatus(final boolean hearingVoice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void onMessageDialogDismissed() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

    // 메뉴 실행
    private void selectMenu(String text) {
        if (text.equals("number one")) {
            IS_MENU1 = true;

            if(!ScriptDB_SERVICE_FLAG){
                startScriptDBservice();
                ScriptDB_SERVICE_FLAG=true;
            }

            if(!VocaDB_SERVICE_FLAG){
                startVocaDBservice();
                VocaDB_SERVICE_FLAG=true;
            }

            chatArrayAdapter.add(new ChatMessage(mSide, "스크립트 읽기 연습을 시작합니다!"));
            // 2017.10.11 연우 추가
            // 스크립트 리스트 출력
            // 스크립트 가져오기- Script 클래스
            scriptTitles = new Script().getScriptTitle();
            String listMsg = ""; // 메시지 문장
            for(int i = 0; i < scriptTitles.size(); i++){
                if(i >= scriptTitles.size()-1){ // 마지막 목록의 줄바꿈을 없애기 위함
                    listMsg += (i+1) + ". " + scriptTitles.get(i);
                } else {
                    listMsg += (i + 1) + ". " + scriptTitles.get(i) + "\n";
                }

            }
            // 번호. 스크립트 제목 형태
            chatArrayAdapter.add(new ChatMessage(mSide, listMsg));

            chatArrayAdapter.add(new ChatMessage(mSide, "스크립트 선택을 위해 'script 번호'를 말해주세요."));
            chatArrayAdapter.add(new ChatMessage(mSide, "연습을 종료하시려면 'goodbye'를 말해주세요!"));

        } else if (text.equals("number to") || text.equals("number two")) {
            chatArrayAdapter.add(new ChatMessage(mSide, "단어장을 펼칩니다!"));
            IS_MENU2 = true;

            //1012 저녁 하늘 수정
            Handler handler=new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if(!VocaDB_SERVICE_FLAG){
                        startVocaDBservice();
                        VocaDB_SERVICE_FLAG=true;
                    }

                    Intent intent = new Intent(getApplication(), VocaActivity.class);
                    startActivityForResult(intent,REQUEST_CODE_PRACTICE_WORD);

                }
            },1700);

        } else if (text.equals("number three")) {

            chatArrayAdapter.add(new ChatMessage(mSide, "나의 성적을 확인합니다!"));
            IS_MENU3 = true;

            //1012 저녁 하늘 수정
            Handler handler=new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {


                    if(!GradeDB_SERVICE_FLAG){
                        startGradeDBservice();
                        GradeDB_SERVICE_FLAG=true;
                    }

                    Log.d("tag-user",userId);
                    if(DEF_FLAG){
                        openAllGradeRecord(userId+"_grade");
                    }
                    Intent intent=new Intent(getApplicationContext(), GradeActivity.class);
                    startActivity(intent);
                    // fade in/out 애니메이션 효과
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            },1700);


        } else if(text.equals("number zero")) {
            chatArrayAdapter.add(new ChatMessage(mSide, "[Chatty 사용법]"));
            chatArrayAdapter.add(new ChatMessage(mSide, "채티를 사용해주셔서 감사합니다.\n" +
                    "음성 인식을 위해 하단의 붉은 버튼을 클릭 후 원하는 명령을 말해주세요.\n" +
                    "버튼은 한 번 클릭시 10초 동안 음성을 인식하고 버튼 위의 회색 막대에 현재 입력중인 내용이 표시됩니다.\n" +
                    "오른쪽 상단에 위치한 메뉴 버튼을 클릭해 사용자 프로필과 단어장, 나의 성적, 설정 메뉴에 접근할 수 있습니다."));
        }
    }

    // 정확도 구하는 메소드
    public static double accuracyCal(int numberOfAllWords, int numberOfWrongWords){
        //Log.d("accuracy",Integer.toString(numberOfAllWords)+","+Integer.toString(numberOfWrongWords));
        totalWordsCount = numberOfAllWords;
        wrongWordsCount = numberOfWrongWords;

        double accuracy;
        if(numberOfWrongWords==0){
            accuracy=100;
        }else{
            int numberOfCorrectWords = numberOfAllWords - numberOfWrongWords;
            accuracy=((double)numberOfCorrectWords/numberOfAllWords)*100;
        }

        Log.d("accuracy",String.valueOf(accuracy));


        return accuracy;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language is not supported");
            } else {
                speakOut();
            }

        } else {
            Log.e("TTS", "Initilization Failed");
        }
    }

    //1012 연우 추가
    private void speakOut() {

        String text = msgText;

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private class SlidingPageAnimationListener implements Animation.AnimationListener {

        public void onAnimationEnd(Animation animation) {
            if (isPageOpen) {
                page.setVisibility(View.INVISIBLE);
                page2.setVisibility(View.INVISIBLE);
                isPageOpen = false;
            } else {
                isPageOpen = true;
            }
        }

        public void onAnimationRepeat(Animation animation) {

        }

        public void onAnimationStart(Animation animation) {

        }

    }

    // 단어장 구현 전에 임시로 채팅창에 출력하는 메소드 - 나중에 수정할 것.
    public void printVoca_toChat(String scriptTitle){
        ArrayList<String> words=searchVoca(userId+"_voca",scriptTitle);

        chatArrayAdapter.add(new ChatMessage(mSide,"단어장 구현전까지 임시로 출력"));
        if(words.size()==0){
            chatArrayAdapter.add(new ChatMessage(mSide,"저장된 단어가 없습니다."));
        }
        for(int j = 0; j <words.size(); j++) {
            chatArrayAdapter.add(new ChatMessage(mSide,words.get(j)));
        }
    }

    public void startVocaDBservice(){
        Log.d("tag-startVocaDB","start");
        Intent intent =new Intent(this, VocaBook.class);
        startService(intent);
    }

    public void startGradeDBservice(){
        Log.d("tag-startGradeDB","start");
        Intent intent =new Intent(this, Grade.class);
        startService(intent);
    }

    public void startScriptDBservice(){
        Log.d("tag-startGradeDB","start");
        Intent intent =new Intent(this, Script.class);
        startService(intent);
    }

    public void startCompare(){

        // 단어 데이터베이스가 실행되지 않았다면 실행.
        if(!VocaDB_SERVICE_FLAG){
            startVocaDBservice();
            VocaDB_SERVICE_FLAG=true;
        }

        // 성적 디비 실행 안 됐으면 실행
        if(!VocaDB_SERVICE_FLAG){
            startVocaDBservice();
            VocaDB_SERVICE_FLAG=true;
        }

        // 연습 결과 문자열
        String result = "";

        // 짧은 배열을 기준으로 삼음
        // 문장 비교를 위한 반복문 사용시 OutOfArrayBoundsException을 막기 위해
        int compareCnt = userAns.size(); // 기준을 사용자 응답 문장 수로 잡은 뒤
        if(userAns.size() > scriptList.size()) // 응답 문장 수가 기존 스크립트 문장 수 보다 크면
            compareCnt = scriptList.size(); // 더 짧은 스크립트 문장 수로 기준을 변경

        // 연습 결과 분석 시작
        for (int i = 0; i < compareCnt; i++) {
            // chatArrayAdapter.add(new ChatMessage(mSide, "사용자 입력: " + userAns.get(i))); // 사용자가 입력한 문장 출력
            // ArrayList<String> wrongWords = currentUser_wrongWords; // 틀린 단어 배열에 저장 - 기존 코드 주석 처리

            // 문장 비교하기
            // ScriptList(i)의 문장 userAns(i)의 문장을 비교하여 틀린 단어를 배열로 받음
            ArrayList<String> wrongWords5 = compareContext(scriptList.get(i), userAns.get(i));
            for(int j = 0; j <wrongWords5.size(); j++) {
                Log.d("중복확인",wrongWords5.get(j));
            }


            // 유저 단어장에 저장
            saveWords_toVoca(wrongWords5);

            // 틀린 단어 출력 - 하나의 메시지 창에 보여주기 위해 하나의 String 객체에 합치기
            String wrongWordsMsg = "틀린 단어:";
            for(int j = 0; j <wrongWords5.size(); j++) {
                wrongWordsMsg += " " + wrongWords5.get(j);
            }
            // 다 합친 틀린 단어 문자열을 채팅창에 띄움
            //chatArrayAdapter.add(new ChatMessage(mSide, wrongWordsMsg));

            // 문장별 정확도 계산 (Comparison 객체의 accuracyCal 메소드 static 호출)
            // 계산을 위해 필요한 값: 모든 단어 숫자, 틀린 단어 숫자 -> 정확도
            // 한 스크립트 문장(정확한 문장)의 모든 단어 숫자 구하기 - 토큰으로 자르기
            StringTokenizer correctStr = new StringTokenizer(scriptList.get(i), " ");
            ArrayList<String> correctWords=new ArrayList<String>(); // 정확한 단어 리스트
            while(correctStr.hasMoreTokens()){
                correctWords.add(correctStr.nextToken().toLowerCase()); //무조건 소문자로 비교하기 위해(대소문자 구분 x)
            }
            // 정확도 계산하기 (정확한 단어 개수, 틀린 단어 개수)
            accuracy = accuracyCal(correctWords.size(), wrongWords5.size());

            // 연습결과 문자열 생성 - 문장 정확도와 틀린 단어 문장 합치기
            if(i == 0) { // 첫 번째 문장 줄바꿈을 안 하기 위해
                result += (i+1) + ". 문장 정확도: " + String.format("%.1f", accuracy) + "%\n" + wrongWordsMsg;
            } else { // 그 다음부터 줄바꿈
                result += "\n" + (i+1) + ". 문장 정확도: " + String.format("%.1f", accuracy) + "%\n" + wrongWordsMsg;
            }

            // 문장 정확도 채팅창에 띄움
            //chatArrayAdapter.add(new ChatMessage(mSide, (i+1) + ". 문장 정확도: " + String.format("%.1f", accuracy) + "%") +"\n" + wrongWordsMsg);
            accCount++; // 평균 정확도 계산을 위해 문장 갯수 증가
            totalAccuracy += accuracy; // 평균 정확도 계산을 위해 전체 정확도 합치기
        }
        // 문장별 정확도 및 틀린 단어 출력
        chatArrayAdapter.add(new ChatMessage(mSide, result));
        // 평균 정확도 구하기 및 출력
        accAvg = totalAccuracy / accCount;
        chatArrayAdapter.add(new ChatMessage(mSide, "평균 정확도: " + String.format("%.1f", accAvg) + "%"));

        // 평균 정확도 성적 데이터베이스에 등록하기
        // 성적 데이터베이스에 등록되어야하는 정보 - 테이블이름, 날짜, 정확도
        // 날짜 구하기
        Calendar today = Calendar.getInstance(); // 캘린더 객체 생성
        Date date = today.getTime(); // 현재 날짜
        // 문자열로 포메팅
        SimpleDateFormat formater = new SimpleDateFormat("yy-MM-dd"); // 포매터 생성
        String practiceDate = formater.format(date);
        // 로그로 확인
        Log.d("연습날짜", practiceDate);
        int score = (int) accAvg; // 성적 정수화
        Log.d("성적", score+"");
        /** 2017.10.08 - 연우 : 추가한 데이터 성적표에서 확인해봐야함**/
        //createTable(userId+"_grade");

        addGrade(userId+"_grade", practiceDate, score);

        // 사용한 변수들 초기화
        userAns = new ArrayList<String>();
        IS_MENU1 = false;
        scriptRun = false;
        accAvg = totalAccuracy = accCount = 0;
    }

    // 유저가 틀린 단어들을 유저 단어장에 저장하는 메소드
    public void saveWords_toVoca(ArrayList<String> words){

        removeDupAndAddWords(userId+"_voca", scriptTitle, words);

    }

    // 스크립트 테이블로부터 스크립트를 가져와서 채팅창에 출력하는 메소드
    public ArrayList<String> getScript(String title){
        ArrayList<String> script=searchScriptArray("scripts", title);
        scrCnt=script.size();

        for (int i = 0; i < scrCnt; i++) {
            chatArrayAdapter.add(new ChatMessage(mSide, script.get(i)+"."));
        }

        return script;
    }

    public void startCompareWords(){

        IS_MENU2 = true;

        if(!VocaDB_SERVICE_FLAG){
            startVocaDBservice();
            VocaDB_SERVICE_FLAG=true;
        }


        ArrayList<String> stillWrongWords=new ArrayList<String>();
        stillWrongWords=compareWords(userWords,userAns); //단어 비교

        // 틀린 단어 로그로 출력
        for(int j = 0; j <stillWrongWords.size(); j++) {
            Log.d("tag-여전히 틀린 단어: ", stillWrongWords.get(j));
        }

        // 틀린 단어 그대로 저장 - 스크립트 별로 연습한 단어는 처음부터 다 꺼내서 연습하기 때문에.
        resetScriptVoca(userId+"_voca",scriptTitle,stillWrongWords);

        Intent intent = new Intent(getApplication(), VocaActivity.class);
        startActivityForResult(intent,REQUEST_CODE_PRACTICE_WORD);

    }

}
