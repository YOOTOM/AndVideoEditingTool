package com.anibear.andvideoeditingtool;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.lmntrx.android.library.livin.missme.ProgressDialog;

public class BaseActivity extends AppCompatActivity {

    private com.lmntrx.android.library.livin.missme.ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initProgressDialog();
    }

    private void initProgressDialog() {
        mProgressDialog = new com.lmntrx.android.library.livin.missme.ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMax(100);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("가공");
    }

    public void showProgressDialog() {
        mProgressDialog.setProgress(0);
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        mProgressDialog.dismiss();
    }

    public void updateProgress(float percent) {
        mProgressDialog.setProgress((int) (percent * 100));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    protected void setBackBtnVisible(boolean visible) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(visible);
        }
    }
}
