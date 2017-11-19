/**
 * Created by Vahe on 11/19/2017.
 */

package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.TransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

public class PersistentTransactionDAO extends SQLiteOpenHelper implements TransactionDAO {

    public static final String DATABASE_NAME = "150648N.db";
    public static final String EXPENSE_COLUMN_ID = "id";
    public static final String EXPENSE_COLUMN_NO = "accountno";
    public static final String EXPENSE_COLUMN_DATE = "date";
    public static final String EXPENSE_COLUMN_TYPE = "type";
    public static final String EXPENSE_COLUMN_AMOUNT = "amount";

    private List<Transaction> transactions;

    public PersistentTransactionDAO(Context context) {
        super(context, DATABASE_NAME , null,1);
        transactions = new LinkedList<>();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // TODO Auto-generated method stub


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS transtable");
        onCreate(db);
    }

    @Override
    public void logTransaction(Date date, String accountNo, ExpenseType expenseType, double amount) {
        Transaction transaction = new Transaction(date, accountNo, expenseType, amount);
        String accountNumber = transaction.getAccountNo();
        Date dates = transaction.getDate();

        byte[] byteDate = dates.toString().getBytes();
        ExpenseType types = transaction.getExpenseType();
        String strType = types.toString();
        byte[] byteType = toString().getBytes();
        Double amounts = transaction.getAmount();

        Calendar c = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c.getTime());
        Log.d("Date",formattedDate);
        byte[] timeStamp = formattedDate.getBytes();

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("accountno", accountNo);
        contentValues.put("amount", amounts);
        contentValues.put("type",strType);
        contentValues.put("date", byteDate);

        db.insert("transtable", null, contentValues);
    }

    @Override
    public List<Transaction> getAllTransactionLogs() {
        transactions.clear();
        Log.d("creation","starting");
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( " select * from transtable", null );

        res.moveToFirst();

        while(res.isAfterLast() == false){

            String accountNo = res.getString(res.getColumnIndex(EXPENSE_COLUMN_NO));
            Double amount = res.getDouble(res.getColumnIndex(EXPENSE_COLUMN_AMOUNT));
            String transType = res.getString(res.getColumnIndex(EXPENSE_COLUMN_TYPE));

            ExpenseType type = ExpenseType.valueOf(transType);
            byte[] date = res.getBlob(res.getColumnIndex(EXPENSE_COLUMN_DATE));

            String str = new String(date, StandardCharsets.UTF_8);
            Log.d("loadedDate",str);

            Date finalDate;
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("E MMM dd yyyy HH:mm:ss 'GMT'z", Locale.ENGLISH);
                finalDate = inputFormat.parse(str);
                transactions.add(new Transaction(finalDate,accountNo,type,amount));
                Log.d("creation","success");
            }catch (java.text.ParseException e){
                Log.d("creation","failed");
                Calendar cal = Calendar.getInstance();

                finalDate = cal.getTime();
                transactions.add(new Transaction(finalDate,accountNo,type,amount));
            }
            res.moveToNext();
        }
        return transactions;
    }

    @Override
    public List<Transaction> getPaginatedTransactionLogs(int limit) {
        int size = transactions.size();
        if (size <= limit) {
            return transactions;
        }
        return transactions.subList(size - limit, size);
    }
}