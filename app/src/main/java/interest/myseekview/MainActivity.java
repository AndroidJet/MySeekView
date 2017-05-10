package interest.myseekview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MySeekBarView mySeekBarView= (MySeekBarView) findViewById(R.id.barView);
        mySeekBarView.setInitMoneyValues(10000);
        mySeekBarView.setMAX_MONEY(100000);
        mySeekBarView.setCallBack(new MySeekBarView.OnSelectCallBack() {
            @Override
            public void OnSelect(int index) {

            }
        });
    }
}
