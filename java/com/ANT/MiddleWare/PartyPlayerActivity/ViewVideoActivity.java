package com.ANT.MiddleWare.PartyPlayerActivity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.ANT.MiddleWare.DASHProxyServer.DashProxyServer;

import java.io.IOException;

public class ViewVideoActivity extends Activity {

    private Button buttonView;
    private EditText editTextLocation;
    private DashProxyServer server = new DashProxyServer();
    public static ConfigureData configureData = new ConfigureData(null);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_view_video);
        buttonView = (Button) findViewById(R.id.button_view_video);
        editTextLocation = (EditText) findViewById(R.id.edittext_video_location);
        initDashProxy();
    }

    private void initDashProxy() {
            configureData.setWorkingMode(ConfigureData.WorkMode.LOCAL_MODE);
    }
}
