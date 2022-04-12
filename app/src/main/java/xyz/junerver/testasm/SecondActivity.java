package xyz.junerver.testasm;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import xyz.junerver.analytics_utils.annotation.TrackDimension;
import xyz.junerver.analytics_utils.annotation.TrackEvent;
import xyz.junerver.analytics_utils.annotation.TrackScreenView;

@TrackScreenView(path = "/second", onTrack = "testFunc3")
public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        findViewById(R.id.btn_s_1).setOnClickListener(v -> {
            testFunc1();
        });
        findViewById(R.id.btn_s_2).setOnClickListener(v -> {
            testFunc2();
        });
        findViewById(R.id.btn_s_3).setOnClickListener(v -> {
            testFunc3();
        });
    }

    @TrackEvent(category = "test_event_category", action = "test_event_action", name = "test_event_name", path = "/second/test_event_path")
    private void testFunc1() {
        System.out.println("testFunc1");
    }

    @TrackDimension(dimension = "test_dimension_dimension", value = "test_dimension_value", path = "/second/test_dimension_path")
    private void testFunc2() {
        System.out.println("testFunc2");
    }

    private void testFunc3() {
        System.out.println("testFunc3");
    }
}