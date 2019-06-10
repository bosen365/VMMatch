package com.vmloft.develop.app.match.ui.main.home;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import butterknife.BindView;

import com.vmloft.develop.app.match.R;
import com.vmloft.develop.app.match.base.ACallback;
import com.vmloft.develop.app.match.base.AppActivity;
import com.vmloft.develop.app.match.bean.AMatch;
import com.vmloft.develop.app.match.bean.AUser;
import com.vmloft.develop.app.match.common.AMatchManager;
import com.vmloft.develop.app.match.common.ASignManager;
import com.vmloft.develop.app.match.glide.ALoader;
import com.vmloft.develop.app.match.router.ARouter;
import com.vmloft.develop.app.match.utils.AUtils;
import com.vmloft.develop.library.im.call.IMCallManager;
import com.vmloft.develop.library.im.router.IMRouter;
import com.vmloft.develop.library.tools.animator.VMAnimator;
import com.vmloft.develop.library.tools.router.VMParams;
import com.vmloft.develop.library.tools.utils.VMDimen;
import com.vmloft.develop.library.tools.widget.toast.VMToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Create by lzan13 on 2019/5/15 23:13
 *
 * 匹配界面
 */
public class MatchActivity extends AppActivity {

    // 匹配类型
    public static final int MATCH_TYPE_TEXT = 0;
    public static final int MATCH_TYPE_CALL = 1;

    @BindView(R.id.match_anim_view) View mAnimView;
    @BindView(R.id.match_anim_view_2) View mAnimView2;
    @BindView(R.id.match_avatar_iv) ImageView mAvatarView;
    @BindView(R.id.match_container) FrameLayout mMatchContainer;

    // 自己
    private AUser mUser;
    private AMatch mMatch;
    // 正在匹配的人，使用 Map 是为了过滤掉重复的信息
    private Map<String, AMatch> mMatchMap = new HashMap<>();

    private int avatarSize;
    private int mMatchType = MATCH_TYPE_TEXT;

    private VMAnimator.AnimatorSetWrap mAnimatorWrap;
    private VMAnimator.AnimatorSetWrap mAnimatorWrap2;

    @Override
    protected int layoutId() {
        return R.layout.activity_match;
    }

    @Override
    protected void initUI() {
        super.initUI();
    }

    @Override
    protected void initData() {
        setTopTitle(R.string.match);
        getTopBar().setTitleColor(R.color.app_title_light);

        VMParams params = ARouter.getParams(mActivity);
        mMatchType = params.arg0;

        mUser = ASignManager.getInstance().getCurrentUser();
        avatarSize = VMDimen.dp2px(48);

        setupUserInfo();

        loadMatchData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startMatch();
    }

    /**
     * 装载用户信息
     */
    private void setupUserInfo() {
        String url = mUser.getAvatar() != null ? mUser.getAvatar().getUrl() : null;
        ALoader.loadAvatar(mActivity, url, mAvatarView);
    }

    /**
     * 获取匹配数据
     */
    private void loadMatchData() {
        AMatchManager.getInstance().getMatchList(new ACallback<List<AMatch>>() {
            @Override
            public void onSuccess(List<AMatch> list) {
                for (AMatch match : list) {
                    // 过滤掉自己的匹配信息
                    String userId = match.getUser().getObjectId();
                    if (userId.equals(mUser.getObjectId())) {
                        continue;
                    }
                    mMatchMap.put(match.getObjectId(), match);
                    // 只显示最近已定数量参与匹配的人
                    if (mMatchMap.size() >= 5) {
                        break;
                    }
                }
                setupMatchList();
            }

            @Override
            public void onError(int code, String desc) {
                VMToast.make(mActivity, "好像有问题哎，稍后再来吧").error();
            }
        });
    }

    /**
     * 开始匹配，需要经自己的信息提交到后端
     */
    private void startMatch() {

        AMatchManager.getInstance().startMatch(new ACallback<AMatch>() {
            @Override
            public void onSuccess(AMatch match) {
                mMatch = match;
            }

            @Override
            public void onError(int code, String desc) {
                VMToast.make(mActivity, "提交匹配信息失败").error();
            }
        });

        mAnimatorWrap = VMAnimator.createAnimator()
            .play(VMAnimator.createOptions(mAnimView, VMAnimator.SCALEX, 2000, VMAnimator.INFINITE, 0f, 20f))
            .with(VMAnimator.createOptions(mAnimView, VMAnimator.SCALEY, 2000, VMAnimator.INFINITE, 0f, 20f))
            .with(VMAnimator.createOptions(mAnimView, VMAnimator.ALPHA, 2000, VMAnimator.INFINITE, 1.0f, 0.0f));
        mAnimatorWrap.startDelay(100);

        mAnimatorWrap2 = VMAnimator.createAnimator()
            .play(VMAnimator.createOptions(mAnimView2, VMAnimator.SCALEX, 2000, VMAnimator.INFINITE, 0f, 20f))
            .with(VMAnimator.createOptions(mAnimView2, VMAnimator.SCALEY, 2000, VMAnimator.INFINITE, 0f, 20f))
            .with(VMAnimator.createOptions(mAnimView2, VMAnimator.ALPHA, 2000, VMAnimator.INFINITE, 1.0f, 0.0f));
        mAnimatorWrap2.startDelay(1100);
    }

    /**
     * 加载匹配数据
     */
    private void setupMatchList() {
        for (AMatch match : mMatchMap.values()) {
            ImageView imageView = new ImageView(mActivity);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(avatarSize, avatarSize);

            int x = AUtils.random(mMatchContainer.getWidth() - avatarSize);
            int y = AUtils.random(mMatchContainer.getHeight() - avatarSize);
            imageView.setX(x);
            imageView.setY(y);
            imageView.setAlpha(0.0f);
            mMatchContainer.addView(imageView, lp);

            final AUser user = match.getUser();
            String url = user.getAvatar() != null ? user.getAvatar().getUrl() : null;
            ALoader.loadAvatar(mActivity, url, imageView);

            imageView.setOnClickListener((View v) -> {
                if (mMatchType == MATCH_TYPE_TEXT) {
                    IMRouter.goIMChat(mActivity, user.getObjectId());
                } else {
                    IMCallManager.getInstance().startCall(user.getObjectId(), IMCallManager.CallType.VOICE);
                }
                onFinish();
            });

            // 动画出现
            long delay = AUtils.random(5) * 500;
            VMAnimator.createAnimator().play(VMAnimator.createOptions(imageView, VMAnimator.ALPHA, 0.0f, 1.0f)).startDelay(delay);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mMatch != null) {
            AMatchManager.getInstance().stopMatch(mMatch);
        }
    }

    @Override
    protected void onDestroy() {
        if (mAnimatorWrap != null) {
            mAnimatorWrap.cancel();
        }
        if (mAnimatorWrap2 != null) {
            mAnimatorWrap2.cancel();
        }
        super.onDestroy();
    }
}
