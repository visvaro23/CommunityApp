package elcarmen.project.community.Business;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.concurrent.ExecutionException;

import elcarmen.project.community.Data.API_Access;
import elcarmen.project.community.Data.HttpGetBitmap;
import elcarmen.project.community.R;

public class EditUserActivity extends AppCompatActivity {

    ImageButton imageButton;
    EditText edtName;
    EditText edtTel1;
    EditText edtTel2;
    EditText edtAddress;
    CheckBox checkPrivate;
    Button btnSave;

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();


    private static int IMG_RESULT = 1;
    InputStream in = null;
    String picturePath;

    String photo = "";
    String photo_thumbnail = "";

    Intent intent;
    String[] FILE;

    Bitmap bitmap;
    User_Singleton user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        user = User_Singleton.getInstance();

        imageButton = findViewById(R.id.imgBut_user);
        edtName = findViewById(R.id.edt_nameUser);
        edtTel1 = findViewById(R.id.edt_tel1User);
        edtTel2 = findViewById(R.id.edt_tel2User);
        edtAddress = findViewById(R.id.edt_addressUser);
        checkPrivate = findViewById(R.id.check_privateUser);
        btnSave = findViewById(R.id.btn_saveEditUser);

        edtName.setText(user.getName());
        edtTel1.setText(user.getCel());
        edtTel2.setText(user.getTel());
        edtAddress.setText(user.getAddress());

        if(user.isPrivateProfile()){
            checkPrivate.setChecked(true);
        }

        imageButton.setImageBitmap(user.getPhoto());

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMG_RESULT);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    public void saveChangesClicked(View view){

        if (!TextUtils.isEmpty(edtName.getText()) && !TextUtils.isEmpty(edtTel1.getText())) {



            ExecuteUploaded executeUploaded = new ExecuteUploaded();
            executeUploaded.execute();
        }
        else {
            edtName.setError("Campo Requerido");
            edtTel1.setError("Campo Requerido");
        }
    }

    public class ExecuteChange extends AsyncTask<String,Void,String> {
        private String id;
        private String name;
        private String tel1;
        private String tel2;
        private String address;
        private Boolean isPrivate;

        private String authToken;
        private boolean isOk = false;

        public ExecuteChange(String id,String authToken,String name, String tel1, String tel2, String address, Boolean isPrivate) {
            this.name = name;
            this.tel1 = tel1;
            this.tel2 = tel2;
            this.address = address;
            this.isPrivate = isPrivate;
            this.id = id;
            this.authToken = authToken;
        }

        @Override
        protected String doInBackground(String... strings) {


            API_Access api = API_Access.getInstance();
            String[] keys = {"id", "auth_token", "name","cel","tel","address","isPrivate"};
            String[] values = {id,authToken,name,tel1,tel2,address,Boolean.toString(isPrivate)};
            isOk = api.post_put_base(keys,values,7,"PUT",1);



            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (isOk) {
                String token = null;
                try {
                    token = (API_Access.getInstance().getJsonObjectResponse()).getString("auth_token");
                    User_Singleton.getInstance().setAuth_token(token);
                    LoginAcivity.actualizarAuth_Token(token, getApplicationContext());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                user.setName(name);
                user.setCel(tel1);
                user.setTel(tel2);
                user.setAddress(address);
                user.setPrivateProfile(isPrivate);
                Toast.makeText(EditUserActivity.this, "Cambio exitoso", Toast.LENGTH_SHORT).show();
                //finish();
            } else
                Toast.makeText(EditUserActivity.this, "Cambio no exitoso", Toast.LENGTH_SHORT).show();

        }
    }

    public class ExecuteUploaded extends AsyncTask<String, Void, String> {
        boolean isOk;
        String rand ="";
        Cloudinary cloudinary = new Cloudinary("cloudinary://523642389612375:w_BVcUQ7VFZ8IQj-iE1-zbqv5iU@ddgkzz2gk");

        @Override
        protected String doInBackground(String... strings) {


            try {
                String path = picturePath; //Aquí sería la ruta donde se haya seleccionado la imagen
                File file = new File(picturePath);
                File f = file.getAbsoluteFile();

                ///////////////////////////////////////////////////////////
                // Generar el random para identificador de la foto
                StringBuilder sb = new StringBuilder(10);
                for( int i = 0; i < 10; i++ )
                    sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
                rand = sb.toString();
                ///////////////////////////////////////////////////////////

                cloudinary.uploader().upload(in, ObjectUtils.asMap("public_id", rand));//.getAbsoluteFile(), ObjectUtils.asMap("public_id", rand));//emptyMap());

                photo_thumbnail = (cloudinary.url().transformation(new Transformation()
                        .radius(360).crop("scale").chain()
                        .angle(0)).imageTag(rand + ".png")).split("\'")[1]; //Random
                // Normal:
                photo = (cloudinary.url().transformation(new Transformation()
                        .radius(0).crop("scale").chain()
                        .angle(0)).imageTag(rand + ".png")).split("\'")[1];

                        //14
                API_Access api = API_Access.getInstance();
                String[] keys = {"id","photo", "photo_thumbnail"};
                String[] values = {Integer.toString(user.getId()),photo,photo_thumbnail};
                isOk = api.post_put_base(keys,values,14,"PUT",1);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(isOk){
                photo_thumbnail = (cloudinary.url().transformation(new Transformation()
                        .radius(360).crop("scale").chain()
                        .angle(0)).imageTag(rand + ".png")).split("\'")[1]; //Random

                HttpGetBitmap request = new HttpGetBitmap();
                Bitmap userImage = null;
                try {
                    userImage = request.execute(photo_thumbnail).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if(userImage == null){
                    userImage = BitmapFactory.decodeResource( EditUserActivity.this.getResources(),
                            R.drawable.user_rounded_photo);
                }

                user.setPhoto(bitmap);
                user.setPhoto_rounded(userImage);


            }
            ExecuteChange executeChange = new ExecuteChange(Integer.toString(user.getId()),user.getAuth_token(),
                    edtName.getText().toString(), edtTel1.getText().toString(),edtTel2.getText().toString(),
                    edtAddress.getText().toString(), checkPrivate.isChecked());

            executeChange.execute();


        }
    }


    //////////////METODOS PARA OBTENER IMAGEN//////////////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {

            if (requestCode == IMG_RESULT && resultCode == RESULT_OK
                    && null != data) {

                Uri URI = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), URI);
                    // Log.d(TAG, String.valueOf(bitmap));

                    try {
                        in = getContentResolver().openInputStream(URI);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    imageButton.setImageBitmap(bitmap);


                    //picturePath = getFileName(URI);
                    picturePath = getPath(EditUserActivity.this,URI);
                    picturePath = getRealPathFromURI(URI);
                    Toast.makeText(this, picturePath, Toast.LENGTH_LONG).show();

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        } catch (Exception e) {
            Toast.makeText(this, "Error", Toast.LENGTH_LONG)
                    .show();
        }
    }


    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }


    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
