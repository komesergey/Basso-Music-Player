package com.basso.basso.ui.activities;

import android.os.Bundle;
import com.basso.basso.R;
import com.basso.basso.ui.fragments.phone.MusicBrowserPhoneFragment;

public class HomeActivity extends BaseActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_base_content, new MusicBrowserPhoneFragment()).commit();
        }
    }

    @Override
    public int setContentView() {
        return R.layout.activity_base;
    }
}
