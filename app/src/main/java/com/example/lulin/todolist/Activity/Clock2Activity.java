package com.example.lulin.todolist.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.lulin.todolist.Bean.Clock;
import com.example.lulin.todolist.Bean.User;
import com.example.lulin.todolist.DBHelper.MyDatabaseHelper;
import com.example.lulin.todolist.Dao.Clock2Dao;
import com.example.lulin.todolist.Dao.ClockDao;
import com.example.lulin.todolist.R;
import com.example.lulin.todolist.Service.Clock2Service;
import com.example.lulin.todolist.Service.FocusService;
import com.example.lulin.todolist.Utils.NetWorkUtils;
import com.example.lulin.todolist.Utils.SPUtils;
import com.example.lulin.todolist.Utils.TimeFormatUtil;
import com.example.lulin.todolist.Widget.Clock2Application;
import com.example.lulin.todolist.Widget.ClockApplication;
import com.example.lulin.todolist.Widget.ClockProgressBar;
import com.example.lulin.todolist.Widget.RippleWrapper;
import com.jaouan.compoundlayout.RadioLayout;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;
import me.drakeet.materialdialog.MaterialDialog;

public class Clock2Activity extends BasicActivity {

    private ClockApplication mApplication;
    private MenuItem mMenuItemIDLE;
    private Button mBtnStart;
    private Button mBtnPause;
    private Button mBtnResume;
    private Button mBtnStop;
    private Button mBtnSkip;
    private TextView mTextCountDown;
    private TextView mTextTimeTile;
    private TextView focus_tint;
    private ClockProgressBar mProgressBar;
    private RippleWrapper mRippleWrapper;
    private long mLastClickTime = 0;
    private String clockTitle;
    private long duration;
    private static final String KEY_FOCUS = "focus";
    private ImageView clock_bg;
    private ImageButton bt_music;
    private static int[] imageArray = new int[]{R.drawable.ic_img2,
            R.drawable.ic_img3,
            R.drawable.ic_img4,
            R.drawable.ic_img5,
            R.drawable.ic_img6,
            R.drawable.ic_img7,
            R.drawable.ic_img8,
            R.drawable.ic_img9,
            R.drawable.ic_img10,
            R.drawable.ic_img11,
            R.drawable.ic_img12};
    private int bg_id;
    private int workLength, shortBreak, longBreak;
    private long id;
    private RadioLayout river, rain, wave, bird, fire;
    private Clock2Dao mDBAdapter;

    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBar();
        setContentView(R.layout.activity_clock2);
        Intent intent = getIntent();
        clockTitle = intent.getStringExtra("clocktitle");
        duration = intent.getLongExtra("duration",0L);
//        workLength = intent.getIntExtra("workLength",ClockApplication.DEFAULT_WORK_LENGTH);
//        shortBreak = intent.getIntExtra("shortBreak",ClockApplication.DEFAULT_SHORT_BREAK);
//        longBreak = intent.getIntExtra("longBreak",ClockApplication.DEFAULT_LONG_BREAK);
        id = intent.getLongExtra("id", 1);

