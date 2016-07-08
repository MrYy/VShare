package com.ANT.MiddleWare.PartyPlayerActivity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.ANT.MiddleWare.PartyPlayerActivity.util.StatisticsActivity;
import com.ANT.MiddleWare.WiFi.WiFiTCP.Method;
import com.baoyz.actionsheet.ActionSheet;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.List;

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

    public LoginFragment() {
        // Required empty public constructor
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
        ((Button) view.findViewById(R.id.button_login)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editText.getText().toString().trim();
                if (editText.equals("")) {
                    name = "ant";
                }
                //test code
                Intent intent = new Intent(getActivity(), ViewVideoActivity.class);
                intent.putExtra(context.getString(R.string.user_name), name);
                intent.putExtra(context.getString(R.string.publish_video), checkBoxPublis.isChecked());
                intent.putExtra("保存用户昵称", checkBoxRem.isChecked());
                startActivity(intent);
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

}
