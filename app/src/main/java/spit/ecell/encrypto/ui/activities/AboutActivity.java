package spit.ecell.encrypto.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import spit.ecell.encrypto.BuildConfig;
import spit.ecell.encrypto.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final String versionInfo = getString(R.string.package_name) + BuildConfig.APPLICATION_ID + "\n"
                + getString(R.string.version_name) + BuildConfig.VERSION_NAME + "\n"
                + getString(R.string.version_code) + BuildConfig.VERSION_CODE;

        TextView versionButton = findViewById(R.id.versionText);
        versionButton.setText(getString(R.string.version_name) + BuildConfig.VERSION_NAME);
        versionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(AboutActivity.this);
                alert.setTitle(getString(R.string.app_name))
                        .setMessage(versionInfo)
                        .show();
            }
        });

    }

    public void onNoticeClick(View view) {
        new LibsBuilder()
                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                .start(this);
    }
}
