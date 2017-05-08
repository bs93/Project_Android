package projekt.budgetdroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Szmolke on 2017-03-31.
 */

public class DbManager extends SQLiteOpenHelper {

    public DbManager(Context context) {
        super(context, "budget_droid.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Currency (" +
                "CUR_ID         INTEGER  PRIMARY KEY AUTOINCREMENT " +
                "UNIQUE " +
                "NOT NULL," +
                "CUR_NAME       TEXT," +
                "CUR_VALUE      NUMERIC," +
                "CUR_LASTUPDATE DATETIME );"
        );

        db.execSQL("INSERT INTO Currency (" +
                "                         CUR_NAME," +
                "                         CUR_VALUE," +
                "                         CUR_LASTUPDATE" +
                "                     )" +
                "                     VALUES (" +
                "                         'PLN'," +
                "                         '4.22'," +
                "                         '2017-03-31 10:00:00'" +
                "                     ), (" +
                "                         'USD'," +
                "                         '1.01'," +
                "                         '2017-03-31 10:00:00'" +
                "                     ),(" +
                "                         'EUR'," +
                "                         '1.00'," +
                "                         '2017-03-31 10:00:00'" +
                "                     )" +
                "                     ;"
        );

        db.execSQL("CREATE TABLE Corrections (" +
                "    TRA_ID          INTEGER  PRIMARY KEY AUTOINCREMENT" +
                "                             UNIQUE" +
                "                             NOT NULL," +
                "    TRA_ADDED_ON    DATETIME," +
                "    TRA_OCCURED_ON  DATETIME," +
                "    TRA_VALUE       NUMERIC," +
                "    TRA_CURRENCY     TEXT," +
                "    TRA_NAME        TEXT," +
                "    TRA_DESCRIPTION TEXT" +
                ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertTransaction(String name, String description, String currency, String occuredOn, double val)
    {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss");
        String date = df.format(Calendar.getInstance().getTime());

        ContentValues mCV = new ContentValues();
        mCV.put("TRA_ADDED_ON", date);
        mCV.put("TRA_OCCURED_ON", occuredOn);
        mCV.put("TRA_VALUE", val);
        mCV.put("TRA_CURRENCY", currency);
        mCV.put("TRA_NAME", name);
        mCV.put("TRA_DESCRIPTION", description);

        this.getWritableDatabase().insertOrThrow("Corrections","",mCV);
    }

    public List<TransactionModel> getAllTransactions() {
        Cursor cursor = this.getReadableDatabase().rawQuery("SELECT TRA_NAME, TRA_OCCURED_ON, TRA_VALUE, TRA_CURRENCY FROM Corrections", null);

        List<TransactionModel> list = new ArrayList<TransactionModel>();

        while(cursor.moveToNext())
        {
            list.add(new TransactionModel(cursor.getString(0),cursor.getString(1),cursor.getString(2), cursor.getString(3)));
        }
        return list;
    }

    public void UpdateCurrencies(String date, String plnRate, String usdRate)
    {
        this.getWritableDatabase().execSQL("UPDATE Currency SET CUR_LASTUPDATE='"+date+"00:00:00'");
        this.getWritableDatabase().execSQL("UPDATE Currency SET CUR_VALUE='"+usdRate+"' WHERE CUR_NAME='USD'");
        this.getWritableDatabase().execSQL("UPDATE Currency SET CUR_VALUE='"+plnRate+"' WHERE CUR_NAME='PLN'");
    }

    public Pair<Double,Double> GetValuesRange(String currency){
        Cursor cursor = this.getReadableDatabase().rawQuery("SELECT min(TRA_VALUE), max(TRA_VALUE) FROM Corrections where TRA_CURRENCY='"+ currency+"';", null);
        cursor.moveToNext();
        return new Pair<>(cursor.getDouble(0), cursor.getDouble(1));
    }
}