        mApplication = (ClockApplication) getApplication();
        setCacheCurrentStatus(ClockApplication.STATE_WAIT);
        mBtnStart = (Button) findViewById(R.id.btn_start2);
        mBtnPause = (Button) findViewById(R.id.btn_pause2);
        mBtnResume = (Button) findViewById(R.id.btn_resume2);
        mBtnStop = (Button) findViewById(R.id.btn_stop2);
        mBtnSkip = (Button) findViewById(R.id.btn_skip2);
        mTextCountDown = (TextView) findViewById(R.id.text_count_down2);
        mTextTimeTile = (TextView) findViewById(R.id.text_time_title2);
        mProgressBar = (ClockProgressBar) findViewById(R.id.tick_progress_bar2);
        mRippleWrapper = (RippleWrapper) findViewById(R.id.ripple_wrapper2);
        focus_tint = (TextView) findViewById(R.id.focus_hint2);
        bt_music = (ImageButton) findViewById(R.id.bt_music2);
        clock_bg = (ImageView) findViewById(R.id.clock_bg2);
        mDBAdapter = new Clock2Dao(getApplicationContext());
        if (isSoundOn()) {
            bt_music.setEnabled(true);
            bt_music.setImageDrawable(getResources().getDrawable(R.drawable.ic_music));
        } else {
            bt_music.setEnabled(false);
            bt_music.setImageDrawable(getResources().getDrawable(R.drawable.ic_music_off));
        }
        SPUtils.put(this, "music_id", R.raw.river);
        Toasty.normal(this, "双击界面打开或关闭白噪音", Toast.LENGTH_SHORT).show();
        updateText(duration);
        initActions();
        initBackgroundImage();
    }

    private void initBackgroundImage() {

        Random random = new Random();
        bg_id = imageArray[random.nextInt(11)];
        //内存优化
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(true);

        Glide.with(getApplicationContext())
                .load(bg_id)
                .apply(options)
                .into(clock_bg);

    }

    private void initActions() {
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = Clock2Service.newIntent(getApplicationContext());
                i.setAction(Clock2Service.ACTION_START2);
                i.putExtra("id", id);
                i.putExtra("clockTitle", clockTitle);
//                i.putExtra("workLength",workLength);
//                i.putExtra("shortBreak",shortBreak);
//                i.putExtra("longBreak",longBreak);
                startService(i);
                mApplication.start();
                setCacheCurrentStatus(ClockApplication.STATE_RUNNING);
                updateButtons();
                updateTitle();
                updateRipple();
                if (getIsFocus(Clock2Activity.this)) {
                    startService(new Intent(Clock2Activity.this, FocusService.class));
                    focus_tint.setVisibility(View.VISIBLE);
                }
            }
        });

        mBtnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = Clock2Service.newIntent(getApplicationContext());
                i.setAction(Clock2Service.ACTION_PAUSE);
                i.putExtra("time_left", (String) mTextCountDown.getText());
                startService(i);
                setCacheCurrentStatus(ClockApplication.STATE_PAUSE);
                mApplication.pause();
                updateButtons();
                updateRipple();
            }
        });

        mBtnResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = Clock2Service.newIntent(getApplicationContext());
                i.setAction(Clock2Service.ACTION_RESUME);
                startService(i);
                setCacheCurrentStatus(ClockApplication.STATE_RUNNING);
                mApplication.resume();
                updateButtons();
                updateRipple();
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final MaterialDialog exitDialog = new MaterialDialog(Clock2Activity.this);
                exitDialog.setTitle("0.0")
                        .setMessage("你要离开了嘛？")
                        .setPositiveButton("是的，是的^_^", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent2 = new Intent(Clock2Activity.this, MainActivity.class);
                                startActivity(intent2);
                                stopService(new Intent(Clock2Activity.this, FocusService.class));
                                Glide.get(Clock2Activity.this).clearMemory();
                                exitApp();
                            }
                        })
                        .setNegativeButton("才不是呢-。-", new View.OnClickListener() {
                            public void onClick(View view) {
                                exitDialog.dismiss();
                            }
                        });

                exitDialog.show();

            }
        });

        mBtnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = Clock2Service.newIntent(getApplicationContext());
                i.setAction(Clock2Service.ACTION_STOP);
                startService(i);
                setCacheCurrentStatus(ClockApplication.STATE_WAIT);
                mApplication.skip();
                reload();
            }
        });

        mRippleWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long clickTime = System.currentTimeMillis();
                if (clickTime - mLastClickTime < 500) {

                    // 修改 SharedPreferences
                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(getApplicationContext()).edit();

                    if (isSoundOn()) {
                        editor.putBoolean("pref_key_tick_sound", false);

                        Intent i = Clock2Service.newIntent(getApplicationContext());
                        i.setAction(Clock2Service.ACTION_TICK_SOUND_OFF);
                        startService(i);
                        bt_music.setImageDrawable(getResources().getDrawable(R.drawable.ic_music_off));
                        bt_music.setEnabled(false);
                        Snackbar.make(view, getResources().getString(R.string.toast_tick_sound_off),
                                Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                    } else {
                        editor.putBoolean("pref_key_tick_sound", true);

                        Intent i = Clock2Service.newIntent(getApplicationContext());
                        i.setAction(Clock2Service.ACTION_TICK_SOUND_ON);
                        startService(i);
                        bt_music.setImageDrawable(getResources().getDrawable(R.drawable.ic_music));
                        bt_music.setEnabled(true);
                        Snackbar.make(view, getResources().getString(R.string.toast_tick_sound_on),
                                Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                    }
                    try {
                        editor.apply();
                    } catch (AbstractMethodError unused) {
                        editor.commit();
                    }

                    updateRipple();
                }

                mLastClickTime = clickTime;
            }
        });

        bt_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflater = LayoutInflater.from(Clock2Activity.this);
                View musicView = layoutInflater.inflate(R.layout.dialog_music, null);
                river = musicView.findViewById(R.id.sound_river);
                rain = musicView.findViewById(R.id.sound_rain);
                wave = musicView.findViewById(R.id.sound_wave);
                bird = musicView.findViewById(R.id.sound_bird);
                fire = musicView.findViewById(R.id.sound_fire);
                river.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SPUtils.put(Clock2Activity.this, "music_id", R.raw.river);
                        Intent i = Clock2Service.newIntent(getApplicationContext());
                        i.setAction(Clock2Service.ACTION_CHANGE_MUSIC);
                        startService(i);
                    }
                });
                rain.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SPUtils.put(Clock2Activity.this, "music_id", R.raw.rain);
                        Intent i = Clock2Service.newIntent(getApplicationContext());
                        i.setAction(Clock2Service.ACTION_CHANGE_MUSIC);
                        startService(i);
                    }
                });
                wave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SPUtils.put(Clock2Activity.this, "music_id", R.raw.ocean);
                        Intent i = Clock2Service.newIntent(getApplicationContext());
                        i.setAction(Clock2Service.ACTION_CHANGE_MUSIC);
                        startService(i);
                    }
                });
                bird.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SPUtils.put(Clock2Activity.this, "music_id", R.raw.bird);
                        Intent i = Clock2Service.newIntent(getApplicationContext());
                        i.setAction(Clock2Service.ACTION_CHANGE_MUSIC);
                        startService(i);
                    }
                });
                fire.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SPUtils.put(Clock2Activity.this, "music_id", R.raw.fire);
                        Intent i = Clock2Service.newIntent(getApplicationContext());
                        i.setAction(Clock2Service.ACTION_CHANGE_MUSIC);
                        startService(i);
                    }
                });
                final MaterialDialog alert = new MaterialDialog(Clock2Activity.this);
                alert.setPositiveButton("关闭", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.dismiss();
                    }
                });
                alert.setContentView(musicView);
                alert.setCanceledOnTouchOutside(true);
                alert.show();

            }

        });
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//
//    }

    /**
     * 记录当前状态
     * // 当前状态
     * public static final int STATE_WAIT = 0;
     * public static final int STATE_RUNNING = 1;
     * public static final int STATE_PAUSE = 2;
     * public static final int STATE_FINISH = 3;
     *
     * @param value
     */
    public void setCacheCurrentStatus(int value) {
        // 修改 SharedPreferences
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext()).edit();
        switch (value){
            case 0:
                Log.i("Clock2Activity","clock 状态改变为 ：STATE_WAIT");
                break;
            case 1:
                Log.i("Clock2Activity","clock 状态改变为 ：STATE_RUNNING");
                break;
            case 2:
                Log.i("Clock2Activity","clock 状态改变为 ：STATE_PAUSE");
                break;
            case 3:
                Log.i("Clock2Activity","clock 状态改变为 ：STATE_FINISH");
                break;
        }
        editor.putInt("aaa", value);
        try {
            editor.apply();
        } catch (AbstractMethodError unused) {
            editor.commit();
        }

    }

    /**
     * 获取当前状态
     *
     * @return
     */
    private int getCacheCurrentStatus() {
        return PreferenceManager.getDefaultSharedPreferences(this).getInt("aaa", 3);
    }
    private void saveTime() {
        mDBAdapter.open();
        mDBAdapter.updateTime(id,duration);
        mDBAdapter.close();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
//            Snackbar.make(layout, "是否删除？（滑动取消）", Snackbar.LENGTH_LONG)
//                    .setAction("确定", new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            Intent intent = new Intent(getApplication(), MainActivity.class);
//                            startActivity(intent);
//                            exitApp();
//                        }
//                    }).show();
            final MaterialDialog exitDialog = new MaterialDialog(this);
            exitDialog.setTitle("提示")
                    .setMessage("本次番茄钟将作废，是否退出")
                    .setPositiveButton("退出", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent2 = new Intent(Clock2Activity.this, MainActivity.class);
                            startActivity(intent2);
                            stopService(new Intent(Clock2Activity.this, FocusService.class));
                            Glide.get(Clock2Activity.this).clearMemory();
                            exitApp();
                        }
                    })
                    .setNegativeButton("取消", new View.OnClickListener() {
                        public void onClick(View view) {
                            exitDialog.dismiss();
                        }
                    });

            exitDialog.show();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onStart() {
        super.onStart();
        reload();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Clock2Service.ACTION_COUNTDOWN_TIMER);
        registerReceiver(mIntentReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mIntentReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        releaseImageViewResouce(clock_bg);
    }

    private void reload() {
//        mApplication.reload();
//        switch (getCacheCurrentStatus()) {
//            case mApplication.STATE_WAIT:
//            case mApplication.STATE_FINISH:

//                mMillisInTotal = TimeUnit.MINUTES.toMillis(getMinutesInTotal());
//                mMillisUntilFinished = mMillisInTotal;
//                break;
//            case mApplication.STATE_RUNNING:
//                if (SystemClock.elapsedRealtime() > mStopTimeInFuture) {
//                    finish();
//                }
//                break;
//        }
//        setCacheCurrentStatus(ClockApplication.STATE_RUNNING);
        mProgressBar.setMaxProgress(mApplication.getMillisInTotal() / 1000);
        mProgressBar.setProgress(mApplication.getMillisUntilFinished() / 1000);

//        updateText(mApplication.getMillisUntilFinished());
        updateText(0);
        updateTitle();
        updateButtons();
//        updateScene();
        updateRipple();
        updateAmount();

        if (getSharedPreferences().getBoolean("pref_key_screen_on", false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void updateText(long millisUntilFinished) {
        mTextCountDown.setText(TimeFormatUtil.formatTime(millisUntilFinished));
    }

    private void updateTitle() {
        if (mApplication.getState() == ClockApplication.STATE_FINISH) {
            String title;

            if (mApplication.getScene() == ClockApplication.SCENE_WORK) {
                title = getResources().getString(R.string.scene_title_work);
            } else {
                title = getResources().getString(R.string.scene_title_break);
            }

            mTextTimeTile.setText(title);
            mTextTimeTile.setVisibility(View.VISIBLE);
            mTextCountDown.setVisibility(View.GONE);
        } else {
            mTextTimeTile.setVisibility(View.GONE);
            mTextCountDown.setVisibility(View.VISIBLE);
        }
    }

    private void updateButtons() {
//        int state = mApplication.getState();
//        int scene = mApplication.getScene();
        int state = getCacheCurrentStatus();
        boolean isPomodoroMode = getSharedPreferences()
                .getBoolean("pref_key_pomodoro_mode", true);

        // 在番茄模式下不能暂停定时器
        mBtnStart.setVisibility(
                state == ClockApplication.STATE_WAIT || state == ClockApplication.STATE_FINISH ?
                        View.VISIBLE : View.GONE);

        if (false) {
            mBtnPause.setVisibility(View.GONE);
            mBtnResume.setVisibility(View.GONE);
        } else {
            mBtnPause.setVisibility(state == ClockApplication.STATE_RUNNING ?
                    View.VISIBLE : View.GONE);
            mBtnResume.setVisibility(state == ClockApplication.STATE_PAUSE ?
                    View.VISIBLE : View.GONE);
            mBtnStop.setVisibility(!(state == ClockApplication.STATE_WAIT ||
                        state == ClockApplication.STATE_FINISH) ?
                        View.VISIBLE : View.GONE);
        }

//        if (scene == ClockApplication.SCENE_WORK) {
//            mBtnSkip.setVisibility(View.GONE);
//            if (isPomodoroMode) {
//                mBtnStop.setVisibility(!(state == ClockApplication.STATE_WAIT ||
//                        state == ClockApplication.STATE_FINISH) ?
//                        View.VISIBLE : View.GONE);
//            } else {
//                mBtnStop.setVisibility(state == ClockApplication.STATE_PAUSE ?
//                        View.VISIBLE : View.GONE);
//            }
//
//        } else {
//            mBtnStop.setVisibility(View.GONE);
//            if (isPomodoroMode) {
//                mBtnSkip.setVisibility(!(state == ClockApplication.STATE_WAIT ||
//                        state == ClockApplication.STATE_FINISH) ?
//                        View.VISIBLE : View.GONE);
//            } else {
//                mBtnSkip.setVisibility(state == ClockApplication.STATE_PAUSE ?
//                        View.VISIBLE : View.GONE);
//            }
//
//        }
    }

    public void updateScene() {
        int scene = mApplication.getScene();

//        int workLength = getSharedPreferences()
//                .getInt("pref_key_work_length", ClockApplication.DEFAULT_WORK_LENGTH);
//        int shortBreak = getSharedPreferences()
//                .getInt("pref_key_short_break", ClockApplication.DEFAULT_SHORT_BREAK);
//        int longBreak = getSharedPreferences()
//                .getInt("pref_key_long_break", ClockApplication.DEFAULT_LONG_BREAK);

//        ((TextView)findViewById(R.id.stage_work_value))
//                .setText(getResources().getString(R.string.stage_time_unit, workLength));
//        ((TextView)findViewById(R.id.stage_short_break_value))
//                .setText(getResources().getString(R.string.stage_time_unit, shortBreak));
//        ((TextView)findViewById(R.id.stage_long_break_value))
//                .setText(getResources().getString(R.string.stage_time_unit, longBreak));

//        findViewById(R.id.stage_work).setAlpha(
//                scene == ClockApplication.SCENE_WORK ? 0.9f : 0.5f);
//        findViewById(R.id.stage_short_break).setAlpha(
//                scene == ClockApplication.SCENE_SHORT_BREAK ? 0.9f : 0.5f);
//        findViewById(R.id.stage_long_break).setAlpha(
//                scene == ClockApplication.SCENE_LONG_BREAK ? 0.9f : 0.5f);
    }

    private void updateRipple() {
        boolean isPlayOn = getSharedPreferences().getBoolean("pref_key_tick_sound", true);

        if (isPlayOn) {
            if (mApplication.getState() == ClockApplication.STATE_RUNNING) {
                mRippleWrapper.start();
                return;
            }
        }

        mRippleWrapper.stop();
    }

    private void updateAmount() {
        long amount = getSharedPreferences().getLong("pref_key_amount_durations", 0);
        TextView textView = (TextView) findViewById(R.id.amount_durations2);
        textView.setText(getResources().getString(R.string.amount_durations, amount));
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Clock2Service.ACTION_COUNTDOWN_TIMER)) {
                String requestAction = intent.getStringExtra(Clock2Service.REQUEST_ACTION);

                switch (requestAction) {
                    case Clock2Service.ACTION_TICK:
                        long millisUntilFinished = intent.getLongExtra(
                                Clock2Service.MILLIS_UNTIL_FINISHED, 0);
                        mProgressBar.setProgress(millisUntilFinished / 1000);
                        Log.i("Clock2Activity", "要更新的值为： " + TimeFormatUtil.formatTime(millisUntilFinished));
                        updateText(millisUntilFinished);
                        break;
                    case Clock2Service.ACTION_FINISH:
                    case Clock2Service.ACTION_AUTO_START:
                        reload();
                        break;
                }
            }
        }
    };

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void exitApp() {
        saveTime();
        stopService(Clock2Service.newIntent(getApplicationContext()));
        mApplication.exit();
        finish();
    }

    private void setStatusBar() {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    //判断是否开启专注模式
    private boolean getIsFocus(Context context) {

        Boolean isFocus = (Boolean) SPUtils.get(context, KEY_FOCUS, false);

        return isFocus;

    }

    //判断是否开启白噪音
    private boolean isSoundOn() {
        return getSharedPreferences().getBoolean("pref_key_tick_sound", true);
    }

    public static void releaseImageViewResouce(ImageView imageView) {
        if (imageView == null) return;
        Drawable drawable = imageView.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }
}