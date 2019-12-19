package com.example.timetableandmemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    TextView timetableTitle;
    Button directAdd;
    Button selectAdd;
    TableRow timetableWeekdaysRow;
    TableRow timetableContentRow;
    LinearLayout timetableColumn_time; //시간표의 첫열(시간 구분선)
    LinearLayout[] timetableColumn_weekdays = new LinearLayout[5]; //0: 월요일, 1: 화요일, 2: 수요일, 3: 목요일, 4: 금요일
    TimeTableManager ttManager = new TimeTableManager(this);
    TimetableVO ttVO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Realm 초기화
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
        Realm realm = Realm.getInstance(config);

        //activity_main.xml에서 id로 객체 찾기
        timetableTitle = (TextView)findViewById(R.id.timetable_title);
        selectAdd = (Button)findViewById(R.id.selectAdd);
        directAdd = (Button)findViewById(R.id.directAdd);
        timetableWeekdaysRow = (TableRow)findViewById(R.id.timetable_weekdays_row);
        timetableContentRow = (TableRow)findViewById(R.id.timetable_content_row);

        //이후 사용할 timetableColumn_time과 timetableColumn_weekdays 객체 찾기
        timetableColumn_time = (LinearLayout)timetableContentRow.getChildAt(0);
        for (int i = 0; i < 5; i++) timetableColumn_weekdays[i] = (LinearLayout)timetableContentRow.getChildAt(i + 1);

        //ADD 버튼 클릭시 동작
        directAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(), DirectAdd.class);
                startActivity(intent);
            }
        });

        //Select버튼 클릭시 동작
        selectAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SubjectListActivity.class);
                startActivity(intent);
            }
        });

        //만들어진 시간표 객체가 존재하지 않으면 id=0로 객체 하나 생성
        if(!isTimetableVOExistInDB(realm)) {
            createTimetableVO(realm, 0, "길게 눌러 제목을 변경");
            /// 테스트< ///
            final TimetableVO ttVO1 = realm.where(TimetableVO.class).equalTo("id", 0).findFirst();

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    SubjectBlock sb1 = new SubjectBlock("강의실1", "월요일", 10, 15, 13, 30);
                    SubjectBlock sb2 = new SubjectBlock("강의실1", "화요일", 12, 0, 13, 0);
                    SubjectSet ss = new SubjectSet("과목과목", "교수교수님");
                    ss.add(sb1);
                    ss.add(sb2);

                    SubjectBlock sb3 = new SubjectBlock("강의실2", "수요일", 9, 30, 12, 0);
                    SubjectBlock sb4 = new SubjectBlock("강의실2", "금요일", 12, 0, 15, 0);
                    SubjectBlock sb5 = new SubjectBlock("강의실2", "화요일", 9, 15, 11, 0);
                    SubjectSet ss2 = new SubjectSet("이름이제법긴과아아목", "교오오수님");
                    ss2.add(sb3);
                    ss2.add(sb4);
                    ss2.add(sb5);

                    ttVO1.addSubjectSet(ss);
                    ttVO1.addSubjectSet(ss2);
                }
            });
            ttManager.setCurrentTimetableVO(ttVO1);
            ttManager.setTitle();

            /// 테스트> ///
        }

        //DB에 저장되어있는 TimetableVO객체 가져오기
        this.ttVO = realm.where(TimetableVO.class).equalTo("id", 0).findFirst();
    }


    @Override
    protected void onResume() {
        super.onResume();

        //TimeTableManager 설정 및 동작
        ttManager.setCurrentTimetableVO(this.ttVO);
        ttManager.setTitle();
        ttManager.calculateStartingAndEndingTimes();
        ttManager.calculateNumberOfHours();

        ttManager.applyTimetableWeekdaysRowText(timetableWeekdaysRow);
        ttManager.applyTitle(timetableTitle);
        ttManager.fillTimetableColumn_time(timetableColumn_time);
        for(int i = 0; i < 5; i ++) ttManager.fillTimetableContentRow(timetableColumn_weekdays[i], i); //각 요일에 SubjectBlock 배치
    }

    //RealmDB내에 TimetableVO객체가 존재하는지 확인
    private Boolean isTimetableVOExistInDB(Realm realm) {
        RealmQuery<TimetableVO> query = realm.where(TimetableVO.class);
        RealmResults<TimetableVO> results = query.findAll();
        if(results.size() == 0) return false;
        else return true;
    }

    //새로운 TimetableVO객체 생성
    public void createTimetableVO(Realm realm, int id, final String title) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                TimetableVO vo = realm.createObject(TimetableVO.class);
                vo.setTitle(title);
            }
        });
    }
}
