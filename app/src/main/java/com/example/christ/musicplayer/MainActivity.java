package com.example.christ.musicplayer;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener {

//    private ActionMenuView mAcitionMenuView;
    private ViewPager vpContent;  // 负责管理歌曲页fragment和录音页fragment
    private ImageView ivTitleMusic;  // 歌曲页图标
    private ImageView ivTitleRecord;  // 录音页图标

    private ImageView pause;//底部暂停按钮
    private ImageView next;//下一首
    private SeekBar seekBar;//底部进度条
    private TextView song;//歌名
    private TextView singer;//歌手
    private ImageView pic;
    private ServiceConnection conn;
    private IBinder mBinder;
    private static Handler mHandler;
    private int position = -1;
    private MusicInfo music;
   // private boolean isPlay = false;
    private ArrayList<MusicInfo> musicInfos;
    private User user;
    private UsersRepo usersRepo;
    private TextView headerName;

    // 动态申请权限
    private static String[] PERMISSIONS = new String[]
            {Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
            };
    private static boolean hasPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 动态申请权限
        hasPermission = PermissionUtil.verifyStoragePermissions(MainActivity.this, PERMISSIONS);

        // ViewPager
        vpContent = (ViewPager) findViewById(R.id.vp_content);

        // 加载toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 加载侧边栏
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        usersRepo = new UsersRepo(this);

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // 用户登录
        View headerView = navigationView.getHeaderView(0);
        headerName = (TextView) headerView.findViewById(R.id.user_name);
        TextView check = (TextView) headerView.findViewById(R.id.check_in);
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                String data = String.valueOf(calendar.get(Calendar.YEAR))+"."+String.valueOf(calendar.get(Calendar.MONTH)+1)
                        +"."+String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                if(user!=null){
                    if(user.getScore()!=0 && data.equals(user.getDate())){
                        Toast.makeText(MainActivity.this,"今天已经签到过",Toast.LENGTH_SHORT).show();
                    } else {
                        user.setScore(user.getScore()+5);
                        usersRepo.update(user);
                        Toast.makeText(MainActivity.this,"签到成功，积分：+5",Toast.LENGTH_SHORT).show();
                    }
                } else
                    Toast.makeText(MainActivity.this,"请先登陆",Toast.LENGTH_SHORT).show();

            }
        });

        // 点播页和录音页的点击事件
        ivTitleMusic = (ImageView) findViewById(R.id.music);
        ivTitleRecord = (ImageView) findViewById(R.id.record);
        ivTitleMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCurrentItem(0);
//                if (vpContent.getCurrentItem() != 0) {
//                    setCurrentItem(0);
//                }
            }
        });
        ivTitleRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCurrentItem(1);
