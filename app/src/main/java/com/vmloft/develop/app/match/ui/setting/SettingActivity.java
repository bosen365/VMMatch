package com.vmloft.develop.app.match.ui.setting;

import android.view.View;
import butterknife.OnClick;
import com.vmloft.develop.app.match.R;
import com.vmloft.develop.app.match.base.AppActivity;
import com.vmloft.develop.app.match.common.ASignManager;
import com.vmloft.develop.app.match.router.ARouter;

/**
 * Create by lzan13 on 2019/05/14
 *
 * 设置界面
 */
public class SettingActivity extends AppActivity {

    @Override
    protected int layoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void initUI() {
        super.initUI();
    }

    @Override
    protected void initData() {
        setTopTitle(R.string.setting);
    }

    @OnClick({ R.id.setting_notify, R.id.setting_chat, R.id.setting_about, R.id.setting_sign_out })
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.setting_notify:
            ARouter.goNotifySetting(mActivity);
            break;
        case R.id.setting_chat:
            ARouter.goChatSetting(mActivity);
            break;
        case R.id.setting_about:
            ARouter.goAboutSetting(mActivity);
            break;
        case R.id.setting_sign_out:
            signOut();
            break;
        }
    }

    /**
     * 退出登录
     */
    private void signOut() {
        ASignManager.getInstance().signOut();
        onFinish();
    }
}
