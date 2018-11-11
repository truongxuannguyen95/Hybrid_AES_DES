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
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FragmentDecrypt extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public FragmentDecrypt() {
    }

    public static FragmentDecrypt newInstance(String param1, String param2) {
        FragmentDecrypt fragment = new FragmentDecrypt();
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

    private Button btnChooseFile, btnDecrypt;
    private TextView tvFileName;
    private EditText edtKeyDecrypt;
    private CheckBox ckbUseKeys;
    private Uri uri;
    private String fileNameDecrypt = "";
    private String filePath = "";
    private ProgressDialog progressDialog;
    private Handler handler = new Handler();
    public static int percent_Decrypted = 0;
    public static boolean cancel = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_decrypt, container, false);
        btnChooseFile = view.findViewById(R.id.btnChooseFile);
        btnDecrypt = view.findViewById(R.id.btnDecrypt);
        tvFileName = view.findViewById(R.id.tvFileName);
        edtKeyDecrypt = view.findViewById(R.id.edtKeyDecrypt);
        ckbUseKeys = view.findViewById(R.id.ckbUseKeys);
        btnChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseFile();
            }
        });

        tvFileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseFile();
            }
        });

        btnDecrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fileNameDecrypt.length() == 0) {
                    Utilities.showAlertDialog("Thông báo", "Vui lòng chọn file để giải mã", getContext(), false);
                } else if (UserPage.isOffile) {
                    if (edtKeyDecrypt.length() == 0) {
                        edtKeyDecrypt.setError("Vui lòng nhập key để giải mã");
                    } else {
                        new MyAsyncTask().execute();
                    }
                } else {
                    if (!ckbUseKeys.isChecked() && edtKeyDecrypt.length() == 0) {
                        edtKeyDecrypt.setError("Vui lòng nhập key để giải mã");
                    } else if (Utilities.isOnline(getContext())) {
                        new MyAsyncTask().execute();
                    } else {
                        Utilities.showAlertDialog("Thông báo", "Thiết bị của bạn chưa được kết nối internet\nVui lòng kết nối internet để sử dụng chức năng này", getContext(), false);
                    }
                }
            }
        });

        ckbUseKeys.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    if (UserPage.isOffile) {
                        Utilities.showAlertDialog("Thông báo", "Chức năng này chỉ sử dụng khi đăng nhập", getContext(), false);
                        ckbUseKeys.setChecked(false);
                    } else {
                        if (ckbUseKeys.isChecked()) {
                            edtKeyDecrypt.setEnabled(false);
                            edtKeyDecrypt.setText(null);
                            edtKeyDecrypt.setError(null);
                            edtKeyDecrypt.setBackgroundColor(Color.DKGRAY);
                        } else {
                            edtKeyDecrypt.setEnabled(true);
                            final int sdk = android.os.Build.VERSION.SDK_INT;
                            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                edtKeyDecrypt.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.corners));
                            } else {
                                edtKeyDecrypt.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.corners));
                            }
                        }
                    }
                }
            }
        });
        return view;
    }

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent = Intent.createChooser(intent, "Chọn file để giải mã");
        startActivityForResult(intent, 0);
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
            fileNameDecrypt = fileName;
            tvFileName.setText(fileName);
            tvFileName.setTextColor(Color.BLACK);
        }
    }

    public void ShowProgressDialog() {
        percent_Decrypted = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (percent_Decrypted < 100) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.setProgress(percent_Decrypted);
                        }
                    });
                }
            }
        }).start();
    }

    private class MyAsyncTask extends AsyncTask<String, String, String> {

        String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String rootPath = storagePath + "/Download/Decrypt";
        File root = new File(rootPath);
        File temp = new File(rootPath + "/temp");
        File file = new File(rootPath + "/" + fileNameDecrypt);
        int flagFailed = 0;
        private boolean flag = false;

        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMax(100);
            progressDialog.setMessage("Đang giải mã...");
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
            byte[] bytes = new byte[32768];
            int nRead;
            try {
                InputStream is = getActivity().getContentResolver().openInputStream(uri);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                while ((nRead = is.read(bytes, 0, bytes.length)) != -1) {
                    buffer.write(bytes, 0, nRead);
                }
                buffer.flush();
                boolean hasKey = false;
                String key = "";
                String fileData = Utilities.byteArrayToString(buffer.toByteArray());
                if (fileData.length() > 32) {
                    String oldKey = fileData.substring(0, 32);
                    oldKey = Hybrid_AES_DES.decrypt("TruongXuanNguyen", oldKey);
                    key = edtKeyDecrypt.getText().toString();
                    key = key + "Nguyen@2018";
                    if (UserPage.isOffile) {
                        String md5Key = Utilities.md5(key);
                        if (oldKey.equals(md5Key)) {
                            hasKey = true;
                        }
                    } else {
                        if (ckbUseKeys.isChecked()) {
                            for (int i = 0; i < HomePage.listKeys.size(); i++) {
                                key = HomePage.listKeys.get(i);
                                key = Hybrid_AES_DES.decrypt("TruongXuanNguyen", key);
                                key = key.trim();
                                String md5Key = Utilities.md5(key.trim());
                                if (oldKey.equals(md5Key)) {
                                    hasKey = true;
                                    break;
                                }
                            }
                        } else {
                            String md5Key = Utilities.md5(key);
                            if (oldKey.equals(md5Key)) {
                                hasKey = true;
                            }
                        }
                    }
                    if (hasKey) {
                        root.mkdirs();
                        temp.createNewFile();
                        fileData = fileData.substring(32);
                        String decryptLen = fileData.substring(0, 16);
                        decryptLen = Hybrid_AES_DES.decrypt(key, decryptLen);
                        int len = Integer.parseInt(decryptLen.trim());
                        fileData = fileData.substring(16);
                        ShowProgressDialog();
                        fileData = Hybrid_AES_DES.decrypt(key, fileData);
                        if (!cancel) {
                            byte[] bytess = Utilities.stringToByteArray(fileData.substring(0, len));
                            BufferedOutputStream bos = null;
                            bos = new BufferedOutputStream(new FileOutputStream(temp, false));
                            bos.write(bytess);
                            bos.flush();
                            bos.close();
                            flag = true;
                        }
                    } else {
                        flag = false;
                    }
                }
            } catch (FileNotFoundException e) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    flagFailed = 1;
                }
                e.printStackTrace();
            } catch (IOException e) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    flagFailed = 1;
                }
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onPostExecute(String result) {
            if (cancel) {
                temp.delete();
                cancel = false;
            } else if (flagFailed == 1) {
                temp.delete();
                Utilities.showAlertDialog("Giải mã thất bại", "Đã xảy ra lỗi trong quá trình đọc file", getContext(), false);
            } else if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                if (flag) {
                    temp.renameTo(file);
                    tvFileName.setText("");
                    fileNameDecrypt = "";
                    edtKeyDecrypt.setText("");
                    Utilities.showAlertDialog("Giải mã thành công", "File giải mã được lưu trong thư mục\n/Download/Decrypt", getContext(), true);
                } else {
                    temp.delete();
                    if (UserPage.isOffile || !ckbUseKeys.isChecked())
                        Utilities.showAlertDialog("Giải mã thất bại", "File này chưa được mã hóa hoặc sai key", getContext(), false);
                    else
                        Utilities.showAlertDialog("Giải mã thất bại", "File này chưa được mã hóa hoặc danh sách key của bạn không chứa key mã hóa file này", getContext(), false);
                }
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