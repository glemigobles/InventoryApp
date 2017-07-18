package com.kubaczeremosz.inventory;


import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.kubaczeremosz.inventory.InventoryProvider.LOG_TAG;

public class ItemActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_ITEM_LOADER = 1;
    private Uri mCurrentItemUri;
    private TextView mNameTextView;
    private TextView mQuantityTextView;
    private TextView mPriceTextView;
    private ImageView mImageView;
    private Button mAdd;
    private Button mDec;
    private Button mOrder;
    private Button mDelete;
    private View mBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();
        mNameTextView = (TextView) findViewById(R.id.item_edit_name);
        mQuantityTextView = (TextView) findViewById(R.id.item_edit_quantity);
        mPriceTextView = (TextView) findViewById(R.id.item_edit_price);
        mImageView = (ImageView) findViewById(R.id.item_image);
        mAdd = (Button) findViewById(R.id.add_quantity);
        mDec = (Button) findViewById(R.id.dec_quantity);
        mOrder = (Button) findViewById(R.id.order_item);
        mDelete = (Button) findViewById(R.id.delete_item);
        mBack=(View)findViewById(R.id.item_back);

        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addQuantity();
            }
        });
        mDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decQuantity();
            }
        });
        mOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderItem();
            }
        });
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_NAME,
                InventoryContract.InventoryEntry.COLUMN_QUANTITY,
                InventoryContract.InventoryEntry.COLUMN_PRICE,
                InventoryContract.InventoryEntry.COLUMN_IMAGE,
        };

        return new CursorLoader(this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }
        if (data.moveToFirst()) {

            int nameColumnIndex = data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_NAME);
            int quantityColumnIndex = data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANTITY);
            int priceColumnIndex = data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRICE);
            int imageColumnIndex = data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = data.getString(nameColumnIndex);
            int quantity = data.getInt(quantityColumnIndex);
            int price = data.getInt(priceColumnIndex);
            final String image = data.getString(imageColumnIndex);

            // Update the views on the screen with the values from the database
            mNameTextView.setText(name);
            mQuantityTextView.setText(Integer.toString(quantity));
            mPriceTextView.setText(Integer.toString(price));

            ViewTreeObserver viewTreeObserver = mImageView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mImageView.setImageBitmap(getBitmapFromUri(Uri.parse(image)));
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mNameTextView.setText("");
        mQuantityTextView.setText("");
        mPriceTextView.setText("");
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

    private void deleteItem() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentItemUri != null) {
            getContentResolver().delete(mCurrentItemUri, null, null);
        }
        // Close the activity
        finish();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel,null);


        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void addQuantity() {
        String quantityString = mQuantityTextView.getText().toString();
        int quantity = Integer.parseInt(quantityString);

        quantity++;

        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY, quantity);
        getContentResolver().update(mCurrentItemUri, values, null, null);

    }

    private void decQuantity() {
        String quantityString = mQuantityTextView.getText().toString();
        int quantity = Integer.parseInt(quantityString);
        if (quantity > 0) {
            quantity--;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY, quantity);
        getContentResolver().update(mCurrentItemUri, values, null, null);

    }

    private void orderItem() {

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String product=mNameTextView.getText().toString();
        intent.putExtra(Intent.EXTRA_EMAIL, "suplier@gmail.com");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Suplly");
        intent.putExtra(Intent.EXTRA_TEXT, "I need some "+ product+" suplly.");

        startActivity(Intent.createChooser(intent, "Send Email"));
    }
}
