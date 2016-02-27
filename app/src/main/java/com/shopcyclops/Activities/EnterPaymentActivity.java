package com.shopcyclops.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.devmarvel.creditcardentry.library.CreditCardForm;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.shopcyclops.R;
import com.shopcyclops.SECRETS;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by Andrew on 9/12/2015.
 */
public class EnterPaymentActivity extends Activity {
    private CreditCardForm editCreditcard;
    private EditText editCVC;
    private Spinner editMonth, editYear;
    private ProgressBar spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enterpayment);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        editCreditcard = (CreditCardForm) findViewById(R.id.credit_card_form);
        editCVC = (EditText) findViewById(R.id.editCVC);
        editMonth = (Spinner) findViewById(R.id.editMonth);
        editYear  = (Spinner) findViewById(R.id.editYear);
        spinner = (ProgressBar) findViewById(R.id.paymentSpin);

        ArrayAdapter<CharSequence> monthadapter = ArrayAdapter.createFromResource(this, R.array.month_array, android.R.layout.simple_spinner_item);
        monthadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editMonth.setAdapter(monthadapter);

        ArrayAdapter<CharSequence> yearadapter = ArrayAdapter.createFromResource(this, R.array.year_array, android.R.layout.simple_spinner_item);
        yearadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editYear.setAdapter(yearadapter);
    }

    public void Cancel(View v)
    {
        NavUtils.navigateUpFromSameTask(this);
    }

    public void SavePayment(View v)
    {
        spinner.setVisibility(View.VISIBLE);
        String cardnumber = editCreditcard.getCreditCard().getCardNumber().toString();
        String cvc = Integer.toString(Integer.parseInt(editCVC.getText().toString()));
        if (cardnumber.equals("")) {
            Toast.makeText(getApplicationContext(), "Card Number is empty", Toast.LENGTH_LONG).show();
        } else if (editMonth.getSelectedItem().toString().equals("MM")) {
            Toast.makeText(getApplicationContext(), "Month isn't chosen", Toast.LENGTH_LONG).show();
        } else if (editYear.getSelectedItem().toString().equals("YY")) {
            Toast.makeText(getApplicationContext(), "Year isn't chosen", Toast.LENGTH_LONG).show();
        } else if (cvc.equals("")) {
            Toast.makeText(getApplicationContext(), "CVC number not chosen", Toast.LENGTH_LONG).show();
        } else {
            try {
                int month = Integer.parseInt(editMonth.getSelectedItem().toString());
                int year = Integer.parseInt(editYear.getSelectedItem().toString());
                Card card = new Card(
                        cardnumber,
                        month,
                        year,
                        cvc
                );

                card.validateCard();
                Stripe stripe = new Stripe(SECRETS.STRIPE_PUBLIC_KEY);

                stripe.createToken(
                        card,
                        new TokenCallback() {
                            public void onSuccess(Token token) {
                                addTokenToUser(token.getId().toString());
                            }

                            public void onError(Exception error) {
                                // Show localized error message
                                System.out.println(error.toString());
                                Toast.makeText(EnterPaymentActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                );
            }
            catch (Exception e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
                System.out.println(e.toString());
            }
        }
    }

    private void addTokenToUser(final String token) {
        try {
            System.out.println(token);
            final SharedPreferences prefs = this.getSharedPreferences(SECRETS.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
            String user_token = prefs.getString(SECRETS.TOKEN_KEY, null);
            String user_email = prefs.getString(SECRETS.EMAIL_KEY, null);
//            JSONObject wrapper = new JSONObject();
//            JSONObject jsonParams = new JSONObject();
//            jsonParams.put("stripetoken", token);
//            wrapper.put("user", jsonParams);
            AsyncHttpClient client = new AsyncHttpClient();
            PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
            client.setCookieStore(myCookieStore);
            client.addHeader("Accept", "application/json");
            client.addHeader("X-User-Token", user_token);
            client.addHeader("X-User-Email", user_email);
//            StringEntity entity = new StringEntity(wrapper.toString());
            client.get(this, SECRETS.BASE_URL + "/mobileaddcard?token="+token, new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject json) {
                    spinner.setVisibility(View.INVISIBLE);
                    System.out.println(throwable.toString());
                    Toast.makeText(getApplicationContext(), "Wrong email or password, please try again", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        spinner.setVisibility(View.INVISIBLE);
                        NavUtils.navigateUpFromSameTask(EnterPaymentActivity.this);
                    } catch (Exception e) {
                        spinner.setVisibility(View.INVISIBLE);
                        System.out.println(e.toString());
                    }
                }
            });
        } catch (Exception e) {
            spinner.setVisibility(View.INVISIBLE);
            e.printStackTrace();
        }
    }
}
