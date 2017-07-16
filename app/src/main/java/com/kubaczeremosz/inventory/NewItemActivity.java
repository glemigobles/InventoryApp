package com.kubaczeremosz.inventory;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NewItemActivity extends AppCompatActivity {

    private EditText mNameEditText;

    private EditText mQuantityEditText;

    private EditText mPriceEditText;

    int quantity = 0;
    int price = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_item_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_item_price);

        Button addItem = (Button) findViewById(R.id.add_item);
        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    insertItem();
                }
            }
        });

    }

    public void insertItem() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        this.quantity = Integer.parseInt(quantityString);
        this.price = Integer.parseInt(priceString);

        // Create a ContentValues object where column names are the keys,
        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_NAME, nameString);
        values.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY, quantity);
        values.put(InventoryContract.InventoryEntry.COLUMN_PRICE, price);

        //inserting
        Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);
        showToast(R.string.insert_item_successful);
    }

    public boolean validateInput() {
        if (mNameEditText.getText().toString().matches("") || mQuantityEditText.getText().toString().matches("") || mPriceEditText.getText().toString().matches("")) {
            showToast(R.string.insert_item_insertdata);
            return false;
        } else if (isNumeric(mQuantityEditText.getText().toString()) && isNumeric(mPriceEditText.getText().toString())) {
            return true;
        } else {
            showToast(R.string.insert_item_failed);
            return false;
        }
    }


    public void showToast(int Rstring){
        Toast.makeText(getApplicationContext(), getString(Rstring),
                Toast.LENGTH_SHORT).show();

    }

    public boolean isNumeric(String str){
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


}
