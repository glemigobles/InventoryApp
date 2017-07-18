package com.kubaczeremosz.inventory;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class NewItemActivity extends AppCompatActivity {

    private static final String LOG_TAG = NewItemActivity.class.getSimpleName();

    private EditText mNameEditText;
    private EditText mQuantityEditText;
    private EditText mPriceEditText;
    private Button mAddItem;
    private Button mAddImage;
    private ImageView mImageView;
    private Uri mImageUri=null;
    private View mBack;

    private final static int SELECT_PHOTO = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_item_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_item_price);
        mImageView=(ImageView) findViewById(R.id.edit_item_image_view);

        mAddItem = (Button) findViewById(R.id.add_item);
        mAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    insertItem();
                }
            }
        });
        mAddImage=(Button)findViewById(R.id.add_image);
        mAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();

                if (Build.VERSION.SDK_INT < 19) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                }
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, getString(R.string.action_select_picture)), SELECT_PHOTO);
            }

        });
        mBack=(View)findViewById(R.id.new_item_back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    /** Handle the result of the image chooser intent launch */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check if request code, result and intent match the image chooser
        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data != null) {
            if (data != null) {
                mImageUri = data.getData();
                //mTextView.setText(mUri.toString());
                mImageView.setImageBitmap(getBitmapFromUri(mImageUri));
            }
            else{
                mImageView.setImageResource(R.drawable.barcodeproduct);
            }
        }

    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }


    private void insertItem() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String imageString = mImageUri.toString();


        int quantity = Integer.parseInt(quantityString);
        int price = Integer.parseInt(priceString);

        // Create a ContentValues object where column names are the keys,
        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_NAME, nameString);
        values.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY, quantity);
        values.put(InventoryContract.InventoryEntry.COLUMN_PRICE, price);
        values.put(InventoryContract.InventoryEntry.COLUMN_IMAGE, imageString);

        //inserting
        Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);
        showToast(R.string.insert_item_successful);
        clearFields();
    }

    private boolean validateInput() {
        if (mNameEditText.getText().toString().isEmpty() || mQuantityEditText.getText().toString().isEmpty() || mPriceEditText.getText().toString().isEmpty()) {
            showToast(R.string.insert_item_insertdata);
            return false;
        }else if(mImageUri==null){
            showToast(R.string.insert_image_failed);
            return false;
        }
        else if (isNumeric(mQuantityEditText.getText().toString()) && isNumeric(mPriceEditText.getText().toString())) {
            return true;
        }
        showToast(R.string.insert_item_failed);
        return false;
    }


    private void showToast(int Rstring){
        Toast.makeText(getApplicationContext(), getString(Rstring),
                Toast.LENGTH_SHORT).show();

    }

    private boolean isNumeric(String str){
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }
    private void clearFields(){
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mImageView.setImageResource(R.drawable.barcodeproduct);
        mImageUri=null;
    }


}
