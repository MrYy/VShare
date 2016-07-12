package com.ANT.MiddleWare.PartyPlayerActivity;

import android.app.Activity;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ANT.MiddleWare.PartyPlayerActivity.bean.DashApplication;
import com.ANT.MiddleWare.PartyPlayerActivity.bean.MenuLayout;
import com.ANT.MiddleWare.PartyPlayerActivity.util.Method;
import com.android.volley.Response;
import com.baoyz.actionsheet.ActionSheet;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import net.gotev.uploadservice.UploadServiceBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.model.PhotoInfo;
import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.ANT.MiddleWare.PartyPlayerActivity.LoginFragment.getBitmap;
import static com.ANT.MiddleWare.PartyPlayerActivity.ViewVideoActivity.userName;

public class UserActivity extends FragmentActivity {
    private TextView username;
    private TextView contact;
    private TextView about;
    private CircleImageView photo;
    private SweetAlertDialog pDialog;
    private Bitmap b;
    private final UploadServiceBroadcastReceiver uploadReceiver =
            new UploadServiceBroadcastReceiver() {

                @Override
                public void onProgress(String uploadId, int progress) {

                }

                @Override
                public void onError(String uploadId, Exception exception) {
                    Log.d("test", exception.getMessage());
                    if (UserActivity.this != null) {
                       closeDialog();
                    }
                    Method.warnDialog(getApplicationContext(), "图片上传失败");
                }

                @Override
                public void onCompleted(String uploadId, int serverResponseCode, byte[] serverResponseBody) {

                        if (UserActivity.this != null) {
                            closeDialog();
                            Method.display(getApplicationContext(), "头像设置成功");
                        }
                }

                @Override
                public void onCancelled(String uploadId) {
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_information);
        MenuLayout menuLayout = (MenuLayout)findViewById(R.id.bottom_menu);
        menuLayout.setFocuse(MenuLayout.BUTTON.RIGHT);
        username=(TextView)findViewById(R.id.username);
        contact=(TextView)findViewById(R.id.contact);
        about=(TextView)findViewById(R.id.about_us);
        photo=(CircleImageView)findViewById(R.id.profile_image_set);
        username.setText(userName);
        String s1 = userName;
        Map<String, String> req = new HashMap<>();
        req.put("name", s1);
        Method.postRequest(UserActivity.this, DashApplication.INFO, req, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject res = new JSONObject(s);
                    if (res.getString("code").equals("200")) {
                        String testurl=res.getJSONObject("data").getString("thumb_url");
                        if(!testurl.equals(null)) {
                            try{ Bitmap portrait = getBitmap(testurl);
                                photo.setImageBitmap(portrait);
                            }catch (IOException io){
                                io.printStackTrace();
                            }
                        }
                    }else {
                        Method.display(UserActivity.this,res.getString("msg"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        photo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ActionSheet.createBuilder(UserActivity.this,getSupportFragmentManager())
                        .setCancelButtonTitle("取消")
                        .setOtherButtonTitles("上传本地头像", "拍照上传头像")
                        .setCancelableOnTouchOutside(true)
                        .setListener(new ActionSheet.ActionSheetListener() {
                            @Override
                            public void onDismiss(ActionSheet actionSheet, boolean b) {

                            }

                            @Override
                            public void onOtherButtonClick(ActionSheet actionSheet, int i) {
                                switch (i) {
                                    case 0:
                                        Method.selectPicture(UserActivity.this, mOnHanlderResultCallback, Method.USE_LOCAL_PICTURE);
                                        break;
                                    case 1:
                                        Method.selectPicture(UserActivity.this, mOnHanlderResultCallback, Method.USER_CAMERA);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }).show();

            }
        });

    }
    private GalleryFinal.OnHanlderResultCallback mOnHanlderResultCallback = new GalleryFinal.OnHanlderResultCallback() {
        @Override
        public void onHanlderSuccess(int reqeustCode, List<PhotoInfo> resultList) {

            String url;
            PhotoInfo photoInfo;
            if (resultList != null) {
                photoInfo = resultList.get(0);
                url = photoInfo.getPhotoPath();
                Method.uploadMultipart(UserActivity.this,url,userName);
                 b = BitmapFactory.decodeFile(url);
                photo.setImageBitmap(b);
                pDialog = new SweetAlertDialog(UserActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                pDialog.setTitleText("图片上传中");
                pDialog.setCancelable(false);
                pDialog.show();
            }
        }

        @Override
        public void onHanlderFailure(int requestCode, String errorMsg) {
        }
    };
    public void closeDialog() {
        if (pDialog.isShowing()) {
            pDialog.dismissWithAnimation();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        uploadReceiver.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        uploadReceiver.unregister(this);

    }
    }

