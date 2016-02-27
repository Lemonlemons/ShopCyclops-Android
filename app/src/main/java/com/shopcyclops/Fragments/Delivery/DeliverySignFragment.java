package com.shopcyclops.Fragments.Delivery;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.shopcyclops.Fragments.Cart.CartItem;
import com.shopcyclops.R;
import com.shopcyclops.SECRETS;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by Andrew on 10/11/2015.
 */
public class DeliverySignFragment extends Fragment {

    SignaturePad mSignaturePad;
    Button cancelBtn;
    Button acceptBtn;
    File pictureFile;
    int order_id;
    ProgressBar deliverySignProgressBar;
    String signitureUrl;

    public static DeliverySignFragment newInstance(int order_id) {
        DeliverySignFragment fragment = new DeliverySignFragment();
        Bundle args = new Bundle();
        args.putInt("order_id", order_id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle b)
    {
        super.onCreate(b);
        order_id = getArguments().getInt("order_id");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_delivery_sign, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        cancelBtn = (Button) getView().findViewById(R.id.cancelSignBtn);
        acceptBtn = (Button) getView().findViewById(R.id.btnAcceptDelivery);
        deliverySignProgressBar = (ProgressBar) getView().findViewById(R.id.progressSignDeliveryBar);

        mSignaturePad = (SignaturePad) getView().findViewById(R.id.signature_pad);
        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onSigned() {
                acceptBtn.setEnabled(true);
            }

            @Override
            public void onClear() {
                acceptBtn.setEnabled(false);
            }
        });

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deliverySignProgressBar.setVisibility(View.VISIBLE);
                Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();
                if (submitSigniture(signatureBitmap)) {
                    completeOrder();
                } else {
                    Toast.makeText(getActivity(), "Unable to store the signature", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

    }

    private void completeOrder()
    {
        try {
            final SharedPreferences prefs = getActivity().getSharedPreferences(SECRETS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
            String token = prefs.getString(SECRETS.TOKEN_KEY, null);
            String user_email = prefs.getString(SECRETS.EMAIL_KEY, null);
            JSONObject wrapper = new JSONObject();
            JSONObject jsonParams = new JSONObject();
            jsonParams.put("is_delivered", true);
            jsonParams.put("signitureurl", signitureUrl);
            wrapper.put("order", jsonParams);
            AsyncHttpClient client = new AsyncHttpClient();
            PersistentCookieStore myCookieStore = new PersistentCookieStore(getActivity());
            client.setCookieStore(myCookieStore);
            client.addHeader("Accept", "application/json");
            client.addHeader("X-User-Token", token);
            client.addHeader("X-User-Email", user_email);
            StringEntity entity = new StringEntity(wrapper.toString());
            client.post(getActivity(), SECRETS.BASE_URL + "/completeorder?id=" + order_id, entity, "application/json", new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject json) {
                    System.out.println(throwable.toString());
                    deliverySignProgressBar.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable)
                {
                    System.out.println(throwable.toString());
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                    try {
                        getActivity().getSupportFragmentManager().popBackStack();
                        getActivity().getSupportFragmentManager().popBackStack();
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }
                    deliverySignProgressBar.setVisibility(View.INVISIBLE);
                }
            });
        }
        catch (Exception e)
        {
            System.out.println(e.toString());
        }
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ShopCyclops");
        System.out.println(mediaStorageDir.toString());
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                System.out.println("failed to create directory");
            }
        }
        // Create a media file name
        String filename = "SignitureImage_OrderId_" + order_id + ".jpg";
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + filename);
        signitureUrl = SECRETS.AWS_IMAGE_URL + filename;

        return mediaFile;
    }

    public void saveBitmapToJPG(Bitmap bitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();
    }

    public boolean submitSigniture(Bitmap bitmap) {
        pictureFile = getOutputMediaFile();

        if (pictureFile == null) {
            return false;
        }
        try {
            saveBitmapToJPG(bitmap, pictureFile);
        } catch (FileNotFoundException e) {
            System.out.println(e.toString());
            return false;
        } catch (IOException e) {
            System.out.println(e.toString());
            return false;
        }

        addImageToBucket task = new addImageToBucket();
        task.execute();
        return true;
    }



    private class addImageToBucket extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            AmazonS3Client s3Client = new AmazonS3Client( new BasicAWSCredentials( SECRETS.AWS_ACCESS_KEY, SECRETS.AWS_SECRET_KEY ) );
            PutObjectRequest putRequest = new PutObjectRequest( SECRETS.AWS_BUCKET_NAME, pictureFile.getName(), pictureFile );
            s3Client.putObject(putRequest);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println(result);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
}
