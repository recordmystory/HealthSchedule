package com.inhatc.healthschedule;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    //Push Test1

    private TextView textApiData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Read 버튼 클릭
        Button btnReader = (Button) findViewById(R.id.btnRead);
        btnReader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NfcReader.class);
                startActivity(intent);
            }
        });

        //Write 버튼 클릭
        Button btnWrite = (Button) findViewById(R.id.btnWrite);
        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NfcWrite.class);
                startActivity(intent);
            }
        });


        //api 데이터 화면표시
        textApiData = (TextView) findViewById(R.id.textApiData);

        //Write 버튼 클릭
        Button getApiData = (Button) findViewById(R.id.btnGetApiData);
        getApiData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String callBackUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
                String serviceKey = "HsRNXBqjnN5wPvLYWibtHMPTb0WvozkrwMOG6f3Ci%2Bwg4FlX5DhJ7EwAA%2F6qZr18U1HydLvkm8uKwhHZy8o7IQ%3D%3D"; //일반 인증키(Encoding)
                String numOfRows = "1000";      //한 페이지 결과 수
                String pageNo = "1";            //페이지 번호
                String dataType = "XML";         //요청 자료형식(XML/JSON)
                String base_date = "20230531" ; //발표일자
                String base_time = "0200";      //발표 시간
                String nx = "55";               //예보지점 X
                String ny = "127";              //예보지점 Y

                String apiUrl = callBackUrl + "?serviceKey=" + serviceKey + "&numOfRows=" + numOfRows + "&pageNo=" + pageNo + "&dataType=" + dataType + "&base_date=" + base_date + "&base_time=" + base_time + "&nx=" + nx + "&ny=" + ny;

                DownloadWebpageTask1 objTask = new DownloadWebpageTask1();
                objTask.execute(apiUrl.toString());

/*

                String strLine = null;
                String strPage="";
                HttpURLConnection urlConn = null;


                try{

                    URL url = new URL(apiUrl);
                    urlConn = (HttpURLConnection) url.openConnection();
                    urlConn.setRequestMethod("GET");
                    urlConn.setRequestProperty("Content-type", "application/json");

                    BufferedReader bufReader;
                    bufReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

                    while ((strLine = bufReader.readLine()) != null){
                        strPage+=strLine;
                    }
                    //return strPage;
                } catch (Exception e) {
                    System.out.println("API 호출 오류");
                    System.out.println(e);
                } finally {
                    urlConn.disconnect();
                }

                System.out.println(strPage);
                textApiData.append(strPage);
*/

            }
        });
    }





    int total = 0;  //API 클릭 횟수

    //API 호출
    private class DownloadWebpageTask1 extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls){
            try{
                String strData = downloadUrl((String)urls[0]);
                return strData;
            }catch(IOException e){
                return "Fail download !";
            }
        }



        protected void onPostExecute(String result){
            String strHeaderCd = "";
            String strBusRouteId = "";
            String strBusRouteNo = "";

            boolean bSet_HeaderCd = false;
            boolean bSet_BusRouteId = false;
            boolean bSet_BusRouteNo = false;

            total++;
            textApiData.setText("");
            textApiData.append("======[ API Data ]==== " + total + "\n");

            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(new StringReader(result));
                int eventType = xpp.getEventType();

                int temp = 0;
                System.out.println(textApiData.length());
                String tagName = "";    //태그 제목 저장
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    switch (eventType) {

                        case XmlPullParser.START_TAG:
                            tagName = xpp.getName();    //태그명 저장 후 해당태그일 경우 텍스트 추출
                            break;

                        case XmlPullParser.END_TAG:
                            break;
                            
                        case XmlPullParser.TEXT:
                            switch (tagName) {
                                case "baseDate": {  //발표일자
                                    textApiData.append("baseDate : " + xpp.getText() + "\n");
                                    temp++;
                                    break;
                                }

                                //Base_time : 0200, 0500, 0800, 1100, 1400, 1700, 2000, 2300 (1일 8회)
                                //API 제공 시간(~이후) : 02:10, 05:10, 08:10, 11:10, 14:10, 17:10, 20:10, 23:10
                                //일 최저,최고기온은 일자당 1번만 존재, 해당하는 시간대에 존재
                                case "baseTime": {  //발표시각
                                    textApiData.append("baseTime : " + xpp.getText() + "\n");
                                    temp++;
                                    break;
                                }
                                case "category": {  //자료구분자
                                    textApiData.append("category : " + xpp.getText() + "\n");
                                    temp++;
                                    break;
                                }
                                case "fcstDate": {  //예보일자
                                    textApiData.append("fcstDate : " + xpp.getText() + "\n");
                                    temp++;
                                    break;
                                }
                                case "fcstTime": {  //예보시각
                                    textApiData.append("fcstTime : " + xpp.getText() + "\n");
                                    temp++;
                                    break;
                                }
                                case "fcstValue": { //예보값
                                    textApiData.append("fcstValue : " + xpp.getText() + "\n");
                                    temp++;
                                    break;
                                }

                            }
                    }
                    eventType = xpp.next();
                    //category - 자료구분자 참조
                    /*
                    POP	강수확률	%
                    PTY	강수형태	코드값
                    PCP	1시간 강수량	범주 (1 mm)
                    REH	습도	%
                    SNO	1시간 신적설	범주(1 cm)
                    SKY	하늘상태	코드값
                    TMP	1시간 기온	℃
                    TMN	일 최저기온	℃
                    TMX	일 최고기온	℃
                    UUU	풍속(동서성분)	m/s
                    VVV	풍속(남북성분)	m/s
                    WAV	파고	M
                    VEC	풍향	deg
                    WSD	풍속	m/s
                    */
                }

                System.out.println("------------------");
                System.out.println(temp);
                System.out.println(textApiData.length());


            } catch (Exception e) {
                textApiData.setText(e.getMessage());
            }
        }

        private String downloadUrl(String myUrl) throws IOException{
            String strLine = null;
            String strPage="";

            HttpURLConnection urlConn = null;
            try{
                URL url = new URL(myUrl);
                urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setRequestMethod("GET");
                urlConn.setRequestProperty("Content-type", "application/json");

                BufferedReader bufReader;
                bufReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

                while ((strLine = bufReader.readLine()) != null){
                    strPage+=strLine;
                }
                return strPage;
            }finally{
                urlConn.disconnect();
            }
        }

    }
}