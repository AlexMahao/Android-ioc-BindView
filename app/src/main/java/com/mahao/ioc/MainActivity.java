package com.mahao.ioc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.BindView;
import com.mahao.ioc_api.ViewFinder;


public class MainActivity extends AppCompatActivity {
    // 通过注解绑定控件
    @BindView(R.id.text)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //代码注入
        ViewFinder.inject(this);
        textView.setText("123");
    }
}
