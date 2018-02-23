package com.youdo.karma.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.youdo.karma.R;
import com.youdo.karma.activity.base.BaseActivity;
import com.youdo.karma.entity.FareActivityModel;
import com.youdo.karma.manager.AppManager;
import com.youdo.karma.net.request.GetFareActvityInfoRequest;
import com.youdo.karma.utils.PreferencesUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by wangyb on 2018/2/23.
 */

public class GetTelFareRuleActivity extends BaseActivity {

    @BindView(R.id.btn_get_fare)
    FancyButton mBtnGetFare;
    @BindView(R.id.qualification)
    TextView mQualification;
    @BindView(R.id.rule)
    TextView mRule;
    @BindView(R.id.condition)
    TextView mCondition;
    @BindView(R.id.way)
    TextView mWay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_tel_fare_rule);
        ButterKnife.bind(this);
        Toolbar toolbar = getActionBarToolbar();
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.mipmap.ic_up);
        }
        new GetFareActvityInfoTask().request();
    }

    class GetFareActvityInfoTask extends GetFareActvityInfoRequest {
        @Override
        public void onPostExecute(FareActivityModel fareActivityModel) {
            if (null != fareActivityModel) {
                mQualification.setText(fareActivityModel.qualify);
                mRule.setText(fareActivityModel.getRule);
                mCondition.setText(fareActivityModel.getCondition);
                mWay.setText(fareActivityModel.getWay);
            }
        }

        @Override
        public void onErrorExecute(String error) {
        }
    }

    @OnClick(R.id.btn_get_fare)
    public void onViewClicked() {
        if (AppManager.getClientUser().is_vip) {
            if (PreferencesUtils.getWhichVip(this) == 0) {
                showDialog();
            } else {
                Intent intent = new Intent(GetTelFareRuleActivity.this, GetTelFareActivity.class);
                startActivity(intent);
            }
        } else {
            showDialog();
        }
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.no_join_activity);
        builder.setPositiveButton(R.string.right_now_join_activity, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(GetTelFareRuleActivity.this, VipCenterActivity.class);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
