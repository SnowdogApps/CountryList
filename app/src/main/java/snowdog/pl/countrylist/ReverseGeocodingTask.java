package snowdog.pl.countrylist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.util.List;

/**
 * Created by bartek on 7/15/14.
 */
public class ReverseGeocodingTask extends AsyncTask<Double, Void, Address> {
    public interface ReverseGeocodingListener {
        public void onAddressFound(Address address);

        public void onAddressNotFound();
    }

    private static final String TAG = "ReverseGeocodingTask";

    private Context mContext;
    private ReverseGeocodingListener mListener;


    public ReverseGeocodingTask(Context context, ReverseGeocodingListener listener) {
        super();
        mContext = context;
        mListener = listener;
    }

    @SuppressLint("NewApi")
    @Override
    protected Address doInBackground(Double... params) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD &&
                Geocoder.isPresent()) {

            Geocoder geocoder = new Geocoder(mContext);
            double latitude = params[0];
            double longitude = params[1];

            Log.d(TAG, "Geocoder is looking for address in " + latitude + " " + longitude);

            List<Address> addresses = null;

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                Log.e(TAG, "" + e.toString());
            }

            if (addresses != null && addresses.size() > 0) {
                return addresses.get(0);
            }
        }

        Log.d(TAG, "Geocoder not present");
        return null;
    }

    @Override
    protected void onPostExecute(Address address) {
        if (mListener == null) {
            return;
        }

        if (address != null) {
            mListener.onAddressFound(address);
        } else {
            mListener.onAddressNotFound();
        }
    }
}

