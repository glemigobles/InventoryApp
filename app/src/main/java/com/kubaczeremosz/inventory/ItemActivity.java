package com.kubaczeremosz.inventory;


import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

public class ItemActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_ITEM_LOADER = 0;
    private Uri mCurrentItemUri;
    private TextView mNameTextView;
    private TextView mQuantityTextView;
    private TextView mPriceTextView;
    private Button mAdd;
    private Button mDec;
    private Button mOrder;
    private Button mDelete;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        Intent intent = getIntent();
        mCurrentItemUri=intent.getData();
        mNameTextView= (TextView)findViewById(R.id.item_edit_name);
        mQuantityTextView =(TextView)findViewById(R.id.item_edit_quantity);
        mPriceTextView =(TextView)findViewById(R.id.item_edit_price);
        mAdd=(Button)findViewById(R.id.add_quantity);
        mDec=(Button)findViewById(R.id.dec_quantity);
        mOrder=(Button)findViewById(R.id.order_item);
        mDelete=(Button)findViewById(R.id.delete_item);

        getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);


    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_NAME,
                InventoryContract.InventoryEntry.COLUMN_QUANTITY,
                InventoryContract.InventoryEntry.COLUMN_PRICE,
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


            // Extract out the value from the Cursor for the given column index
            String name = data.getString(nameColumnIndex);
            int quantity = data.getInt(quantityColumnIndex);
            int price = data.getInt(priceColumnIndex);

            // Update the views on the screen with the values from the database
            mNameTextView.setText(name);
            mQuantityTextView.setText(quantity);
            mPriceTextView.setText(Integer.toString(price));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mNameTextView.setText("");
        mQuantityTextView.setText("");
        mPriceTextView.setText("");

    }

    private void saveItem() {
        String quantityString =mQuantityTextView.getText().toString();
        int quantity = Integer.parseInt(quantityString);

        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY,quantity);

       getContentResolver().update(mCurrentItemUri, values, null, null);
    }
}
