package com.google.cloud.android.speech;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;


public class Comparison {

    public static void setAllScripts(){

    }

    //비교 시작 - 반환값: 종합적인 중복을 뺀 틀린 단어들
    public static ArrayList<String> comparing(ArrayList<String> scriptList, ArrayList<String> userAns){
       ArrayList<String> Composite_words=new ArrayList<String>();

        int count=scriptList.size();

        if(count>0){
            ArrayList<String> words=new ArrayList<String>();
            words=compareContext(scriptList.get(count-1), userAns.get(count-1));

            for(int i=0; i<words.size(); i++){
                Composite_words.add(words.get(i));
            }

            count--;
        }

        Composite_words=removeDuplicateArray(Composite_words);

        return Composite_words;
    }

    // 배열안에 중복된 값을 제거하는 메소드
    public static ArrayList<String> removeDuplicateArray(ArrayList<String> duplicateWords){
        ArrayList<String> arrayList=duplicateWords;
        HashSet<String> hs=new HashSet<String>(arrayList);

        ArrayList<String> removeDuplicateWords=new ArrayList<String>(hs);

        return removeDuplicateWords;
    }

    //단어 비교
    public static ArrayList<String> compareWords(ArrayList<String> wrongWords, ArrayList<String> userWords){
        Log.d("tag-compare","start");
        ArrayList<String> stillWrongWords=new ArrayList<String>();
        int count;

        for(int i=0; i<wrongWords.size(); i++){

            count=0;

            for(int h=0; h<userWords.size(); h++){
                if(!wrongWords.get(i).equals(userWords.get(h))){
                    Log.d("tag-단어 비교중",wrongWords.get(i)+":틀림");
                    count++;
                }
            }

            if(count==userWords.size()){
                stillWrongWords.add(wrongWords.get(i));
            }
        }

        //여전히 틀린 단어 로그로 출력
        for(int u=0; u<stillWrongWords.size(); u++)
            Log.d("tag-stillWrongWords",stillWrongWords.get(u));


        return stillWrongWords;
    }


    //문장을 하나씩 가져와서 낱말 비교 - 반환값: 해당 문장의 틀린 단어들
    public static ArrayList<String> compareContext(String correctString, String userString){

        StringTokenizer correctStr = new StringTokenizer(correctString, " ");
       // while(correctStr.hasMoreTokens()){  Log.d("토큰",correctStr.nextToken().toLowerCase()); }
        StringTokenizer userStr = new StringTokenizer(userString," ");

        ArrayList<String> wrongWords=new ArrayList<String>();
        ArrayList<Integer> correctWordsNumber=new ArrayList<Integer>();

        ArrayList<String> correctWords=new ArrayList<String>();
        while(correctStr.hasMoreTokens()){
            correctWords.add(correctStr.nextToken().toLowerCase()); //무조건 소문자로 비교하기 위해(대소문자 구분 x)

        }

        ArrayList<String> userWords=new ArrayList<String>();
        while(userStr.hasMoreTokens()){
            userWords.add(userStr.nextToken().toLowerCase()); //무조건 소문자로 비교하기 위해(대소문자 구분 x)

        }

            for(int i=0; i<correctWords.size(); i++){

                int count=0;
                for(int h=0; h<userWords.size(); h++){

                    if( correctWords.get(i).equals(userWords.get(h))){
                        userWords.remove(h);
                        count++;

                    }
                }
                if(count != 1){ //틀린단어 저장하기
                    wrongWords.add(correctWords.get(i));
                }


            }

            if(userWords.size()==0) Log.d("compareResult","맞아용");
            else Log.d("compareResult","틀려용");

        //틀린단어 로그에 출력하기
        for(int i=0; i<wrongWords.size(); i++){
            Log.d("수정전 wrongWords",wrongWords.get(i));
        }

        ArrayList<String> wrongWordsRemoveDup=new ArrayList<String>(removeDuplicateArray(wrongWords));

        //틀린단어 로그에 출력하기
        for(int i=0; i<wrongWordsRemoveDup.size(); i++){
            Log.d("수정후 wrongWordsRemoveDup",wrongWordsRemoveDup.get(i));
        }

        accuracyCal(correctWords.size(), wrongWordsRemoveDup.size());

        return wrongWordsRemoveDup;
    }

    //문장 정확도 계산
    public static void accuracyCal(int numberOfAllWords, int numberOfWrongWords){
        //Log.d("accuracy",Integer.toString(numberOfAllWords)+","+Integer.toString(numberOfWrongWords));
        double accuracy;
        if(numberOfWrongWords==0){
            accuracy=100;
        }else{
            accuracy=((double)numberOfWrongWords/numberOfAllWords)*100;
        }

        Log.d("accuracy",String.valueOf(accuracy));
    }

}
