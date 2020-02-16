package cn.luern0313.wristbilibili.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import cn.luern0313.wristbilibili.R;

public class TextActivity extends AppCompatActivity
{
    Context ctx;
    Intent intent;

    TextView uiTitle;
    TextView uiText;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        ctx = this;
        intent = getIntent();

        uiTitle = findViewById(R.id.text_title_title);
        uiText = findViewById(R.id.text_textview);
        uiTitle.setText(intent.getStringExtra("title"));
        uiText.setText(Html.fromHtml(intent.getStringExtra("text")));
    }
}
