package com.google.cloud.android.speech;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.util.ArrayList;

import static com.google.cloud.android.speech.ChatActivity.userId;
import static com.google.cloud.android.speech.Grade.getDateList;
import static com.google.cloud.android.speech.Grade.getGradeList;

public class GradeActivity extends AppCompatActivity {
    // 그래프 객체
    GraphView graph;
    ArrayList gradesList;
    ArrayList<String> datesList;
    Button exitBtn;
    TextView doEX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade);


        doEX=(TextView)findViewById(R.id.doEX);
        exitBtn=(Button)findViewById(R.id.exitBtn);
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 커스텀 액션바 사용
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);

        graph = (GraphView) findViewById(R.id.graph);

        // 연우 추가
        //String userId = "user2"; // 테스트를 위해 임의 아이디 지정
        //createTable(userId + "_grade"); // 점수 테이블 생성
        gradesList = getGradeList(userId+"_grade");  // 점수 배열 가져오기
        datesList = getDateList(userId + "_grade"); // 날짜 배열 가져오기

        // 배열 길이
        int gradesListSize = gradesList.size();
        int datesListSize = datesList.size();

        // 그래프 축에 넣을 값
        String[] dates = new String[datesListSize];
        for(int i = 0; i < dates.length; i++)
            dates[i] = datesList.get(i);


        String[] scores = new String[]{"0", "20", "40", "60", "80", "100"};

        // use static labels for horizontal and vertical labels
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);

        //1012 저녁 하늘 추가
        if(dates.length==1){
            String[] dates2 = new String[2];
            dates2[0]="0";
            dates2[1]="0";
            doEX.setText("스크립트 연습을 해주세요!");
            staticLabelsFormatter.setHorizontalLabels(dates2);
        }else{
            staticLabelsFormatter.setHorizontalLabels(dates);
        }
        staticLabelsFormatter.setVerticalLabels(scores);
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

        //DataPoint배열 생성
        DataPoint[] dataList = new DataPoint[gradesListSize];
        for(int i = 0; i < gradesListSize; i++){
            double score = (double)gradesList.get(i) / 10.0;
            dataList[i] = new DataPoint(i, score);
        }


        // 그래프에 넣을 Series 생성
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataList);

        // styling series
        series.setColor(Color.CYAN);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(15);
        series.setThickness(8);

        // 데이터 클릭시 자세한 성적을 띄워줌
        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(getApplicationContext(), "평균 정확도: "+dataPoint.getY()*10+"%", Toast.LENGTH_SHORT).show();
            }
        });



        graph.addSeries(series);

    }
}
