package rnfive.htfu.pisprinkler.define;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.NumberPicker;

import rnfive.htfu.pisprinkler.UrlAsync;
import rnfive.htfu.pisprinkler.listener.UrlResponseListener;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusAlert {
    private static final String TAG = StatusAlert.class.getSimpleName();
    private UrlResponseListener listener;
    public Context context;

    public StatusAlert() {}
    public StatusAlert withContext(Context context) {
        this.context = context;
        return this;
    }
    public StatusAlert withListener(UrlResponseListener listener) {
        this.listener = listener;
        return this;
    }

    public void setDelay() {
        final NumberPicker numberPicker = new NumberPicker(context);
        numberPicker.setMaxValue(7);
        numberPicker.setMinValue(1);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(numberPicker);
        builder.setTitle("Set Delay");
        builder.setPositiveButton("OK", (dialog, which) -> {
            int days = numberPicker.getValue();
            callUrl("delay/set/" + days);
        });
        builder.setNegativeButton("CANCEL", (dialog, which) -> {

        });
        builder.create();
        builder.show();
    }

    public void cancelDelay() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Cancel Delay?");
        builder.setPositiveButton("OK", (dialog, which) -> {
            callUrl("delay/cancel");
        });
        builder.setNegativeButton("CANCEL", (dialog, which) -> {

        });
        builder.create();
        builder.show();
    }

    public void cancelProgram() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Stop program?");
        builder.setPositiveButton("OK", (dialog, which) -> {
            callUrl("program/cancel");
        });
        builder.setNegativeButton("CANCEL", (dialog, which) -> {

        });
        builder.create();
        builder.show();
    }

    private void callUrl(String val) {
        UrlAsync async = new UrlAsync().withListener(listener);
        async.execute("GET", val);
    }
}
