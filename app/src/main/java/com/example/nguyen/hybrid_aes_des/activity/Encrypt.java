package com.example.nguyen.hybrid_aes_des.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.example.nguyen.hybrid_aes_des.R;
import com.example.nguyen.hybrid_aes_des.Utilities;
import com.example.nguyen.hybrid_aes_des.model.Hybrid_AES_DES;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Encrypt extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public Encrypt() {
    }

    public static Encrypt newInstance(String param1, String param2) {
        Encrypt fragment = new Encrypt();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private Button btnChooseFile, btnRandom, btnEncrypt;
    private CheckBox ckbRandom;
    private TextView tvFileName;
    private EditText edtKeyEncrypt;
    private Uri uri;
    private String fileNameEncrypt = "";
    private String filePath = "";
    private ProgressDialog progressDialog;
    private Handler handler = new Handler();
    public static int percent_Encrypted = 0;
    public static boolean cancel = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_encrypt, container, false);
        tvFileName = view.findViewById(R.id.tvFileName);
        edtKeyEncrypt = view.findViewById(R.id.edtKeyEncrypt);
        btnChooseFile = view.findViewById(R.id.btnChooseFile);
        btnRandom = view.findViewById(R.id.btnRandom);
        btnEncrypt = view.findViewById(R.id.btnEncrypt);
        ckbRandom = view.findViewById(R.id.ckbRandom);

        btnChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent = Intent.createChooser(intent, "Chọn file để mã hóa");
                startActivityForResult(intent, 0);
            }
        });

        ckbRandom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    if (isChecked) {
                        edtKeyEncrypt.setText(Utilities.rand());
                        edtKeyEncrypt.setError(null);
                        btnRandom.setVisibility(View.VISIBLE);
                    } else {
                        btnRandom.setVisibility(View.GONE);
                        edtKeyEncrypt.setText("");
                    }
                }
            }
        });

        btnRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtKeyEncrypt.setText(Utilities.rand());
                edtKeyEncrypt.setError(null);
            }
        });

        btnEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fileNameEncrypt.length() == 0) {
                    Utilities.showAlertDialog("Thông báo", "Vui lòng chọn file để mã hóa", getContext());
                } else if (edtKeyEncrypt.getText().toString().length() < 8) {
                    edtKeyEncrypt.setError("Key phải từ 8 ký tự trở lên");
                } else if (Utilities.isOnline(getContext())) {
                    new MyAsyncTask().execute();
                } else {
                    Utilities.showAlertDialog("Thông báo", "Thiết bị của bạn chưa được kết nối internet\nVui lòng kiểm tra kết nối internet", getContext());
                }
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        if (resultCode != getActivity().RESULT_OK) return;
        if (requestCode == 0) {
            uri = data.getData();
            filePath = uri.getPath();
            String fileName = filePath;
            if (filePath.contains("/")) {
                fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            }
            fileNameEncrypt = fileName;
            tvFileName.setText(fileName);
            tvFileName.setTextColor(Color.BLACK);
        }

    }

    public void ShowProgressDialog() {
        percent_Encrypted = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(percent_Encrypted < 100){
                    try{
                        Thread.sleep(200);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.setProgress(percent_Encrypted);
                        }
                    });
                }
            }
        }).start();
    }

    private class MyAsyncTask extends AsyncTask<String, String, String> {

        String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String rootPath = storagePath + "/Download/Encrypt";
        File root = new File(rootPath);
        File temp = new File(rootPath + "/temp");
        File file = new File(rootPath + "/" + fileNameEncrypt);

        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMax(100);
            progressDialog.setMessage("Đang mã hóa...");
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Hủy bỏ", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    progressDialog.dismiss();
                    cancel = true;
                }
            });
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            ShowProgressDialog();
            byte[] bytes = new byte[32768];
            int nRead;
            try {
                InputStream is = getActivity().getContentResolver().openInputStream(uri);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                while ((nRead = is.read(bytes, 0, bytes.length)) != -1) {
                    buffer.write(bytes, 0, nRead);
                }
                buffer.flush();
                String fileData = Utilities.byteArrayToString(buffer.toByteArray());
                String key = edtKeyEncrypt.getText().toString();
                key = key + "Nguyen@2018";
                fileData = Hybrid_AES_DES.encrypt(key, fileData);
                root.mkdirs();
                temp.createNewFile();
                String encryptKey = "";
                if (!UserPage.isOffile) {
                    encryptKey = Hybrid_AES_DES.encrypt("TruongXuanNguyen", key);
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    String owner = currentUser.getUid();
                    DatabaseReference mData = FirebaseDatabase.getInstance().getReference();
                    boolean flag = false;
                    for (int i = 0; i < HomePage.listKeys.size(); i++) {
                        if (encryptKey.equals(HomePage.listKeys.get(i))) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        mData.child("users").child(owner).push().setValue(encryptKey)
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Utilities.showAlertDialog("Mã hóa thất bại", "Thiết bị của bạn chưa được kết nối internet\nVui lòng kiểm tra kết nối internet", getContext());
                                    }
                                });
                    }
                }
                String md5Key = Utilities.md5(key);
                encryptKey = Hybrid_AES_DES.encrypt("TruongXuanNguyen", md5Key);
                int len = buffer.size();
                String encryptLen = Hybrid_AES_DES.encrypt(key, len + "");
                byte[] bytess = Utilities.stringToByteArray(encryptKey + encryptLen + fileData);
                BufferedOutputStream bos = null;
                bos = new BufferedOutputStream(new FileOutputStream(temp, false));
                bos.write(bytess);
                bos.flush();
                bos.close();
            } catch (FileNotFoundException e) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    Utilities.showAlertDialog("Mã hóa thất bại", "File này không còn tồn tại", getContext());
                }
                e.printStackTrace();
            } catch (IOException e) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    Utilities.showAlertDialog("Mã hóa thất bại", "Đã xảy ra lỗi trong quá trình đọc file", getContext());
                }
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onPostExecute(String result) {
            if (progressDialog != null && progressDialog.isShowing()) {
                temp.renameTo(file);
                tvFileName.setText("");
                edtKeyEncrypt.setText("");
                fileNameEncrypt = "";
                ckbRandom.setChecked(false);
                btnRandom.setVisibility(View.GONE);
                progressDialog.dismiss();
                Utilities.showAlertDialog("Mã hóa thành công", "File mã hóa được lưu trong thư mục\n/Download/Encrypt", getContext());
            } else {
                temp.delete();
            }
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}