//                if (vpContent.getCurrentItem() != 1) {
//                    setCurrentItem(1);
//                }
            }
        });

        initContentFragment();

        //底部播放进度
        pause = (ImageView) findViewById(R.id.play_btn);
        next = (ImageView) findViewById(R.id.next_btn);
        song = (TextView) findViewById(R.id.song);
        singer = (TextView) findViewById(R.id.singer);
        seekBar = (SeekBar) findViewById(R.id.progress);
        pic = (ImageView) findViewById(R.id.album_art);
        musicInfos = new ArrayList<MusicInfo>();
        LinearLayout bottom = (LinearLayout) findViewById(R.id.bottom);



        bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position!=-1){
                    Intent intent = new Intent(MainActivity.this,PlayAndRecord.class);
                    intent.putExtra("music", music);
                    intent.putExtra("binder", (PlayerService.mBinder)mBinder);
             //       intent.putExtra("isPlay",isPlay);
                    startActivity(intent);
                }
            }
        });
        //绑定播放器服务
        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBinder = service;
            //    Toast.makeText(MainActivity.this,"mbinder",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                conn = null;
            }
        };
        Intent intent = new Intent(this,PlayerService.class);
        startService(intent);
        bindService(intent,conn, Context.BIND_AUTO_CREATE);


        //更新UI
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 123) {
                    if(mBinder != null) {
                        try {
                            Parcel reply = Parcel.obtain();
                            mBinder.transact(104, Parcel.obtain(), reply, 0);
                            int location = reply.readInt();
                            seekBar.setProgress(location);
                            int max = reply.readInt();
                            seekBar.setMax(max);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        //创建子线程，在子线程中处理耗时工作
        Thread mThread = new Thread() {
            @Override
            public void run() {
                while (true){
                    try{
                        Thread.sleep(100);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    mHandler.obtainMessage(123).sendToTarget();
                }
            }
        };
        mThread.start();
        //进度条
        seekBar.setEnabled(true);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    try{
                        Parcel data = Parcel.obtain();
                        data.writeInt(progress);
                        mBinder.transact(105,data,Parcel.obtain(),0);
                    }catch (RemoteException e){
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        //暂停按钮
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Parcel data = Parcel.obtain();
                try{
                    mBinder.transact(100,Parcel.obtain(),data,0);
                }catch (RemoteException e){
                    e.printStackTrace();
                }

                if(data.readInt()==1){
            //        isPlay = false;
                    pause.setImageResource(R.drawable.play_btn);
                } else{
            //        isPlay = true;
                    if(music!=null)
                        pause.setImageResource(R.drawable.pause);
                }

                try{
                    Parcel reply = Parcel.obtain();
                    mBinder.transact(101,Parcel.obtain(),reply,0);
                }catch(RemoteException e){
                    e.printStackTrace();
                }
            }
        });
        //下一首按钮
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position!=-1){
                    position = position + 1;
                    music = musicInfos.get(position);
                    song.setText(music.getTitle());
                    singer.setText(music.getArtist());
                    pic.setImageBitmap(MediaUtil.getAlbumArt(MainActivity.this,music.getAlbum_id()));
                //    isPlay = true;
                    pause.setImageResource(R.drawable.pause);
                    Parcel data = Parcel.obtain();
                    data.writeString(music.getUrl());
                    Log.e("next",music.getUrl());
                    try{
                        mBinder.transact(102,data,Parcel.obtain(),0);
                    }catch (RemoteException e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //接收listFragment传来的数据
    public void getMessage(int position,ArrayList<MusicInfo> musics){
        this.position = position;
        musicInfos = musics;
        music = musicInfos.get(position);
        song.setText(music.getTitle());
        singer.setText(music.getArtist());
        pic.setImageBitmap(MediaUtil.getAlbumArt(this, music.getAlbum_id()));
    //    isPlay = true;
        pause.setImageResource(R.drawable.pause);

        Parcel data = Parcel.obtain();
        data.writeString(music.getUrl());
        try{
            mBinder.transact(102,data,Parcel.obtain(),0);
        }catch (RemoteException e){
            e.printStackTrace();
        }

        Intent intent = new Intent(MainActivity.this,PlayAndRecord.class);
        intent.putExtra("music", music);
        intent.putExtra("binder", (PlayerService.mBinder)mBinder);
    //    intent.putExtra("isPlay",isPlay);
        startActivity(intent);
    }

    // 切换fragment
    private void setCurrentItem(int position) {
        boolean isOne = false;
        boolean isTwo = false;
        switch (position) {
            case 0:
                isOne = true;
                break;
            case 1:
                isTwo = true;
                break;
            default:
                isOne = true;
                break;
        }
        vpContent.setCurrentItem(position);
        ivTitleMusic.setSelected(isOne);
        ivTitleRecord.setSelected(isTwo);
    }

    // 初始化fragment
    private void initContentFragment() {
        ArrayList<Fragment> mFragmentList = new ArrayList<>();
        if(!hasPermission)
            return;
        mFragmentList.add(new ServiceFragment());
        mFragmentList.add(new LocalFragment());
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(), mFragmentList);
        vpContent.setAdapter(adapter);
        // 设置ViewPager最大缓存的页面个数(cpu消耗少)
        vpContent.setOffscreenPageLimit(2);
        vpContent.addOnPageChangeListener(this);
        setCurrentItem(0);
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }
    @Override
    public void onPageSelected(int position) {
        switch (position) {
            case 0:
                setCurrentItem(0);
                break;
            case 1:
                setCurrentItem(1);
                break;
            case 2:
                setCurrentItem(2);
                break;
            default:
                break;
        }
    }
    @Override
    public void onPageScrollStateChanged(int state) {
    }

    // 开启侧边栏的按钮
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // toolbar上居中图标组的布局加载
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main , menu); //将menu关联
        return super.onCreateOptionsMenu(menu);
    }

    // 搜索
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.refresh){
            EventBus.getDefault().post("refresh");
        }

        return super.onOptionsItemSelected(item);
    }

    // 侧边栏里的item的点击事件
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.message) {
            if(user==null){
                Toast.makeText(this,"请先登陆",Toast.LENGTH_SHORT).show();
            } else{
                LayoutInflater factory = LayoutInflater.from(MainActivity.this);
                View newview = factory.inflate(R.layout.dialog_user_message,null);
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                final EditText edit_username = (EditText) newview.findViewById(R.id.username);
                final EditText edit_password = (EditText) newview.findViewById(R.id.password);
                final EditText edit_age = (EditText) newview.findViewById(R.id.age);
                RadioGroup radioGroup = (RadioGroup) newview.findViewById(R.id.gender);
                final RadioButton male_btn = (RadioButton) radioGroup.findViewById(R.id.male);
                final RadioButton female_btn = (RadioButton)radioGroup.findViewById(R.id.female);
                TextView score = (TextView) newview.findViewById(R.id.score);
                TextView date = (TextView) newview.findViewById(R.id.date);
                final TextView userid = (TextView) newview.findViewById(R.id.userid);
                final Button edit = (Button) newview.findViewById(R.id.edit_btn);
                Button quit = (Button) newview.findViewById(R.id.quit_btn);
                final AlertDialog object ;
                //设置弹出窗口的信息
                edit_age.setText(String.valueOf(user.getAge()));
                edit_username.setText(user.getUsername());
                edit_password.setText(user.getPassword());
                if(user.getGender()==0){//男
                    radioGroup.check(male_btn.getId());
                } else if(user.getGender()==1){ //女
                    radioGroup.check(female_btn.getId());
                }
                score.setText(String.valueOf(user.getScore()));
                date.setText(user.getDate());
                userid.setText(String.valueOf(user.getId()));

                dialog.setView(newview);
                View topbar = factory.inflate(R.layout.dialog_tab,null);
                dialog.setCustomTitle(topbar);
                TextView title = (TextView)topbar.findViewById(R.id.title);
                title.setText("用户信息");
                object = dialog.show();

                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(edit.getText().equals("编辑")){
                            edit.setText("保存");
                            edit_age.setFocusable(true);
                            edit_age.setFocusableInTouchMode(true);
                            edit_password.setFocusable(true);
                            edit_password.setFocusableInTouchMode(true);
                            edit_username.setFocusable(true);
                            edit_username.setFocusableInTouchMode(true);
                            male_btn.setClickable(true);
                            female_btn.setClickable(true);
                        } else if(edit.getText().equals("保存")){
                            edit.setText("编辑信息");
                            edit_age.setFocusable(false);
                            edit_age.setFocusableInTouchMode(false);
                            edit_password.setFocusable(false);
                            edit_password.setFocusableInTouchMode(false);
                            edit_username.setFocusable(false);
                            edit_username.setFocusableInTouchMode(false);
                            male_btn.setClickable(false);
                            female_btn.setClickable(false);
                            //保存修改
                            int gender = 0;
                            if(female_btn.isChecked())
                                gender = 1;
                            User newUser = new User(edit_username.getText().toString(),edit_password.getText().toString(),
                                    Integer.parseInt(edit_age.getText().toString()),gender);
                            newUser.setId(user.getId());
                            newUser.setDate(user.getDate());
                            newUser.setScore(user.getScore());
                            usersRepo.update(newUser);
                            user = newUser;
                            headerName.setText(user.getUsername());
                            Toast.makeText(MainActivity.this,"修改成功",Toast.LENGTH_SHORT).show();
                            object.dismiss();
                        }
                    }
                });
                quit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        object.dismiss();
                    }
                });
            }
        } else if(id == R.id.login){
            if(user!=null){
                Toast.makeText(MainActivity.this,"已退出登陆",Toast.LENGTH_SHORT).show();
                user = null;
                headerName.setText("请先登陆");
            } else {
                LayoutInflater factory = LayoutInflater.from(MainActivity.this);
                View newview = factory.inflate(R.layout.login,null);
                final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                final EditText edit_username = (EditText) newview.findViewById(R.id.login_username);
                final EditText edit_password = (EditText) newview.findViewById(R.id.login_password);
                final EditText edit_age = (EditText) newview.findViewById(R.id.login_age);
                RadioGroup radioGroup = (RadioGroup) newview.findViewById(R.id.login_gender);
                final RadioButton male_btn = (RadioButton) radioGroup.findViewById(R.id.login_male);
                final RadioButton female_btn = (RadioButton)radioGroup.findViewById(R.id.login_female);
                final LinearLayout ageLayout = (LinearLayout) newview.findViewById(R.id.ageLayout);
                final LinearLayout genderLayout = (LinearLayout) newview.findViewById(R.id.genderLayout);
                final Button login = (Button) newview.findViewById(R.id.login_regis);
                Button quit = (Button) newview.findViewById(R.id.login_quit);
                final AlertDialog object ;
                login.setText("登陆");
                dialog.setView(newview);
                View topbar = factory.inflate(R.layout.dialog_tab,null);
                dialog.setCustomTitle(topbar);
                TextView title = (TextView)topbar.findViewById(R.id.title);
                title.setText("登陆/注册");
                object = dialog.show();
                login.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(login.getText().equals("登陆")){
                            String name = edit_username.getText().toString();
                            User getUser = usersRepo.getUserByName(name);
                            if(name.equals(getUser.getUsername())){
                                if(getUser.getPassword().equals(edit_password.getText().toString())){
                                    user = getUser;
                                    headerName.setText(user.getUsername());
                                    Toast.makeText(MainActivity.this,"登陆成功",Toast.LENGTH_SHORT).show();
                                    object.dismiss();
                                }
                                else
                                    Toast.makeText(MainActivity.this,"密码错误",Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this,"该用户不存在，请完善信息后注册",Toast.LENGTH_SHORT).show();
                                ageLayout.setVisibility(View.VISIBLE);
                                genderLayout.setVisibility(View.VISIBLE);
                                login.setText("注册");
                            }
                        }else{
                            int gender = 0;
                            if(female_btn.isChecked())
                                gender = 1;
                            User register = new User(edit_username.getText().toString(),edit_password.getText().toString(),
                                    Integer.parseInt(edit_age.getText().toString()),gender);
                            int ID = usersRepo.insert(register);
                            register.setId(ID);
                            user = register;
                            headerName.setText(user.getUsername());
                            Toast.makeText(MainActivity.this,"注册且登陆成功",Toast.LENGTH_SHORT).show();
                            object.dismiss();
                        }

                        // 发送user
                        if (user != null) {
                            EventBus.getDefault().postSticky(user);

                        }
                    }
                });
                quit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { object.dismiss(); }
                });

            }
        } else if (id == R.id.credit_shop) {
            Toast.makeText(this,"敬请期待积分商城上线",Toast.LENGTH_SHORT).show();
        } else if(id == R.id.setting){
            Toast.makeText(this,"敬请期待设置页面上线",Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults){
        boolean isAllGranted = true;
        for (int result : grantResults){
            if(result !=  PackageManager.PERMISSION_GRANTED){
                isAllGranted = false;
                break;
            }
        }
        if(isAllGranted){
            hasPermission = true;
        } else {
            Toast.makeText(MainActivity.this, "没有权限", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRestart(){
        super.onRestart();
        if(mBinder!=null) {
            Parcel data = Parcel.obtain();
            try {
                mBinder.transact(100, Parcel.obtain(), data, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (data.readInt() == 1)
                pause.setImageResource(R.drawable.pause);
            else
                pause.setImageResource(R.drawable.play_btn);
        }

    }
}
