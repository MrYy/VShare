package com.ANT.MiddleWare.PartyPlayerActivity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.ANT.MiddleWare.PartyPlayerActivity.bean.DashApplication;
import com.ANT.MiddleWare.PartyPlayerActivity.util.LoginDialog;
import com.ANT.MiddleWare.PartyPlayerActivity.util.Method;
import com.android.volley.Response;
import com.baoyz.actionsheet.ActionSheet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.model.PhotoInfo;
import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {
    private static final String TAG = LoginFragment.class.getSimpleName();
    private GalleryFinal.OnHanlderResultCallback mOnHanlderResultCallback = new GalleryFinal.OnHanlderResultCallback() {
        @Override
        public void onHanlderSuccess(int reqeustCode, List<PhotoInfo> resultList) {

            String url;
            PhotoInfo photoInfo;
            if (resultList != null) {
                photoInfo = resultList.get(0);
                url = photoInfo.getPhotoPath();
                Log.d(TAG, "photo url:" + url);
                Bitmap b = BitmapFactory.decodeFile(url);
                photo.setImageBitmap(b);
            }
        }

        @Override
        public void onHanlderFailure(int requestCode, String errorMsg) {
        }
    };

    private View view;
    private Activity context;
    private CheckBox checkBoxRem;
    private CheckBox checkBoxPublis;
    private EditText editText;
    private CircleImageView photo;
    private Bitmap portrait;
    private EditText password;
    private DashApplication app;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (DashApplication)getActivity().getApplication();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_login, container, false);
        context = getActivity();
        checkBoxRem = (CheckBox) view.findViewById(R.id.checkbox_remember_account);
        checkBoxPublis = (CheckBox) view.findViewById(R.id.checkbox_publish_video);
        editText = (EditText) view.findViewById(R.id.edittext_name);
        password = (EditText) view.findViewById(R.id.edittext_password);
        editText.clearFocus();
        editText.setSelected(false);
        Map<String,String> user = app.getUser();
        if (user.size() > 0) {
            editText.setText(user.get("name"));
            password.setText(user.get("password"));
            checkBoxRem.setChecked(true);
        }
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            String s1 = editText.getText().toString();
                Map<String, String> req = new HashMap<>();
                req.put("name", s1);
                Method.postRequest(getActivity(), DashApplication.INFO, req, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        try {
                            JSONObject res = new JSONObject(s);
                            if (res.getString("code").equals("200")) {
                                String testurl=res.getJSONObject("data").getString("thumb_url");
                                if(!testurl.equals("")) {
                               try{ portrait = getBitmap(testurl);
                                   photo.setImageBitmap(portrait);
                                }catch (IOException io){
                                   io.printStackTrace();
                               }
                                }
                            }else {
                                Method.display(getContext(),res.getString("msg"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            photo.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.profile_default));
                        }
                    }
                });

            }
        });
        ((Button) view.findViewById(R.id.button_login)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = editText.getText().toString().trim();
                final String pwd = password.getText().toString().trim();
                if (name.equals("")||pwd.equals("")) {
                    Method.display(getActivity(),"用户名或密码为空");
                    return;
                }
                ConnectivityManager con=(ConnectivityManager)getContext().getSystemService(Activity.CONNECTIVITY_SERVICE);
                boolean internet=con.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
                if (!internet) {
                    Method.display(getContext(),"当前没有移动网络，观看或分享本地视频");
                        Intent intent = new Intent(getActivity(), ViewVideoActivity.class);
                        intent.putExtra(context.getString(R.string.user_name), name);
                        intent.putExtra(context.getString(R.string.publish_video), checkBoxPublis.isChecked());
                        intent.putExtra("保存用户昵称", checkBoxRem.isChecked());
                        if (checkBoxRem.isChecked()) {
                            app.setUser(name, pwd);
                        }
                        startActivity(intent);
                }
                Map<String, String> req = new HashMap<String, String>();
                req.put("name", name);
                req.put("password", pwd);

                Method.postRequest(getActivity(), DashApplication.LOGIN, req, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        try {
                            JSONObject result = new JSONObject(s);
                            String code = result.getString("code");
                            String msg = result.getString("msg");
                            if (code.equals("200")) {
                                Intent intent = new Intent(getActivity(), ViewVideoActivity.class);
                                intent.putExtra(context.getString(R.string.user_name), name);
                                intent.putExtra(context.getString(R.string.publish_video), checkBoxPublis.isChecked());
                                intent.putExtra("保存用户昵称", checkBoxRem.isChecked());
                                if (checkBoxRem.isChecked()) {
                                    app.setUser(name, pwd);
                                }
                                startActivity(intent);
                            }
                            Method.display(getActivity(),msg);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
                //test code

            }
        });
        (view.findViewById(R.id.textview_register)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginDialog dialog = new LoginDialog(getActivity());
                dialog.show();
            }
        });
        photo = (CircleImageView) view.findViewById(R.id.profile_image);
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActionSheet.createBuilder(getActivity(), getActivity().getSupportFragmentManager())
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
                                        Method.selectPicture(getActivity(), mOnHanlderResultCallback, Method.USE_LOCAL_PICTURE);
                                        break;
                                    case 1:
                                        Method.selectPicture(getActivity(), mOnHanlderResultCallback, Method.USER_CAMERA);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }).show();

            }
        });
        return view;
    }
    @SuppressLint("NewApi")
    public static Bitmap getBitmap(String path) throws IOException {

        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("GET");
        if(conn.getResponseCode() == 200){
            InputStream inputStream = conn.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return bitmap;
        }
        return null;
    }


}
