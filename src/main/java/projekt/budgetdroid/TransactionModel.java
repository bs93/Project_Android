package projekt.budgetdroid;

/**
 * Created by Szmolke on 2017-04-06.
 */

public class TransactionModel {
    private String name;
    private String date;
    private String value;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    private String currency;

    public TransactionModel(String name, String date, String value, String currency){
        this.name = name;
        this.date = date;
        this.value = value;
        this.currency = currency;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getValue() {
        return value;
    }

    public Double getConvertedValue()
    {
        return new Double(value);
    }

    public void setValue(String value) {
        this.value = value;
    }
}
